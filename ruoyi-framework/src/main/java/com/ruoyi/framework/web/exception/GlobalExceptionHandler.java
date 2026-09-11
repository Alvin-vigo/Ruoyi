package com.ruoyi.framework.web.exception;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 *
 * @author ruoyi
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 业务异常
     */
    @ExceptionHandler(ServiceException.class)
    public AjaxResult handleServiceException(ServiceException e) {
        log.error("业务异常: {}", e.getMessage(), e);
        Integer code = e.getCode();
        return code != null ? AjaxResult.error(e.getMessage()) : AjaxResult.error(e.getMessage());
    }

    /**
     * 参数校验异常
     */
    @ExceptionHandler(BindException.class)
    public AjaxResult handleBindException(BindException e) {
        String message = "参数校验失败";
        if (e.getBindingResult().hasErrors()) {
            ObjectError error = e.getBindingResult().getAllErrors().get(0);
            message = error.getDefaultMessage();
        }
        log.error("参数校验异常: {}", message, e);
        return AjaxResult.error(message);
    }

    /**
     * 运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public AjaxResult handleRuntimeException(RuntimeException e) {
        log.error("运行时异常: {}", e.getMessage(), e);
        return AjaxResult.error("系统内部错误，请联系管理员");
    }

    /**
     * 系统异常
     */
    @ExceptionHandler(Exception.class)
    public AjaxResult handleException(Exception e) {
        log.error("系统异常: {}", e.getMessage(), e);
        return AjaxResult.error("系统繁忙，请稍后重试");
    }
}
