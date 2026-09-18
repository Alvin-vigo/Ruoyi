package com.ruoyi.common.utils;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * spring工具类 方便在非spring管理环境中获取bean
 *
 * @author ruoyi
 */
@Component
public final class SpringUtils implements BeanFactoryPostProcessor, ApplicationContextAware {
    /** Spring应用上下文环境 */
    private static ConfigurableListableBeanFactory beanFactory;

    /** Spring应用上下文 */
    private static ApplicationContext applicationContext;

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        SpringUtils.beanFactory = beanFactory;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringUtils.applicationContext = applicationContext;
    }

    /**
     * 获取 applicationContext
     *
     * @return ApplicationContext
     */
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * 通过 name 获取 Bean
     *
     * @param <T>  泛型
     * @param name Bean 名称
     * @return Bean 对象
     * @throws BeansException 异常
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(String name) throws BeansException {
        return (T) beanFactory.getBean(name);
    }

    /**
     * 通过 class 获取 Bean
     *
     * @param <T> 泛型
     * @param clz Bean 类型
     * @return Bean 对象
     * @throws BeansException 异常
     */
    public static <T> T getBean(Class<T> clz) throws BeansException {
        return beanFactory.getBean(clz);
    }

    /**
     * 通过 name, 以及 Clazz 获取 Bean（带泛型）
     *
     * @param <T>  泛型
     * @param name Bean 名称
     * @param clz  Bean 类型
     * @return Bean 对象
     * @throws BeansException 异常
     */
    public static <T> T getBean(String name, Class<T> clz) throws BeansException {
        return beanFactory.getBean(name, clz);
    }

    /**
     * 判断 Bean 是否存在
     *
     * @param name Bean 名称
     * @return 是否存在
     */
    public static boolean containsBean(String name) {
        return beanFactory.containsBean(name);
    }

    /**
     * 判断 Bean 是否为单例
     *
     * @param name Bean 名称
     * @return 是否单例
     */
    public static boolean isSingleton(String name) {
        return beanFactory.isSingleton(name);
    }

    /**
     * 获取 Bean 的类型
     *
     * @param name Bean 名称
     * @return 类型
     */
    public static Class<?> getType(String name) {
        return beanFactory.getType(name);
    }

    /**
     * 获取 Bean 的别名
     *
     * @param name Bean 名称
     * @return 别名数组
     */
    public static String[] getAliases(String name) {
        return beanFactory.getAliases(name);
    }
}
