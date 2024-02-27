package com.atguigu.gmall.product.controller;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.product.service.IndexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 首页信息查询的接口类
 */
@RestController
@RequestMapping("/api/index")
public class IndexController {
    @Autowired
    private IndexService indexService;

    /**
     * 获取首页的分类信息
     * @return
     */
    @GetMapping("/getIndexCategory")
    public List<JSONObject> getIndexCategory() {
        return indexService.getIndexCategory();
    }
}
