package com.arnanzz.xiaohashu.user.relation.biz.domain.service;

import com.arnanzz.framework.common.response.PageResponse;
import com.arnanzz.framework.common.response.Response;
import com.arnanzz.xiaohashu.user.relation.biz.model.vo.*;

/**
 * @author ArnanZZ
 * @version 1.0
 * @description:
 **/
public interface RelationService {

    /**
     * 关注用户
     */
    Response<?> follow(FollowUserReqVO followUserReqVO);

    /**
     * 取关用户
     */
    Response<?> unfollow(UnFollowUserReqVO unfollowUserReqVO);

    /**
     * 查询关注列表
     */
    PageResponse<FindFollowingUserRspVO> findFollowingList(FindFollowingListReqVO findFollowingListReqVO);

    /**
     * 查询关注列表
     */
    PageResponse<FindFansUserRspVO> findFansList(FindFansListReqVO findFansListReqVO);
}
