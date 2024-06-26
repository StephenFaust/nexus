package com.mao.nexus.io.common;

import com.mao.nexus.exception.RpcException;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ：StephenMao
 * @date ：2022/6/13 19:50
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RpcResponse extends MateData {
    private String status;
    private Map<String, String> headers = new HashMap<>();
    private Object data;
    private RpcException exception;
}
