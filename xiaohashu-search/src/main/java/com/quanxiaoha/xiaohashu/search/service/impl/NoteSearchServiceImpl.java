package com.quanxiaoha.xiaohashu.search.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.quanxiaoha.xiaohashu.common.constant.DateConstants;
import com.quanxiaoha.xiaohashu.common.response.PageResponse;
import com.quanxiaoha.xiaohashu.common.util.DateUtils;
import com.quanxiaoha.xiaohashu.search.enums.NotePublishTimeRangeEnum;
import com.quanxiaoha.xiaohashu.search.enums.NoteSortEnum;
import com.quanxiaoha.xiaohashu.search.index.NoteIndex;
import com.quanxiaoha.xiaohashu.search.model.VO.SearchNoteReqVO;
import com.quanxiaoha.xiaohashu.search.model.VO.SearchNoteRespVO;
import com.quanxiaoha.xiaohashu.search.service.NoteSearchService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.lucene.search.function.CombineFunction;
import org.elasticsearch.common.lucene.search.function.FieldValueFactorFunction;
import org.elasticsearch.common.lucene.search.function.FunctionScoreQuery;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FieldValueFactorFunctionBuilder;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
public class NoteSearchServiceImpl implements NoteSearchService {
    @Resource
    private RestHighLevelClient restHighLevelClient;
    @Override
    public PageResponse<SearchNoteRespVO> searchNote(SearchNoteReqVO searchNoteReqVO) {
        //get query info
        String keyword = searchNoteReqVO.getKeyword();
        Integer pageNo = searchNoteReqVO.getPageNo();
        Integer type =  searchNoteReqVO.getType();
        Integer sort =  searchNoteReqVO.getSort();
        Integer timeRange = searchNoteReqVO.getPublishTimeRange();
        //construct query quest
        SearchRequest searchRequest = new SearchRequest(NoteIndex.NAME);
        //query body
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        QueryBuilder queryBuilder = QueryBuilders.multiMatchQuery(keyword)
                .field(NoteIndex.FIELD_NOTE_TOPIC)
                .field(NoteIndex.FIELD_NOTE_TITLE,2.0f);
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(queryBuilder);
        if(ObjectUtil.isNotEmpty(type)){
            boolQueryBuilder.must(QueryBuilders.termQuery(NoteIndex.FIELD_NOTE_TYPE,type));
        }
        //time range
        NotePublishTimeRangeEnum notePublishTimeRangeEnum = NotePublishTimeRangeEnum.valueOf(timeRange);
        if(ObjectUtil.isNotEmpty(notePublishTimeRangeEnum)){
            String end = DateUtils.localDateTime2String(LocalDateTime.now());
            String start = null;
            switch (notePublishTimeRangeEnum) {
                case ONE_DAY ->
                        DateUtils.localDateTime2String(LocalDateTime.now().minusDays(1));
                case ONE_WEEK ->
                        DateUtils.localDateTime2String(LocalDateTime.now().minusWeeks(1));
                case HALF_YEAR ->
                        DateUtils.localDateTime2String(LocalDateTime.now().minusMonths(6));
            }
            if(StringUtils.isNotBlank(start)){
                boolQueryBuilder.filter(QueryBuilders.rangeQuery(NoteIndex.FIELD_NOTE_CREATE_TIME)
                        .gte(start).lte(end));
            }
        }
        NoteSortEnum noteSortEnum = NoteSortEnum.valueOf(sort);
        if(ObjectUtil.isNotEmpty(noteSortEnum)){
            switch (noteSortEnum) {
                case LATEST ->
                        searchSourceBuilder.sort(new FieldSortBuilder(NoteIndex.FIELD_NOTE_CREATE_TIME).order(SortOrder.DESC));
                case MOST_COLLECT ->
                        searchSourceBuilder.sort(new FieldSortBuilder(NoteIndex.FIELD_NOTE_COLLECT_TOTAL).order(SortOrder.DESC));
                case MOST_LIKE ->
                        searchSourceBuilder.sort(new FieldSortBuilder(NoteIndex.FIELD_NOTE_LIKE_TOTAL).order(SortOrder.DESC));
                case MOST_COMMENT ->
                        searchSourceBuilder.sort(new FieldSortBuilder(NoteIndex.FIELD_NOTE_COMMENT_TOTAL).order(SortOrder.DESC));
            }
            searchSourceBuilder.query(boolQueryBuilder);
        }else{
            // 创建 FilterFunctionBuilder 数组
            // "functions": [
            //         {
            //           "field_value_factor": {
            //             "field": "like_total",
            //             "factor": 0.5,
            //             "modifier": "sqrt",
            //             "missing": 0
            //           }
            //         },
            //         {
            //           "field_value_factor": {
            //             "field": "collect_total",
            //             "factor": 0.3,
            //             "modifier": "sqrt",
            //             "missing": 0
            //           }
            //         },
            //         {
            //           "field_value_factor": {
            //             "field": "comment_total",
            //             "factor": 0.2,
            //             "modifier": "sqrt",
            //             "missing": 0
            //           }
            //         }
            //       ],
            FunctionScoreQueryBuilder.FilterFunctionBuilder[] filterFunctionBuilders = new FunctionScoreQueryBuilder.FilterFunctionBuilder[] {
                    // function 1
                    new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                            new FieldValueFactorFunctionBuilder(NoteIndex.FIELD_NOTE_LIKE_TOTAL)
                                    .factor(0.5f)
                                    .modifier(FieldValueFactorFunction.Modifier.SQRT)
                                    .missing(0)
                    ),
                    // function 2
                    new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                            new FieldValueFactorFunctionBuilder(NoteIndex.FIELD_NOTE_COLLECT_TOTAL)
                                    .factor(0.3f)
                                    .modifier(FieldValueFactorFunction.Modifier.SQRT)
                                    .missing(0)
                    ),
                    // function 3
                    new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                            new FieldValueFactorFunctionBuilder(NoteIndex.FIELD_NOTE_COMMENT_TOTAL)
                                    .factor(0.2f)
                                    .modifier(FieldValueFactorFunction.Modifier.SQRT)
                                    .missing(0)
                    )
            };
            FunctionScoreQueryBuilder functionScoreQueryBuilder = QueryBuilders.functionScoreQuery(queryBuilder,
                            filterFunctionBuilders)
                    .scoreMode(FunctionScoreQuery.ScoreMode.SUM)
                    .boostMode(CombineFunction.SUM);
            searchSourceBuilder.query(functionScoreQueryBuilder);
            //sort body
            SortBuilder sortBuilder = new FieldSortBuilder("_score").order(SortOrder.DESC);
            searchSourceBuilder.sort(sortBuilder);
        }

        //highlight part
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field(NoteIndex.FIELD_NOTE_TITLE);
        searchSourceBuilder.highlighter(highlightBuilder);
        //set page
        int pageSize = 10;
        int from = (pageNo - 1) * pageSize;
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(pageSize);
        searchRequest.source(searchSourceBuilder);
        List<SearchNoteRespVO> searchNoteRespVOList = new ArrayList<>();
        long total = 0;
        try{
            //get search results
            log.info("==========SearchRequest:\t{}",searchRequest);
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            total =  searchResponse.getHits().getTotalHits().value;
            SearchHits hits = searchResponse.getHits();
            log.info("========hit {} documents ",total);
            //process search results
            for (SearchHit hit: hits) {
                log.info("========get hit {}",hit.getSourceAsString());
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                //get specific field
                Long noteId = (Long) sourceAsMap.get(NoteIndex.FIELD_NOTE_ID);
                String cover = (String) sourceAsMap.get(NoteIndex.FIELD_NOTE_COVER);
                String title = (String) sourceAsMap.get(NoteIndex.FIELD_NOTE_TITLE);
                String avatar = (String) sourceAsMap.get(NoteIndex.FIELD_NOTE_AVATAR);
                String nickname = (String) sourceAsMap.get(NoteIndex.FIELD_NOTE_NICKNAME);
                // 获取更新时间
                String updateTimeStr = (String) sourceAsMap.get(NoteIndex.FIELD_NOTE_UPDATE_TIME);
                LocalDateTime updateTime = LocalDateTime.parse(updateTimeStr, DateConstants.DATE_FORMAT_Y_M_D_H_M_S);
                Integer likeTotal = (Integer) sourceAsMap.get(NoteIndex.FIELD_NOTE_LIKE_TOTAL);

                String highlightField = null;
                if(CollectionUtil.isNotEmpty(hit.getHighlightFields()) && hit.getHighlightFields().containsKey(NoteIndex.FIELD_NOTE_TITLE)) {
                    highlightField = hit.getHighlightFields().get(NoteIndex.FIELD_NOTE_TITLE).fragments()[0].toString();
                }
                SearchNoteRespVO searchNoteRespVO = SearchNoteRespVO.builder()
                        .noteId(noteId)
                        .cover(cover)
                        .title(title)
                        .avatar(avatar)
                        .nickname(nickname)
                        .updateTime(updateTime)
                        .likeTotal(likeTotal)
                        .highlightTitle(highlightField)
                        .build();
                searchNoteRespVOList.add(searchNoteRespVO);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return PageResponse.success(searchNoteRespVOList, total, pageSize);
    }
}
