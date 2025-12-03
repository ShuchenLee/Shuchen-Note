package com.quanxiaoha.xiaohashu.user.biz.domain.mapper;

import com.quanxiaoha.xiaohashu.user.biz.domain.dataobject.RoleDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
@Mapper
public interface RoleDOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(RoleDO record);

    int insertSelective(RoleDO record);

    RoleDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(RoleDO record);

    int updateByPrimaryKey(RoleDO record);

    List<RoleDO> selectEnabledList();
}