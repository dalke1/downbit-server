package com.darc.downbit.common.dto;

import com.darc.downbit.common.constant.StatusCodeEnum;

/**
 * @author darc
 * @version 0.1
 * @createDate 2024/7/22-1:39:05
 * @description
 */

public record RestResp<T>(Integer code, String message, T result) {

    public static <T> RestResp<T> of(Integer code, String message, T result) {
        return new RestResp<>(code, message, result);
    }

    public static <T> RestResp<T> ok(T result) {
        return new RestResp<>(StatusCodeEnum.OK.getCode(), StatusCodeEnum.OK.getMessage(), result);
    }

    public static <T> RestResp<T> ok() {
        return ok(null);
    }

    public static <T> RestResp<T> badRequest(T result) {
        return new RestResp<>(StatusCodeEnum.BAD_REQUEST.getCode(), StatusCodeEnum.BAD_REQUEST.getMessage(), result);
    }

    public static <T> RestResp<T> unauthorized(T result) {
        return new RestResp<>(StatusCodeEnum.UNAUTHORIZED.getCode(), StatusCodeEnum.UNAUTHORIZED.getMessage(), result);
    }

    public static <T> RestResp<T> forbidden(T result) {
        return new RestResp<>(StatusCodeEnum.FORBIDDEN.getCode(), StatusCodeEnum.FORBIDDEN.getMessage(), result);
    }

    public static <T> RestResp<T> notFound(T result) {
        return new RestResp<>(StatusCodeEnum.NOT_FOUND.getCode(), StatusCodeEnum.NOT_FOUND.getMessage(), result);
    }

    public static <T> RestResp<T> methodNotAllowed(T result) {
        return new RestResp<>(StatusCodeEnum.METHOD_NOT_ALLOWED.getCode(), StatusCodeEnum.METHOD_NOT_ALLOWED.getMessage(), result);
    }

    public static <T> RestResp<T> internalServerError(T result) {
        return new RestResp<>(StatusCodeEnum.INTERNAL_SERVER_ERROR.getCode(), StatusCodeEnum.INTERNAL_SERVER_ERROR.getMessage(), result);
    }

    public static <T> RestResp<T> serviceUnavailable(T result) {
        return new RestResp<>(StatusCodeEnum.SERVICE_UNAVAILABLE.getCode(), StatusCodeEnum.SERVICE_UNAVAILABLE.getMessage(), result);
    }
}
