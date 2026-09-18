package com.ruoyi.framework.web.service;

import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.system.domain.SysUser;
import com.ruoyi.system.mapper.SysUserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户身份加载服务（若依标准）：根据用户名从 sys_user 加载并构建 LoginUser
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService
{
    private static final Logger log = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    @Autowired
    private SysUserMapper sysUserMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
    {
        SysUser user = sysUserMapper.selectUserByUserName(username);
        if (user == null)
        {
            log.info("登录用户：{} 不存在", username);
            throw new UsernameNotFoundException("登录用户：" + username + " 不存在");
        }
        else if ("1".equals(user.getStatus()))
        {
            log.info("登录用户：{} 已被停用", username);
            throw new RuntimeException("账号：" + username + " 已停用");
        }
        else if ("2".equals(user.getDelFlag()))
        {
            log.info("登录用户：{} 已被删除", username);
            throw new RuntimeException("账号：" + username + " 不存在");
        }

        List<String> roleKeys = sysUserMapper.selectRoleKeysByUserId(user.getUserId());

        Set<String> permissions = new HashSet<>();
        if (roleKeys != null && roleKeys.contains("admin"))
        {
            // 管理员默认拥有全部权限
            permissions.add("*:*:*");
        }

        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(user.getUserId());
        loginUser.setDeptId(user.getDeptId());
        loginUser.setUserName(user.getUserName());
        loginUser.setNickName(user.getNickName());
        loginUser.setAvatar(user.getAvatar());
        loginUser.setPassword(user.getPassword());
        loginUser.setRoles(roleKeys);
        loginUser.setPermissions(permissions);
        return loginUser;
    }
}
