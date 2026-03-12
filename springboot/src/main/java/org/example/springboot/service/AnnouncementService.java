package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.entity.Announcement;
import org.example.springboot.mapper.AnnouncementMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 公告管理服务
 */
@Service
public class AnnouncementService extends ServiceImpl<AnnouncementMapper, Announcement> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(AnnouncementService.class);

    @Resource
    private AnnouncementMapper announcementMapper;

    /**
     * 分页查询公告列表
     *
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param title 公告标题（可选）
     * @param type 公告类型（可选）
     * @return 分页结果
     */
    public Page<Announcement> list(Integer pageNum, Integer pageSize, String title, Announcement.Type type) {
        LOGGER.info("Listing announcements, pageNum: {}, pageSize: {}, title: {}, type: {}", 
                pageNum, pageSize, title, type);


        
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        if(type != null){
            wrapper.eq(Announcement::getType, type);
        }
        if (title != null && !title.trim().isEmpty()) {
            wrapper.like(Announcement::getTitle, title);
        }

        wrapper.orderByDesc(Announcement::getCreatedAt);
        
        Page<Announcement> page = new Page<>(pageNum, pageSize);
        return announcementMapper.selectPage(page, wrapper);

    }

    /**
     * 根据ID查询公告
     *
     * @param id 公告ID
     * @return 公告详情
     */
    public Result<Announcement> getById(Long id) {
        LOGGER.info("Getting announcement by id: {}", id);
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement == null) {
            return Result.error("-1", "公告不存在");
        }
        return Result.success(announcement);
    }

    /**
     * 添加公告
     *
     * @param announcement 公告信息
     * @return 操作结果
     */
    @Transactional
    public Result<?> add(Announcement announcement) {
        LOGGER.info("Adding new announcement: {}", announcement);
        
        // 设置创建和更新时间
        announcement.setCreatedAt(LocalDateTime.now());
        announcement.setUpdatedAt(LocalDateTime.now());
        
        announcementMapper.insert(announcement);
        return Result.success();
    }

    /**
     * 更新公告
     *
     * @param announcement 公告信息
     * @return 操作结果
     */
    @Transactional
    public Result<?> update(Announcement announcement) {
        LOGGER.info("Updating announcement: {}", announcement);
        Announcement original = announcementMapper.selectById(announcement.getId());
        if (original == null) {
            return Result.error("-1", "公告不存在");
        }
        
        // 更新时间
        announcement.setUpdatedAt(LocalDateTime.now());
        
        if (announcementMapper.updateById(announcement) <= 0) {
            return Result.error("-1", "公告不存在");
        }
        return Result.success();
    }

    /**
     * 删除公告
     *
     * @param id 公告ID
     * @return 操作结果
     */
    @Transactional
    public Result<?> delete(Long id) {
        LOGGER.info("Deleting announcement: {}", id);
        if (announcementMapper.deleteById(id) <= 0) {
            return Result.error("-1", "公告不存在");
        }
        return Result.success();
    }

    /**
     * 批量删除公告
     *
     * @param ids 公告ID列表
     * @return 操作结果
     */
    @Transactional
    public Result<?> batchDelete(List<Long> ids) {
        LOGGER.info("Batch deleting announcements: {}", ids);
        if (ids == null || ids.isEmpty()) {
            return Result.error("-1", "请选择要删除的记录");
        }
        
        announcementMapper.deleteByIds(ids);
        return Result.success();
    }

    /**
     * 获取最新的公告列表
     *
     * @param limit 获取条数
     * @return 公告列表
     */
    public Result<List<Announcement>> getLatest(int limit) {
        LOGGER.info("Getting latest {} announcements", limit);
        List<Announcement> announcements = announcementMapper.selectList(
            new LambdaQueryWrapper<Announcement>()
                .orderByDesc(Announcement::getCreatedAt)
                .last("LIMIT " + limit)
        );
        return Result.success(announcements);
    }
} 