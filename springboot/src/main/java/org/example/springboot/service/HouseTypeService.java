package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.entity.House;
import org.example.springboot.entity.HouseType;
import org.example.springboot.mapper.HouseTypeMapper;
import org.example.springboot.mapper.HouseMapper;
import org.example.springboot.util.HouseTypeReferenceAreas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class HouseTypeService extends ServiceImpl<HouseTypeMapper, HouseType> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(HouseTypeService.class);
    
    @Resource
    private HouseTypeMapper houseTypeMapper;
    
    @Resource
    private HouseMapper houseMapper;
    


    public Page<HouseType> list(Integer pageNum, Integer pageSize, String name) {
        LOGGER.info("Listing house types, pageNum: {}, pageSize: {}, name: {}", pageNum, pageSize, name);
        LambdaQueryWrapper<HouseType> wrapper = new LambdaQueryWrapper<>();
        if (name != null && !name.trim().isEmpty()) {

            wrapper.like(HouseType::getName, name);
        }
        wrapper.orderByDesc(HouseType::getCreatedAt);
        
        Page<HouseType> page = page(new Page<>(pageNum, pageSize), wrapper);
        if (page.getRecords() != null && !page.getRecords().isEmpty()) {
            HouseTypeReferenceAreas.apply(page.getRecords());
        }
        return page;
    }


    public Result<HouseType> getById(Long id) {
        LOGGER.info("Getting house type by id: {}", id);
        HouseType houseType = houseTypeMapper.selectById(id);
        if (houseType == null) {
            return Result.error("-1", "房屋类型不存在");
        }
        HouseTypeReferenceAreas.apply(houseType);
        return Result.success(houseType);
    }

    @Transactional
    public Result<?> add(HouseType houseType) {
        LOGGER.info("Adding new house type: {}", houseType);
        // 检查名称是否重复
        if (exists(houseType.getName())) {
            return Result.error("-1", "房屋类型名称已存在");
        }
        save(houseType);
        return Result.success();
    }

    private boolean exists(String name) {
        return count(new LambdaQueryWrapper<HouseType>()
                .eq(HouseType::getName, name)) > 0;
    }

    @Transactional
    public Result<?> update(HouseType houseType) {
        LOGGER.info("Updating house type: {}", houseType);
        // 检查名称是否与其他记录重复
        LambdaQueryWrapper<HouseType> wrapper = new LambdaQueryWrapper<HouseType>()
                .eq(HouseType::getName, houseType.getName())
                .ne(HouseType::getId, houseType.getId());
        if (count(wrapper) > 0) {
            return Result.error("-1", "房屋类型名称已存在");
        }
        if (!updateById(houseType)) {
            return Result.error("-1", "房屋类型不存在");
        }
        return Result.success();
    }

    @Transactional
    public Result<?> delete(Long id) {
        LOGGER.info("Deleting house type: {}", id);
        // 检查是否有房屋使用此类型
        if (houseMapper.selectCount(new LambdaQueryWrapper<House>()
                .eq(House::getHouseTypeId, id)) > 0) {
            return Result.error("-1", "该房屋类型已被使用，无法删除");
        }
        if (!removeById(id)) {
            return Result.error("-1", "房屋类型不存在");
        }
        return Result.success();
    }

    @Transactional
    public Result<?> batchDelete(List<Long> ids) {
        LOGGER.info("Batch deleting house types: {}", ids);
        if (ids == null || ids.isEmpty()) {
            return Result.error("-1", "请选择要删除的记录");
        }
        
        // 检查是否有房屋使用这些类型
        if (houseMapper.selectCount(new LambdaQueryWrapper<House>()
                .in(House::getHouseTypeId, ids)) > 0) {
            return Result.error("-1", "选中的房屋类型中存在已被使用的类型，无法删除");
        }
        


        removeBatchByIds(ids);
        return Result.success();
    }
} 