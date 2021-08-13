package com.gupaoedu.vip.spring.framework.beans.support;

import com.gupaoedu.vip.spring.framework.annotation.GPController;
import com.gupaoedu.vip.spring.framework.annotation.GPService;
import com.gupaoedu.vip.spring.framework.beans.config.BeanDefinition;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author mengxiangqian 2021/8/8
 */
public class BeanDefinitionReader {

    private Properties contextConfig = new Properties();
    /**
     * 保存扫描的结果
     */
    private List<String> registryBeanClasses = new ArrayList<String>();

    public BeanDefinitionReader(String... contextConfigLocations) {
        doLoadingConfig(contextConfigLocations[0]);

        doScanPackage(contextConfig.getProperty("scanPackage"));
    }

    public List<BeanDefinition> loadBeanDefinition() {
        List<BeanDefinition> result = new ArrayList<BeanDefinition>();
        try {
            for (String registryBeanClass : registryBeanClasses) {

                Class<?> beanClass = Class.forName(registryBeanClass);
                String beanClassName = beanClass.getName();
                String beanName;
                if (beanClass.isAnnotationPresent(GPController.class)) {
                    beanName = toLowerFirstCase(beanClass.getSimpleName());
                    result.add(new BeanDefinition(beanName, beanClassName));
                } else if (beanClass.isAnnotationPresent(GPService.class)) {
                    //在多个包下出现相同的类名，只能寄几（自己）起一个全局唯一的名字
                    //自定义命名
                    beanName = beanClass.getAnnotation(GPService.class).value();
                    if ("".equals(beanName.trim())) {
                        beanName = toLowerFirstCase(beanClass.getSimpleName());
                    }

                    result.add(new BeanDefinition(beanName, beanClassName));

                    //如果是接口注入
                    for (Class<?> i : beanClass.getInterfaces()) {
                        result.add(new BeanDefinition(i.getSimpleName(), beanClassName));
                    }
                }
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private String toLowerFirstCase(String beanName) {
        char[] chars = beanName.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }


    private void doScanPackage(String scanPackage) {
        String replace = scanPackage.replaceAll("\\.", "/");
        URL url = this.getClass().getClassLoader().getResource("/" + replace);
        File scanFile = new File(url.getFile());
        File[] files = scanFile.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                doScanPackage(scanPackage + "." + file.getName());
            } else {
                if (!file.getName().endsWith(".class")) {
                    continue;
                }
                String className = scanPackage + "." + file.getName().replace(".class", "");
                registryBeanClasses.add(className);
            }
        }
    }

    private void doLoadingConfig(String contextConfigLocation) {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
        try {
            contextConfig.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public Properties getContextConfig() {
        return contextConfig;
    }
}
