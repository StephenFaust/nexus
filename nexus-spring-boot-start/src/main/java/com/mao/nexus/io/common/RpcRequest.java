package com.mao.nexus.io.common;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ：StephenMao
 * @date ：2022/6/13 19:51
 */
@Data
public class RpcRequest implements Serializable {
    private String serviceName;
    private String method;
    private Map<String, String> headers = new HashMap<>();
    private Class<?>[] parameterTypes;
    private Object[] parameters;
}