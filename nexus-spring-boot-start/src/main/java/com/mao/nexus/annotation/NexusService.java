package com.mao.nexus.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * @author ：StephenMao
 * @date ：2022/6/13 16:49
 */
@Component
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NexusService {
    String value() default "";
}
