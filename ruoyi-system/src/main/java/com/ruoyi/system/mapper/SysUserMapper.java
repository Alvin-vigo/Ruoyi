package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系统用户数据访问层
 */
@Mapper
public interface SysUserMapper
{
    /**
     * 根据用户名查询用户
     */
    public SysUser selectUserByUserName(@Param("userName") String userName);

    /**
     * 根据用户ID查询角色权限字符串列表
     */
    public List<String> selectRoleKeysByUserId(@Param("userId") Long userId);
}
