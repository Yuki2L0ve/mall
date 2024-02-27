package com.atguigu.gmall.list.controller;

import com.atguigu.gmall.list.service.SearchService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 商品搜索的控制层
 */
@RestController
@RequestMapping("/api/search")
public class SearchController {
    @Resource
    private SearchService searchService;

    /**
     * 商品搜索
     * @param searchData
     * @return
     */
    @GetMapping
    public Map<String, Object> search(@RequestParam Map<String, String> searchData) {
        return searchService.search(searchData);
    }
}
