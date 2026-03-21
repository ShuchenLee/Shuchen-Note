package com.quanxiaoha.xiaohashu.search.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.quanxiaoha.xiaohashu.common.response.PageResponse;
import com.quanxiaoha.xiaohashu.common.util.NumUtils;
import com.quanxiaoha.xiaohashu.search.index.UserIndex;
import com.quanxiaoha.xiaohashu.search.model.VO.SearchUserReqVO;
import com.quanxiaoha.xiaohashu.search.model.VO.SearchUserRespVO;
import com.quanxiaoha.xiaohashu.search.service.UserSearchService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class UserSearchServiceImpl implements UserSearchService {
    private final RestHighLevelClient restHighLevelClient;

    public UserSearchServiceImpl(RestHighLevelClient restHighLevelClient) {
        this.restHighLevelClient = restHighLevelClient;
    }

    @Override
    public PageResponse<SearchUserRespVO> searchUser( SearchUserReqVO searchUserReqVO) {
        String keyword = searchUserReqVO.getKeyword();
        Integer pageNo = searchUserReqVO.getPageNo();
        //construct es search request
        SearchRequest searchRequest = new SearchRequest(UserIndex.NAME);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //multi-search on nickname and xiaohashu_id
        searchSourceBuilder.query(QueryBuilders.multiMatchQuery(
                keyword,UserIndex.FIELD_USER_NICKNAME,UserIndex.FIELD_USER_XIAOHASHU_ID));
        //sort builder
        SortBuilder sortBuilder = new FieldSortBuilder(UserIndex.FIELD_USER_FANS_TOTAL)
                .order(SortOrder.DESC);
        searchSourceBuilder.sort(sortBuilder);
        //highlight builder
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field(UserIndex.FIELD_USER_NICKNAME)
                .preTags("<strong>")
                .postTags("</strong>");
        searchSourceBuilder.highlighter(highlightBuilder);
        int pageSize = 10;
        int offset = (pageNo - 1) * pageSize;
        searchSourceBuilder.from(offset);
        searchSourceBuilder.size(pageSize);
        searchRequest.source(searchSourceBuilder);
        List<SearchUserRespVO> searchResultList = null;
        long total = 0;
        try{
            log.info("==========SearchRequest:\t{}",searchRequest);
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            total =  searchResponse.getHits().getTotalHits().value;
            SearchHits hits = searchResponse.getHits();
            log.info("========hit {} documents ",total);
            searchResultList = new ArrayList();
            //transfer hit to resp
            for(SearchHit hit : hits){
                log.info("=======document {}",hit.getSourceAsString());
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                // get specific field
                Long userId = ((Number) sourceAsMap.get(UserIndex.FIELD_USER_ID)).longValue();
                String nickname = (String) sourceAsMap.get(UserIndex.FIELD_USER_NICKNAME);
                String avatar = (String) sourceAsMap.get(UserIndex.FIELD_USER_AVATAR);
                String xiaohashuId = (String) sourceAsMap.get(UserIndex.FIELD_USER_XIAOHASHU_ID);
                Integer noteTotal = (Integer) sourceAsMap.get(UserIndex.FIELD_USER_NOTE_TOTAL);
                Integer fansTotal = (Integer) sourceAsMap.get(UserIndex.FIELD_USER_FANS_TOTAL);
                //get highlight field
                String highlightNickName = null;
                if(CollectionUtil.isNotEmpty(hit.getHighlightFields()) && hit.getHighlightFields().containsKey(UserIndex.FIELD_USER_NICKNAME))
                    highlightNickName = hit.getHighlightFields().get(UserIndex.FIELD_USER_NICKNAME).fragments()[0].toString();
                //get resp vo
                SearchUserRespVO searchUserRespVO = SearchUserRespVO.builder()
                        .userId(userId)
                        .nickName(nickname)
                        .avatar(avatar)
                        .fansTotal(NumUtils.parseNum(fansTotal))
                        .noteTotal(noteTotal)
                        .xiaohashuId(xiaohashuId)
                        .highlightNickName(highlightNickName)
                        .build();
                searchResultList.add(searchUserRespVO);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return PageResponse.success(searchResultList,total,pageNo,pageSize);
    }
}
