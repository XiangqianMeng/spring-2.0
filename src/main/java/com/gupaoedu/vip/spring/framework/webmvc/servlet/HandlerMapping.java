package com.gupaoedu.vip.spring.framework.webmvc.servlet;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

/**
 * @author Meng 2021/8/9
 */
public class HandlerMapping {
    private Pattern patternUrl;
    private Method method;
    private Object controller;

    public HandlerMapping(Pattern patternUrl, Method method, Object controller) {
        this.patternUrl = patternUrl;
        this.method = method;
        this.controller = controller;
    }

    public Pattern getPatternUrl() {
        return patternUrl;
    }

    public void setPatternUrl(Pattern patternUrl) {
        this.patternUrl = patternUrl;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Object getController() {
        return controller;
    }

    public void setController(Object controller) {
        this.controller = controller;
    }
}
