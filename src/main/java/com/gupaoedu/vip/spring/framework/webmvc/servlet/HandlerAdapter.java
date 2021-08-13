package com.gupaoedu.vip.spring.framework.webmvc.servlet;

import com.gupaoedu.vip.spring.framework.annotation.GPRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Meng 2021/8/9
 */
public class HandlerAdapter {


    public ModelAndView handler(HttpServletRequest req, HttpServletResponse resp, HandlerMapping handlerMapping) throws Exception {

        Method method = handlerMapping.getMethod();
        //形参列表
        Class<?>[] parameterTypes = method.getParameterTypes();

        //参数注解
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        Map<String, Integer> paramIndesMapping = new HashMap<String, Integer>();

        //如果带有request和response参数
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> parameterType = parameterTypes[i];
            if (parameterType == HttpServletRequest.class || parameterType == HttpServletResponse.class) {
                paramIndesMapping.put(parameterType.getName(), i);
            }

        }
        //遍历所有带注解参数
        for (int i = 0; i < parameterAnnotations.length; i++) {
            for (Annotation a : parameterAnnotations[i]) {
                if (a instanceof GPRequestParam) {
                    String paramName = ((GPRequestParam) a).value();
                    if (!"".equals(paramName)) {
                        paramIndesMapping.put(paramName, i);
                    }
                }
            }
        }
        Object[] paramValues = new Object[parameterTypes.length];

        //遍历请求参数
        Map<String, String[]> params = req.getParameterMap();
        for (Map.Entry<String, String[]> param : params.entrySet()) {
            String reqParamName = param.getKey();
            if (!paramIndesMapping.containsKey(reqParamName)) {
                continue;
            }
            String value = Arrays.toString(param.getValue()).replaceAll("\\[|\\]", "")
                    .replaceAll("\\s+", ",");

            Integer index = paramIndesMapping.get(reqParamName);
            paramValues[index] = castStringValue(value, parameterTypes[index]);
        }
        if (paramIndesMapping.containsKey(HttpServletRequest.class.getName())) {
            Integer index = paramIndesMapping.get(HttpServletRequest.class.getName());
            paramValues[index] = req;
        }
        if (paramIndesMapping.containsKey(HttpServletResponse.class.getName())) {
            Integer index = paramIndesMapping.get(HttpServletResponse.class.getName());
            paramValues[index] = resp;
        }

        //反射执行
        Object result = method.invoke(handlerMapping.getController(), paramValues);
        if (result == null || result instanceof Void) {
            return null;
        }
        if (method.getReturnType() == ModelAndView.class) {
            return (ModelAndView) result;
        }
        return null;
    }

    private Object castStringValue(String value, Class<?> paramType) {
        if (String.class == paramType) {
            return value;
        } else if (Integer.class == paramType) {
            return Integer.valueOf(value);
        } else if (Double.class == paramType) {
            return Double.valueOf(value);
        } else {
            if (value != null) {
                return value;
            }
            return null;
        }

    }
}
