package com.ruoyi.common.exception;

/**
 * 业务异常
 *
 * @author ruoyi
 */
public class ServiceException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /** 错误码 */
    private Integer code;

    /** 错误提示 */
    private String message;

    /**
     * 构造一个新的业务异常
     *
     * @param message 错误提示
     */
    public ServiceException(String message) {
        this.message = message;
    }

    /**
     * 构造一个新的业务异常
     *
     * @param message 错误提示
     * @param code    错误码
     */
    public ServiceException(String message, Integer code) {
        this.message = message;
        this.code = code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }
}
