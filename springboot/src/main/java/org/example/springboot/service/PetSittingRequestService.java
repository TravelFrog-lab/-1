package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.entity.Owner;
import org.example.springboot.entity.PetSitter;
import org.example.springboot.entity.PetSittingRequest;
import org.example.springboot.entity.User;
import org.example.springboot.mapper.OwnerMapper;
import org.example.springboot.mapper.PetSitterMapper;
import org.example.springboot.mapper.PetSittingRequestMapper;
import org.example.springboot.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 宠物代喂服务请求管理服务
 */
@Service
public class PetSittingRequestService extends ServiceImpl<PetSittingRequestMapper, PetSittingRequest> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(PetSittingRequestService.class);
    
    @Resource
    private PetSittingRequestMapper petSittingRequestMapper;
    
    @Resource
    private OwnerMapper ownerMapper;
    @Resource
    private UserMapper userMapper;
    
    @Resource
    private PetSitterMapper petSitterMapper;

    /**
     * 分页查询代喂服务请求列表
     *
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param ownerId 业主ID（可选）
     * @param sitterId 代喂员ID（可选）
     * @param status 状态（可选）
     * @return 分页结果
     */
    public Result<Page<PetSittingRequest>> list(Integer pageNum, Integer pageSize, Long ownerId, Long sitterId, PetSittingRequest.Status status,String ownerName,String sitterName) {
        LOGGER.info("Listing pet sitting requests, pageNum: {}, pageSize: {}, ownerId: {}, sitterId: {}, status: {}", 
                pageNum, pageSize, ownerId, sitterId, status);
        
        LambdaQueryWrapper<PetSittingRequest> wrapper = new LambdaQueryWrapper<>();

        if(StringUtils.isNotBlank(ownerName)){
            List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>().like(User::getName, ownerName));
            List<Long> userIds = users.stream().map(User::getId).collect(Collectors.toList());
            if(!userIds.isEmpty()){
              
                List<Owner> owners = ownerMapper.selectList(new LambdaQueryWrapper<Owner>().in(Owner::getUserId, userIds));
                List<Long> ownerIds = owners.stream().map(Owner::getId).collect(Collectors.toList());
                if(!ownerIds.isEmpty()){
              

                    wrapper.in(PetSittingRequest::getOwnerId, ownerIds);
                }else{
                    return Result.success(new Page<PetSittingRequest>(pageNum, pageSize));
                }
            }else{
                return Result.success(new Page<PetSittingRequest>(pageNum, pageSize));
            }
        }

        if(StringUtils.isNotBlank(sitterName)){
            List<PetSitter> petSitters = petSitterMapper.selectList(new LambdaQueryWrapper<PetSitter>().like(PetSitter::getName, sitterName));
            List<Long> sitterIds = petSitters.stream().map(PetSitter::getId).collect(Collectors.toList());  
            if(sitterIds != null && !sitterIds.isEmpty()){
                wrapper.in(PetSittingRequest::getSitterId, sitterIds);
            }else{
                return Result.success(new Page<PetSittingRequest>(pageNum, pageSize));
            }
        }





        if (ownerId != null) {
            wrapper.eq(PetSittingRequest::getOwnerId, ownerId);
        }

        if (sitterId != null) {
            wrapper.eq(PetSittingRequest::getSitterId, sitterId);
        }
        if (status != null) {
            wrapper.eq(PetSittingRequest::getStatus, status);
        }
        wrapper.orderByDesc(PetSittingRequest::getCreatedAt);
        
        Page<PetSittingRequest> page = new Page<>(pageNum, pageSize);
        Page<PetSittingRequest> resultPage = petSittingRequestMapper.selectPage(page, wrapper);
        // 填充关联信息
        resultPage.getRecords().forEach(this::fillAssociatedInfo);
        return Result.success(resultPage);
    }

    /**
     * 填充请求关联的业主和代喂员信息
     */
    private void fillAssociatedInfo(PetSittingRequest request) {
        Owner owner = ownerMapper.selectById(request.getOwnerId());

        if(owner!=null){
            Long userId = owner.getUserId();
            User user = userMapper.selectById(userId);
            owner.setUser(user);
            request.setOwner(owner);
        }else{
            User nullUser = new User();
            nullUser.setName("未知用户");
            Owner nullOwner = new Owner();
            nullOwner.setUser(nullUser);
            request.setOwner(nullOwner);
        }


        PetSitter sitter = petSitterMapper.selectById(request.getSitterId());
        request.setSitter(sitter);
    }

    /**
     * 根据ID查询代喂服务请求
     *
     * @param id 请求ID
     * @return 请求详情
     */
    public Result<PetSittingRequest> getById(Long id) {
        LOGGER.info("Getting pet sitting request by id: {}", id);
        PetSittingRequest request = petSittingRequestMapper.selectById(id);
        if (request == null) {
            return Result.error("-1", "服务请求不存在");
        }
        fillAssociatedInfo(request);
        return Result.success(request);
    }

    /**
     * 添加代喂服务请求
     *
     * @param request 请求信息
     * @return 操作结果
     */
    @Transactional
    public Result<?> add(PetSittingRequest request) {
        LOGGER.info("Adding new pet sitting request: {}", request);
        // 检查业主是否存在
        Owner owner = ownerMapper.selectById(request.getOwnerId());
        if (owner == null) {
            return Result.error("-1", "业主不存在");
        }
        
        // 检查代喂员是否存在且状态是否可用
        PetSitter sitter = petSitterMapper.selectById(request.getSitterId());
        if (sitter == null) {
            return Result.error("-1", "代喂员不存在");
        }
        if (sitter.getStatus() != PetSitter.Status.ACTIVE) {
            return Result.error("-1", "该代喂员当前不可用");
        }
        
        // 设置初始状态
        request.setStatus(PetSittingRequest.Status.PENDING);
        request.setCreatedAt(LocalDateTime.now());
        
        petSittingRequestMapper.insert(request);
        return Result.success();
    }

    /**
     * 更新代喂服务请求
     *
     * @param request 请求信息
     * @return 操作结果
     */
    @Transactional
    public Result<?> update(PetSittingRequest request) {
        LOGGER.info("Updating pet sitting request: {}", request);
        PetSittingRequest original = petSittingRequestMapper.selectById(request.getId());
        if (original == null) {
            return Result.error("-1", "服务请求不存在");
        }
        
        // 检查状态变更是否合法
        if (!isValidStatusTransition(original.getStatus(), request.getStatus())) {
            return Result.error("-1", "非法的状态变更");
        }
        
        if (petSittingRequestMapper.updateById(request) <= 0) {
            return Result.error("-1", "服务请求不存在");
        }
        return Result.success();
    }

    /**
     * 检查状态变更是否合法
     * PENDING -> CONTACTED/CANCELLED
     * CONTACTED -> COMPLETED/CANCELLED
     * COMPLETED/CANCELLED -> 不可变更
     *
     * @param from 原状态
     * @param to 目标状态
     * @return 是否合法
     */
    private boolean isValidStatusTransition(PetSittingRequest.Status from, PetSittingRequest.Status to) {
        if (from == to) {
            return true;
        }

        return switch (from) {
            case PENDING -> to == PetSittingRequest.Status.CONTACTED || to == PetSittingRequest.Status.CANCELLED;
            case CONTACTED -> to == PetSittingRequest.Status.COMPLETED || to == PetSittingRequest.Status.CANCELLED;
            case COMPLETED, CANCELLED -> false;
            default -> false;
        };
    }

    /**
     * 删除代喂服务请求
     * 只能删除已完成或已取消的请求
     *
     * @param id 请求ID
     * @return 操作结果
     */
    @Transactional
    public Result<?> delete(Long id) {
        LOGGER.info("Deleting pet sitting request: {}", id);
        PetSittingRequest request = petSittingRequestMapper.selectById(id);
        if (request == null) {
            return Result.error("-1", "服务请求不存在");
        }
        
        // 只能删除已完成或已取消的请求
        if (request.getStatus() != PetSittingRequest.Status.COMPLETED
                && request.getStatus() != PetSittingRequest.Status.CANCELLED) {
            return Result.error("-1", "只能删除已完成或已取消的服务请求");
        }

        if (petSittingRequestMapper.deleteById(id) <= 0) {
            return Result.error("-1", "服务请求不存在");
        }
        return Result.success();
    }

    /**
     * 批量删除代喂服务请求
     * 只能删除已完成或已取消的请求
     *
     * @param ids 请求ID列表
     * @return 操作结果
     */
    @Transactional
    public Result<?> batchDelete(List<Long> ids) {
        LOGGER.info("Batch deleting pet sitting requests: {}", ids);
        if (ids == null || ids.isEmpty()) {
            return Result.error("-1", "请选择要删除的记录");
        }
        
        // 检查是否都是已完成或已取消的请求
        if (petSittingRequestMapper.selectCount(new LambdaQueryWrapper<PetSittingRequest>()
                .in(PetSittingRequest::getId, ids)
                .notIn(PetSittingRequest::getStatus, 
                    PetSittingRequest.Status.COMPLETED,
                    PetSittingRequest.Status.CANCELLED)) > 0) {
            return Result.error("-1", "只能删除已完成或已取消的服务请求");
        }

        petSittingRequestMapper.deleteBatchIds(ids);
        return Result.success();
    }
} 