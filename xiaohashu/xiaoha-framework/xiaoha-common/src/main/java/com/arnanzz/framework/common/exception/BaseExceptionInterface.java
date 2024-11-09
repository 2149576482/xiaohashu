package com.arnanzz.framework.common.exception;

/**
 * @author ArnanZZ
 * @version 1.0
 * @description: 基础异常接口
 **/
public interface BaseExceptionInterface {

    // 获取异常码
    String getErrorCode();

    // 获取异常信息
    String getErrorMessage();

}
