package com.gupaoedu.vip.spring.framework.aop.config;

import lombok.Data;

/**
 * @author Meng 2021/8/10
 */
@Data
public class AopConfig {

    private String pointCut;
    private String aspectClass;
    private String aspectBefore;
    private String aspectAfter;
    private String aspectAfterThrow;
    private String aspectAfterThrowingName;
}
