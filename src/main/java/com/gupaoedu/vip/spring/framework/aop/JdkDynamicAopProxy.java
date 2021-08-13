package com.gupaoedu.vip.spring.framework.aop;

import com.gupaoedu.vip.spring.framework.aop.aspect.Advice;
import com.gupaoedu.vip.spring.framework.aop.support.AdviceSupport;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

/**
 * @author Meng 2021/8/10
 */
public class JdkDynamicAopProxy implements InvocationHandler {
    private AdviceSupport config;

    public JdkDynamicAopProxy(AdviceSupport config) {
        this.config = config;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Map<String, Advice> advices = config.getAdvice(method);
        methodInvoke(advices, "before");

        Object invokeResult;
        try {
            invokeResult = method.invoke(config.getTarget(), args);
            methodInvoke(advices, "after");
        } catch (Exception e) {
            methodInvoke(advices, "afterThrow");
            throw e;
        }
        return invokeResult;
    }

    private void methodInvoke(Map<String, Advice> advices, String str) {
        Advice advice = advices.get(str);
        Method method = advice.getMethod();
        Object aspect = advice.getAspect();
        try {
            method.invoke(aspect);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public Object getProxy() {
        return Proxy.newProxyInstance(this.getClass().getClassLoader(), this.config.getTargetClass().getInterfaces(), this);
    }
}
