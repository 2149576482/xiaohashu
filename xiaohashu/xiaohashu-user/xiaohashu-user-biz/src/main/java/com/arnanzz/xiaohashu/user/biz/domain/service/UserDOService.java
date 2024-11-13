package com.arnanzz.xiaohashu.user.biz.domain.service;

import com.arnanzz.framework.common.response.Response;
import com.arnanzz.xiaohashu.user.biz.model.vo.user.UpdateUserInfoReqVO;

public interface UserDOService{

    /**
     * 更新用户信息
     *
     * @param updateUserInfoReqVO
     * @return
     */
    Response<?> updateUserInfo(UpdateUserInfoReqVO updateUserInfoReqVO);

}
