package com.quanxiaoha.xiaohashu.search.contorller;

import com.quanxiaoha.xiaohashu.common.response.PageResponse;
import com.quanxiaoha.xiaohashu.search.model.VO.SearchNoteReqVO;
import com.quanxiaoha.xiaohashu.search.model.VO.SearchNoteRespVO;
import com.quanxiaoha.xiaohashu.search.model.VO.SearchUserReqVO;
import com.quanxiaoha.xiaohashu.search.model.VO.SearchUserRespVO;
import com.quanxiaoha.xiaohashu.search.service.NoteSearchService;
import com.quanxiaoha.xiaohashu.search.service.UserSearchService;
import com.quanxiaoha.xiaoshu.operationlog.aspect.ApiOperationLog;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/search")
@Slf4j
public class SearchUserController {
    @Resource
    private UserSearchService userSearchService;
    @Resource
    private NoteSearchService noteSearchService;
    @PostMapping("/user")
    @ApiOperationLog(description = "search user by nickname and xiaohashu_id")
    public PageResponse<SearchUserRespVO> searchUser(@RequestBody SearchUserReqVO searchUserReqVO){
        return userSearchService.searchUser(searchUserReqVO);
    }

    @PostMapping("/note")
    @ApiOperationLog(description = "search notes by topic and title")
    public PageResponse<SearchNoteRespVO> searchUser(@RequestBody SearchNoteReqVO searchNoteReqVO){
        return noteSearchService.searchNote(searchNoteReqVO);
    }
}
