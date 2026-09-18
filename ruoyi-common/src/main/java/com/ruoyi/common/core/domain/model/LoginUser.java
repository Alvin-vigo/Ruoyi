package com.ruoyi.common.core.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 登录用户身份（存于 Redis 会话，需可序列化）。
 * 自包含，不引用系统模块实体，避免 common 与 system 循环依赖。
 */
public class LoginUser implements UserDetails, Serializable
{
    private static final long serialVersionUID = 1L;

    /** 用户ID */
    private Long userId;

    /** 部门ID */
    private Long deptId;

    /** 登录账号 */
    private String userName;

    /** 用户昵称 */
    private String nickName;

    /** 头像 */
    private String avatar;

    /** 登录IP */
    private String ipaddr;

    /** 登录时间（时间戳） */
    private Long loginTime;

    /** 过期时间（时间戳） */
    private Long expireTime;

    /** 登录token */
    private String token;

    /** 角色集合（roleKey，如 admin） */
    private List<String> roles;

    /** 权限集合 */
    private Set<String> permissions;

    /** 数据库中的加密密码（transient：仅用于初次认证，不写入 Redis 会话） */
    private transient String password;

    public LoginUser()
    {
    }

    public LoginUser(Long userId, String userName, List<String> roles, Set<String> permissions)
    {
        this.userId = userId;
        this.userName = userName;
        this.roles = roles;
        this.permissions = permissions;
    }

    @JsonIgnore
    @Override
    public String getPassword()
    {
        return this.password;
    }

    /** 仅在内存中回填数据库加密密码，供 BCrypt 校验；不序列化进 Redis */
    public void setPassword(String password)
    {
        this.password = password;
    }

    @JsonIgnore
    @Override
    public String getUsername()
    {
        return this.userName;
    }

    /** 账户未过期 */
    @JsonIgnore
    @Override
    public boolean isAccountNonExpired()
    {
        return true;
    }

    /** 账户未锁定 */
    @JsonIgnore
    @Override
    public boolean isAccountNonLocked()
    {
        return true;
    }

    /** 凭证未过期 */
    @JsonIgnore
    @Override
    public boolean isCredentialsNonExpired()
    {
        return true;
    }

    /** 启用状态 */
    @JsonIgnore
    @Override
    public boolean isEnabled()
    {
        return true;
    }

    /** 授权集合：角色以 ROLE_ 前缀、权限直接作为 authority */
    @JsonIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities()
    {
        List<GrantedAuthority> authorityList = new java.util.ArrayList<>();
        if (roles != null)
        {
            for (String role : roles)
            {
                authorityList.add(new SimpleGrantedAuthority("ROLE_" + role));
            }
        }
        if (permissions != null)
        {
            for (String permission : permissions)
            {
                authorityList.add(new SimpleGrantedAuthority(permission));
            }
        }
        return authorityList;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public Long getDeptId()
    {
        return deptId;
    }

    public void setDeptId(Long deptId)
    {
        this.deptId = deptId;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public String getUserName()
    {
        return userName;
    }

    public String getNickName()
    {
        return nickName;
    }

    public void setNickName(String nickName)
    {
        this.nickName = nickName;
    }

    public String getAvatar()
    {
        return avatar;
    }

    public void setAvatar(String avatar)
    {
        this.avatar = avatar;
    }

    public String getIpaddr()
    {
        return ipaddr;
    }

    public void setIpaddr(String ipaddr)
    {
        this.ipaddr = ipaddr;
    }

    public Long getLoginTime()
    {
        return loginTime;
    }

    public void setLoginTime(Long loginTime)
    {
        this.loginTime = loginTime;
    }

    public Long getExpireTime()
    {
        return expireTime;
    }

    public void setExpireTime(Long expireTime)
    {
        this.expireTime = expireTime;
    }

    public String getToken()
    {
        return token;
    }

    public void setToken(String token)
    {
        this.token = token;
    }

    public List<String> getRoles()
    {
        return roles;
    }

    public void setRoles(List<String> roles)
    {
        this.roles = roles;
    }

    public Set<String> getPermissions()
    {
        return permissions;
    }

    public void setPermissions(Set<String> permissions)
    {
        this.permissions = permissions;
    }
}
