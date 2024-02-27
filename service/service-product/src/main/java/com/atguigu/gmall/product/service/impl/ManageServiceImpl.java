package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.common.constant.ProductConst;
import com.atguigu.gmall.common.execption.GmallException;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.list.feign.GoodsFeign;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.mapper.*;
import com.atguigu.gmall.product.service.ManageService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * 后台管理页面使用的服务层的实现类
 */
@Service
@Transactional(rollbackFor = Exception.class)
@Log4j2
public class ManageServiceImpl implements ManageService {
    @Resource
    private BaseCategory1Mapper baseCategory1Mapper;
    @Resource
    private BaseCategory2Mapper baseCategory2Mapper;
    @Resource
    private BaseCategory3Mapper baseCategory3Mapper;
    @Resource
    private BaseAttrInfoMapper baseAttrInfoMapper;
    @Resource
    private BaseAttrValueMapper baseAttrValueMapper;
    @Resource
    private BaseTradeMarkMapper baseTradeMarkMapper;
    @Resource
    private BaseSaleAttrMapper baseSaleAttrMapper;
    @Resource
    private SpuInfoMapper spuInfoMapper;
    @Resource
    private SpuImageMapper spuImageMapper;
    @Resource
    private SpuSaleAttrMapper spuSaleAttrMapper;
    @Resource
    private SpuSaleAttrValueMapper spuSaleAttrValueMapper;
    @Resource
    private SkuInfoMapper skuInfoMapper;
    @Resource
    private SkuImageMapper skuImageMapper;
    @Resource
    private SkuAttrValueMapper skuAttrValueMapper;
    @Resource
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;
    @Autowired
    private GoodsFeign goodsFeign;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 查询所有的一级分类
     *
     * @return
     */
    @Override
    public List<BaseCategory1> getBaseCategory1() {
        return baseCategory1Mapper.selectList(null);
    }

    /**
     * 根据一级分类id，查询出该一级分类下的所有二级分类
     *
     * @param c1Id
     * @return
     */
    @Override
    public List<BaseCategory2> getBaseCategory2(Long c1Id) {
        return baseCategory2Mapper.selectList(new LambdaQueryWrapper<BaseCategory2>().eq(BaseCategory2::getCategory1Id, c1Id));
    }

    /**
     * 根据二级分类id，查询出该二级分类下的所有三级分类
     *
     * @param c2Id
     * @return
     */
    @Override
    public List<BaseCategory3> getBaseCategory3(Long c2Id) {
        return baseCategory3Mapper.selectList(new LambdaQueryWrapper<BaseCategory3>().eq(BaseCategory3::getCategory2Id, c2Id));
    }

    /**
     * 获取平台属性列表
     *
     * @param category3Id
     * @return
     */
    @Override
    public List<BaseAttrInfo> getBaseAttrInfo(Long category3Id) {
        return baseAttrInfoMapper.selectBaseAttrInfoByCategoryId(category3Id);
    }

    /**
     * 删除平台属性
     *
     * @param attrId
     */
    @Override
    public void deleteBaseAttrInfo(Long attrId) {
        // 参数校验
        if (attrId == null) {
            return ;
        }
        // 删除 --> 名称
        int i = baseAttrInfoMapper.deleteById(attrId);
        if (i < 0) {
            throw new RuntimeException("删除平台属性名称失败！");
        }
        // 平台属性值也要删除
        int delete = baseAttrValueMapper.delete(new LambdaQueryWrapper<BaseAttrValue>().eq(BaseAttrValue::getAttrId, attrId));
        if (delete < 0) {
            throw new RuntimeException("删除平台属性值失败！");
        }

    }

    /**
     * 查询所有的品牌列表
     *
     * @return
     */
    @Override
    public List<BaseTrademark> getBaseTrademark() {
        return baseTradeMarkMapper.selectList(null);
    }

    /**
     * 查询所有的基础销售属性
     *
     * @return
     */
    @Override
    public List<BaseSaleAttr> getBaseSaleAttr() {
        return baseSaleAttrMapper.selectList(null);
    }

