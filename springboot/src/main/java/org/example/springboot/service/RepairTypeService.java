package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.entity.RepairRecord;
import org.example.springboot.entity.RepairType;
import org.example.springboot.mapper.RepairRecordMapper;
import org.example.springboot.mapper.RepairTypeMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 维修类型管理服务
 */
@Service
public class RepairTypeService extends ServiceImpl<RepairTypeMapper, RepairType> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(RepairTypeService.class);
    
    @Resource
    private RepairTypeMapper repairTypeMapper;
    
    @Resource
    private RepairRecordMapper repairRecordMapper;

    /**
     * 分页查询维修类型列表
     *
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param name 类型名称（可选）
     * @return 分页结果
     */
    public Page<RepairType> list(Integer pageNum, Integer pageSize, String name) {
        LOGGER.info("Listing repair types, pageNum: {}, pageSize: {}, name: {}", 
                pageNum, pageSize, name);
        

        LambdaQueryWrapper<RepairType> wrapper = new LambdaQueryWrapper<>();
        if (name != null && !name.trim().isEmpty()) {
            wrapper.like(RepairType::getName, name);
        }
        wrapper.orderByDesc(RepairType::getCreatedAt);
        
        Page<RepairType> page = new Page<>(pageNum, pageSize);
        return repairTypeMapper.selectPage(page, wrapper);
    }


    /**
     * 根据ID查询维修类型
     *
     * @param id 类型ID
     * @return 类型详情
     */
    public Result<RepairType> getById(Long id) {
        LOGGER.info("Getting repair type by id: {}", id);
        RepairType repairType = repairTypeMapper.selectById(id);
        if (repairType == null) {
            return Result.error("-1", "维修类型不存在");
        }
        return Result.success(repairType);
    }

    /**
     * 添加维修类型
     *
     * @param repairType 类型信息
     * @return 操作结果
     */
    @Transactional
    public Result<?> add(RepairType repairType) {
        LOGGER.info("Adding new repair type: {}", repairType);
        // 检查名称是否重复
        if (repairTypeMapper.selectCount(new LambdaQueryWrapper<RepairType>()
                .eq(RepairType::getName, repairType.getName())) > 0) {
            return Result.error("-1", "维修类型名称已存在");
        }
        
        repairTypeMapper.insert(repairType);
        return Result.success();
    }

    /**
     * 更新维修类型
     *
     * @param repairType 类型信息
     * @return 操作结果
     */
    @Transactional
    public Result<?> update(RepairType repairType) {
        LOGGER.info("Updating repair type: {}", repairType);
        // 检查名称是否与其他记录重复
        if (repairTypeMapper.selectCount(new LambdaQueryWrapper<RepairType>()
                .eq(RepairType::getName, repairType.getName())
                .ne(RepairType::getId, repairType.getId())) > 0) {
            return Result.error("-1", "维修类型名称已存在");
        }
        
        if (repairTypeMapper.updateById(repairType) <= 0) {
            return Result.error("-1", "维修类型不存在");
        }
        return Result.success();
    }

    /**
     * 删除维修类型
     *
     * @param id 类型ID
     * @return 操作结果
     */
    @Transactional
    public Result<?> delete(Long id) {
        LOGGER.info("Deleting repair type: {}", id);
        // 检查是否有维修记录使用此类型
        if (repairRecordMapper.selectCount(new LambdaQueryWrapper<RepairRecord>()
                .eq(RepairRecord::getRepairTypeId, id)) > 0) {
            return Result.error("-1", "该维修类型已被使用，无法删除");
        }

        if (repairTypeMapper.deleteById(id) <= 0) {
            return Result.error("-1", "维修类型不存在");
        }
        return Result.success();
    }

    /**
     * 批量删除维修类型
     *
     * @param ids 类型ID列表
     * @return 操作结果
     */
    @Transactional
    public Result<?> batchDelete(List<Long> ids) {
        LOGGER.info("Batch deleting repair types: {}", ids);
        if (ids == null || ids.isEmpty()) {
            return Result.error("-1", "请选择要删除的记录");
        }
        
        // 检查是否有维修记录使用这些类型
        if (repairRecordMapper.selectCount(new LambdaQueryWrapper<RepairRecord>()
                .in(RepairRecord::getRepairTypeId, ids)) > 0) {
            return Result.error("-1", "选中的维修类型中存在已被使用的类型，无法删除");
        }

        repairTypeMapper.deleteBatchIds(ids);
        return Result.success();
    }

    /**
     * 获取所有维修类型列表
     *
     * @return 类型列表
     */
    public Result<List<RepairType>> listAll() {
        LOGGER.info("Listing all repair types");
        List<RepairType> types = repairTypeMapper.selectList(
            new LambdaQueryWrapper<RepairType>()
                .orderByDesc(RepairType::getCreatedAt)
        );
        return Result.success(types);
    }
} 