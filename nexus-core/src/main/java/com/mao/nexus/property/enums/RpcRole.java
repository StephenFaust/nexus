package com.mao.nexus.property.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author StephenMao
 * @date 2022/6/26 12:48
 */
@Getter
@AllArgsConstructor
public enum RpcRole {
    Client("client"),
    Server("server");

    private String role;
}
