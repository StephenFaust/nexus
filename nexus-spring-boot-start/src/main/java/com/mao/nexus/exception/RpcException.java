package com.mao.nexus.exception;

/**
 * @author ：StephenMao
 * @date ：2022/6/13 16:53
 */
public class RpcException extends RuntimeException {
    public RpcException(String message) {
        super(message);
    }


    public RpcException(String message, Throwable cause) {
        super(message, cause);
    }
}