    /**
     * 保存spu的信息
     *
     * @param spuInfo
     */
    @Override
    public void saveSpuInfo(SpuInfo spuInfo) {
        // 参数校验
        if (spuInfo == null) {
            throw new RuntimeException("参数错误");
        }
        // 判断是修改还是新增
        if (spuInfo.getId() != null) {  // 修改
            int update = spuInfoMapper.updateById(spuInfo);
            if (update < 0) {
                throw new RuntimeException("修改SPU失败");
            }
            // 删除图片
            int deleteImage = spuImageMapper.delete(new LambdaQueryWrapper<SpuImage>().eq(SpuImage::getSpuId, spuInfo.getId()));
            // 删除销售属性名称
            int deleteSaleAttr = spuSaleAttrMapper.delete(new LambdaQueryWrapper<SpuSaleAttr>().eq(SpuSaleAttr::getSpuId, spuInfo.getId()));
            // 删除销售属性值
            int deleteSaleAttrValue = spuSaleAttrValueMapper.delete(new LambdaQueryWrapper<SpuSaleAttrValue>().eq(SpuSaleAttrValue::getSpuId, spuInfo.getId()));
            // 判断删除的结果
            if (deleteImage < 0 || deleteSaleAttr < 0 || deleteSaleAttrValue < 0) {
                throw new RuntimeException("修改SPU失败");
            }
        } else {    // 新增
            int insert = spuInfoMapper.insert(spuInfo);
            if (insert <= 0) {
                throw new RuntimeException("新增SPU失败");
            }
        }

        // 获取SPU的id
        Long spuId = spuInfo.getId();
        // 新增图片表
        addSpuImage(spuId, spuInfo.getSpuImageList());
        // 新增销售属性名称表
        addSpuSaleAttr(spuId, spuInfo.getSpuSaleAttrList());

    }

    /**
     * 分页条件查询spuInfo
     *
     * @param page  请求的页码
     * @param size  每页显示的记录数
     * @param category3Id   三级分类ID
     * @return
     * IPage<SpuInfo>：这是MyBatis Plus中的一个接口，用于表示分页查询的结果。它包含了分页信息（如当前页码、每页大小、总记录数等）以及分页结果的数据列表。
     */
    @Override
    public IPage<SpuInfo> pageSpuInfo(Integer page, Integer size, Long category3Id) {
        // 这里调用了MyBatis Plus的selectPage方法来实现分页查询
        return spuInfoMapper.selectPage(new Page<>(page, size), new LambdaQueryWrapper<SpuInfo>().eq(SpuInfo::getCategory3Id, category3Id));
    }

    /**
     * 根据SPU的id查询销售属性列表
     *
     * @param spuId
     * @return
     */
    @Override
    public List<SpuSaleAttr> getSpuSaleAttr(Long spuId) {
        return spuSaleAttrMapper.selectSpuAttrBySpuId(spuId);
    }

    /**
     * 查询指定SPU的图片列表
     *
     * @param spuId
     * @return
     */
    @Override
    public List<SpuImage> getSpuImage(Long spuId) {
        return spuImageMapper.selectList(new LambdaQueryWrapper<SpuImage>().eq(SpuImage::getSpuId, spuId));
    }

    /**
     * 保存SKU的信息
     *
     * @param skuInfo
     */
    @Override
    public void saveSkuInfo(SkuInfo skuInfo) {
        // 参数校验
        if (skuInfo == null) {
            throw new RuntimeException("参数错误");
        }
        // 判断用户是新增SKU还是修改SKU
        if (skuInfo.getId() != null) {  // 修改SKU
            // 修改skuInfo
            int update = skuInfoMapper.updateById(skuInfo);
            if (update < 0) {
                throw new RuntimeException("修改SKU失败");
            }
            // 删除图片
            int deleteImage = skuImageMapper.delete(new LambdaQueryWrapper<SkuImage>().eq(SkuImage::getSkuId, skuInfo.getId()));
            // 销售属性
            int deleteSaleAttr = skuSaleAttrValueMapper.delete(new LambdaQueryWrapper<SkuSaleAttrValue>().eq(SkuSaleAttrValue::getSkuId, skuInfo.getId()));
            // 平台属性
            int deleteAttr = skuAttrValueMapper.delete(new LambdaQueryWrapper<SkuAttrValue>().eq(SkuAttrValue::getSkuId, skuInfo.getId()));
            if (deleteImage < 0 || deleteSaleAttr < 0 || deleteAttr < 0) {
                throw new RuntimeException("修改SKU失败");
            }
        } else {    // 新增SKU
            int insert = skuInfoMapper.insert(skuInfo);
            if (insert <= 0) {
                throw new RuntimeException("新增SKU失败");
            }
        }
        // 获取SKU的id
        Long skuId = skuInfo.getId();
        // 新增SKU图片
        addSkuImage(skuId, skuInfo.getSkuImageList());
        // 新增平台属性
        addSkuAttrValue(skuId, skuInfo.getSkuAttrValueList());
        // 新增销售属性
        addSkuSaleAttrValue(skuId, skuInfo.getSkuSaleAttrValueList(), skuInfo.getSpuId());
    }

