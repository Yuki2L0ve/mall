package com.atguigu.gmall.seckill.pojo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户排队对象
 */
@Data
public class UserRecode implements Serializable {

	private static final long serialVersionUID = 1L;
	
	//秒杀用户名
	private String username;
	//创建时间
	private Date createTime;
	//秒杀状态  1:排队中，2:秒杀等待支付,3:秒杀失败,4:支付完成
	private Integer status;
	//秒杀的商品ID
	private String goodsId;
	//应付金额
	private String money;
	//订单号
	private String orderId;
	//时间段
	private String time;
	//购买的数量
	private Integer num;
	//消息
	private String msg;
}
