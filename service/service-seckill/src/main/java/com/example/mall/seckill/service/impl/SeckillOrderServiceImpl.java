package com.example.mall.seckill.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.client.naming.utils.StringUtils;
import com.example.mall.model.activity.SeckillGoods;
import com.example.mall.seckill.mapper.SeckillOrderMapper;
import com.example.mall.seckill.pojo.SeckillOrder;
import com.example.mall.seckill.pojo.UserRecode;
import com.example.mall.seckill.service.SeckillOrderService;
import com.example.mall.seckill.util.DateUtil;
import lombok.extern.log4j.Log4j2;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 秒杀下单使用的接口实现类
 */
@Service
@Transactional(rollbackFor = Exception.class)
@Log4j2
public class SeckillOrderServiceImpl implements SeckillOrderService {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Resource
    private SeckillOrderMapper seckillOrderMapper;
    @Autowired
    private RedissonClient redissonClient;

    /**
     * 秒杀下单: 真实排队----同步排队
     *  @param time
     * @param goodsId
     * @param num
     * @return
     */
    @Override
    public UserRecode addSeckillOrder(String time, String goodsId, Integer num) {
        String username = "lipei";
        UserRecode userRecode = new UserRecode();
        //记录用户的排队次数,只放过用户第一次排队: 情理的情况: b.取消订单(主动  超时)-->清理 c.付钱-->清理 d.下单失败-->清理
        Long increment = redisTemplate.opsForValue().increment("User_Queue_Count_" + username, 1);
        if(increment > 1){
            userRecode.setStatus(3);
            userRecode.setMsg("重复排队,秒杀失败!!!!");
            return userRecode;
        }
        //记录哪个用户 买哪个时间段的 哪个商品 买几个?
        userRecode.setUsername(username);
        userRecode.setCreateTime(new Date());
        userRecode.setStatus(1);
        userRecode.setMsg("排队中!");
        userRecode.setGoodsId(goodsId);
        userRecode.setTime(time);
        userRecode.setNum(num);
        //将用户的排队状态记录到redis中去--异步记录排队状态
        CompletableFuture.runAsync(()->{
            //将排队的状态写入到redis中去
            redisTemplate.opsForValue().set("User_Recode_" + username, userRecode);
            //下单需要保证相对公平MQ
            rabbitTemplate.convertAndSend("seckill_order_exchange",
                    "seckill.order.add",
                    JSONObject.toJSONString(userRecode));
        }, threadPoolExecutor).whenCompleteAsync((a, b)->{
            if(b != null){
                //保存用户排队状态或发送下单消息失败出现异常,需要将用户的排队标识位清理,防止用户不能下单
                redisTemplate.delete("User_Queue_Count_" + username);
                //更新用户的排队状态为失败
                userRecode.setStatus(3);
                userRecode.setMsg("秒杀失败,请重试!");
                redisTemplate.opsForValue().set("User_Recode_" + username, userRecode);
            }
        });
        //排队状态返回给用户
        return userRecode;
    }

    /**
     * 查询用户的排队状态
     *
     * @return
     */
    @Override
    public UserRecode getUserRecode() {
        String username = "lipei";
        return (UserRecode)redisTemplate.opsForValue().get("User_Recode_" + username);
    }

