package com.darc.downbit.common.exception;

/**
 * @author darc
 * @version 0.1
 * @createDate 2024/12/27-01:46:30
 * @description
 */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
