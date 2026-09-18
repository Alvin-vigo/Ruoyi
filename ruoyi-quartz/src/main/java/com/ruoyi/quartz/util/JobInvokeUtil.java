package com.ruoyi.quartz.util;

import com.ruoyi.common.utils.SpringUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.quartz.domain.SysJob;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 任务执行工具（根据 invokeTarget 反射调用 Spring Bean 方法）
 *
 * 支持的调用目标字符串写法：
 * <pre>
 *   followupRemindTask.scanOverdueFollowup        无参（可省略括号）
 *   followupRemindTask.scanOverdueFollowup()      无参
 *   medicalRemindTask.scanLowStock('10')          单引号字符串
 *   ryTask.ryMultipleParams('ry', true, 2000L, 3.5D, 100)
 *   com.ruoyi.quartz.task.RyTask.ryParams('ry')   全限定类名（非 Spring Bean）
 * </pre>
 *
 * @author ruoyi
 */
public class JobInvokeUtil {

    /**
     * 执行定时任务
     */
    public static void invokeMethod(SysJob sysJob) throws Exception {
        String invokeTarget = sysJob.getInvokeTarget();
        String beanName = getBeanName(invokeTarget);
        String methodName = getMethodName(invokeTarget);
        List<Object[]> methodParams = getMethodParams(invokeTarget);
        if (StringUtils.isEmpty(beanName) || StringUtils.isEmpty(methodName)) {
            throw new IllegalArgumentException("调用目标字符串格式错误，无法解析：" + invokeTarget);
        }
        if (!isValidClassName(beanName)) {
            Object bean = SpringUtils.getBean(beanName);
            invokeMethod(bean, methodName, methodParams);
        } else {
            Object bean = Class.forName(beanName).getDeclaredConstructor().newInstance();
            invokeMethod(bean, methodName, methodParams);
        }
    }

    /**
     * 执行方法
     */
    private static void invokeMethod(Object bean, String methodName, List<Object[]> methodParams)
            throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException {
        if (methodParams != null && !methodParams.isEmpty()) {
            Method method = lookupMethod(bean.getClass(), methodName, getMethodParamTypes(methodParams));
            method.invoke(bean, getMethodParamValues(methodParams));
        } else {
            Method method = lookupMethod(bean.getClass(), methodName);
            method.invoke(bean);
        }
    }

    /**
     * 查找方法：当前类找不到时向父类回溯，避免声明在父类/被代理时抛 NoSuchMethodException
     */
    private static Method lookupMethod(Class<?> clazz, String methodName, Class<?>... paramTypes)
            throws NoSuchMethodException {
        Class<?> current = clazz;
        NoSuchMethodException last = null;
        while (current != null) {
            try {
                Method method = current.getDeclaredMethod(methodName, paramTypes);
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException e) {
                last = e;
                current = current.getSuperclass();
            }
        }
        throw last;
    }

    /**
     * 获取 Bean 名称（第一个 '.' 之前的部分）
     */
    private static String getBeanName(String invokeTarget) {
        String beanName = StringUtils.substringBefore(invokeTarget, "(");
        int dotIndex = beanName.indexOf(".");
        return dotIndex > 0 ? beanName.substring(0, dotIndex) : beanName;
    }

    /**
     * 获取方法名称（第一个 '.' 之后、第一个 '(' 之前的部分，无括号时取到末尾）
     */
    private static String getMethodName(String invokeTarget) {
        String methodName = StringUtils.substringAfter(invokeTarget, ".");
        int parenIndex = methodName.indexOf("(");
        if (parenIndex > 0) {
            methodName = methodName.substring(0, parenIndex);
        }
        return methodName.trim();
    }

    /**
     * 获取方法参数（支持 '字符串' / true|false / 2000L / 3.5D / 100）
     */
    private static List<Object[]> getMethodParams(String invokeTarget) {
        String methodStr = StringUtils.substringBetween(invokeTarget, "(", ")");
        if (StringUtils.isEmpty(methodStr)) {
            return null;
        }
        // 按逗号切分，但忽略引号内部的逗号
        String[] params = methodStr.split(",(?=([^\"']*[\"'][^\"']*[\"'])*[^\"']*$)");
        List<Object[]> methodParams = new ArrayList<>();
        for (String param : params) {
            String str = StringUtils.trimToEmpty(param);
            if (str.isEmpty()) {
                continue;
            }
            methodParams.add(parseParam(str));
        }
        return methodParams;
    }

    /**
     * 单个参数解析为 [值, 类型]
     */
    private static Object[] parseParam(String str) {
        // 'xxx' 或 "xxx" 视为字符串
        if (StringUtils.contains(str, "'") || StringUtils.contains(str, "\"")) {
            String value = StringUtils.replace(str, "'", "");
            value = StringUtils.replace(value, "\"", "");
            return new Object[]{value, String.class};
        }
        // 布尔
        if (StringUtils.equals(str, "true") || StringUtils.equalsIgnoreCase(str, "false")) {
            return new Object[]{Boolean.valueOf(str), Boolean.class};
        }
        // 长整形 2000L
        if (StringUtils.containsIgnoreCase(str, "L")) {
            return new Object[]{Long.valueOf(StringUtils.replaceIgnoreCase(str, "L", "")), Long.class};
        }
        // 浮点 3.5D
        if (StringUtils.containsIgnoreCase(str, "D")) {
            return new Object[]{Double.valueOf(StringUtils.replaceIgnoreCase(str, "D", "")), Double.class};
        }
        // 其余按整形处理
        return new Object[]{Integer.valueOf(str), Integer.class};
    }

    private static Class<?>[] getMethodParamTypes(List<Object[]> methodParams) {
        Class<?>[] classes = new Class<?>[methodParams.size()];
        int index = 0;
        for (Object[] param : methodParams) {
            classes[index++] = (Class<?>) param[1];
        }
        return classes;
    }

    private static Object[] getMethodParamValues(List<Object[]> methodParams) {
        Object[] values = new Object[methodParams.size()];
        int index = 0;
        for (Object[] param : methodParams) {
            values[index++] = param[0];
        }
        return values;
    }

    /**
     * 是否为类路径（多于一个 '.' 视为全限定类名）
     */
    public static boolean isValidClassName(String invokeTarget) {
        return StringUtils.countMatches(invokeTarget, ".") > 1;
    }
}
