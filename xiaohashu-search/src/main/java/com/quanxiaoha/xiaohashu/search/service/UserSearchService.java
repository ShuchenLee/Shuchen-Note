package com.quanxiaoha.xiaohashu.search.service;

import com.quanxiaoha.xiaohashu.common.response.PageResponse;
import com.quanxiaoha.xiaohashu.search.model.VO.SearchUserReqVO;
import com.quanxiaoha.xiaohashu.search.model.VO.SearchUserRespVO;

public interface UserSearchService {
    /**
     * search users
     * @param searchUserReqVO
     * @return
     */
    PageResponse<SearchUserRespVO> searchUser(SearchUserReqVO searchUserReqVO);
}
