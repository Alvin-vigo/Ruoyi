package com.ruoyi.controller;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.model.LoginBody;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.framework.security.SecurityUtils;
import com.ruoyi.framework.web.service.SysLoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 登录鉴权控制器（标准若依）
 */
@RestController
public class SysLoginController
{
    @Autowired
    private SysLoginService loginService;

    /**
     * 登录接口：校验账号密码，返回 token
     */
    @PostMapping("/login")
    public AjaxResult login(@RequestBody LoginBody loginBody)
    {
        try
        {
            String token = loginService.login(loginBody.getUsername(), loginBody.getPassword());
            AjaxResult ajax = AjaxResult.success();
            ajax.put("token", token);
            return ajax;
        }
        catch (Exception e)
        {
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 获取登录用户信息：用户名、昵称、头像、角色、权限
     */
    @GetMapping("/getInfo")
    public AjaxResult getInfo()
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null)
        {
            return AjaxResult.error("会话已过期，请重新登录");
        }

        Map<String, Object> user = new HashMap<>();
        user.put("userName", loginUser.getUserName());
        user.put("nickName", loginUser.getNickName());
        user.put("avatar", loginUser.getAvatar());

        AjaxResult ajax = AjaxResult.success();
        ajax.put("user", user);
        ajax.put("roles", loginUser.getRoles());
        ajax.put("permissions", loginUser.getPermissions());
        return ajax;
    }
}
