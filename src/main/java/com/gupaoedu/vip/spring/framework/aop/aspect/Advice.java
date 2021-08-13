package com.gupaoedu.vip.spring.framework.aop.aspect;

import lombok.Data;

import java.lang.reflect.Method;

/**
 * @author Meng 2021/8/10
 */
@Data
public class Advice {

    private Object aspect;
    private Method method;
    private String throwName;

    public Advice(Object aspect, Method method) {
        this.aspect = aspect;
        this.method = method;
    }
}
