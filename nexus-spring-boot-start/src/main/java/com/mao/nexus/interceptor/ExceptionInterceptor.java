package com.mao.nexus.interceptor;

/**
 * @author ：StephenMao
 * @date ：2022/6/16 15:14
 */
public interface ExceptionInterceptor {
    void handel(Exception ex) throws Exception;
}
