package com.atguigu.gmall.list.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.list.service.SearchService;
import com.atguigu.gmall.model.list.Goods;
import com.atguigu.gmall.model.list.SearchResponseAttrVo;
import com.atguigu.gmall.model.list.SearchResponseTmVo;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 商品搜索相关的接口实现类
 */
@Service
public class SearchServiceImpl implements SearchService {
    /**
     * RestHighLevelClient是Elasticsearch官方提供的Java客户端，用于与Elasticsearch集群进行通信。
     * 它是Elasticsearch Java API的一部分，提供了一个高级的、易于使用的客户端，用于执行各种操作，如索引文档、搜索、更新、删除等。
     */
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    /**
     * 商品搜索
     *
     * @param searchData
     * @return
     */
    @Override
    public Map<String, Object> search(Map<String, String> searchData) {
        try {
            // 参数校验，条件拼接
            SearchRequest searchRequest = buildSearchParams(searchData);
            // 执行搜索
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            // 解析结果并返回
            return getSearchResult(searchResponse);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 构建查询条件
     *
     * @param searchData
     * @return
     */
    private SearchRequest buildSearchParams(Map<String, String> searchData) {
        // 初始化请求对象
        SearchRequest searchRequest = new SearchRequest("goods_java0107");
        // 构建条件构造器
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 构建一个组合查询对象
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        // 拼接条件，关键字不为空，则作为查询条件
        String keywords = searchData.get("keywords");
        if (!StringUtils.isEmpty(keywords)) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("title", keywords));
        }
        // 若分类不为空，作为查询条件    category=1:手机
        String category = searchData.get("category");
        if (!StringUtils.isEmpty(category)) {
            // 切分获取分类的id
            String[] split = category.split(":");
            boolQueryBuilder.must(QueryBuilders.termQuery("category3Id", split[0]));
        }
        // 设置品牌条件
        String tradeMark = searchData.get("tradeMark");
        if (!StringUtils.isEmpty(tradeMark)) {
            // 切分获取品牌的名字和id
            String[] split = tradeMark.split(":");
            boolQueryBuilder.must(QueryBuilders.termQuery("tmId", split[0]));
        } else {
            // 设置聚合条件   select tm_id as tmId from sku_info where name like '%手机%' group by tm_id
            /**
             *  AggregationBuilders.terms是一个用于创建术语（terms）聚合的工厂方法。术语聚合会将文档按照某个字段的值进行分组，并计算每个唯一值的文档数量。
             *  "aggTmId"是聚合的名称，这个名称会在搜索结果中用来标识这个聚合。field("tmId")指定了用于分组的字段，这里是tmId字段。
             *  这段代码构建了一个三级聚合结构：
             *  1. 最外层的聚合按tmId字段分组。
             *  2. 对于每个tmId分组，内部的tmName聚合按tmName字段的值进一步分组。
             *  3. 对于每个tmName分组，内部的tmLogoUrl聚合按tmLogoUrl字段的值进一步分组。
             *  执行这个搜索请求后，你将得到一个聚合结果，它包含了按tmId、tmName和tmLogoUrl分组的文档数量。
             */
            searchSourceBuilder.aggregation(    // aggregation方法用于添加一个聚合到搜索请求中。
                    AggregationBuilders.terms("aggTmId").field("tmId")
                            // 这是第一个子聚合，它会在每个tmId分组内部，进一步按tmName字段的值进行分组。
                            .subAggregation(AggregationBuilders.terms("aggTmName").field("tmName"))
                            // 这是第二个子聚合，它会在每个tmId分组内部，进一步按tmLogoUrl字段的值进行分组。
                            .subAggregation(AggregationBuilders.terms("aggTmLogoUrl").field("tmLogoUrl"))
                            .size(100)
            );
        }
        // 构建平台属性查询条件: 平台属性用户选择N个
        // where 关键字匹配 and 分类匹配 and 品牌匹配 and (平台属性id匹配 and 平台属性值匹配)
        searchData.entrySet().stream().forEach(param -> {
            // 获取参数的key
            String key = param.getKey();
            if (key.startsWith("attr_")) {
                // 平台属性条件: 1:联通4G
                String value = param.getValue();
                String[] split = value.split(":");
                // 构建nested查询使用的bool查询
                BoolQueryBuilder nestedBoolQueryBuilder = QueryBuilders.boolQuery();
                // 平台属性id必须等于用户选择的平台属性id
                nestedBoolQueryBuilder.must(QueryBuilders.termQuery("attrs.attrId", split[0]));
                // 平台属性值必须等于用户选择的值
                nestedBoolQueryBuilder.must(QueryBuilders.termQuery("attrs.attrValue", split[1]));
                // 将两个平台属性id和值的条件放入到最大的where条件后面去
                boolQueryBuilder.must(QueryBuilders.nestedQuery("attrs", nestedBoolQueryBuilder, ScoreMode.None));
            }
        });
        // 价格查询条件: 0-500元  3000元以上
        String price = searchData.get("price");
        if (!StringUtils.isEmpty(price)) {
            // 价格处理：0-500 3000
            price = price.replace("元", "").replace("以上", "");
            // 切分[0, 500] [3000]
            String[] split = price.split("-");
            // 大于等于第一个值
            boolQueryBuilder.must(QueryBuilders.rangeQuery("price").gte(split[0]));
            // 判断是否有第二个值
            if (split.length > 1) {
                boolQueryBuilder.must(QueryBuilders.rangeQuery("price").lt(split[1]));
            }
        }


        // 设置全部的查询条件
        searchSourceBuilder.query(boolQueryBuilder);
        // 设置平台属性聚合条件
        searchSourceBuilder.aggregation(
                AggregationBuilders.nested("aggAttrs", "attrs")
                        .subAggregation(
                                AggregationBuilders.terms("aggAttrId").field("attrs.attrId")
                                        .subAggregation(AggregationBuilders.terms("aggAttrName").field("attrs.attrName"))
                                        .subAggregation(AggregationBuilders.terms("aggAttrValue").field("attrs.attrValue"))
                                        .size(100)
                        )
        );
        // 设置排序
        String sortField = searchData.get("sortField");
        String sortRule = searchData.get("sortRule");
        if (!StringUtils.isEmpty(sortField) && !StringUtils.isEmpty(sortRule)) {
            searchSourceBuilder.sort(sortField, SortOrder.valueOf(sortRule));
        } else {
            // 说明没有排序规则，那么我们可以自定义默认排序：id降序（新品排序）
            searchSourceBuilder.sort("id", SortOrder.DESC);
        }
        // 设置分页
        Integer size = 100;
        searchSourceBuilder.size(size);
        // 获取页码
        Integer page = getPage(searchData.get("pageNum"));
        /**
         * es中默认从0开始
         * 1页 --> 0~99
         * 2页 --> 100~199
         */
        searchSourceBuilder.from((page - 1) * size);

        // 设置条件
        searchRequest.source(searchSourceBuilder);
        // 设置高亮属性
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");
        highlightBuilder.preTags("<em style=color:red>");
        highlightBuilder.postTags("</em>");
        searchSourceBuilder.highlighter(highlightBuilder);
        // 返回结果
        return searchRequest;
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
     * 解析搜索的返回结果
     *
     * @param searchResponse
     * @return
     */
    private Map<String, Object> getSearchResult(SearchResponse searchResponse) {
        // 返回结果初始化
        Map<String, Object> result = new HashMap<>();
        // 商品列表初始化
        List<Goods> goodsList = new ArrayList<>();
        // 获取所有命中的数据
        SearchHits hits = searchResponse.getHits();
        // 获取总命中的数据
        long totalHits = hits.getTotalHits();
        // 保存总命中的数据
        result.put("totalHits", totalHits);
        // 获取迭代器
        Iterator<SearchHit> iterator = hits.iterator();
        // 迭代解析数据
        while (iterator.hasNext()) {
            // 获取每条数据
            SearchHit next = iterator.next();
            // 获取每条数据的原始数据
            String sourceAsString = next.getSourceAsString();
            // 反序列化： 原始数据，没有任何高亮
            Goods goods = JSONObject.parseObject(sourceAsString, Goods.class);
            // 将高亮的数据获取出来
            Map<String, HighlightField> highlightFields = next.getHighlightFields();
            if (highlightFields != null && !highlightFields.isEmpty()) {
                // 将title高亮的数据取出来
                HighlightField highlightField = highlightFields.get("title");
                if (highlightField != null) {
                    Text[] fragments = highlightField.getFragments();
                    if (fragments != null && fragments.length > 0) {
                        String title = "";
                        for (Text fragment : fragments) {
                            title += fragment;
                        }
                        // 替换
                        goods.setTitle(title);
                    }
                }
            }
            // 保存数据
            goodsList.add(goods);
        }
        // 保存商品列表
        result.put("goodsList", goodsList);
        // 解析全部的聚合结果
        Aggregations aggregations = searchResponse.getAggregations();
        // 解析品牌的聚合结果
        List<SearchResponseTmVo> searchResponseTmVoList = getTmAggeResult(aggregations);
        // 保存品牌聚合结果
        result.put("searchResponseTmVoList", searchResponseTmVoList);
        // 解析平台属性的聚合结果
        List<SearchResponseAttrVo> searchResponseAttrVoList = getSearchAttrAggResult(aggregations);
        result.put("searchResponseAttrVoList", searchResponseAttrVoList);

        // 返回结果
        return result;
    }

    /**
     * 获取平台属性的聚合结果
     *
     * @param aggregations
     * @return
     */
    private List<SearchResponseAttrVo> getSearchAttrAggResult(Aggregations aggregations) {
        // 通过别名获取nested类型的聚合结果
        ParsedNested aggAttrs = aggregations.get("aggAttrs");
        // 获取子聚合的结果: 平台属性id的聚合结果
        Aggregations subAggregations = aggAttrs.getAggregations();
        ParsedLongTerms aggAttrIds = subAggregations.get("aggAttrId");
        // 获取平台属性id聚合结果的集合
        List<SearchResponseAttrVo> collect = aggAttrIds.getBuckets().stream().map(aggAttrId -> {
            // 初始化平台属性VO对象
            SearchResponseAttrVo searchResponseAttrVo = new SearchResponseAttrVo();
            // 获取平台属性的id
            long attrId = ((Terms.Bucket) aggAttrId).getKeyAsNumber().longValue();
            searchResponseAttrVo.setAttrId(attrId);
            // 获取子聚合中的名字
            ParsedStringTerms aggAttrName = ((Terms.Bucket) aggAttrId).getAggregations().get("aggAttrName");
            List<? extends Terms.Bucket> attrNameBuckets = aggAttrName.getBuckets();
            if (attrNameBuckets != null && !attrNameBuckets.isEmpty()) {
                // 获取平台属性的名字
                String attrName = attrNameBuckets.get(0).getKeyAsString();
                searchResponseAttrVo.setAttrName(attrName);
            }
            // 获取子聚合中所有的值
            ParsedStringTerms aggAttrValue = ((Terms.Bucket) aggAttrId).getAggregations().get("aggAttrValue");
            List<? extends Terms.Bucket> aggAttrValueBuckets = aggAttrValue.getBuckets();
            if (aggAttrValueBuckets != null && !aggAttrValueBuckets.isEmpty()) {
                // 获取所有的平台属性值
                List<String> attrValueList = aggAttrValueBuckets.stream().map(aggAttrValueBucket -> {
                    // 获取值的名字,并返回
                    return ((Terms.Bucket) aggAttrValueBucket).getKeyAsString();

                }).collect(Collectors.toList());
                // 保存值列表
                searchResponseAttrVo.setAttrValueList(attrValueList);
            }
            return searchResponseAttrVo;
        }).collect(Collectors.toList());
        // 返回结果
        return collect;
    }

    /**
     * 解析品牌的聚合结果
     *
     * @param aggregations
     * @return
     */
    private List<SearchResponseTmVo> getTmAggeResult(Aggregations aggregations) {
        // 获取品牌的聚合结果
        ParsedLongTerms aggTmIdResult = aggregations.get("aggTmId");
        if (aggTmIdResult == null)  return null;
        // 获取每条品牌id的聚合结果
        List<SearchResponseTmVo> collect = aggTmIdResult.getBuckets().stream().map(aggTmId -> {
            SearchResponseTmVo searchResponseTmVo = new SearchResponseTmVo();
            // 获取品牌的id
            long tmId = ((Terms.Bucket) aggTmId).getKeyAsNumber().longValue();
            // 设置品牌id
            searchResponseTmVo.setTmId(tmId);
            // 包含子聚合的名字
            ParsedStringTerms aggTmName = ((Terms.Bucket) aggTmId).getAggregations().get("aggTmName");
            List<? extends Terms.Bucket> tmNameBuckets = aggTmName.getBuckets();
            if (tmNameBuckets != null && !tmNameBuckets.isEmpty()) {
                // 获取品牌的名字
                String tmName = tmNameBuckets.get(0).getKeyAsString();
                searchResponseTmVo.setTmName(tmName);
            }
            // 包含子聚合的logo
            ParsedStringTerms aggTmLogoUrl = ((Terms.Bucket) aggTmId).getAggregations().get("aggTmLogoUrl");
            List<? extends Terms.Bucket> tmLogoUrlBuckets = aggTmLogoUrl.getBuckets();
            if (tmLogoUrlBuckets != null && !tmLogoUrlBuckets.isEmpty()) {
                // 获取品牌的logo
                String tmLogoUrl = tmLogoUrlBuckets.get(0).getKeyAsString();
                searchResponseTmVo.setTmLogoUrl(tmLogoUrl);
            }
            // 返回结果
            return searchResponseTmVo;
        }).collect(Collectors.toList());

        return collect;
    }
}
