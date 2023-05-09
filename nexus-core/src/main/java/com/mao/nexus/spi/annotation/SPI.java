package com.mao.nexus.spi.annotation;

import java.lang.annotation.*;

/**
 * @author ：StephenMao
 * @date ：2022/6/23 10:55
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface SPI {
    /**
     * 扩展点名。
     */
    String value() default "";
}

