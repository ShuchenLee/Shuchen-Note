package com.quanxiaoha.xiaohashu.note.biz.domain.mapper;

import com.quanxiaoha.xiaohashu.note.biz.domain.dataobject.NoteCollectionDO;
import com.quanxiaoha.xiaohashu.note.biz.domain.dataobject.NoteLikeDO;
import com.quanxiaoha.xiaohashu.note.biz.model.dto.CollectNoteDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface NoteCollectionDOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(NoteCollectionDO record);

    int insertSelective(NoteCollectionDO record);

    NoteCollectionDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(NoteCollectionDO record);

    int updateByPrimaryKey(NoteCollectionDO record);

    int selectIfCollectd(@Param("userId") Long userId, @Param("noteId") Long noteId);

    List<NoteCollectionDO> selectAllByUserId(@Param("userId")Long userId);

    List<NoteCollectionDO> selectAllByUserIdLimit(@Param("userId")Long userId);

    int insertOrUpdate(CollectNoteDTO collectNoteDTO);

    int update2UncollectByUserIdAndNoteId(NoteCollectionDO noteCollectionDO);
}