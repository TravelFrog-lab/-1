package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.entity.House;
import org.example.springboot.entity.HouseType;
import org.example.springboot.entity.Owner;
import org.example.springboot.entity.RepairRecord;
import org.example.springboot.entity.PropertyFee;
import org.example.springboot.mapper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class HouseService extends ServiceImpl<HouseMapper, House> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(HouseService.class);
    
    @Resource
    private HouseMapper houseMapper;
    
    @Resource
    private HouseTypeMapper houseTypeMapper;
    
    @Resource
    private OwnerMapper ownerMapper;
    
    @Resource
    private RepairRecordMapper repairRecordMapper;
    
    @Resource
    private PropertyFeeMapper propertyFeeMapper;

    public Page<House> list(Integer pageNum, Integer pageSize, String address, House.Status status) {
        LOGGER.info("Listing houses, pageNum: {}, pageSize: {}, address: {}, status: {}", 
                pageNum, pageSize, address, status);
        

        LambdaQueryWrapper<House> wrapper = new LambdaQueryWrapper<>();
        if (address != null && !address.trim().isEmpty()) {
            wrapper.like(House::getAddress, address);
        }
        if (status != null) {
            wrapper.eq(House::getStatus, status);
        }
        wrapper.orderByDesc(House::getCreatedAt);
        
        Page<House> page = new Page<>(pageNum, pageSize);
        Page<House> resultPage = houseMapper.selectPage(page, wrapper);
        // 填充房屋类型信息
        resultPage.getRecords().forEach(house -> {
            HouseType houseType = houseTypeMapper.selectById(house.getHouseTypeId());
            house.setHouseType(houseType);
        });
        return resultPage;
    }


    public Result<House> getById(Long id) {
        LOGGER.info("Getting house by id: {}", id);

        House house = houseMapper.selectById(id);
        if (house == null) {
            return Result.error("-1", "房屋不存在");
        }
        HouseType houseType = houseTypeMapper.selectById(house.getHouseTypeId());
        house.setHouseType(houseType);
        return Result.success(house);
    }

    @Transactional
    public Result<?> add(House house) {
        LOGGER.info("Adding new house: {}", house);
        // 检查房屋类型是否存在
        HouseType houseType = houseTypeMapper.selectById(house.getHouseTypeId());
        if (houseType == null) {
            return Result.error("-1", "房屋类型不存在");
        }
        
        houseMapper.insert(house);
        return Result.success();
    }

    @Transactional
    public Result<?> update(House house) {
        LOGGER.info("Updating house: {}", house);
        // 检查房屋类型是否存在
        HouseType houseType = houseTypeMapper.selectById(house.getHouseTypeId());
        if (houseType == null) {
            return Result.error("-1", "房屋类型不存在");
        }
        
        if (houseMapper.updateById(house) <= 0) {
            return Result.error("-1", "房屋不存在");
        }
        return Result.success();
    }

    @Transactional
    public Result<?> delete(Long id) {
        LOGGER.info("Deleting house: {}", id);
        
        // 检查是否有业主居住
        LambdaQueryWrapper<Owner> ownerWrapper = new LambdaQueryWrapper<>();
        ownerWrapper.eq(Owner::getHouseId, id);
        if (ownerMapper.selectCount(ownerWrapper) > 0) {
            return Result.error("-1", "该房屋已有业主居住，无法删除");
        }
        
        // 检查是否有维修记录
        LambdaQueryWrapper<RepairRecord> repairWrapper = new LambdaQueryWrapper<>();
        repairWrapper.eq(RepairRecord::getHouseId, id);
        if (repairRecordMapper.selectCount(repairWrapper) > 0) {
            return Result.error("-1", "该房屋存在维修记录，无法删除");
        }
        
        // 检查是否有物业费记录
        LambdaQueryWrapper<PropertyFee> feeWrapper = new LambdaQueryWrapper<>();
        feeWrapper.eq(PropertyFee::getHouseId, id);
        if (propertyFeeMapper.selectCount(feeWrapper) > 0) {
            return Result.error("-1", "该房屋存在物业费记录，无法删除");
        }

        if (houseMapper.deleteById(id) <= 0) {
            return Result.error("-1", "房屋不存在");
        }
        return Result.success();
    }

    @Transactional
    public Result<?> batchDelete(List<Long> ids) {
        LOGGER.info("Batch deleting houses: {}", ids);
        if (ids == null || ids.isEmpty()) {
            return Result.error("-1", "请选择要删除的记录");
        }
        
        // 检查是否有业主居住
        LambdaQueryWrapper<Owner> ownerWrapper = new LambdaQueryWrapper<>();
        ownerWrapper.in(Owner::getHouseId, ids);
        if (ownerMapper.selectCount(ownerWrapper) > 0) {
            return Result.error("-1", "选中的房屋中存在已有业主居住的房屋，无法删除");
        }
        
        // 检查是否有维修记录
        LambdaQueryWrapper<RepairRecord> repairWrapper = new LambdaQueryWrapper<>();
        repairWrapper.in(RepairRecord::getHouseId, ids);
        if (repairRecordMapper.selectCount(repairWrapper) > 0) {
            return Result.error("-1", "选中的房屋中存在维修记录的房屋，无法删除");
        }

        houseMapper.deleteBatchIds(ids);
        return Result.success();
    }
} 