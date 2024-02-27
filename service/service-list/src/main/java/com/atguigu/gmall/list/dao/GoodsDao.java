package com.atguigu.gmall.list.dao;

import com.atguigu.gmall.model.list.Goods;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * es中商品的dao层接口
 * 这个接口用于声明与Elasticsearch交互的数据访问层（DAO）的方法。
 * @Repository用于标记一个接口或类作为Spring容器中的一个bean，即一个数据访问对象（DAO）。
 * Spring Data会为这个接口提供实现，使得你可以使用这个接口来执行CRUD（创建、读取、更新、删除）操作。
 * ElasticsearchRepository是Spring Data Elasticsearch提供的一个接口，它提供了一组标准的CRUD方法，用于操作Elasticsearch中的文档。
 * 泛型参数<Goods, Long>指定了两个类型：
 * Goods：这是实体类，代表了要操作的数据模型。在Elasticsearch中，这个类对应的实例将被索引为文档。
 * Long：这是实体类中用于唯一标识文档的主键类型。在这个例子中，每个Goods文档都有一个Long类型的ID。
 */
@Repository
public interface GoodsDao extends ElasticsearchRepository<Goods, Long> {
}
