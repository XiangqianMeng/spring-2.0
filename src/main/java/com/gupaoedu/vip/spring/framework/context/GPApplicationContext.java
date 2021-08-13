package com.gupaoedu.vip.spring.framework.context;


import com.gupaoedu.vip.spring.framework.annotation.GPAutowired;
import com.gupaoedu.vip.spring.framework.aop.JdkDynamicAopProxy;
import com.gupaoedu.vip.spring.framework.aop.config.AopConfig;
import com.gupaoedu.vip.spring.framework.aop.support.AdviceSupport;
import com.gupaoedu.vip.spring.framework.beans.BeanWrapper;
import com.gupaoedu.vip.spring.framework.beans.config.BeanDefinition;
import com.gupaoedu.vip.spring.framework.beans.support.BeanDefinitionReader;

import java.lang.reflect.Field;
import java.util.*;

/**
 * @author Meng
 */
public class GPApplicationContext {

    private BeanDefinitionReader reader;
    private Map<String, BeanDefinition> beanDefinitionMap = new HashMap<String, BeanDefinition>();
    private Map<String, Object> factoryBeanObjectCache = new HashMap<String, Object>();
    private Map<String, BeanWrapper> factoryBeanInstanceCache = new HashMap<String, BeanWrapper>();


    public GPApplicationContext(String... contextConfigLocations) {

        try {
            //加载配置文件
            reader = new BeanDefinitionReader(contextConfigLocations);
            //解析配置文件，封装为BeanDefinition对象
            List<BeanDefinition> beanDefinitions = reader.loadBeanDefinition();
            //将beanDefinition信息缓存
            doRegistryBeanDefinition(beanDefinitions);
            //实例化对象并执行依赖注入
            doAutowired();
            System.out.println("=============IoC初始化完成===========");
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void doAutowired() {
        for (Map.Entry<String, BeanDefinition> beanDefinitionEntry : beanDefinitionMap.entrySet()) {
            BeanDefinition beanDefinition = beanDefinitionEntry.getValue();
            String beanName = beanDefinition.getFactoryBeanName();
            getBean(beanName);
        }
    }

    public Object getBean(String beanName) {
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        //反射生成实例对象
        Object instance = instanceBean(beanName, beanDefinition);
        //对实例对象进行包装
        BeanWrapper beanWrapper = new BeanWrapper(instance);
        //将装饰后的wrapper对象保存到IoC容器
        factoryBeanInstanceCache.put(beanName, beanWrapper);
        //执行依赖注入
        populateBean(beanDefinition, beanWrapper);

        return beanWrapper.getInstance();

    }

    private void populateBean(BeanDefinition beanDefinition, BeanWrapper beanWrapper) {
        try {
            String fieldName;
            Class clazz = Class.forName(beanDefinition.getBeanClassName());
            for (Field field : clazz.getDeclaredFields()) {
                if (!field.isAnnotationPresent(GPAutowired.class)) {
                    continue;
                }
                fieldName = field.getAnnotation(GPAutowired.class).value().trim();
                //如果用户没有自定义的fieldName，则默认根据类型注入
                if ("".equals(fieldName)) {
                    fieldName = field.getName();
                }
                field.setAccessible(true);
                try {
                    if (null != factoryBeanInstanceCache.get(fieldName)) {
                        field.set(beanWrapper.getInstance(), factoryBeanInstanceCache.get(fieldName).getInstance());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Object instanceBean(String beanName, BeanDefinition beanDefinition) {
        String className = beanDefinition.getBeanClassName();
        Object instance = null;
        try {
            if (this.factoryBeanObjectCache.containsKey(beanName)) {
                return this.factoryBeanObjectCache.get(beanName);
            }
            Class<?> clazz = Class.forName(className);

            AopConfig aopConfig = instanceAopConfig(beanDefinition);
            AdviceSupport adviseSupport = new AdviceSupport(aopConfig);
            adviseSupport.setTarget(clazz.newInstance());
            adviseSupport.setTargetClass(clazz);
            if (adviseSupport.pointCutClass()) {
                instance = new JdkDynamicAopProxy(adviseSupport).getProxy();
            } else {
                instance = clazz.newInstance();
            }
            //对原生对象进行缓存
            this.factoryBeanObjectCache.put(beanName, instance);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return instance;
    }

    private AopConfig instanceAopConfig(BeanDefinition beanDefinition) {
        AopConfig aopConfig = new AopConfig();
        aopConfig.setPointCut(reader.getContextConfig().getProperty("pointCut"));
        aopConfig.setAspectClass(reader.getContextConfig().getProperty("aspectClass"));
        aopConfig.setAspectBefore(reader.getContextConfig().getProperty("aspectBefore"));
        aopConfig.setAspectAfter(reader.getContextConfig().getProperty("aspectAfter"));
        aopConfig.setAspectAfterThrow(reader.getContextConfig().getProperty("aspectAfterThrow"));
        aopConfig.setAspectAfterThrowingName(reader.getContextConfig().getProperty("aspectAfterThrowingName"));
        return aopConfig;
    }

    private void doRegistryBeanDefinition(List<BeanDefinition> beanDefinitions) throws Exception {
        for (BeanDefinition beanDefinition : beanDefinitions) {
            if (this.beanDefinitionMap.containsKey(beanDefinition.getFactoryBeanName())) {
                throw new Exception("The " + beanDefinition.getFactoryBeanName() + " is exists!");
            }

            beanDefinitionMap.put(beanDefinition.getFactoryBeanName(), beanDefinition);
            beanDefinitionMap.put(beanDefinition.getBeanClassName(), beanDefinition);
        }
    }

    public int getBeanDefinitionCount() {
        return this.beanDefinitionMap.size();
    }
    public String[] getBeanDefinitionNames() {
        return this.beanDefinitionMap.keySet().toArray(new String[this.beanDefinitionMap.size()]);
    }

    public Properties getConfig() {
        return this.reader.getContextConfig();
    }


}
