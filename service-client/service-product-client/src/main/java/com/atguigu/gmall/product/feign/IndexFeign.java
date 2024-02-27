package com.atguigu.gmall.product.feign;

import com.alibaba.fastjson.JSONObject;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * 首页使用的内部调用Feign接口
 */
@FeignClient(name = "service-product", path = "/api/index", contextId = "indexFeign")
public interface IndexFeign {
    /**
     * 获取首页的分类信息
     * @return
     * 最终发起一个HTTP请求到URL为http://service-product/api/index/getIndexCategory
     * 其实也就是将请求发送给远程服务调用，即service-product模块中IndexController下的getIndexCategory()这里
     */
    @GetMapping("/getIndexCategory")
    public List<JSONObject> getIndexCategory();
}