    /**
     * 秒杀真实下单方法
     *
     * @param userRecodeString
     */
    @Override
    public void listenerAddSeckillOrder(String userRecodeString) {
        //反序列化
        UserRecode userRecode =
                JSONObject.parseObject(userRecodeString, UserRecode.class);
        //获取时间段: 2022111414
        String time = userRecode.getTime();
        //获取用户名
        String username = userRecode.getUsername();
        //获取商品id
        String goodsId = userRecode.getGoodsId();
        //获取数量
        Integer num = userRecode.getNum();
        //判断时间段是否争取:2022111414
        String nowTime =
                DateUtil.data2str(DateUtil.getDateMenus().get(0), DateUtil.PATTERN_YYYYMMDDHH);
        if(!nowTime.equals(time)){
            //用户购买的商品活动没有开始或者已经结束,秒杀失败!
            userRecode.setStatus(3);
            userRecode.setMsg("用户购买的商品活动没有开始或者已经结束,秒杀失败!");
            //更新
            redisTemplate.opsForValue().set("User_Recode_" + username, userRecode);
            //保存用户排队状态或发送下单消息失败出现异常,需要将用户的排队标识位清理,防止用户不能下单
            redisTemplate.delete("User_Queue_Count_" + username);
            return;
        }
        //根据time和goodsId获取商品的数据
        SeckillGoods seckillGoods =
                (SeckillGoods)redisTemplate.opsForHash().get(time, goodsId);
        if(seckillGoods == null){
            //商品不在redis中存在了
            userRecode.setStatus(3);
            userRecode.setMsg("商品不存在,秒杀失败!");
            //更新
            redisTemplate.opsForValue().set("User_Recode_" + username, userRecode);
            //保存用户排队状态或发送下单消息失败出现异常,需要将用户的排队标识位清理,防止用户不能下单
            redisTemplate.delete("User_Queue_Count_" + username);
            return;
        }
        //获取商品的限购
        Integer seckillLimit = seckillGoods.getSeckillLimit();
        if(seckillLimit < num){
            //超出限购
            userRecode.setStatus(3);
            userRecode.setMsg("超出商品的限购数量,每个商品限购" + seckillLimit + "个,秒杀失败!");
            //更新
            redisTemplate.opsForValue().set("User_Recode_" + username, userRecode);
            //保存用户排队状态或发送下单消息失败出现异常,需要将用户的排队标识位清理,防止用户不能下单
            redisTemplate.delete("User_Queue_Count_" + username);
            return;
        }
        //库存是否足够  -- 方案1
        for (int i = 0; i < num; i++) {
            Object o = redisTemplate.opsForList().rightPop("Seckill_Goods_Stock_Queue_" + goodsId);
            if (o == null) {
                // 判断是否需要回滚
                if (i > 0) {
                    // 回滚
                    String[] ids = getIds(i, goodsId);
                    //回滚
                    redisTemplate.opsForList().leftPushAll("Seckill_Goods_Stock_Queue_" + goodsId, ids);
                }
                //库存不足
                userRecode.setStatus(3);
                userRecode.setMsg("商品库存不足,秒杀失败!");
                //更新
                redisTemplate.opsForValue().set("User_Recode_" + username, userRecode);
                //保存用户排队状态或发送下单消息失败出现异常,需要将用户的排队标识位清理,防止用户不能下单
                redisTemplate.delete("User_Queue_Count_" + username);
                return;
            }
        }
        // 库存足够，做库存自增
        Long increment = redisTemplate.opsForHash().increment("Seckill_Goods_Increment_" + time, goodsId, -num);
        //库存足够,做库存自增--方案二
//        if(increment < 0) {
//            redisTemplate.opsForHash().increment("Seckill_Goods_Increment_" + time, goodsId, num);
//            //库存不足
//            userRecode.setStatus(3);
//            userRecode.setMsg("商品库存不足,秒杀失败!");
//            //更新
//            redisTemplate.opsForValue().set("User_Recode_" + username, userRecode);
//            //保存用户排队状态或发送下单消息失败出现异常,需要将用户的排队标识位清理,防止用户不能下单
//            redisTemplate.delete("User_Queue_Count_" + username);
//            return;
//        }

        seckillGoods.setStockCount(increment.intValue());
        // 更新商品最新的库存数据
        redisTemplate.opsForHash().put(time, goodsId, seckillGoods);
        //生成秒杀订单
        SeckillOrder seckillOrder = new SeckillOrder();
        seckillOrder.setId(UUID.randomUUID().toString().replace("-", ""));
        seckillOrder.setGoodsId(goodsId);
        seckillOrder.setNum(num);
        seckillOrder.setMoney(seckillGoods.getCostPrice().multiply(new BigDecimal(num)).doubleValue() + "");
        seckillOrder.setUserId(username);
        seckillOrder.setCreateTime(new Date());
        seckillOrder.setStatus("0");
        //将数据写入redis
        redisTemplate.opsForHash().put("Seckill_Order_" + time, seckillOrder.getId(), seckillOrder);
        //修改排队的状态
        userRecode.setStatus(2);
        userRecode.setMsg("秒杀等待支付!");
        userRecode.setOrderId(seckillOrder.getId());
        userRecode.setMoney(seckillOrder.getMoney());
        //更新
        redisTemplate.opsForValue().set("User_Recode_" + username, userRecode);
        //发送延迟消息: 15分钟
        rabbitTemplate.convertAndSend("seckill_order_nomal_exchange",
                "seckill.order.dead",
                username,
                (message -> {
                    //获取消息的属性
                    MessageProperties messageProperties = message.getMessageProperties();
                    //设置过期时间
                    messageProperties.setExpiration("900000");
                    //返回
                    return message;
                }));

        //TODO----订单的后续处理: 1.取消 2.超时取消 3.付钱

    }