    /**
     * 新增SKU的销售属性
     * @param skuId
     * @param skuSaleAttrValueList
     * @param spuId
     */
    private void addSkuSaleAttrValue(Long skuId, List<SkuSaleAttrValue> skuSaleAttrValueList, Long spuId) {
        // 遍历保存每个销售属性的信息
        skuSaleAttrValueList.stream().forEach(skuSaleAttrValue -> {
            // 补全sku id
            skuSaleAttrValue.setSkuId(skuId);
            // 补全spu id
            skuSaleAttrValue.setSpuId(spuId);
            // 新增
            int insert = skuSaleAttrValueMapper.insert(skuSaleAttrValue);
            if (insert <= 0) {
                throw new RuntimeException("新增SKU的销售属性失败");
            }
        });
    }


    /**
     * 新增平台属性
     * @param skuId
     * @param skuAttrValueList
     */
    private void addSkuAttrValue(Long skuId, List<SkuAttrValue> skuAttrValueList) {
        // 遍历保存sku的每个平台属性和值的信息
        skuAttrValueList.stream().forEach(skuAttrValue -> {
            // 补全sku的id
            skuAttrValue.setSkuId(skuId);
            // 保存数据
            int insert = skuAttrValueMapper.insert(skuAttrValue);
            if (insert <= 0) {
                throw new RuntimeException("新增SKU的平台属性失败");
            }
        });
    }

    /**
     * 分页查询SKU的信息
     *
     * @param page
     * @param size
     * @return
     */
    @Override
    public IPage<SkuInfo> list(Integer page, Integer size) {
        return skuInfoMapper.selectPage(new Page<>(page, size), null);
    }

    /**
     * 新增SKU的图片列表
     * @param skuId
     * @param skuImageList
     */
    private void addSkuImage(Long skuId, List<SkuImage> skuImageList) {
        // 遍历保存每一张图片
        skuImageList.stream().forEach(skuImage -> {
            // 补全sku的id
            skuImage.setSkuId(skuId);
            // 新增
            int insert = skuImageMapper.insert(skuImage);
            if (insert <= 0) {
                throw new RuntimeException("新增SKU的图片失败");
            }
        });
    }

    /**
     * 保存SPU的销售属性信息
     * @param spuId
     * @param spuSaleAttrList
     */
    private void addSpuSaleAttr(Long spuId, List<SpuSaleAttr> spuSaleAttrList) {
        spuSaleAttrList.stream().forEach(spuSaleAttr -> {
            // 补全SPU的id
            spuSaleAttr.setSpuId(spuId);
            // 保存数据
            int insert = spuSaleAttrMapper.insert(spuSaleAttr);
            if (insert <= 0) {
                throw new RuntimeException("新增SPU的销售属性名称失败");
            }
            // 保存这个名称的值列表
            addSpuSaleAttrValue(spuId, spuSaleAttr.getSpuSaleAttrValueList(), spuSaleAttr.getSaleAttrName());
        });
    }

    /**
     * 保存SPU的销售值列表
     * @param spuId
     * @param spuSaleAttrValueList
     * @param saleAttrName
     */
    private void addSpuSaleAttrValue(Long spuId, List<SpuSaleAttrValue> spuSaleAttrValueList, String saleAttrName) {
        spuSaleAttrValueList.stream().forEach(spuSaleAttrValue -> {
            // 补全SPU的id
            spuSaleAttrValue.setSpuId(spuId);
            // 补全销售属性的名称
            spuSaleAttrValue.setSaleAttrName(saleAttrName);
            // 保存数据
            int insert = spuSaleAttrValueMapper.insert(spuSaleAttrValue);
            if (insert <= 0) {
                throw new RuntimeException("新增SPU的销售属性值失败");
            }
        });
    }

    /**
     * 新增SPU的图片
     * @param spuId
     * @param spuImageList
     */
    private void addSpuImage(Long spuId, List<SpuImage> spuImageList) {
        // 遍历新增SPU的图片
        spuImageList.stream().forEach(spuImage -> {
            // 补全SPU的id
            spuImage.setSpuId(spuId);
            // 保存spu的图片
            int insert = spuImageMapper.insert(spuImage);
            if (insert <= 0) {
                throw new RuntimeException("新增SPU的图片失败");
            }
        });
    }

