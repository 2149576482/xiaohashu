package com.arnanzz.framework.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author ArnanZZ
 * @version 1.0
 * @description: 是否删除枚举
 **/
@Getter
@AllArgsConstructor
public enum DeletedEnum {

    YES(true),
    NO(false);

    private final Boolean value;

}
