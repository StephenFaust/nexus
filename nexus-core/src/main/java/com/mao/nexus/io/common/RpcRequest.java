package com.mao.nexus.io.common;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ：StephenMao
 * @date ：2022/6/13 19:51
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RpcRequest extends MateData {

    private String serviceName;
    private int retryCount;
    private int retryInternal;
    private String clazzName;
    private String method;
    private Map<String, String> headers = new HashMap<>();
    private Class<?>[] parameterTypes;
    private Object[] parameters;
}
