package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.entity.Owner;
import org.example.springboot.entity.ParkingSpace;
import org.example.springboot.entity.PrivateParking;
import org.example.springboot.entity.User;
import org.example.springboot.mapper.OwnerMapper;
import org.example.springboot.mapper.ParkingSpaceMapper;
import org.example.springboot.mapper.PrivateParkingMapper;
import org.example.springboot.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 私人车位管理服务
 */
@Service
public class PrivateParkingService extends ServiceImpl<PrivateParkingMapper, PrivateParking> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(PrivateParkingService.class);
    
    @Resource
    private PrivateParkingMapper privateParkingMapper;
    
    @Resource
    private ParkingSpaceMapper parkingSpaceMapper;
    
    @Resource
    private OwnerMapper ownerMapper;

    @Resource
    private UserMapper userMapper;

    /**
     * 分页查询私人车位使用记录
     *
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param plateInfo 车牌信息（可选）
     * @return 分页结果
     */
    public Page<PrivateParking> list(Integer pageNum, Integer pageSize, String plateInfo,String owerId,String ownerName,String location) {
        LOGGER.info("Listing private parking records, pageNum: {}, pageSize: {}, plateInfo: {},owerId: {},ownerName: {},location: {}", 
                pageNum, pageSize, plateInfo,owerId,ownerName,location);
        




        LambdaQueryWrapper<PrivateParking> wrapper = new LambdaQueryWrapper<>();
        if(StringUtils.isNotBlank(location)){
            // 查询parkingSpace表，获取parkingSpaceId,根据parkingSpaceId查询privateParking表，获取privateParking记录
            List<ParkingSpace> parkingSpaces = parkingSpaceMapper.selectList(new LambdaQueryWrapper<ParkingSpace>().eq(ParkingSpace::getLocation, location));
            if(!parkingSpaces.isEmpty()){
                List<Long> parkingSpaceIds = parkingSpaces.stream().map(ParkingSpace::getId).collect(Collectors.toList());
                if(!parkingSpaceIds.isEmpty()){
                    wrapper.in(PrivateParking::getParkingSpaceId, parkingSpaceIds);
                }else{
                    return new Page<PrivateParking>(pageNum, pageSize);
                }


            }else{
                return new Page<PrivateParking>(pageNum, pageSize);
            }
        }

            if(StringUtils.isNotBlank(ownerName)){

                // 查询user表，获取userId,根据userId查询owner表，获取ownerId，根据ownerId查询privateParking表，获取privateParking记录
                List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>().eq(User::getName, ownerName));
                if(!users.isEmpty()){

                    List<Long> userIds = users.stream().map(User::getId).collect(Collectors.toList());
                    if(!userIds.isEmpty()){
                    List<Owner> owners = ownerMapper.selectList(new LambdaQueryWrapper<Owner>().in(Owner::getUserId, userIds));
                    if(!owners.isEmpty()){
                        List<Long> ownerIds = owners.stream().map(Owner::getId).collect(Collectors.toList());
                        if(!ownerIds.isEmpty()){
                            wrapper.in(PrivateParking::getOwnerId, ownerIds);
                            }else{
                                return new Page<PrivateParking>(pageNum, pageSize);
                            }
                        }else{
                            return new Page<PrivateParking>(pageNum, pageSize);
                        }

                    }else{
                        return new Page<PrivateParking>(pageNum, pageSize);
                    }
                }else{
                    return new Page<PrivateParking>(pageNum, pageSize);
                }
            }







        if(StringUtils.isNotBlank(owerId)){
            wrapper.eq(PrivateParking::getOwnerId, owerId);
        }

        if (plateInfo != null && !plateInfo.trim().isEmpty()) {
            wrapper.like(PrivateParking::getPlateInfo, plateInfo);
        }

        wrapper.orderByDesc(PrivateParking::getCreatedAt);
        
        Page<PrivateParking> page = new Page<>(pageNum, pageSize);
        Page<PrivateParking> resultPage = privateParkingMapper.selectPage(page, wrapper);
        // 填充关联信息
        resultPage.getRecords().forEach(this::fillAssociatedInfo);
        return resultPage;
    }


    /**
     * 填充车位使用记录关联的车位和业主信息
     */
    private void fillAssociatedInfo(PrivateParking privateParking) {
        ParkingSpace parkingSpace = parkingSpaceMapper.selectById(privateParking.getParkingSpaceId());
        privateParking.setParkingSpace(parkingSpace);
        Owner owner = ownerMapper.selectById(privateParking.getOwnerId());
        if(owner!=null){
            Long userId = owner.getUserId();
            User user = userMapper.selectById(userId);
            owner.setUser(user);
            privateParking.setOwner(owner);
        }else{
            User nullUser = new User();
            nullUser.setName("未知用户");
            Owner nullOwner = new Owner();
            nullOwner.setUser(nullUser);
            privateParking.setOwner(nullOwner);
        }

    }

    /**
     * 根据ID查询私人车位使用记录
     *
     * @param id 记录ID
     * @return 记录详情
     */
    public Result<PrivateParking> getById(Long id) {
        LOGGER.info("Getting private parking record by id: {}", id);
        PrivateParking privateParking = privateParkingMapper.selectById(id);
        if (privateParking == null) {
            return Result.error("-1", "私人车位使用记录不存在");
        }
        fillAssociatedInfo(privateParking);
        return Result.success(privateParking);
    }

    /**
     * 添加私人车位使用记录
     *
     * @param privateParking 使用记录信息
     * @return 操作结果
     */
    @Transactional
    public Result<?> add(PrivateParking privateParking) {
        LOGGER.info("Adding new private parking record: {}", privateParking);
        // 检查车位是否存在且是私人车位
        ParkingSpace parkingSpace = parkingSpaceMapper.selectById(privateParking.getParkingSpaceId());
        if (parkingSpace == null) {
            return Result.error("-1", "车位不存在");
        }
        if (parkingSpace.getType() != ParkingSpace.Type.PRIVATE) {
            return Result.error("-1", "只能分配私人车位");
        }
        
        // 检查车位是否已被占用
        LambdaQueryWrapper<PrivateParking> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PrivateParking::getParkingSpaceId, privateParking.getParkingSpaceId());
        if (privateParkingMapper.selectCount(wrapper) > 0) {
            return Result.error("-1", "该车位已被分配");
        }
        
        // 检查业主是否存在
        Owner owner = ownerMapper.selectById(privateParking.getOwnerId());
        if (owner == null) {
            return Result.error("-1", "业主不存在");
        }
        

        privateParkingMapper.insert(privateParking);
        return Result.success();
    }

    /**
     * 更新私人车位使用记录
     *
     * @param privateParking 使用记录信息
     * @return 操作结果
     */
    @Transactional
    public Result<?> update(PrivateParking privateParking) {
        LOGGER.info("Updating private parking record: {}", privateParking);
        PrivateParking original = privateParkingMapper.selectById(privateParking.getId());
        if (original == null) {
            return Result.error("-1", "私人车位使用记录不存在");
        }
        
        // 如果更改了车位，检查新车位是否可用
        if (!original.getParkingSpaceId().equals(privateParking.getParkingSpaceId())) {
            ParkingSpace parkingSpace = parkingSpaceMapper.selectById(privateParking.getParkingSpaceId());
            if (parkingSpace == null) {
                return Result.error("-1", "车位不存在");
            }
            if (parkingSpace.getType() != ParkingSpace.Type.PRIVATE) {
                return Result.error("-1", "只能分配私人车位");
            }
            
            // 检查新车位是否已被占用
            LambdaQueryWrapper<PrivateParking> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(PrivateParking::getParkingSpaceId, privateParking.getParkingSpaceId())
                   .ne(PrivateParking::getId, privateParking.getId());  // 排除当前记录
            if (privateParkingMapper.selectCount(wrapper) > 0) {
                return Result.error("-1", "该车位已被分配");
            }
        }
        
        if (privateParkingMapper.updateById(privateParking) <= 0) {
            return Result.error("-1", "私人车位使用记录不存在");
        }
        return Result.success();
    }

    /**
     * 删除私人车位使用记录
     * 只能删除已退出的记录
     *
     * @param id 记录ID
     * @return 操作结果
     */
    @Transactional
    public Result<?> delete(Long id) {
        LOGGER.info("Deleting private parking record: {}", id);
        PrivateParking privateParking = privateParkingMapper.selectById(id);
        if (privateParking == null) {
            return Result.error("-1", "私人车位使用记录不存在");
        }
        


        if (privateParkingMapper.deleteById(id) <= 0) {
            return Result.error("-1", "私人车位使用记录不存在");
        }
        return Result.success();
    }

    /**
     * 批量删除私人车位使用记录
     * 只能删除已退出的记录
     *
     * @param ids 记录ID列表
     * @return 操作结果
     */
    @Transactional
    public Result<?> batchDelete(List<Long> ids) {
        LOGGER.info("Batch deleting private parking records: {}", ids);
        if (ids == null || ids.isEmpty()) {
            return Result.error("-1", "请选择要删除的记录");
        }
        


        privateParkingMapper.deleteBatchIds(ids);
        return Result.success();
    }
} 