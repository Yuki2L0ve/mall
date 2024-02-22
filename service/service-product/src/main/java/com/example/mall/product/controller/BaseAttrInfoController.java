package com.example.mall.product.controller;

import com.example.mall.common.result.Result;
import com.example.mall.model.product.BaseAttrInfo;
import com.example.mall.product.service.BaseAttrInfoService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 平台属性的控制层
 */
@RestController
@RequestMapping("/api/baseAttrInfo")
public class BaseAttrInfoController {
    @Resource
    private BaseAttrInfoService baseAttrInfoService;

    /**
     * 主键查询
     * @param id
     * @return
     */
    @GetMapping("/getBaseAttrInfo/{id}")
    public Result getBaseAttrInfo(@PathVariable("id") Long id) {
        return Result.ok(baseAttrInfoService.getBaseAttrInfo(id));
    }

    /**
     * 查询所有
     * @return
     */
    @GetMapping("/getAll")
    public Result getAll() {
        return Result.ok(baseAttrInfoService.getAll());
    }

    /**
     * 新增
     * @param baseAttrInfo
     * @return
     */
    @PostMapping
    public Result add(@RequestBody BaseAttrInfo baseAttrInfo) {
        baseAttrInfoService.add(baseAttrInfo);
        return Result.ok();
    }

    /**
     * 修改
     * @param baseAttrInfo
     * @return
     */
    @PutMapping
    public Result update(@RequestBody BaseAttrInfo baseAttrInfo) {
        baseAttrInfoService.update(baseAttrInfo);
        return Result.ok();
    }

    /**
     * 删除
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable("id") Long id) {
        baseAttrInfoService.delete(id);
        return Result.ok();
    }

    /**
     * 条件查询
     * @param baseAttrInfo
     * @return
     */
    @PostMapping("/search")
    public Result search(@RequestBody BaseAttrInfo baseAttrInfo) {
        return Result.ok(baseAttrInfoService.search(baseAttrInfo));
    }

    /**
     * 分页查询
     * @param page
     * @param size
     * @return
     */
    @GetMapping("/{page}/{size}")
    public Result page(@PathVariable("page") Integer page, @PathVariable("size") Integer size) {
        return Result.ok(baseAttrInfoService.page(page, size));
    }

    /**
     * 分页条件查询
     * @param page
     * @param size
     * @return
     */
    @PostMapping("/search/{page}/{size}")
    public Result search(@RequestBody BaseAttrInfo baseAttrInfo,
                         @PathVariable("page") Integer page,
                         @PathVariable("size") Integer size) {
        return Result.ok(baseAttrInfoService.search(baseAttrInfo, page, size));
    }

}
