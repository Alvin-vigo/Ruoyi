package com.ruoyi.common.core.domain;

import java.util.HashMap;

/**
 * 统一返回结果类
 *
 * @author ruoyi
 */
public class AjaxResult extends HashMap<String, Object> {
    private static final long serialVersionUID = 1L;

    /** 状态码标签 */
    public static final String CODE_TAG = "code";

    /** 返回消息标签 */
    public static final String MSG_TAG = "msg";

    /** 数据对象标签 */
    public static final String DATA_TAG = "data";

    /** 操作成功状态码 */
    public static final long SUCCESS = 200;

    /** 操作失败状态码 */
    public static final long ERROR = 500;

    /**
     * 初始化一个新创建的 AjaxResult 对象
     */
    public AjaxResult() {
    }

    /**
     * 初始化一个新创建的 AjaxResult 对象
     *
     * @param code 状态码
     * @param msg  返回内容
     */
    public AjaxResult(long code, String msg) {
        super.put(CODE_TAG, code);
        super.put(MSG_TAG, msg);
    }

    /**
     * 初始化一个新创建的 AjaxResult 对象
     *
     * @param code 状态码
     * @param msg  返回内容
     * @param data 数据对象
     */
    public AjaxResult(long code, String msg, Object data) {
        super.put(CODE_TAG, code);
        super.put(MSG_TAG, msg);
        if (data != null) {
            super.put(DATA_TAG, data);
        }
    }

    /**
     * 返回成功消息
     *
     * @return 成功消息
     */
    public static AjaxResult success() {
        return AjaxResult.success("操作成功");
    }

    /**
     * 返回成功消息
     *
     * @param msg 返回内容
     * @return 成功消息
     */
    public static AjaxResult success(String msg) {
        return AjaxResult.success(msg, null);
    }

    /**
     * 返回成功消息
     *
     * @param msg  返回内容
     * @param data 数据对象
     * @return 成功消息
     */
    public static AjaxResult success(String msg, Object data) {
        return new AjaxResult(SUCCESS, msg, data);
    }

    /**
     * 返回成功消息（带数据对象，消息默认为"操作成功"）
     *
     * @param data 数据对象
     * @return 成功消息
     */
    public static AjaxResult success(Object data) {
        return AjaxResult.success("操作成功", data);
    }

    /**
     * 返回失败消息
     *
     * @return 失败消息
     */
    public static AjaxResult error() {
        return AjaxResult.error("操作失败");
    }

    /**
     * 返回失败消息
     *
     * @param msg 返回内容
     * @return 失败消息
     */
    public static AjaxResult error(String msg) {
        return AjaxResult.error(msg, null);
    }

    /**
     * 返回失败消息
     *
     * @param msg  返回内容
     * @param data 数据对象
     * @return 失败消息
     */
    public static AjaxResult error(String msg, Object data) {
        return new AjaxResult(ERROR, msg, data);
    }

    /**
     * 获取状态码
     */
    public long getCode() {
        Object value = get(CODE_TAG);
        return value != null ? ((Number) value).longValue() : 0L;
    }

    /**
     * 获取返回消息
     */
    public String getMsg() {
        Object value = get(MSG_TAG);
        return value != null ? value.toString() : null;
    }

    /**
     * 获取返回数据
     */
    public Object getData() {
        return get(DATA_TAG);
    }
}
