package com.atguigu.gmall.model.cart;

import com.atguigu.gmall.model.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel(description = "购物车")
public class CartInfo extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "用户id")
    @TableField("user_id")
    private String userId;

    @ApiModelProperty(value = "skuid")
    @TableField("sku_id")
    private Long skuId;

    @ApiModelProperty(value = "放入购物车时价格")
    @TableField("cart_price")
    private BigDecimal cartPrice;

    @ApiModelProperty(value = "数量")
    @TableField("sku_num")
    private Integer skuNum;

    @ApiModelProperty(value = "图片文件")
    @TableField("img_url")
    private String imgUrl;

    @ApiModelProperty(value = "sku名称 (冗余)")
    @TableField("sku_name")
    private String skuName;

    @ApiModelProperty(value = "isChecked")
    @TableField("is_checked")
    private Integer isChecked = 1;

    // 实时价格 skuInfo.price
    @TableField(exist = false)
    BigDecimal skuPrice;

}
