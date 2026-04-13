package org.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.example.springboot.entity.HousekeeperServiceRelation;
import org.example.springboot.service.HousekeeperService;

@Mapper
public interface HousekeeperServiceRelationMapper extends BaseMapper<HousekeeperServiceRelation> {
} 