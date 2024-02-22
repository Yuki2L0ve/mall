package com.example.mall.list.service;

import java.util.Map;

/**
 * 商品搜索相关的接口类
 */
public interface SearchService {

    /**
     * 商品搜索
     *
     * @param searchData
     * @return
     */
    public Map<String, Object> search(Map<String, String> searchData);
}
