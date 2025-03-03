package com.darc.downbit.common.exception;

import lombok.Getter;

/**
 * @author darc
 * @version 0.1
 * @createDate 2024/12/21-03:57:21
 * @description
 */
@Getter
public class EmptyFileNameException extends RuntimeException {

    public EmptyFileNameException(String message) {
        super(message);
    }

}
