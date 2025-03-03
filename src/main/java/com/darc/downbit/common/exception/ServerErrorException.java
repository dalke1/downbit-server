package com.darc.downbit.common.exception;

/**
 * @author darc
 * @version 0.1
 * @createDate 2024/12/27-01:49:05
 * @description
 */
public class ServerErrorException extends RuntimeException {
    public ServerErrorException(String message) {
        super(message);
    }
}
