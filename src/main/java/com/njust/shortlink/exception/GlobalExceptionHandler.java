package com.njust.shortlink.exception;

import com.njust.shortlink.pojo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Result<Void>> handleResponseStatus(ResponseStatusException exception) {
        String message = exception.getReason() == null ? "请求无法处理" : exception.getReason();
        return ResponseEntity.status(exception.getStatusCode())
                .body(Result.fail(message));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleValidation(MethodArgumentNotValidException exception) {
        var error = exception.getBindingResult().getFieldError();
        return Result.fail(error == null ? "请求参数不合法" : error.getDefaultMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleUnreadableBody(HttpMessageNotReadableException exception) {
        return Result.fail("请求体为空或格式不正确，请检查 JSON 和日期格式");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleException(Exception exception) {
        // 保留 Spring MVC 异常原有的 HTTP 状态，例如 405 和 415。
        if (exception instanceof ErrorResponse error) {
            return ResponseEntity.status(error.getStatusCode())
                    .headers(error.getHeaders())
                    .body(Result.fail("请求无法处理，请检查路径、请求方式和 Content-Type"));
        }
        log.error("Unexpected error while processing request", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.fail("服务器内部错误，请稍后重试"));
    }
}
