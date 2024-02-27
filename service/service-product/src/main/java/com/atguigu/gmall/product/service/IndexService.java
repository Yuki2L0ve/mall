package com.atguigu.gmall.product.service;

import com.alibaba.fastjson.JSONObject;

import java.util.List;

/**
 * 首页信息查询的接口类
 */
public interface IndexService {
    /**
     * 获取首页的分类信息
     *
     * @return
     */
    public List<JSONObject> getIndexCategory();
}
