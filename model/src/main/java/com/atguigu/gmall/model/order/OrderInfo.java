package com.atguigu.gmall.model.order;

import com.atguigu.gmall.model.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@ApiModel(description = "订单信息")
@TableName("order_info")
public class OrderInfo extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "收货人")
    @TableField("consignee")
    private String consignee;

    @ApiModelProperty(value = "收件人电话")
    @TableField("consignee_tel")
    private String consigneeTel;

    @ApiModelProperty(value = "总金额")
    @TableField("total_amount")
    private BigDecimal totalAmount;

    @ApiModelProperty(value = "订单状态")
    @TableField("order_status")
    private String orderStatus;

    @ApiModelProperty(value = "用户id")
    @TableField("user_id")
    private String userId;

    @ApiModelProperty(value = "付款方式")
    @TableField("payment_way")
    private String paymentWay;

    @ApiModelProperty(value = "送货地址")
    @TableField("delivery_address")
    private String deliveryAddress;

    @ApiModelProperty(value = "订单备注")
    @TableField("order_comment")
    private String orderComment;

    @ApiModelProperty(value = "订单交易编号（第三方支付用)")
    @TableField("out_trade_no")
    private String outTradeNo;

    @ApiModelProperty(value = "订单描述(第三方支付用)")
    @TableField("trade_body")
    private String tradeBody;

    @ApiModelProperty(value = "创建时间")
    @TableField("create_time")
    private Date createTime;

    @ApiModelProperty(value = "失效时间")
    @TableField("expire_time")
    private Date expireTime;

    @ApiModelProperty(value = "进度状态")
    @TableField("process_status")
    private String processStatus;

    @ApiModelProperty(value = "物流单编号")
    @TableField("tracking_no")
    private String trackingNo;

    @ApiModelProperty(value = "父订单编号")
    @TableField("parent_order_id")
    private Long parentOrderId;

    @ApiModelProperty(value = "图片路径")
    @TableField("img_url")
    private String imgUrl;

    @TableField(exist = false)
    private List<OrderDetail> orderDetailList;

    @TableField(exist = false)
    private String wareId;

    // 计算总价格
    public void sumTotalAmount() {
        BigDecimal totalAmount = new BigDecimal("0");
        for (OrderDetail orderDetail : orderDetailList) {
            totalAmount = totalAmount.add(orderDetail.getOrderPrice().multiply(new BigDecimal(orderDetail.getSkuNum())));
        }
        this.totalAmount = totalAmount;
    }

}
