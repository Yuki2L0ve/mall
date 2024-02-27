package com.atguigu.gmall.oauth.mapper;

import com.atguigu.gmall.model.user.UserInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户表的mapper映射
 */
@Mapper
public interface UserInfoMapper extends BaseMapper<UserInfo> {

}
