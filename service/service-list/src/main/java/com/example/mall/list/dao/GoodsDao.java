package com.example.mall.list.dao;

import com.example.mall.model.list.Goods;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * es中商品的dao层接口
 */
@Repository
public interface GoodsDao extends ElasticsearchRepository<Goods, Long> {
}
