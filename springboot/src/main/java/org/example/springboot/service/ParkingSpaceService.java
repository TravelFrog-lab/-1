package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.entity.ParkingSpace;
import org.example.springboot.entity.PrivateParking;
import org.example.springboot.entity.ParkingFee;
import org.example.springboot.mapper.ParkingSpaceMapper;
import org.example.springboot.mapper.PrivateParkingMapper;
import org.example.springboot.mapper.ParkingFeeMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ParkingSpaceService extends ServiceImpl<ParkingSpaceMapper, ParkingSpace> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ParkingSpaceService.class);
    
    @Resource
    private ParkingSpaceMapper parkingSpaceMapper;
    
    @Resource
    private PrivateParkingMapper privateParkingMapper;
    
    @Resource
    private ParkingFeeMapper parkingFeeMapper;

    public Page<ParkingSpace> list(Integer pageNum, Integer pageSize, String code, ParkingSpace.Type type,String status) {
        LOGGER.info("Listing parking spaces, pageNum: {}, pageSize: {}, code: {}, type: {}, status: {}", 
                pageNum, pageSize, code, type, status);
        


        LambdaQueryWrapper<ParkingSpace> wrapper = new LambdaQueryWrapper<>();
               if(StringUtils.isNotBlank(status)){
            // status 为 0 表示空闲，为 1 表示占用
            List<Long> ids = parkingSpaceMapper.selectList(null).stream()
                    .map(ParkingSpace::getId)
                    .filter(id -> "1".equals(status) == isOccupied(id))
                    .toList();
            if (!ids.isEmpty()) {
                wrapper.in(ParkingSpace::getId, ids);
            } else {
                // 如果没有符合条件的记录，添加一个不可能的条件
                wrapper.eq(ParkingSpace::getId, -1L);
            }
        }
        if (code != null && !code.trim().isEmpty()) {
            wrapper.like(ParkingSpace::getCode, code);
        }
        if (type != null) {
            wrapper.eq(ParkingSpace::getType, type);
        }
 
        wrapper.orderByDesc(ParkingSpace::getCreatedAt);
        


        Page<ParkingSpace> page = new Page<>(pageNum, pageSize);
        List<ParkingSpace> parkingSpaces = parkingSpaceMapper.selectPage(page, wrapper).getRecords();
        parkingSpaces.forEach(space -> {
            space.setStatus(isOccupied(space.getId()) ? "1" : "0");
        });
        page.setRecords(parkingSpaces);

        return page;


    }


    public Result<ParkingSpace> getById(Long id) {
        LOGGER.info("Getting parking space by id: {}", id);
        ParkingSpace parkingSpace = parkingSpaceMapper.selectById(id);
        if (parkingSpace == null) {
            return Result.error("-1", "车位不存在");
        }

        return Result.success(parkingSpace);
    }
    
    // 判断车位是否占用
    public Boolean isOccupied(Long id) {
        LOGGER.info("Checking if parking space is occupied: {}", id);
        // 查询私人车位
        PrivateParking privateParking = privateParkingMapper.selectOne(new LambdaQueryWrapper<PrivateParking>().eq(PrivateParking::getParkingSpaceId, id));
        if (privateParking != null) {
            LOGGER.info("<占用：>"+id);
            return true;
        }
        // 查询parkingFee，List<ParkingFee>
        List<ParkingFee> parkingFees = parkingFeeMapper.selectList(new LambdaQueryWrapper<ParkingFee>().eq(ParkingFee::getParkingSpaceId, id));
        if (!parkingFees.isEmpty()) {
            // 查询parkingFee的结束时间
            for (ParkingFee parkingFee : parkingFees) {
                if(parkingFee.getExitTime()==null){
//                if (parkingFee.getExitTime().isAfter(LocalDateTime.now())) {
//                    return true;
//                }
                    LOGGER.info("<占用：>"+parkingFee);
                    return true;
                }
            }

        }


        return false;
    }



    @Transactional

    public Result<?> add(ParkingSpace parkingSpace) {

        LOGGER.info("Adding new parking space: {}", parkingSpace);
        // 检查编号是否重复
        if (parkingSpaceMapper.selectCount(new LambdaQueryWrapper<ParkingSpace>()
                .eq(ParkingSpace::getCode, parkingSpace.getCode())) > 0) {
            return Result.error("-1", "车位编号已存在");
        }
        
        parkingSpaceMapper.insert(parkingSpace);
        return Result.success();
    }

    @Transactional
    public Result<?> update(ParkingSpace parkingSpace) {
        LOGGER.info("Updating parking space: {}", parkingSpace);
        // 检查编号是否与其他记录重复
        LambdaQueryWrapper<ParkingSpace> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ParkingSpace::getCode, parkingSpace.getCode())
               .ne(ParkingSpace::getId, parkingSpace.getId());
        if (parkingSpaceMapper.selectCount(wrapper) > 0) {
            return Result.error("-1", "车位编号已存在");
        }
        
        if (parkingSpaceMapper.updateById(parkingSpace) <= 0) {
            return Result.error("-1", "车位不存在");
        }
        return Result.success();
    }

    @Transactional
    public Result<?> delete(Long id) {
        LOGGER.info("Deleting parking space: {}", id);
        
        // 检查是否有使用记录
        if (privateParkingMapper.selectCount(new LambdaQueryWrapper<PrivateParking>()
                .eq(PrivateParking::getParkingSpaceId, id)) > 0) {
            return Result.error("-1", "该车位存在使用记录，无法删除");
        }
        
        // 检查是否有停车费记录
        if (parkingFeeMapper.selectCount(new LambdaQueryWrapper<ParkingFee>()
                .eq(ParkingFee::getParkingSpaceId, id)) > 0) {
            return Result.error("-1", "该车位存在停车费记录，无法删除");
        }

        if (parkingSpaceMapper.deleteById(id) <= 0) {
            return Result.error("-1", "车位不存在");
        }
        return Result.success();
    }

    @Transactional
    public Result<?> batchDelete(List<Long> ids) {
        LOGGER.info("Batch deleting parking spaces: {}", ids);
        if (ids == null || ids.isEmpty()) {
            return Result.error("-1", "请选择要删除的记录");
        }
        
        // 检查是否有使用记录
        if (privateParkingMapper.selectCount(new LambdaQueryWrapper<PrivateParking>()
                .in(PrivateParking::getParkingSpaceId, ids)) > 0) {
            return Result.error("-1", "选中的车位中存在使用记录的车位，无法删除");
        }
        
        // 检查是否有停车费记录
        if (parkingFeeMapper.selectCount(new LambdaQueryWrapper<ParkingFee>()
                .in(ParkingFee::getParkingSpaceId, ids)) > 0) {
            return Result.error("-1", "选中的车位中存在停车费记录的车位，无法删除");
        }

        parkingSpaceMapper.deleteBatchIds(ids);
        return Result.success();
    }

    public Result<List<ParkingSpace>> listByType(ParkingSpace.Type type) {
        LOGGER.info("Listing parking spaces by type: {}", type);
        if (type == null) {
            return Result.error("-1", "车位类型不能为空");
        }
        
        LambdaQueryWrapper<ParkingSpace> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ParkingSpace::getType, type)
              .orderByDesc(ParkingSpace::getCreatedAt);
        
        List<ParkingSpace> parkingSpaces = parkingSpaceMapper.selectList(wrapper);
        parkingSpaces.forEach(space -> {
            space.setStatus(isOccupied(space.getId()) ? "1" : "0");
        });
        return Result.success(parkingSpaces);
    }
} 