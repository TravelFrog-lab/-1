package org.example.springboot.config;

import org.example.springboot.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 将未捕获异常转为统一 Result，避免仅返回 Spring 默认 500 页面，便于前端与日志排查。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        LOGGER.error("未处理异常", e);
        String msg = e.getMessage();
        if (msg == null || msg.isBlank()) {
            msg = e.getClass().getSimpleName();
        }
        return Result.error("-1", "系统异常: " + msg);
    }
}
