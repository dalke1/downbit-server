package com.darc.downbit.common.exception;

import com.darc.downbit.common.dto.RestResp;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.util.validation.metadata.DatabaseException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;


/**
 * @author darc
 * @version 0.1
 * @createDate 2024/12/3-04:12:50
 * @description
 */

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(RefreshPage.class)
    @ResponseStatus(HttpStatus.OK)
    public RestResp<String> handleRefreshPageException(RefreshPage e) {
        log.info(e.getMessage());
        return RestResp.refreshPage(e.getMessage());
    }

    @ExceptionHandler(NoSuchVideoException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public RestResp<String> handleNoSuchVideoException(NoSuchVideoException e) {
        log.error(e.getMessage());
        return RestResp.badRequest(e.getMessage());
    }

    @ExceptionHandler(FileNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public RestResp<String> handleNoSuchVideoException(FileNotFoundException e) {
        log.error(e.getMessage());
        return RestResp.badRequest(e.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public RestResp<String> handleBadRequestException(BadRequestException e) {
        log.error(e.getMessage());
        return RestResp.badRequest(e.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public RestResp<String> handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        // 返回400状态码，参数校验失败
        StringBuilder sb = new StringBuilder();
        BindingResult bindingResult = e.getBindingResult();
        log.error("请求[ {} ] {} 的参数校验发生错误", request.getMethod(), request.getRequestURL());
        for (ObjectError objectError : bindingResult.getAllErrors()) {
            FieldError fieldError = (FieldError) objectError;
            log.error("参数 {} = {} 校验错误：{}", fieldError.getField(), fieldError.getRejectedValue(), fieldError.getDefaultMessage());
            sb.append(fieldError.getField()).append(": ").append(fieldError.getDefaultMessage()).append(";");
        }

        return RestResp.badRequest(sb.toString());
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(BadTokenException.class)
    public RestResp<Integer> handleBadTokenException(BadTokenException e) {
        // 返回401状态码，未授权
        log.error(e.getMessage());
        return RestResp.unauthorized(401);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public RestResp<String> handleHttpRequestMethodNotSupportedException(HttpServletRequest request) {
        // 返回405状态码，请求方法不支持
        log.error("请求[ {} ] {} 的方法不支持", request.getMethod(), request.getRequestURL());
        return RestResp.methodNotAllowed("请求方法不支持");
    }

    @ExceptionHandler(EmptyFileNameException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestResp<String> handleEmptyFileNameException(EmptyFileNameException e) {
        log.error(e.getMessage());
        return RestResp.internalServerError("服务器出现状况了,请稍后重试");
    }

    @ExceptionHandler(ServerErrorException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestResp<String> handleServerErrorException(ServerErrorException e) {
        log.error(e.getMessage());
        return RestResp.internalServerError(e.getMessage());
    }

    @ExceptionHandler(JsonException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestResp<String> handleJsonException(JsonException e) {
        log.error(e.getMessage());
        return RestResp.internalServerError(e.getMessage());
    }

    @ExceptionHandler(CommentNotFoundException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestResp<String> handleCommentNotFoundException(CommentNotFoundException e) {
        log.error(e.getMessage());
        return RestResp.internalServerError(e.getMessage());
    }

    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    @ExceptionHandler(DatabaseException.class)
    public RestResp<String> handleDatabaseException(DatabaseException e) {
        // 返回503状态码，建议前端稍后重试
        log.error("数据库异常");
        return RestResp.serviceUnavailable(e.getMessage() + ",请稍后重试");
    }

    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    @ExceptionHandler(RedisConnectionFailureException.class)
    public RestResp<String> handleRedisConnectionFailureException() {
        // 返回503状态码，建议前端稍后重试
        log.error("Redis连接失败");
        return RestResp.serviceUnavailable("Redis连接失败,请稍后重试");
    }

    @ExceptionHandler(NoMoreRecommendException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public RestResp<String> handleNoMoreRecommendException(NoMoreRecommendException e) {
        log.error(e.getMessage());
        return RestResp.serviceUnavailable(e.getMessage());
    }

}
