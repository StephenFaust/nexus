package com.mao.nexus.spi;

/**
 * @author ：StephenMao
 * @date ：2022/6/23 11:02
 */
public class Holder<T> {
    private volatile T value;

    public void set(T value) {
        this.value = value;
    }

    public T get() {
        return value;
    }
}