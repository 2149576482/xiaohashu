package com.arnanzz.xiaohashu.auth.model.vo.verificationcode;

import com.arnanzz.framework.common.validator.PhoneNumber;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author ArnanZZ
 * @version 1.0
 * @description: 接口入参VO - 手机号
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SendVerificationCodeReqVO {

    @NotBlank(message = "手机号不能为空")
    @PhoneNumber
    private String phone;

}
