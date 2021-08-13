package com.gupaoedu.vip.spring.framework.aop.support;

import com.gupaoedu.vip.spring.framework.aop.aspect.Advice;
import com.gupaoedu.vip.spring.framework.aop.config.AopConfig;
import com.sun.corba.se.impl.util.RepositoryId;
import lombok.Data;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.sun.corba.se.impl.util.RepositoryId.cache;

/**
 * @author Meng 2021/8/10
 */
@Data
public class AdviceSupport {

    private AopConfig aopConfig;
    private Object target;
    private Class<?> targetClass;
    private Pattern pointCutClassPattern;

    private Map<Method, Map<String, Advice>> methodCache = new HashMap<Method, Map<String, Advice>>();


    public AdviceSupport(AopConfig aopConfig) {

        this.aopConfig = aopConfig;
    }

    private void parse() {
        String pointCut = aopConfig.getPointCut()
                .replaceAll("\\.", "\\\\.")
                .replaceAll("\\\\.\\*", ".*")
                .replaceAll("\\(", "\\\\(")
                .replaceAll("\\)", "\\\\)");

        //匹配class的正则
        String pointCutForClassRegex = pointCut.substring(0, pointCut.lastIndexOf("\\(") - 4);
        String s = "class " + pointCutForClassRegex.substring(pointCutForClassRegex.lastIndexOf(" ") + 1);
        System.out.println("=====================================================");
        System.out.println(s);
        pointCutClassPattern = Pattern.compile(s);

        //匹配方法的正则
        Pattern pointCutPattern = Pattern.compile(pointCut);

        Map<String, Method> aspectMethods = new HashMap<String, Method>();

        try {
            Class<?> aspect = Class.forName(aopConfig.getAspectClass());
            for (Method method : aspect.getMethods()) {
                aspectMethods.put(method.getName(), method);
            }

            for (Method method : this.targetClass.getMethods()) {
                String methodStr = method.toString();
                if (methodStr.contains("throws")) {
                    methodStr = methodStr.substring(0, methodStr.lastIndexOf("throws")).trim();
                }
                if (pointCutPattern.matcher(methodStr).matches()) {
                    Map<String, Advice> advices = new HashMap<String, Advice>();
                    if (null != aopConfig.getAspectBefore() && !"".equals(aopConfig.getAspectBefore())) {
                        advices.put("before", new Advice(aspect.newInstance(), aspectMethods.get(aopConfig.getAspectBefore())));
                    }
                    if (null != aopConfig.getAspectAfter() && !"".equals(aopConfig.getAspectAfter())) {
                        advices.put("after", new Advice(aspect.newInstance(), aspectMethods.get(aopConfig.getAspectAfter())));
                    }
                    if (null != aopConfig.getAspectAfterThrow() && !"".equals(aopConfig.getAspectAfterThrow())) {
                        Advice advice = new Advice(aspect.newInstance(), aspectMethods.get(aopConfig.getAspectAfterThrow()));
                        advice.setThrowName(aopConfig.getAspectAfterThrowingName());
                        advices.put("afterThrow", advice);
                    }
                    methodCache.put(method, advices);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public boolean pointCutClass() {
        System.out.println(targetClass.toString());
        boolean matches = pointCutClassPattern.matcher(targetClass.toString()).matches();
        System.out.println(matches);
        return matches;
    }

    //
    public Map<String, Advice> getAdvice(Method method) throws NoSuchMethodException {
        Map<String, Advice> cache = methodCache.get(method);
        if(null == cache){
            Method m = targetClass.getMethod(method.getName(),method.getParameterTypes());
            cache = methodCache.get(m);
            this.methodCache.put(m, cache);
        }
        return cache;
    }


    public void setTargetClass(Class<?> targetClass) {
        this.targetClass = targetClass;
        parse();
    }
}
