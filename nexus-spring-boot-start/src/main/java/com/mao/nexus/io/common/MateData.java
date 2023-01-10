package com.mao.nexus.io.common;

import lombok.Data;

import java.io.Serializable;

/**
 * @author ：StephenMao
 * @date ：2022/6/29 14:46
 */
@Data
public class MateData implements Serializable {
    private static final long serialVersionUID = 1905122041950251207L;

    private Long uniqueIdentification;
}
