package com.ruoyi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * 若依医疗系统启动类
 *
 * @author ruoyi
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class RuoYiMedicalApplication {

    public static void main(String[] args) {
        // 启动 SpringBoot 应用
        SpringApplication.run(RuoYiMedicalApplication.class, args);
        System.out.println("============================================");
        System.out.println("  若依医疗管理系统启动成功！");
        System.out.println("  接口文档: http://localhost:8080/doc.html");
        System.out.println("============================================");
    }
}
