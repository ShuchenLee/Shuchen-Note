package com.quanxiaoha.xiaohashu.count.biz.domain.mapper;

import com.quanxiaoha.xiaohashu.count.biz.domain.dataobject.UserCountDO;
import com.quanxiaoha.xiaohashu.count.biz.model.dto.CountPublishNoteDTO;
import org.apache.ibatis.annotations.Param;


public interface UserCountDOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(UserCountDO record);

    int insertSelective(UserCountDO record);

    UserCountDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(UserCountDO record);

    int updateByPrimaryKey(UserCountDO record);

    int insertOrUpdateFansTotalByUserId(@Param("userId") Long userId, @Param("fansTotal")Integer fansTotal);
    int insertOrUpdateFollowingTotalByUserId(@Param("userId") Long userId, @Param("followingTotal")Integer followingTotal);
    int insertOrUpdatePublishNote(@Param("userId")Long userId, @Param("count")Integer count);

    int insertOrUpdateLikeTotalByUserId(@Param("userId")Long userId, @Param("likeTotal")Integer likeTotal);
    int insertOrUpdateCollectTotalByUserId(@Param("userId")Long userId, @Param("collectTotal")Integer collectTotal);
}