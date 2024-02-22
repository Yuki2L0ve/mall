package com.example.mall.web.controller;

import com.example.mall.list.feign.SearchFeign;
import com.example.mall.web.util.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * 搜索页页面的前端控制层
 */
@Controller
@RequestMapping("/page/search")
public class SearchController {
    /**
     * 商品详情页的URL前缀
     */
    @Value("${item.url}")
    private String itemUrl;

    @Autowired
    private SearchFeign searchFeign;

    /**
     * 打开搜索页面
     * @return
     */
    @GetMapping
    public String search(@RequestParam Map<String, String> searchData, Model model) {
        // 远程调用搜索微服务查询商品的数据
        Map<String, Object> searchResult = searchFeign.search(searchData);
        // 需要将结果存储到model中去
        model.addAllAttributes(searchResult);
        // 将查询条件也存储到model中去，用于条件的回显
        model.addAttribute("searchData", searchData);
        // 拼接本次请求后的url
        String url = getUrl(searchData);
        model.addAttribute("url", url);
        // 获取排序的url
        String sortUrl = getSortUrl(searchData);
        model.addAttribute("sortUrl", sortUrl);
        // 获取总记录数
        Object totalHits = searchResult.get("totalHits");
        // 获取当前页码
        Integer pageNum = getPage(searchData.get("pageNum"));
        // 分页对象初始化
        Page pageInfo = new Page<>(Long.valueOf(totalHits.toString()), pageNum, 100);
        model.addAttribute("pageInfo", pageInfo);
        // 将商品详情页的跳转地址存储到model中
        model.addAttribute("itemUrl", itemUrl);
        // 打开搜索页面
        return "list";
    }

    /**
     * 计算页码
     * @param pageNum
     * @return
     */
    private Integer getPage(String pageNum) {
        try {
            // 计算页码
            int i = Integer.parseInt(pageNum);
            // es中默认设置能够查询的数据是1w条 ---> 页码最大值为200（自己定义的）
            // 防止负数, 防止超过200上限
            if (i <= 0 || i > 200)  return 1;
            // 返回结果
            return i;
        } catch (Exception e) {
            return 1;   // 出错则跳转到第一页
        }
    }


    /**
     * 拼接当前请求的url
     * @param searchData
     * @return
     */
    private String getUrl(Map<String, String> searchData) {
        // 初始化
        StringBuffer sb = new StringBuffer("/page/search?");
        // 遍历map拼接参数
        searchData.entrySet().stream().forEach(entry -> {
            // 参数的名字
            String key = entry.getKey();
            if (!key.equals("pageNum")) {
                // 参数的值
                String value = entry.getValue();
                // 拼接
                sb.append(key).append("=").append(value).append("&");
            }
        });
        // 最终的url返回
        return sb.toString().substring(0, sb.toString().length() - 1);
    }

    /**
     * 获取排序的url
     * @param searchData
     * @return
     */
    private String getSortUrl(Map<String, String> searchData) {
        // 初始化
        StringBuffer sb = new StringBuffer("/page/search?");
        // 遍历map拼接参数
        searchData.entrySet().stream().forEach(entry -> {
            // 参数的名字
            String key = entry.getKey();
            if (!key.equals("sortField") && !key.equals("sortRule") && !key.equals("pageNum")) {
                // 参数的值
                String value = entry.getValue();
                // 拼接
                sb.append(key).append("=").append(value).append("&");
            }
        });
        // 最终的url返回
        return sb.toString().substring(0, sb.toString().length() - 1);
    }

}