    /**
     * 取消秒杀订单: 超时取消 主动取消
     *
     * @param username
     */
    @Override
    public void cancelSeckillOrder(String username) {
        //状态初始化
        String msg = "超时取消";
        if(StringUtils.isEmpty(username)){
            //还是主动取消: 从本地线程获取
            username = "lipei";
            msg= "主动取消";
        }
        //控制用户重复取消秒杀订单
        RLock lock = redissonClient.getLock("Cancel_Seckill_Order_Lock_" + username);
        //抢锁,保证同一个用户只接受一个请求去取消秒杀订单
        if(lock.tryLock()){
            try {
                //从redis中获取用户的排队状态
                UserRecode userRecode =
                        (UserRecode)redisTemplate.opsForValue().get("User_Recode_" + username);
                if(userRecode != null){
                    //获取时间段
                    String time = userRecode.getTime();
                    //获取订单号
                    String orderId = userRecode.getOrderId();
                    //从redis中获取订单的信息
                    SeckillOrder seckillOrder =
                            (SeckillOrder)redisTemplate.opsForHash().get("Seckill_Order_" + time, orderId);
                    //取消订单,将订单的状态修改为:取消: a b
                    seckillOrder.setStatus(msg);
                    //将数据写入数据库
                    int insert = seckillOrderMapper.insert(seckillOrder);
                    if(insert <= 0){
                        throw new RuntimeException(msg + "订单失败");
                    }
                    //回滚库存
                    rollbackSeckillGoodsStock(time, userRecode.getGoodsId(), userRecode.getNum());
                    //清理redis中的用户产生的所有key
                    clearRedisUserFlag(username, time ,orderId);
                }
            }catch (Exception e){
                log.error("取消订单失败,失败的原因为: " + e.getMessage());
            }finally {
                //释放锁
                lock.unlock();
            }
        }else{
            //重复取消
            return;
        }
    }

    /**
     * 回滚商品的库存数据
     * @param time
     * @param goodsId
     * @param num
     */
    private void rollbackSeckillGoodsStock(String time, String goodsId, Integer num) {
        //从redis中获取商品的数据
        SeckillGoods seckillGoods
                = (SeckillGoods)redisTemplate.opsForHash().get(time, goodsId);
        //商品库存自增值: 回滚后的商品真实剩余库存
        Long increment =
                redisTemplate.opsForHash().increment("Seckill_Goods_Increment_" + time, goodsId, num);
        if(seckillGoods != null){
            //活动未结束: 需要回滚商品数据
            seckillGoods.setStockCount(increment.intValue());
            redisTemplate.opsForHash().put(time, goodsId, seckillGoods);
            //商品队列
            String[] ids = getIds(num, goodsId);
            redisTemplate.opsForList().leftPushAll("Seckill_Goods_Stock_Queue_" + goodsId, ids);
        }

    }


    /**
     * 清理用户的标识位: 排队计数器 排队状态 用户的订单信息
     * @param username
     * @param time
     * @param orderId
     */
    private void clearRedisUserFlag(String username, String time, String orderId) {
        //排队计数器
        redisTemplate.delete("User_Queue_Count_" + username);
        //排队状态
        redisTemplate.delete("User_Recode_" + username);
        //订单信息
        redisTemplate.opsForHash().delete("Seckill_Order_" + time, orderId);
    }

    /**
     * 构建商品库存数组
     * @param stockCount
     * @param goodsId
     * @return
     */
    private String[] getIds(Integer stockCount, String goodsId) {
        //声明一个库存长度的数组
        String[] ids = new String[stockCount];
        //为每个元素赋值
        for (int i = 0; i < stockCount; i++) {
            ids[i] = goodsId;
        }
        //返回
        return ids;
    }

    /**
     * 修改支付结果
     *
     * @param payResultJsonString
     */
    @Override
    public void updateSeckillOrder(String payResultJsonString) {
        //获取支付结果,并反序列化
        Map<String, String> payResultMap =
                JSONObject.parseObject(payResultJsonString, Map.class);
        //获取附加参数
        String attachString = payResultMap.get("attach");
        //附加参数反序列化
        Map<String, String> attach = JSONObject.parseObject(attachString, Map.class);
        //获取用户名
        String username = attach.get("username");
        //从redis中获取用户的排队状态
        UserRecode userRecode =
                (UserRecode)redisTemplate.opsForValue().get("User_Recode_" + username);
        //防止重复操作,订单若已经被处理了,redis中不会有订单的信息
        if(userRecode != null){
            //获取时间段
            String time = userRecode.getTime();
            //获取订单号
            String orderId = userRecode.getOrderId();
            //从redis中获取订单的信息
            SeckillOrder seckillOrder =
                    (SeckillOrder)redisTemplate.opsForHash().get("Seckill_Order_" + time, orderId);
            if(seckillOrder != null){
                //将订单的信息进行修改
                seckillOrder.setStatus("已支付");
                seckillOrder.setOutTradeNo(payResultMap.get("transaction_id"));
                int insert = seckillOrderMapper.insert(seckillOrder);
                if(insert <= 0){
                    throw new RuntimeException("修改订单的支付结果失败!");
                }
                //清理标识位
                clearRedisUserFlag(username, time, orderId);
            }
        }else{
            //获取支付结果的订单号
            String orderId = payResultMap.get("out_trade_no");
            //订单在数据库,查询数据库中订单的信息
            SeckillOrder seckillOrder = seckillOrderMapper.selectById(orderId);
            //数据库中订单已经支付,但是是两个流水号
            if(seckillOrder.getStatus().equals("已支付")){
                if(!seckillOrder.getOutTradeNo().equals(payResultMap.get("transaction_id"))){
                    //重复支付,退款!

                }
            }else{
                //取消:a.主动 b.超时---->退款!

            }
        }
    }
}
