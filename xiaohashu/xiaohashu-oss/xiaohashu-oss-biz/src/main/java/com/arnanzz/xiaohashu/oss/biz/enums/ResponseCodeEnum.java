package com.arnanzz.xiaohashu.oss.biz.enums;

import com.arnanzz.framework.common.exception.BaseExceptionInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author ArnanZZ
 * @version 1.0
 * @description: 通用枚举
 **/
@Getter
@AllArgsConstructor
public enum ResponseCodeEnum implements BaseExceptionInterface {

    // ----------- 通用异常状态码 -----------
    SYSTEM_ERROR("OSS-10000", "出错啦，后台小哥正在努力修复中..."),
    PARAM_NOT_VALID("OSS-10001", "参数错误"),

    // ----------- 业务异常状态码 -----------
    FILE_UPLOAD_FAIL("OSS-20000", "文件上传失败"),
    FILE_NOT_EXIST("OSS-20001", "文件不存在"),
    STORAGE_TYPE_ERROR("OSS-20002", "存储类型错误"),
    ;

    // 异常码
    private final String errorCode;
    // 错误信息
    private final String errorMessage;

}