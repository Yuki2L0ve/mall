package com.atguigu.gmall.oauth.config;

import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.oauth.mapper.UserInfoMapper;
import com.atguigu.gmall.oauth.util.UserJwt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;

/**
 * 自定义授权认证类
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    ClientDetailsService clientDetailsService;
    @Resource
    private UserInfoMapper userInfoMapper;

    /**
     * 自定义授权认证
     *
     * @param username
     * @return
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 取出身份，如果身份为空说明没有认证
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // 没有认证统一采用httpbasic认证，httpbasic中存储了client_id和client_secret，开始认证client_id和client_secret
        if (authentication == null) {
            // 通过客户端id查询客户端秘钥
            ClientDetails clientDetails = clientDetailsService.loadClientByClientId(username);
            if (clientDetails != null) {
                // 获取查询到的秘钥
                String clientSecret = clientDetails.getClientSecret();
                // 校验用户的客户端id和客户端秘钥是否正确
                return new User(username,
                        clientSecret,
                        AuthorityUtils.commaSeparatedStringToAuthorityList(""));
            }
        }
        // 密码模式---校验用户的用户名是否为空
        if (StringUtils.isEmpty(username)) {
            throw new RuntimeException("用户名不能为空！！！");
        }
        // 根据用户名查询用户信息
        UserInfo userInfo = userInfoMapper.selectOne(new LambdaQueryWrapper<UserInfo>().eq(UserInfo::getLoginName, username));
        if (userInfo == null || userInfo.getId() == null) {
            throw new RuntimeException("用户不存在！！！");
        }
        String pwd = new BCryptPasswordEncoder().encode("atguigu");
        // 创建User对象,校验用户名密码
        UserJwt userDetails = new UserJwt(username,
                userInfo.getPasswd(),
                AuthorityUtils.commaSeparatedStringToAuthorityList(""));
        // 返回结果
        return userDetails;
    }
}
