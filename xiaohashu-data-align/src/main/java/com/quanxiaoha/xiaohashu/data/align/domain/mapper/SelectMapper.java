package com.quanxiaoha.xiaohashu.data.align.domain.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SelectMapper {
    /**
     * 日增量表：笔记数计数变更 - 批量查询
     * @param tableNameSuffix
     * @param batchSize
     * @return
     */
    List<Long> selectBatchFromDataAlignNotePublishCountTempTable(@Param("tableNameSuffix") String tableNameSuffix,
                                                               @Param("batchSize") int batchSize);
    /**
     * 查询 t_note 笔记表，获取笔记总数
     * @param userId
     * @return
     */
    int selectCountFromNotePublishTableByUserId(long userId);
    /**
     * 日增量表：关注数计数变更 - 批量查询
     * @param tableNameSuffix
     * @param batchSize
     * @return
     */
    List<Long> selectBatchFromDataAlignFollowingCountTempTable(@Param("tableNameSuffix") String tableNameSuffix,
                                                               @Param("batchSize") int batchSize);
    /**
     * 查询 t_following 关注表，获取关注总数
     * @param userId
     * @return
     */
    int selectCountFromFollowingTableByUserId(long userId);
    /**
     * 日增量表：粉丝数计数变更 - 批量查询
     * @param tableNameSuffix
     * @param batchSize
     * @return
     */
    List<Long> selectBatchFromDataAlignFansCountTempTable(@Param("tableNameSuffix") String tableNameSuffix,
                                                               @Param("batchSize") int batchSize);
    /**
     * 查询 t_fans 粉丝表，获取粉丝总数
     * @param userId
     * @return
     */
    int selectCountFromFansTableByUserId(long userId);
    /**
     * 日增量表：笔记点赞数计数变更 - 批量查询
     * @param tableNameSuffix
     * @param batchSize
     * @return
     */
    List<Long> selectBatchFromDataAlignNoteLikeCountTempTable(@Param("tableNameSuffix") String tableNameSuffix,
                                                               @Param("batchSize") int batchSize);
    /**
     * 查询 t_note_likes 点赞表，获取点赞总数
     * @param noteId
     * @return
     */
    int selectCountFromNoteLikeTableByUserId(long noteId);

    List<Long> selectBatchFromDataAlignNoteCollectCountTempTable(@Param("tableNameSuffix") String tableNameSuffix,
                                                              @Param("batchSize") int batchSize);
    /**
     * 查询 t_note_collections 关注表，获取关注总数
     * @param noteId
     * @return
     */
    int selectCountFromNoteCollectTableByUserId(long noteId);

}
