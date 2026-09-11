package com.ruoyi.framework.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis 配置类
 *
 * @author ruoyi
 */
@Configuration
@MapperScan("com.ruoyi.system.mapper")
public class MyBatisConfig {
}
