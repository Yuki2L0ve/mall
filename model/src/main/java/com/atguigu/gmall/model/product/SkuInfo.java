package com.atguigu.gmall.model.product;

import com.atguigu.gmall.model.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * SkuInfo
 * </p>
 *
 */
@Data
@ApiModel(description = "SkuInfo")
@TableName("sku_info")
public class SkuInfo extends BaseEntity {
	
	private static final long serialVersionUID = 1L;
	
	@ApiModelProperty(value = "商品id")
	@TableField("spu_id")
	private Long spuId;

	@ApiModelProperty(value = "价格")
	@TableField("price")
	private BigDecimal price;

	@ApiModelProperty(value = "sku名称")
	@TableField("sku_name")
	private String skuName;

	@ApiModelProperty(value = "商品规格描述")
	@TableField("sku_desc")
	private String skuDesc;

	@ApiModelProperty(value = "重量")
	@TableField("weight")
	private String weight;

	@ApiModelProperty(value = "品牌(冗余)")
	@TableField("tm_id")
	private Long tmId;

	@ApiModelProperty(value = "三级分类id（冗余)")
	@TableField("category3_id")
	private Long category3Id;

	@ApiModelProperty(value = "默认显示图片(冗余)")
	@TableField("sku_default_img")
	private String skuDefaultImg;

	@ApiModelProperty(value = "是否销售（1：是 0：否）")
	@TableField("is_sale")
	private Short isSale;

	@ApiModelProperty(value = "商品库存")
	@TableField("stock")
	private Integer stock;

	@TableField(exist = false)
	List<SkuImage> skuImageList;

	@TableField(exist = false)
	List<SkuAttrValue> skuAttrValueList;

	@TableField(exist = false)
	List<SkuSaleAttrValue> skuSaleAttrValueList;
}

