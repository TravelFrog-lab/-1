package org.example.springboot.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.springboot.entity.HousekeepingOrder;

@Mapper
public interface HousekeepingOrderMapper extends BaseMapper<HousekeepingOrder> {
} 