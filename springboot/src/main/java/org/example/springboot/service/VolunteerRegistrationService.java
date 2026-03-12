package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.entity.Owner;
import org.example.springboot.entity.User;
import org.example.springboot.entity.VolunteerActivity;
import org.example.springboot.entity.VolunteerRegistration;
import org.example.springboot.mapper.OwnerMapper;
import org.example.springboot.mapper.UserMapper;
import org.example.springboot.mapper.VolunteerActivityMapper;
import org.example.springboot.mapper.VolunteerRegistrationMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 志愿活动报名管理服务
 */
@Service
public class VolunteerRegistrationService extends ServiceImpl<VolunteerRegistrationMapper, VolunteerRegistration> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(VolunteerRegistrationService.class);
    
    @Resource
    private VolunteerRegistrationMapper volunteerRegistrationMapper;
    
    @Resource
    private VolunteerActivityMapper volunteerActivityMapper;
    
    @Resource
    private OwnerMapper ownerMapper;

    @Resource
    private UserMapper userMapper;

    /**
     * 分页查询报名记录
     *
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param name 活动名称（可选）
     * @param volunteerId 志愿者ID（可选）
     * @return 分页结果
     */
    public Result<Page<VolunteerRegistration>> list(Integer pageNum, Integer pageSize, String name, Long volunteerId, String status, String volunteerName) {
        LOGGER.info("Listing volunteer registrations, pageNum: {}, pageSize: {}, name: {}, volunteerId: {}, status: {}, volunteerName: {}",
                pageNum, pageSize, name, volunteerId, status, volunteerName);
        


        LambdaQueryWrapper<VolunteerRegistration> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(name)) {
            List<VolunteerActivity> activities = volunteerActivityMapper.selectList(
                new LambdaQueryWrapper<VolunteerActivity>().like(VolunteerActivity::getName, name));
            List<Long> activityIds = activities.stream()
                .map(VolunteerActivity::getId)
                .collect(Collectors.toList());
            
            // 如果没有找到匹配的活动，返回空结果而不是执行可能导致SQL错误的查询
            if (activityIds.isEmpty()) {
                return Result.success(new Page<>(pageNum, pageSize));
            }
            wrapper.in(VolunteerRegistration::getActivityId, activityIds);
        }

        if (StringUtils.isNotBlank(volunteerName)) {
            List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>().like(User::getName, volunteerName));
            List<Long> userIds = users.stream().map(User::getId).collect(Collectors.toList());
            if(userIds!=null){
               List<Owner> owners = ownerMapper.selectList(new LambdaQueryWrapper<Owner>().in(Owner::getUserId, userIds));
               List<Long> ownerIds = owners.stream().map(Owner::getId).collect(Collectors.toList());
               if(ownerIds!=null){
                wrapper.in(VolunteerRegistration::getVolunteerId, ownerIds);
               }else{
                return Result.success(new Page<>(pageNum, pageSize));
               }
            }else{
                return Result.success(new Page<>(pageNum, pageSize));
            }

        }



        if (volunteerId != null) {
            wrapper.eq(VolunteerRegistration::getVolunteerId, volunteerId);
        }
        if (StringUtils.isNotBlank(status)) {
            wrapper.eq(VolunteerRegistration::getStatus, status);
        }
        wrapper.orderByDesc(VolunteerRegistration::getCreatedAt);
        

        Page<VolunteerRegistration> page = new Page<>(pageNum, pageSize);
        Page<VolunteerRegistration> resultPage = volunteerRegistrationMapper.selectPage(page, wrapper);
        
        // 填充关联信息
        resultPage.getRecords().forEach(this::fillAssociatedInfo);
        return Result.success(resultPage);
    }

    /**
     * 填充报名记录关联的活动和志愿者信息
     */
    private void fillAssociatedInfo(VolunteerRegistration registration) {
        VolunteerActivity activity = volunteerActivityMapper.selectById(registration.getActivityId());
        registration.setActivity(activity);
        
        Owner volunteer = ownerMapper.selectById(registration.getVolunteerId());
        if(volunteer!=null){
            Long userId = volunteer.getUserId();
            User user = userMapper.selectById(userId);
            volunteer.setUser(user);
            registration.setVolunteer(volunteer);
        }else{
            User nullUser = new User();
            nullUser.setName("未知用户");
            Owner nullVolunteer = new Owner();
            nullVolunteer.setUser(nullUser);
            registration.setVolunteer(nullVolunteer);
        }

        registration.setVolunteer(volunteer);
    }

    /**
     * 根据ID查询报名记录
     *
     * @param id 记录ID
     * @return 记录详情
     */
    public Result<VolunteerRegistration> getById(Long id) {
        LOGGER.info("Getting volunteer registration by id: {}", id);
        VolunteerRegistration registration = volunteerRegistrationMapper.selectById(id);
        if (registration == null) {
            return Result.error("-1", "报名记录不存在");
        }
        fillAssociatedInfo(registration);
        return Result.success(registration);
    }

    /**
     * 签到
     *
     * @param id 记录ID
     * @return 操作结果
     */
    @Transactional
    public Result<?> checkIn(Long id) {
        LOGGER.info("Checking in volunteer registration: {}", id);
        VolunteerRegistration registration = volunteerRegistrationMapper.selectById(id);
        if (registration == null) {
            return Result.error("-1", "报名记录不存在");
        }
        
        // 检查活动是否存在
        VolunteerActivity activity = volunteerActivityMapper.selectById(registration.getActivityId());
        if (activity == null) {
            return Result.error("-1", "志愿活动不存在");
        }
        
        // 检查是否在活动时间内
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(activity.getStartTime()) || now.isAfter(activity.getEndTime())) {
            return Result.error("-1", "不在活动时间内，无法签到");
        }
        
        // 检查是否已签到
        if (registration.getStatus() == VolunteerRegistration.Status.CHECKED_IN) {
            return Result.error("-1", "已签到，请勿重复操作");
        }
        
        // 更新状态
        registration.setStatus(VolunteerRegistration.Status.CHECKED_IN);
        registration.setUpdatedAt(LocalDateTime.now());
        
        volunteerRegistrationMapper.updateById(registration);
        return Result.success();
    }

    /**
     * 取消报名
     *
     * @param id 记录ID
     * @return 操作结果
     */
    @Transactional
    public Result<?> cancel(Long id) {
        LOGGER.info("Canceling volunteer registration: {}", id);
        VolunteerRegistration registration = volunteerRegistrationMapper.selectById(id);
        if (registration == null) {
            return Result.error("-1", "报名记录不存在");
        }
        
        // 检查活动是否存在
        VolunteerActivity activity = volunteerActivityMapper.selectById(registration.getActivityId());
        if (activity == null) {
            return Result.error("-1", "志愿活动不存在");
        }
        
        // 检查活动是否已开始
        if (activity.getStartTime().isBefore(LocalDateTime.now())) {
            return Result.error("-1", "活动已开始，无法取消报名");
        }
        
        // 删除报名记录
        if (volunteerRegistrationMapper.deleteById(id) <= 0) {
            return Result.error("-1", "报名记录不存在");
        }
        
        // 更新活动报名人数
        activity.setCurrentParticipants(activity.getCurrentParticipants() - 1);
        activity.setUpdatedAt(LocalDateTime.now());
        volunteerActivityMapper.updateById(activity);
        
        return Result.success();
    }
} 