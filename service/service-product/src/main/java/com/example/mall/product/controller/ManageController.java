package com.example.mall.product.controller;

import com.example.mall.common.constant.ProductConst;
import com.example.mall.common.result.Result;
import com.example.mall.model.product.BaseAttrInfo;
import com.example.mall.model.product.SkuInfo;
import com.example.mall.model.product.SpuInfo;
import com.example.mall.product.service.ManageService;
import com.mysql.jdbc.log.Log;
import io.swagger.models.auth.In;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 后台管理页面使用的控制层
 */
@RestController
@RequestMapping("/admin/product")
public class ManageController {
    @Resource
    private ManageService manageService;

    /**
     * 查询所有的一级分类
     * @return
     */
    @GetMapping("/getCategory1")
    public Result getCategory1() {
        return Result.ok(manageService.getBaseCategory1());
    }

    /**
     * 根据一级分类id，查询出该一级分类下的所有二级分类
     * @param c1Id
     * @return
     */
    @GetMapping("/getCategory2/{c1Id}")
    public Result getCategory2(@PathVariable("c1Id") Long c1Id) {
        return Result.ok(manageService.getBaseCategory2(c1Id));
    }

    /**
     * 根据二级分类id，查询出该二级分类下的所有三级分类
     * @param c2Id
     * @return
     */
    @GetMapping("/getCategory3/{c2Id}")
    public Result getCategory3(@PathVariable("c2Id") Long c2Id) {
        return Result.ok(manageService.getBaseCategory3(c2Id));
    }

    /**
     * 保存平台属性信息
     * @param baseAttrInfo
     * @return
     */
    @PostMapping("/saveAttrInfo")
    public Result saveAttrInfo(@RequestBody BaseAttrInfo baseAttrInfo) {
        manageService.saveBaseAttrInfo(baseAttrInfo);
        return Result.ok();
    }

    /**
     * 查询指定三级分类对应的平台属性列表
     * @param category1Id
     * @param category2Id
     * @param category3Id
     * @return
     */
    @GetMapping("/attrInfoList/{category1Id}/{category2Id}/{category3Id}")
    public Result attrInfoList(@PathVariable("category1Id") Long category1Id,
                               @PathVariable("category2Id") Long category2Id,
                               @PathVariable("category3Id") Long category3Id) {
        return Result.ok(manageService.getBaseAttrInfo(category3Id));
    }

    /**
     * 删除平台属性名称和值
     * @param attrId
     */
    @DeleteMapping("/deleteBaseAttrInfo/{attrId}")
    public Result<Object> deleteBaseAttrInfo(@PathVariable("attrId") Long attrId) {
        manageService.deleteBaseAttrInfo(attrId);
        return Result.ok();
    }

    /**
     * 查询品牌列表
     * @return
     */
    @GetMapping("/baseTrademark/getTrademarkList")
    public Result getTrademarkList() {
        return Result.ok(manageService.getBaseTrademark());
    }

    /**
     * 查询所有的基础销售属性
     * @return
     */
    @GetMapping("/baseSaleAttrList")
    public Result baseSaleAttrList() {
        return Result.ok(manageService.getBaseSaleAttr());
    }

    /**
     * 保存SPU的信息
     * @param spuInfo
     * @return
     */
    @PostMapping("/saveSpuInfo")
    public Result saveSpuInfo(@RequestBody SpuInfo spuInfo) {
        manageService.saveSpuInfo(spuInfo);
        return Result.ok();
    }

    /**
     * 分页条件查询SPU的信息
     * @param page
     * @param size
     * @param category3Id
     * @return
     */
    @GetMapping("/{page}/{size}")
    public Result pageSpuInfo(@PathVariable Integer page, @PathVariable Integer size, Long category3Id) {
        return Result.ok(manageService.pageSpuInfo(page, size, category3Id));
    }

    @GetMapping("/spuSaleAttrList/{spuId}")
    public Result spuSaleAttrList(@PathVariable Long spuId) {
        return Result.ok(manageService.getSpuSaleAttr(spuId));
    }

    /**
     * 查询指定SPU的图片列表
     * @param spuId
     * @return
     */

    @GetMapping("/spuImageList/{spuId}")
    public Result spuImageList(@PathVariable Long spuId) {
        return Result.ok(manageService.getSpuImage(spuId));
    }

    /**
     * 保存SKU的信息
     * @param skuInfo
     * @return
     */
    @PostMapping("/saveSkuInfo")
    public Result saveSkuInfo(@RequestBody SkuInfo skuInfo) {
        manageService.saveSkuInfo(skuInfo);
        return Result.ok();
    }

    /**
     * 分页查询SKU的信息
     * @param page
     * @param size
     * @return
     */
    @GetMapping("/list/{page}/{size}")
    public Result list(@PathVariable Integer page, @PathVariable Integer size) {
        return Result.ok(manageService.list(page, size));
    }

    /**
     * 商品的上架
     * @param skuId
     * @return
     */
    @GetMapping("/onSale/{skuId}")
    public Result onSale(@PathVariable Long skuId) {
        manageService.upOrDown(skuId, ProductConst.SKU_ON_SALE);
        return Result.ok();
    }

    /**
     * 商品的下架
     * @param skuId
     * @return
     */
    @GetMapping("/cancelSale/{skuId}")
    public Result cancelSale(@PathVariable Long skuId) {
        manageService.upOrDown(skuId, ProductConst.SKU_CANCEL_SALE);
        return Result.ok();
    }
}
