package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.entity.PetSitter;
import org.example.springboot.entity.PetSittingRequest;
import org.example.springboot.mapper.PetSitterMapper;
import org.example.springboot.mapper.PetSittingRequestMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PetSitterService extends ServiceImpl<PetSitterMapper, PetSitter> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(PetSitterService.class);
    
    @Resource
    private PetSitterMapper petSitterMapper;
    
    @Resource
    private PetSittingRequestMapper petSittingRequestMapper;

    public Page<PetSitter> list(Integer pageNum, Integer pageSize, String name, PetSitter.Status status) {
        LOGGER.info("Listing pet sitters, pageNum: {}, pageSize: {}, name: {}, status: {}", 
                pageNum, pageSize, name, status);
        

        LambdaQueryWrapper<PetSitter> wrapper = new LambdaQueryWrapper<>();
        if (name != null && !name.trim().isEmpty()) {
            wrapper.like(PetSitter::getName, name);
        }
        if (status != null) {
            wrapper.eq(PetSitter::getStatus, status);
        }
        wrapper.orderByDesc(PetSitter::getCreatedAt);
        
        Page<PetSitter> page = new Page<>(pageNum, pageSize);
        return petSitterMapper.selectPage(page, wrapper);

    }

    public Result<PetSitter> getById(Long id) {
        LOGGER.info("Getting pet sitter by id: {}", id);
        PetSitter sitter = petSitterMapper.selectById(id);
        if (sitter == null) {
            return Result.error("-1", "宠物代喂员不存在");
        }
        return Result.success(sitter);
    }

    @Transactional
    public Result<?> add(PetSitter sitter) {
        LOGGER.info("Adding new pet sitter: {}", sitter);
        // 检查手机号是否重复
        if (petSitterMapper.selectCount(new LambdaQueryWrapper<PetSitter>()
                .eq(PetSitter::getPhone, sitter.getPhone())) > 0) {
            return Result.error("-1", "手机号已存在");
        }
        
        // 检查身份证号是否重复
        if (petSitterMapper.selectCount(new LambdaQueryWrapper<PetSitter>()
                .eq(PetSitter::getIdCard, sitter.getIdCard())) > 0) {
            return Result.error("-1", "身份证号已存在");
        }
        
        // 设置初始状态
        sitter.setStatus(PetSitter.Status.ACTIVE);
        
        petSitterMapper.insert(sitter);
        return Result.success();
    }

    @Transactional
    public Result<?> update(PetSitter sitter) {
        LOGGER.info("Updating pet sitter: {}", sitter);
        // 检查手机号是否与其他记录重复
        if (petSitterMapper.selectCount(new LambdaQueryWrapper<PetSitter>()
                .eq(PetSitter::getPhone, sitter.getPhone())
                .ne(PetSitter::getId, sitter.getId())) > 0) {
            return Result.error("-1", "手机号已存在");
        }
        
        // 检查身份证号是否与其他记录重复
        if (petSitterMapper.selectCount(new LambdaQueryWrapper<PetSitter>()
                .eq(PetSitter::getIdCard, sitter.getIdCard())
                .ne(PetSitter::getId, sitter.getId())) > 0) {
            return Result.error("-1", "身份证号已存在");
        }
        
        if (petSitterMapper.updateById(sitter) <= 0) {
            return Result.error("-1", "宠物代喂员不存在");
        }
        return Result.success();
    }

    @Transactional
    public Result<?> delete(Long id) {
        LOGGER.info("Deleting pet sitter: {}", id);
        
        // 检查是否有未完成的代喂请求
        if (petSittingRequestMapper.selectCount(new LambdaQueryWrapper<PetSittingRequest>()
                .eq(PetSittingRequest::getSitterId, id)
                .in(PetSittingRequest::getStatus,
                    PetSittingRequest.Status.PENDING,
                    PetSittingRequest.Status.CONTACTED)) > 0) {
            return Result.error("-1", "该代喂员存在未完成的代喂请求，无法删除");
        }

        if (petSitterMapper.deleteById(id) <= 0) {
            return Result.error("-1", "宠物代喂员不存在");
        }
        return Result.success();
    }

    @Transactional
    public Result<?> batchDelete(List<Long> ids) {
        LOGGER.info("Batch deleting pet sitters: {}", ids);
        if (ids == null || ids.isEmpty()) {
            return Result.error("-1", "请选择要删除的记录");
        }
        
        // 检查是否有未完成的代喂请求
        if (petSittingRequestMapper.selectCount(new LambdaQueryWrapper<PetSittingRequest>()
                .in(PetSittingRequest::getSitterId, ids)
                .in(PetSittingRequest::getStatus,
                    PetSittingRequest.Status.PENDING,
                    PetSittingRequest.Status.CONTACTED)) > 0) {
            return Result.error("-1", "选中的代喂员中存在未完成代喂请求的人员，无法删除");
        }

        petSitterMapper.deleteBatchIds(ids);
        return Result.success();
    }

    @Transactional
    public Result<?> updateStatus(Long id, PetSitter.Status status) {
        LOGGER.info("Updating pet sitter status: {} to {}", id, status);
        PetSitter sitter = petSitterMapper.selectById(id);
        if (sitter == null) {
            return Result.error("-1", "宠物代喂员不存在");
        }
        
        // 如果要设置为不可用，检查是否有未完成的代喂请求
        if (status == PetSitter.Status.DISABLED) {
            if (petSittingRequestMapper.selectCount(new LambdaQueryWrapper<PetSittingRequest>()
                    .eq(PetSittingRequest::getSitterId, id)
                    .in(PetSittingRequest::getStatus,
                        PetSittingRequest.Status.PENDING,
                        PetSittingRequest.Status.CONTACTED)) > 0) {
                return Result.error("-1", "该代喂员存在未完成的代喂请求，无法设置为不可用");
            }
        }
        
        sitter.setStatus(status);
        petSitterMapper.updateById(sitter);
        
        return Result.success();
    }
} 