    /**
     * 保存平台属性
     *
     * @param baseAttrInfo
     */
    @Override
    public void saveBaseAttrInfo(BaseAttrInfo baseAttrInfo) {
        // 参数校验
        if (baseAttrInfo == null || StringUtils.isEmpty(baseAttrInfo.getAttrName())) {
            throw new GmallException("参数错误", ResultCodeEnum.FAIL.getCode());
        }
        // 判断用户是修改还是新增
        if (baseAttrInfo.getId() != null) { // 修改
            int update = baseAttrInfoMapper.updateById(baseAttrInfo);
            if (update < 0) {
                throw new RuntimeException("修改平台属性名称表数据失败！");
            }
            // 所有旧的值都全部删掉
            int delete = baseAttrValueMapper.delete(new LambdaQueryWrapper<BaseAttrValue>().eq(BaseAttrValue::getAttrId, baseAttrInfo.getId()));
            if (delete < 0) {
                throw new RuntimeException("修改平台属性名称表数据失败！");
            }
        } else {    // 新增
            // 新增平台属性名称表
            int insert = baseAttrInfoMapper.insert(baseAttrInfo);
            if (insert <= 0) {
                throw new RuntimeException("新增平台属性名称表数据失败！");
            }
        }
        // 新增成功，就能获取平台属性的id
        Long attrId = baseAttrInfo.getId();
        // 将id补充到平台属性每个值的对象中
        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();

        // 写法1：普通for循环
//        for (BaseAttrValue baseAttrValue : attrValueList) {
//            // 判断用户输入的属性值是否为空，比如 A B C D，前三者不为空，但D为空，那么我们只保存A B C
//            if (!StringUtils.isEmpty(baseAttrValue.getValueName())) {
//                // 补充平台属性的id
//                baseAttrValue.setAttrId(attrId);
//                // 保存到数据库
//                int count1 = baseAttrValueMapper.insert(baseAttrValue);
//                if (count1 <= 0) {
//                    throw new GmallException("新增平台属性值名称失败！", ResultCodeEnum.FAIL.getCode());
//                }
//            }
//        }

        // 写法2：流式编程
        attrValueList.stream().forEach(baseAttrValue -> {
            if (!StringUtils.isEmpty(baseAttrValue.getValueName())) {
                // 补充平台属性的id
                baseAttrValue.setAttrId(attrId);
                // 保存到数据库
                int insertCount = baseAttrValueMapper.insert(baseAttrValue);
                if (insertCount <= 0) {
                    throw new GmallException("新增平台属性值名称失败！", ResultCodeEnum.FAIL.getCode());
                }
            }
        });
    }

    /**
     * 商品上架或者下架
     *
     * @param skuId
     * @param status
     */
    @Override
    public void upOrDown(Long skuId, Short status) {
        // 参数校验
        if (skuId == null)  return ;
        // 查询商品是否存在
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
        if (skuInfo == null || skuInfo.getId() == null) return ;
        // 修改SKU的上下架状态
        // 将skuInfo对象的isSale属性设置为传入的status值,修改的是Java对象的状态
        skuInfo.setIsSale(status);
        // 修改
        // 然而，仅仅修改了skuInfo对象的状态并不会自动更新数据库中的记录。在Java中，对象的状态改变并不会自动同步到数据库。
        // 为了将这些更改持久化到数据库，需要执行一个更新操作。skuInfoMapper.updateById(skuInfo); 执行的是数据库记录的更新
        int update = skuInfoMapper.updateById(skuInfo);
        if (update < 0) {
            throw new RuntimeException(("上下架失败!"));
        }


        // 将数据写入es或者将数据从es中删除   --TODO feign调用是一次失败（数据不一致） --> 待优化--MQ
        if (status.equals(ProductConst.SKU_ON_SALE)) {  // 写入es
            // goodsFeign.addGoods(skuId);
            /**
             * 这是调用RabbitTemplate实例的convertAndSend方法，用于发送消息到RabbitMQ。
             * "product_exchange"是交换机（Exchange）的名称。在这个例子中，消息被发送到名为product_exchange的直接交换机。
             * "sku.upper"是路由键（Routing Key）。RabbitMQ使用路由键来决定如何将消息路由到绑定的队列。
             * 在这个例子中，路由键sku.upper可能用于标识商品上架的消息。
             * skuId + ""是要发送的消息的内容。这里，skuId是一个变量，它可能代表了商品的唯一标识符。通过调用+ ""，将skuId转换为字符串形式，以便发送。
             * 这段代码的作用是将一个包含商品ID的消息发送到RabbitMQ的product_exchange交换机。
             * 这个消息将根据路由键sku.upper被路由到相应的队列。然后，监听这个队列的消费者可以接收到这个消息，
             * 并执行相应的业务逻辑，比如更新Elasticsearch中的库存数据。
             */
            rabbitTemplate.convertAndSend("product_exchange", "sku.upper", skuId + "");
        } else {    // 从es中删除
            // goodsFeign.removeGoods(skuId);
            rabbitTemplate.convertAndSend("product_exchange", "sku.down", skuId + "");
        }
    }
}
