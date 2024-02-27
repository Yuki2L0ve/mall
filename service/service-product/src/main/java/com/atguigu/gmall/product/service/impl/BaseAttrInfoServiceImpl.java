package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.common.execption.GmallException;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.atguigu.gmall.product.mapper.BaseAttrInfoMapper;
import com.atguigu.gmall.product.service.BaseAttrInfoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * 平台属性相关的服务层的实现类
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class BaseAttrInfoServiceImpl implements BaseAttrInfoService {
    @Resource
    private BaseAttrInfoMapper baseAttrInfoMapper;

    /**
     * 新增
     * @param baseAttrInfo
     */
    @Override
    public void add(BaseAttrInfo baseAttrInfo) {
        // 参数校验
        if (baseAttrInfo == null) {
            throw new GmallException("参数不能为空", ResultCodeEnum.FAIL.getCode());
        }
        // 新增
        int count = baseAttrInfoMapper.insert(baseAttrInfo);
        if (count <= 0) {
            throw new GmallException("新增失败，请重试！", ResultCodeEnum.FAIL.getCode());
        }
    }

    /**
     * 修改
     * @param baseAttrInfo
     */
    @Override
    public void update(BaseAttrInfo baseAttrInfo) {
        // 参数校验
        if (baseAttrInfo == null || baseAttrInfo.getId() == null) {
            throw new GmallException("参数不能为空", ResultCodeEnum.FAIL.getCode());
        }
        // 修改
        int count = baseAttrInfoMapper.updateById(baseAttrInfo);
        if (count < 0) {
            throw new GmallException("修改失败，请重试！", ResultCodeEnum.FAIL.getCode());
        }

    }

    /**
     * 删除
     * @param id
     */
    @Override
    public void delete(Long id) {
        // 参数校验
        if (id == null) {
            throw new GmallException("参数不能为空", ResultCodeEnum.FAIL.getCode());
        }
        // 删除
        int count = baseAttrInfoMapper.deleteById(id);
        if (count < 0) {
            throw new GmallException("删除失败，请重试！", ResultCodeEnum.FAIL.getCode());
        }
    }

    /**
     * 分页条件查询
     *
     * @param baseAttrInfo
     * @param page
     * @param size
     * @return
     */
    @Override
    public IPage<BaseAttrInfo> search(BaseAttrInfo baseAttrInfo, Integer page, Integer size) {
        // 参数校验
        if (page == null) {
            page = 1;   // 默认第一页
        }
        if (size == null) {
            size = 10;  // 默认10条
        }
        // 参数校验
        if (baseAttrInfo == null) { // 表示查询全部
            return baseAttrInfoMapper.selectPage(new Page<>(page, size), null);
        }
        // 拼接查询条件
        LambdaQueryWrapper wrapper = buildQueryWrapper(baseAttrInfo);
        return baseAttrInfoMapper.selectPage(new Page<>(page, size), wrapper);
    }

    /**
     * 拼接查询条件
     * @param baseAttrInfo
     * @return
     */
    private LambdaQueryWrapper buildQueryWrapper(BaseAttrInfo baseAttrInfo) {
        // 拼接查询条件，声明条件构造器
        LambdaQueryWrapper<BaseAttrInfo> wrapper = new LambdaQueryWrapper<>();
        // id不为空，作为条件: 等于
        if (baseAttrInfo.getId() != null) {
            wrapper.eq(BaseAttrInfo::getId, baseAttrInfo.getId());
        }
        // 名字：模糊查询like
        if (!StringUtils.isEmpty(baseAttrInfo.getAttrName())) {
            wrapper.like(BaseAttrInfo::getAttrName, baseAttrInfo.getAttrName());
        }
        // 分类id：等于
        if (baseAttrInfo.getCategoryId() != null) {
            wrapper.eq(BaseAttrInfo::getCategoryId, baseAttrInfo.getCategoryId());
        }

        // 返回拼接好的条件
        return wrapper;
    }

    /**
     * 分页查询
     *
     * @param page
     * @param size
     * @return
     */
    @Override
    public IPage<BaseAttrInfo> page(Integer page, Integer size) {
        // 参数校验
        if (page == null) {
            page = 1;   // 默认第一页
        }
        if (size == null) {
            size = 10;  // 默认10条
        }
        // 分页查询
        return baseAttrInfoMapper.selectPage(new Page<>(page, size), null);
    }

    /**
     * 条件查询
     *
     * @param baseAttrInfo
     * @return
     */
    @Override
    public List<BaseAttrInfo> search(BaseAttrInfo baseAttrInfo) {
        // 参数校验
        if (baseAttrInfo == null) { // 表示查询全部
            return baseAttrInfoMapper.selectList(null);
        }
        // 拼接查询条件
        LambdaQueryWrapper wrapper = buildQueryWrapper(baseAttrInfo);
        // 设定条件之后，进行查询，然后返回查询结果
        return baseAttrInfoMapper.selectList(wrapper);
    }

    /**
     * 主键查询
     * @param id
     * @return
     */
    @Override
    public BaseAttrInfo getBaseAttrInfo(Long id) {
        return baseAttrInfoMapper.selectById(id);
    }

    /**
     * 查询全部
     * @return
     */
    @Override
    public List<BaseAttrInfo> getAll() {
        return baseAttrInfoMapper.selectList(null);
    }
}
