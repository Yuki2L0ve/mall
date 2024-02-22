package com.example.mall.cart.mapper;

import com.example.mall.model.cart.CartInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 购物车表的mapper映射
 */
@Mapper
public interface CartInfoMapper extends BaseMapper<CartInfo> {

    /**
     * 修改购物车数量
     * @param id
     * @param num
     * @param username
     * @return
     */
    @Update("update cart_info set sku_num = #{num} where id = #{id} and user_id = #{username}")
    public int updateCartInfoNum(@Param("id") Long id, @Param("num") Integer num, @Param("username") String username);

    /**
     * 修改选中状态：全选 or 全不选
     * @param username
     * @param status
     * @return
     */
    @Update("update cart_info set is_checked = #{status} where user_id = #{username}")
    public int updateCheckAll(@Param("username") String username, @Param("status") Short status);

    /**
     * 修改单个选中状态：选中 or 不选中
     * @param username
     * @param status
     * @param id
     * @return
     */
    @Update("update cart_info set is_checked = #{status} where user_id = #{username} and id = #{id}")
    public int updateCheck(@Param("username") String username, @Param("status") Short status, @Param("id") Long id);
}
