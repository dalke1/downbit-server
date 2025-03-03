package com.darc.downbit.common.exception;

/**
 * @author darc
 * @version 0.1
 * @createDate 2025/2/15-23:19:31
 * @description
 */
public class CommentNotFoundException extends RuntimeException {
    public CommentNotFoundException(String message) {
        super(message);
    }
}
