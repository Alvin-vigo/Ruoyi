package com.ruoyi.generator.util;

import org.apache.velocity.app.Velocity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Velocity 引擎工厂
 *
 * @author ruoyi
 */
public class VelocityInitializer {
    private static final Logger log = LoggerFactory.getLogger(VelocityInitializer.class);

    /**
     * 初始化 vm 方法
     */
    public static void initVelocity() {
        Properties p = new Properties();
        try {
            // 加载classpath目录下的vm文件
            p.setProperty("resource.loader", "class");
            // 设置vm文件加载器
            p.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
            // 定义字符集
            p.setProperty(Velocity.ENCODING_DEFAULT, "UTF-8");
            p.setProperty(Velocity.OUTPUT_ENCODING, "UTF-8");
            // 初始化Velocity引擎，指定配置Properties
            Velocity.init(p);
        } catch (Exception e) {
            log.error("Velocity 初始化失败：", e);
            throw new RuntimeException("Velocity 初始化失败", e);
        }
    }
}
