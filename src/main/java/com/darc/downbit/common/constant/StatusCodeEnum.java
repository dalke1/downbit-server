package com.darc.downbit.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author darc
 * @version 0.1
 * @createDate 2024/7/22-1:27:35
 * @description
 */
@Getter
@AllArgsConstructor
public enum StatusCodeEnum {


    OK(200, "OK"),
    BAD_REQUEST(400, "Bad Request"),
    UNAUTHORIZED(401, "Unauthorized"),
    FORBIDDEN(403, "Forbidden"),
    NOT_FOUND(404, "Not Found"),
    METHOD_NOT_ALLOWED(405, "Method Not Allowed"),
    INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
    SERVICE_UNAVAILABLE(503, "Service Unavailable");


    private final int code;
    private final String message;
}
