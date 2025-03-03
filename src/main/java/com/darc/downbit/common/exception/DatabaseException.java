package com.darc.downbit.common.exception;


/**
 * @author darc
 * @version 0.1
 * @createDate 2024/12/3-04:21:58
 * @description
 */

public class DatabaseException extends RuntimeException {
    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
