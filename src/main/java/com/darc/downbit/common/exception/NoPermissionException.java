package com.darc.downbit.common.exception;

/**
 * @author darc
 * @version 0.1
 * @createDate 2025/3/2-16:32:24
 * @description
 */
public class NoPermissionException extends RuntimeException {
    public NoPermissionException(String message) {
        super(message);
    }
}
