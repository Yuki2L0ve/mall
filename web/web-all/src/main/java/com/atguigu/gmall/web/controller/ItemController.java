package com.atguigu.gmall.web.controller;

import com.atguigu.gmall.item.feign.ItemFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.PrintWriter;
import java.util.Map;

/**
 * 商品详情页面的控制层
 */
@Controller
@RequestMapping("/item")
public class ItemController {
    @Autowired
    private ItemFeign itemFeign;
    @Autowired
    private TemplateEngine templateEngine;

    /**
     * 打开商品详情页面
     * @param model
     * @return
     */
    @GetMapping
    public String itemPage(Model model, Long skuId) {
        // 获取商品的详情页面数据
        Map<String, Object> itemInfo = itemFeign.getItemInfo(skuId);
        if (itemInfo == null || itemInfo.isEmpty()) {
            return "商品不存在！！";
        }
        // 将数据保存到model
        model.addAllAttributes(itemInfo);
        // 打开商品详情页面
        return "item";
    }

    /**
     * 为商品创建静态页面
     * @param skuId
     * @return
     */
    @GetMapping("/createHtml")
    @ResponseBody
    public String createHtml(Long skuId) throws Exception{
        // 查询指定商品的详情页面需要的全部数据
        Map<String, Object> itemInfo = itemFeign.getItemInfo(skuId);
        if (itemInfo == null || itemInfo.isEmpty()) {
            return "商品不存在！！";
        }
        // 初始化数据容器      类似于Model   G:\others\workspace\idea\project\mall
        Context context = new Context();
        context.setVariables(itemInfo);
        // 初始化文件对象
        File file = new File("G:/others/workspace/idea/project/mall/html", skuId + ".html");
        // 初始化文件输出对象
        PrintWriter printWriter = new PrintWriter(file, "utf-8");

        // 生成静态页面到指定的目录
        /**
         * 1. 模板页面是哪个
         * 2. 数据装在哪里
         * 3. 生成好的页面保存到哪里
         */
        templateEngine.process("item", context, printWriter);
        // 关闭
        printWriter.flush();
        printWriter.close();
        // 返回
        return "创建页面成功！";
    }
}
