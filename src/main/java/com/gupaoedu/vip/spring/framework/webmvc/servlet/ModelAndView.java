package com.gupaoedu.vip.spring.framework.webmvc.servlet;

import java.util.Map;

/**
 * @author Meng 2021/8/9
 */
public class ModelAndView {
    private String viewName;
    private Map<String, ?> model;

    public ModelAndView(String viewName, Map<String, ?> model) {
        this.viewName = viewName;
        this.model = model;
    }

    public ModelAndView(String viewName) {
        this.viewName = viewName;
    }

    public String getViewName() {
        return viewName;
    }

    public Map<String, ?> getModel() {
        return model;
    }
}
