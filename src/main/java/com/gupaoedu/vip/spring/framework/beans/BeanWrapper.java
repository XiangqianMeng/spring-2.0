package com.gupaoedu.vip.spring.framework.beans;

/**
 * @author Meng 2021/8/8
 */
public class BeanWrapper {
    private Object instance;

    public BeanWrapper(Object instance) {
        this.instance = instance;
    }

    public Object getInstance() {
        return instance;
    }

    public void setInstance(Object instance) {
        this.instance = instance;
    }
}
