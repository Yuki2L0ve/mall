package com.atguigu.gmall.product.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.product.mapper.BaseCategoryViewMapper;
import com.atguigu.gmall.product.service.IndexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 首页信息查询的接口类
 */
@Service
public class IndexServiceImpl implements IndexService {
    @Autowired
    private BaseCategoryViewMapper baseCategoryViewMapper;

    /**
     * 获取首页的分类信息
     *
     * @return
     */
    @Override
    public List<JSONObject> getIndexCategory() {
        // 查询出所有的一级、二级、三级分类的信息
        List<BaseCategoryView> baseCategory1ViewList = baseCategoryViewMapper.selectList(null);
        // 以一级分类为单位进行分桶，把所有一级分类不同的数据分开进行保存: 一级重复 二级重复 三级不重复
        Map<Long, List<BaseCategoryView>> category1Map =
                baseCategory1ViewList.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory1Id));

        // 对这个map进行遍历
        List<JSONObject> collect = category1Map.entrySet().stream().map(category1 -> {
            // 返回结果初始化
            JSONObject category1Json = new JSONObject();
            // 获取一级分类的id
            Long category1Id = category1.getKey();
            category1Json.put("categoryId", category1Id);
            // 获取每个一级分类id所对应的全部二级三级分类
            List<BaseCategoryView> baseCategory2ViewList = category1.getValue();
            // 获取一级分类的名字
            String category1Name = baseCategory2ViewList.get(0).getCategory1Name();
            category1Json.put("categoryName", category1Name);

            // 以二级分类为单位进行分桶，把所有二级分类不同的数据分开进行保存: 二级重复 三级不重复
            Map<Long, List<BaseCategoryView>> category2Map =
                    baseCategory2ViewList.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory2Id));
            // 对这个map进行遍历
            List<JSONObject> category2JsonList = category2Map.entrySet().stream().map(category2 -> {
                // 返回结果初始化
                JSONObject category2Json = new JSONObject();
                // 获取二级分类id
                Long category2Id = category2.getKey();
                category2Json.put("categoryId", category2Id);
                // 获取每个二级分类id所对应的全部三级分类数据: 此时一级分类、二级分类都已经确定，三级分类不重复
                List<BaseCategoryView> baseCategory3ViewList = category2.getValue();
                // 获取二级分类的名字
                String category2Name = baseCategory3ViewList.get(0).getCategory2Name();
                category2Json.put("categoryName", category2Name);
                // 遍历以上这个List，取出对应的三级分类id和名字，供页面展示
                List<JSONObject> category3JsonList = baseCategory3ViewList.stream().map(baseCategory3View -> {
                    // 返回结果初始化
                    JSONObject category3Json = new JSONObject();
                    // 获取三级分类id
                    Long category3Id = baseCategory3View.getCategory3Id();
                    category3Json.put("categoryId", category3Id);
                    // 获取三级分类的名字
                    String category3Name = baseCategory3View.getCategory3Name();
                    category3Json.put("categoryName", category3Name);
                    // 返回三级的结果
                    return category3Json;
                }).collect(Collectors.toList());
                // 保存每个二级分类对应的三级分类信息
                category2Json.put("childCategory", category3JsonList);
                // 返回二级的结果
                return category2Json;
            }).collect(Collectors.toList());
            // 保存每个一级分类对应的二级分类信息
            category1Json.put("childCategory", category2JsonList);
            // 返回一级的结果
            return category1Json;
        }).collect(Collectors.toList());

        return collect;
    }
}
