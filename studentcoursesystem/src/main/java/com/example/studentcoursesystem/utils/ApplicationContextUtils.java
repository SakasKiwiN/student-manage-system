package com.example.studentcoursesystem.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Spring ApplicationContext 工具类
 * 用于在非Spring管理的类中获取Spring Bean
 */
@Component
public class ApplicationContextUtils implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ApplicationContextUtils.applicationContext = applicationContext;
    }

    /**
     * 获取ApplicationContext
     */
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * 通过name获取Bean
     */
    public static Object getBean(String name) {
        return getApplicationContext().getBean(name);
    }

    /**
     * 通过class获取Bean
     */
    public static <T> T getBean(Class<T> clazz) {
        return getApplicationContext().getBean(clazz);
    }

    /**
     * 通过name和class获取Bean
     */
    public static <T> T getBean(String name, Class<T> clazz) {
        return getApplicationContext().getBean(name, clazz);
    }

    /**
     * 检查是否包含指定name的Bean
     */
    public static boolean containsBean(String name) {
        return getApplicationContext().containsBean(name);
    }

    /**
     * 安全地获取Bean，如果获取失败返回null
     */
    public static <T> T getBeanSafely(Class<T> clazz) {
        try {
            return getApplicationContext().getBean(clazz);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 安全地获取Bean，如果获取失败返回null
     */
    public static Object getBeanSafely(String name) {
        try {
            return getApplicationContext().getBean(name);
        } catch (Exception e) {
            return null;
        }
    }
}