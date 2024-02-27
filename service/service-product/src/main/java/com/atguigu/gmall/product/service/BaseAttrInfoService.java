package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

/**
 * 平台属性相关的服务层
 */
public interface BaseAttrInfoService {

    /**
     * 主键查询
     * @param id
     * @return
     */
    public BaseAttrInfo getBaseAttrInfo(Long id);

    /**
     * 查询全部
     * @return
     */
    public List<BaseAttrInfo> getAll();

    /**
     * 新增
     * @param baseAttrInfo
     */
    public void add(BaseAttrInfo baseAttrInfo);

    /**
     * 修改
     * @param baseAttrInfo
     */
    public void update(BaseAttrInfo baseAttrInfo);

    /**
     * 删除
     * @param id
     */
    public void delete(Long id);

    /**
     * 条件查询
     * @param baseAttrInfo
     * @return
     */
    public List<BaseAttrInfo> search(BaseAttrInfo baseAttrInfo);

    /**
     * 分页查询
     *
     * @param page
     * @param size
     * @return
     */
    public IPage<BaseAttrInfo> page(Integer page, Integer size);

    /**
     * 分页条件查询
     *
     * @param baseAttrInfo
     * @param page
     * @param size
     * @return
     */
    public IPage<BaseAttrInfo> search(BaseAttrInfo baseAttrInfo, Integer page, Integer size);
}
