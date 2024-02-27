package com.atguigu.gmall.web.controller;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.product.feign.IndexFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * 首页页面的控制层
 */
@Controller
@RequestMapping("/index")
public class IndexController {
    @Autowired
    private IndexFeign indexFeign;

    /**
     * 打开首页
     * @param model
     * @return
     */
    @GetMapping
    public String index(Model model) {
        // 远程调用接口获取分类的数据信息
        List<JSONObject> categoryList = indexFeign.getIndexCategory();
        System.out.println(categoryList);
        // 存储到Model
        model.addAttribute("categoryList", categoryList);
        // 打开页面
        return "index";
    }
}
