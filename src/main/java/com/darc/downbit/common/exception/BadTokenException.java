package com.darc.downbit.common.exception;

/**
 * @author darc
 * @version 0.1
 * @createDate 2025/3/2-16:31:51
 * @description
 */
public class BadTokenException extends RuntimeException {
    public BadTokenException(String message) {
        super(message);
    }
}
