package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.entity.Owner;
import org.example.springboot.entity.VolunteerActivity;
import org.example.springboot.entity.VolunteerRegistration;
import org.example.springboot.mapper.OwnerMapper;
import org.example.springboot.mapper.VolunteerActivityMapper;
import org.example.springboot.mapper.VolunteerRegistrationMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 志愿活动管理服务
 */
@Service
public class VolunteerActivityService extends ServiceImpl<VolunteerActivityMapper, VolunteerActivity> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(VolunteerActivityService.class);
    
    @Resource
    private VolunteerActivityMapper volunteerActivityMapper;
    
    @Resource
    private VolunteerRegistrationMapper volunteerRegistrationMapper;
    
    @Resource
    private OwnerMapper ownerMapper;

    /**
     * 分页查询志愿活动列表
     *
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param name 活动名称（可选）
     * @return 分页结果
     */
    public Result<Page<VolunteerActivity>> list(Integer pageNum, Integer pageSize, String name) {
        LOGGER.info("Listing volunteer activities, pageNum: {}, pageSize: {}, name: {}", 
                pageNum, pageSize, name);
        
        LambdaQueryWrapper<VolunteerActivity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(name)) {
            wrapper.like(VolunteerActivity::getName, name);
        }
        wrapper.orderByDesc(VolunteerActivity::getCreatedAt);
        
        Page<VolunteerActivity> page = new Page<>(pageNum, pageSize);
        return Result.success(volunteerActivityMapper.selectPage(page, wrapper));
    }

    /**
     * 根据ID查询志愿活动
     *
     * @param id 活动ID
     * @return 活动详情
     */
    public Result<VolunteerActivity> getById(Long id) {
        LOGGER.info("Getting volunteer activity by id: {}", id);
        VolunteerActivity activity = volunteerActivityMapper.selectById(id);
        if (activity == null) {
            return Result.error("-1", "志愿活动不存在");
        }
        return Result.success(activity);
    }

    /**
     * 添加志愿活动
     *
     * @param activity 活动信息
     * @return 操作结果
     */
    @Transactional
    public Result<?> add(VolunteerActivity activity) {
        LOGGER.info("Adding new volunteer activity: {}", activity);
        
        // 检查时间是否合法
        if (activity.getStartTime().isAfter(activity.getEndTime())) {
            return Result.error("-1", "开始时间不能晚于结束时间");
        }
        
        // 检查人数限制是否合法
        if (activity.getMaxParticipants() <= 0) {
            return Result.error("-1", "最大参与人数必须大于0");
        }
        
        activity.setCurrentParticipants(0);
        activity.setCreatedAt(LocalDateTime.now());
        activity.setUpdatedAt(LocalDateTime.now());
        
        volunteerActivityMapper.insert(activity);
        return Result.success();
    }

    /**
     * 更新志愿活动
     *
     * @param activity 活动信息
     * @return 操作结果
     */
    @Transactional
    public Result<?> update(VolunteerActivity activity) {
        LOGGER.info("Updating volunteer activity: {}", activity);
        VolunteerActivity original = volunteerActivityMapper.selectById(activity.getId());
        if (original == null) {
            return Result.error("-1", "志愿活动不存在");
        }
        
        // 检查时间是否合法
        if (activity.getStartTime().isAfter(activity.getEndTime())) {
            return Result.error("-1", "开始时间不能晚于结束时间");
        }
        
        // 检查人数限制是否合法
        if (activity.getMaxParticipants() < original.getCurrentParticipants()) {
            return Result.error("-1", "最大参与人数不能小于当前报名人数");
        }
        
        activity.setUpdatedAt(LocalDateTime.now());
        
        if (volunteerActivityMapper.updateById(activity) <= 0) {
            return Result.error("-1", "志愿活动不存在");
        }
        return Result.success();
    }

    /**
     * 删除志愿活动
     * 只能删除未开始或已结束的活动
     *
     * @param id 活动ID
     * @return 操作结果
     */
    @Transactional
    public Result<?> delete(Long id) {
        LOGGER.info("Deleting volunteer activity: {}", id);
        VolunteerActivity activity = volunteerActivityMapper.selectById(id);
        if (activity == null) {
            return Result.error("-1", "志愿活动不存在");
        }
        
        LocalDateTime now = LocalDateTime.now();
        // 检查活动是否在进行中
        if (now.isAfter(activity.getStartTime()) && now.isBefore(activity.getEndTime())) {
            return Result.error("-1", "活动正在进行中，无法删除");
        }

        // 删除相关的报名记录
        volunteerRegistrationMapper.delete(new LambdaQueryWrapper<VolunteerRegistration>()
                .eq(VolunteerRegistration::getActivityId, id));

        if (volunteerActivityMapper.deleteById(id) <= 0) {
            return Result.error("-1", "志愿活动不存在");
        }
        return Result.success();
    }

    /**
     * 批量删除志愿活动
     * 只能删除未开始或已结束的活动
     *
     * @param ids 活动ID列表
     * @return 操作结果
     */
    @Transactional
    public Result<?> batchDelete(List<Long> ids) {
        LOGGER.info("Batch deleting volunteer activities: {}", ids);
        if (ids == null || ids.isEmpty()) {
            return Result.error("-1", "请选择要删除的记录");
        }
        
        LocalDateTime now = LocalDateTime.now();
        // 检查是否有正在进行的活动
        if (volunteerActivityMapper.selectCount(new LambdaQueryWrapper<VolunteerActivity>()
                .in(VolunteerActivity::getId, ids)
                .lt(VolunteerActivity::getStartTime, now)
                .gt(VolunteerActivity::getEndTime, now)) > 0) {
            return Result.error("-1", "选中的活动中存在正在进行的活动，无法删除");
        }

        // 删除相关的报名记录
        volunteerRegistrationMapper.delete(new LambdaQueryWrapper<VolunteerRegistration>()
                .in(VolunteerRegistration::getActivityId, ids));

        volunteerActivityMapper.deleteBatchIds(ids);
        return Result.success();
    }

    /**
     * 报名参加志愿活动
     *
     * @param activityId 活动ID
     * @param volunteerId 志愿者ID
     * @return 操作结果
     */
    @Transactional
    public Result<?> register(Long activityId, Long volunteerId) {
        LOGGER.info("Registering volunteer activity: activityId={}, volunteerId={}", activityId, volunteerId);
        // 检查活动是否存在
        VolunteerActivity activity = volunteerActivityMapper.selectById(activityId);
        if (activity == null) {
            return Result.error("-1", "志愿活动不存在");
        }
        
        // 检查活动是否已开始
        if (activity.getStartTime().isBefore(LocalDateTime.now())) {
            return Result.error("-1", "活动已开始，无法报名");
        }
        
        // 检查是否已达到人数上限
        if (activity.getCurrentParticipants() >= activity.getMaxParticipants()) {
            return Result.error("-1", "活动报名人数已达上限");
        }
        
        // 检查志愿者是否存在
        Owner volunteer = ownerMapper.selectById(volunteerId);
        if (volunteer == null) {
            return Result.error("-1", "志愿者不存在");
        }
        
        // 检查是否已报名
        if (volunteerRegistrationMapper.selectCount(new LambdaQueryWrapper<VolunteerRegistration>()
                .eq(VolunteerRegistration::getActivityId, activityId)
                .eq(VolunteerRegistration::getVolunteerId, volunteerId)) > 0) {
            return Result.error("-1", "已报名该活动");
        }
        
        // 创建报名记录
        VolunteerRegistration registration = new VolunteerRegistration();
        registration.setActivityId(activityId);
        registration.setVolunteerId(volunteerId);
        registration.setStatus(VolunteerRegistration.Status.REGISTERED);
        registration.setRegisterTime(LocalDateTime.now());
        registration.setCreatedAt(LocalDateTime.now());
        registration.setUpdatedAt(LocalDateTime.now());
        
        volunteerRegistrationMapper.insert(registration);
        
        // 更新报名人数
        activity.setCurrentParticipants(activity.getCurrentParticipants() + 1);
        activity.setUpdatedAt(LocalDateTime.now());
        volunteerActivityMapper.updateById(activity);
        
        return Result.success();
    }

    /**
     * 取消报名
     *
     * @param activityId 活动ID
     * @param volunteerId 志愿者ID
     * @return 操作结果
     */
    @Transactional
    public Result<?> cancelRegistration(Long activityId, Long volunteerId) {
        LOGGER.info("Canceling volunteer registration: activityId={}, volunteerId={}", activityId, volunteerId);
        // 检查活动是否存在
        VolunteerActivity activity = volunteerActivityMapper.selectById(activityId);
        if (activity == null) {
            return Result.error("-1", "志愿活动不存在");
        }
        
        // 检查活动是否已开始
        if (activity.getStartTime().isBefore(LocalDateTime.now())) {
            return Result.error("-1", "活动已开始，无法取消报名");
        }
        
    //     删除报名记录
       if (volunteerRegistrationMapper.delete(new LambdaQueryWrapper<VolunteerRegistration>()
               .eq(VolunteerRegistration::getActivityId, activityId)
               .eq(VolunteerRegistration::getVolunteerId, volunteerId)) <= 0) {
           return Result.error("-1", "未找到报名记录");
       }

       // 更新报名人数
       activity.setCurrentParticipants(Math.max(0, activity.getCurrentParticipants() - 1));
       activity.setUpdatedAt(LocalDateTime.now());
       volunteerActivityMapper.updateById(activity);
        
        return Result.success();
    }
} 