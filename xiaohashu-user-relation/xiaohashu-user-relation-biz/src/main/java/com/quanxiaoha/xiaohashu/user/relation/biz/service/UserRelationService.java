package com.quanxiaoha.xiaohashu.user.relation.biz.service;

import com.quanxiaoha.xiaohashu.common.response.PageResponse;
import com.quanxiaoha.xiaohashu.common.response.Response;
import com.quanxiaoha.xiaohashu.user.relation.biz.model.vo.req.FansListReqVO;
import com.quanxiaoha.xiaohashu.user.relation.biz.model.vo.req.FollowListReqVO;
import com.quanxiaoha.xiaohashu.user.relation.biz.model.vo.req.FollowUserReqVO;
import com.quanxiaoha.xiaohashu.user.relation.biz.model.vo.req.UnfollowUserReqVO;
import com.quanxiaoha.xiaohashu.user.relation.biz.model.vo.resp.FansListRespVO;
import com.quanxiaoha.xiaohashu.user.relation.biz.model.vo.resp.FollowListRespVO;
import jakarta.validation.constraints.NotNull;

public interface UserRelationService {
    Response<?> followUser(FollowUserReqVO followUserReqVO);

    Response<?> unfollowUser(UnfollowUserReqVO unfollowUserReqVO);

    PageResponse<FollowListRespVO> getFollowList(FollowListReqVO followListReqVO);

    PageResponse<FansListRespVO> getFansList(FansListReqVO fansListReqVO);
}
