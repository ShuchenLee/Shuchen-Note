package com.quanxiaoha.xiaohashu.user.relation.biz.contoller;

import com.quanxiaoha.xiaohashu.common.response.PageResponse;
import com.quanxiaoha.xiaohashu.common.response.Response;
import com.quanxiaoha.xiaohashu.user.relation.biz.model.vo.req.FansListReqVO;
import com.quanxiaoha.xiaohashu.user.relation.biz.model.vo.req.FollowListReqVO;
import com.quanxiaoha.xiaohashu.user.relation.biz.model.vo.req.FollowUserReqVO;
import com.quanxiaoha.xiaohashu.user.relation.biz.model.vo.req.UnfollowUserReqVO;
import com.quanxiaoha.xiaohashu.user.relation.biz.model.vo.resp.FansListRespVO;
import com.quanxiaoha.xiaohashu.user.relation.biz.model.vo.resp.FollowListRespVO;
import com.quanxiaoha.xiaohashu.user.relation.biz.service.UserRelationService;
import com.quanxiaoha.xiaoshu.operationlog.aspect.ApiOperationLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/relation")
public class UserRelationController {
    @Autowired
    private UserRelationService userRelationService;

    @PostMapping("/follow")
    @ApiOperationLog(description = "关注用户")
    public Response<?> follow(@Validated @RequestBody FollowUserReqVO followUserReqVO){
        return userRelationService.followUser(followUserReqVO);
    }
    @PostMapping("/unfollow")
    @ApiOperationLog(description = "关注用户")
    public Response<?> unfollow(@Validated @RequestBody UnfollowUserReqVO unfollowUserReqVO){
        return userRelationService.unfollowUser(unfollowUserReqVO);
    }
    @PostMapping("/follow/list")
    @ApiOperationLog(description = "获取用户的关注列表")
    public PageResponse<FollowListRespVO> getFollList(@Validated @RequestBody FollowListReqVO followListReqVO) {
        return userRelationService.getFollowList(followListReqVO);
    }
    @PostMapping("/fans/list")
    @ApiOperationLog(description = "获取用户的粉丝列表")
    public PageResponse<FansListRespVO> getFollList(@Validated @RequestBody FansListReqVO fansListReqVO) {
        return userRelationService.getFansList(fansListReqVO);
    }
}
