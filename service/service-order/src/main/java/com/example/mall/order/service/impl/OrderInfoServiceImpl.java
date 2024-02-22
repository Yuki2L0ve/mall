package com.example.mall.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.example.mall.cart.feign.CartFeign;
import com.example.mall.common.constant.OrderConst;
import com.example.mall.model.cart.CartInfo;
import com.example.mall.model.enums.OrderStatus;
import com.example.mall.model.enums.ProcessStatus;
import com.example.mall.model.order.OrderDetail;
import com.example.mall.model.order.OrderInfo;
import com.example.mall.model.payment.PaymentInfo;
import com.example.mall.order.mapper.OrderDetailMapper;
import com.example.mall.order.mapper.OrderInfoMapper;
import com.example.mall.order.mapper.PaymentInfoMapper;
import com.example.mall.order.service.OrderInfoService;
import com.example.mall.order.util.OrderThreadLocalUtil;
import com.example.mall.payment.feign.PaymentWxFeign;
import com.example.mall.product.feign.ProductFeign;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 订单相关的接口实现类
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class OrderInfoServiceImpl implements OrderInfoService {
    @Resource
    private OrderInfoMapper orderInfoMapper;
    @Resource
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private CartFeign cartFeign;
    @Resource
    private RedisTemplate redisTemplate;
    @Autowired
    private ProductFeign productFeign;
    @Resource
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private PaymentWxFeign paymentWxFeign;
    @Resource
    private PaymentInfoMapper paymentInfoMapper;

    /**
     * 新增订单
     *
     * @param orderInfo
     */
    @Override
    public void addOrder(OrderInfo orderInfo) {
        // 参数校验
        if (orderInfo == null)  return ;
        // 使用redis进行记录
        Long increment = redisTemplate.opsForValue().increment("User_Order_Add_Count_" + OrderThreadLocalUtil.get(), 1);
        if (increment > 1)  {
            throw new RuntimeException("新增订单失败，重复下单！！！");
        }
        try {
            // 肯定是第一次
            redisTemplate.expire("User_Order_Add_Count_" + OrderThreadLocalUtil.get(), 10, TimeUnit.SECONDS);
            // 远程调用购物车微服务，获取本次购买的购物车数据和总金额
            Map<String, Object> cartFeignResult = cartFeign.getOrderAddInfo();
            if (cartFeignResult == null || cartFeignResult.isEmpty()) {
                throw new RuntimeException("新增订单失败，购物车未勾选任何商品！！！");
            }
            // 补全订单对象缺少的内容
            orderInfo.setOrderStatus(OrderStatus.UNPAID.getComment());
            orderInfo.setUserId(OrderThreadLocalUtil.get());
            orderInfo.setCreateTime(new Date());
            orderInfo.setExpireTime(new Date(System.currentTimeMillis() + 1800000));
            orderInfo.setProcessStatus(ProcessStatus.UNPAID.getComment());
            orderInfo.setTotalAmount(new BigDecimal(cartFeignResult.get("totalMoney").toString()));
            // 将订单保存到数据库中去
            int insert = orderInfoMapper.insert(orderInfo);
            if (insert <= 0) {
                throw new RuntimeException("新增订单失败，请重试！！！");
            }
            // 获取订单id
            Long orderId = orderInfo.getId();
            // 根据购物车数据包装订单详情
            List cartInfoList = (List) cartFeignResult.get("cartInfoList");
            // 新增订单详情数据，同时统计需要扣减库存的商品id和商品的扣减数量
            Map<String, String> decountMap = addOrderDetail(orderId, cartInfoList);
            // 清空购物车
            //cartFeign.deleteCart();
            // 扣减库存
            productFeign.decountStock(decountMap);
            // 对这笔订单进行30分钟倒计时
            rabbitTemplate.convertAndSend("order_nomal_exchange", "order.nomal", orderId + "", (message -> {
                // 获取消息属性
                MessageProperties messageProperties = message.getMessageProperties();
                // 设置消息的过期时间：单位是毫秒
                messageProperties.setExpiration("30000");
                // 返回
                return message;
            }));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        } finally {
            // 下单完成后，不论成功还是失败，都要清理这个标识位，否则用户无法下新单
            redisTemplate.delete("User_Order_Add_Count_" + OrderThreadLocalUtil.get());
        }
    }


    /**
     * 保存订单详情
     *
     * @param orderId
     * @param cartInfoList
     * @return
     */
    private Map<String, String> addOrderDetail(Long orderId, List cartInfoList) {
        // 记录扣减的商品数据
        Map<String, String> decountMap = new ConcurrentHashMap<>();
        // 将订单详情保存到数据库中
        cartInfoList.stream().forEach(o -> {
            // 序列化
            String s = JSONObject.toJSONString(o);
            // 反序列化
            CartInfo cartInfo = JSONObject.parseObject(s, CartInfo.class);
            // 初始化订单详情对象
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orderId);
            orderDetail.setSkuId(cartInfo.getSkuId());
            orderDetail.setSkuName(cartInfo.getSkuName());
            orderDetail.setImgUrl(cartInfo.getImgUrl());
            orderDetail.setOrderPrice(cartInfo.getSkuPrice());
            orderDetail.setSkuNum(cartInfo.getSkuNum());
            // 保存
            int insert = orderDetailMapper.insert(orderDetail);
            if (insert <= 0) {
                throw new RuntimeException("新增订单详情失败，请重试！！！");
            }
            // 记录
            decountMap.put(cartInfo.getSkuId() + "", cartInfo.getSkuNum() + "");
        });
        // 返回
        return decountMap;
    }

    /**
     * 取消订单: 1.主动 2.超时
     *
     * @param orderId
     */
    @Override
    public void cancelOrder(Long orderId) {
        //参数校验
        if(orderId == null){
            return;
        }
        //使用redis进行记录用户的取消次数
        Long increment =
                redisTemplate.opsForValue().increment("User_Order_Cancel_Count_" + orderId, 1);
        if(increment > 1){
            throw new RuntimeException("取消订单失败,重复取消!!");
        }

        try {
            //肯定是第一次
            redisTemplate.expire("User_Order_Cancel_Count_" + orderId, 10, TimeUnit.SECONDS);
            //判断是主动还是超时
            String username = OrderThreadLocalUtil.get();
            //修改订单
            int update = 0;
            if (!StringUtils.isEmpty(username)) {
                update = orderInfoMapper.cancelOrder(orderId, OrderStatus.CANCEL.getComment(), OrderStatus.UNPAID.getComment());
            } else {
                update = orderInfoMapper.cancelOrder(orderId, OrderStatus.TIMEOUT.getComment(), OrderStatus.UNPAID.getComment());
            }
            if(update < 0){
                throw new RuntimeException("取消订单失败,请重试!");
            }
            //回滚库存
            rollbackStock(orderId);
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("取消订单失败,让事务回滚!");
        }finally {
            //取消订单完成以后,无论成功或失败,都要清理这个标识位,否则用户无法取消这笔订单
            redisTemplate.delete("User_Order_Cancel_Count_" + orderId);
        }
    }

    /**
     * 回滚库存
     * @param orderId
     */
    private void rollbackStock(Long orderId) {
        Map<String, String> rollbackMap = new ConcurrentHashMap<>();
        // 查询订单的详情列表信息
        List<OrderDetail> orderDetailList =
                orderDetailMapper.selectList(new LambdaQueryWrapper<OrderDetail>().eq(OrderDetail::getOrderId, orderId));
        // 统计商品id和数量
        orderDetailList.stream().forEach(orderDetail -> {
            // 获取商品id
            Long skuId = orderDetail.getSkuId();
            // 获取商品需要回滚的库存数量
            Integer skuNum = orderDetail.getSkuNum();
            // 记录商品id和需要回滚的库存数量
            rollbackMap.put(skuId + "", skuNum + "");
        });
        // 远程调用product微服务，进行库存回滚
        productFeign.rollbackStock(rollbackMap);
    }

    /**
     * 修改订单的支付结果
     *
     * @param resultJsonString
     */
    @Override
    public void updateOrder(String resultJsonString) {
        //将结果反序列化
        Map<String, String> result =
                JSONObject.parseObject(resultJsonString, Map.class);
        //获取订单号
        String orderId = result.get("out_trade_no");
        //查询支付信息表
        PaymentInfo paymentInfo =
                paymentInfoMapper.selectOne(
                        new LambdaQueryWrapper<PaymentInfo>()
                                .eq(PaymentInfo::getOrderId, orderId)
                                .eq(PaymentInfo::getPaymentStatus, OrderStatus.UNPAID.getComment()));
        //防止重复修改订单的状态
        if(paymentInfo == null){
            return;
        }
        String tradeNo = "";
        //获取支付的渠道: WX  ZFB
        String paymentType = paymentInfo.getPaymentType();
        if(paymentType.equals(OrderConst.WX_PAY)){
            //微信判断
            if(result.get("return_code").equals("SUCCESS") &&
                    result.get("result_code").equals("SUCCESS")){
                tradeNo = updateOrderByPayWay(result, OrderConst.WX_PAY);
            }
        }else{
            //支付宝
            if(result.get("trade_status").equals("TRADE_SUCCESS")){
                tradeNo = updateOrderByPayWay(result, OrderConst.ZFB_PAY);
            }
        }
        if(!StringUtils.isEmpty(tradeNo)){
            //将支付信息记录进行修改
            paymentInfo.setPaymentStatus("已支付");
            paymentInfo.setTradeNo(tradeNo);
            int update = paymentInfoMapper.updateById(paymentInfo);
            if(update < 0){
                throw new RuntimeException("修改支付状态记录失败");
            }
        }
    }

    /**
     * 根据支付渠道修改订单的支付结果
     * @param payResult
     * @param Payway
     * @return
     */
    private String updateOrderByPayWay(Map<String, String> payResult, String Payway){
        String tradeNo = "";
        //获取订单号
        String orderId = payResult.get("out_trade_no");
        //查询这笔订单,并且状态为未支付的才进行修改
        OrderInfo orderInfo = orderInfoMapper.selectById(orderId);
        if(orderInfo == null || orderInfo.getId() == null){
            return tradeNo;
        }
        if(orderInfo.getOrderStatus().equals(OrderStatus.UNPAID.getComment())){
            //修改订单的状态
            orderInfo.setOrderStatus(OrderStatus.PAID.getComment());
            orderInfo.setProcessStatus(ProcessStatus.PAID.getComment());
            if(Payway.equals(OrderConst.WX_PAY)){
                tradeNo = payResult.get("transaction_id");
                //微信
                orderInfo.setOutTradeNo(tradeNo);
            }else{
                tradeNo = payResult.get("trade_no");
                //支付宝
                orderInfo.setOutTradeNo(tradeNo);
            }

            orderInfo.setTradeBody(JSONObject.toJSONString(payResult));
            //修改
            int update = orderInfoMapper.updateById(orderInfo);
            if(update < 0){
                throw new RuntimeException("修改订单的支付结果失败,操作数据库失败!");
            }
            //通知商家发货!

        }else{
            //订单的前置状态不对!--->退款!--TODO

        }

        return tradeNo;
    }

}
