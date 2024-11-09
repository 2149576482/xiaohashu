package com.arnanzz.framework.biz.operationlog.aspect;

import java.lang.annotation.*;

/**
 * @author ArnanZZ
 * @version 1.0
 * @description:
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
public @interface ApiOperationLog {
    /**
     * API 功能描述
     *
     * @return
     */
    String description() default "";

}