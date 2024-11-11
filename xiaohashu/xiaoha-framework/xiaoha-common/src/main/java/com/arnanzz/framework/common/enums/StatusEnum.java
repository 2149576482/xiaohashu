package com.arnanzz.framework.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author ArnanZZ
 * @version 1.0
 * @description: 状态枚举
 **/
@Getter
@AllArgsConstructor
public enum StatusEnum {
    // 启用
    ENABLE(0),
    // 禁用
    DISABLED(1);

    private final Integer value;
}
