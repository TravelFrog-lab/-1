package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.entity.Carousel;
import org.example.springboot.mapper.CarouselMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 轮播图管理服务
 */
@Service
public class CarouselService extends ServiceImpl<CarouselMapper, Carousel> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CarouselService.class);
    
    @Resource
    private CarouselMapper carouselMapper;

    /**
     * 分页查询轮播图列表
     *
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param title 标题（可选）
     * @param tag 标签（可选）
     * @return 分页结果
     */
    public Result<Page<Carousel>> list(Integer pageNum, Integer pageSize, String title, String tag) {
        LOGGER.info("Listing carousels, pageNum: {}, pageSize: {}, title: {}, tag: {}", 
                pageNum, pageSize, title, tag);
        
        LambdaQueryWrapper<Carousel> wrapper = new LambdaQueryWrapper<>();
        if (title != null && !title.trim().isEmpty()) {
            wrapper.like(Carousel::getTitle, title);
        }
        if (tag != null && !tag.trim().isEmpty()) {
            wrapper.eq(Carousel::getTag, tag);
        }
        wrapper.orderByAsc(Carousel::getSortOrder)
               .orderByDesc(Carousel::getCreatedAt);
        
        Page<Carousel> page = new Page<>(pageNum, pageSize);
        return Result.success(carouselMapper.selectPage(page, wrapper));
    }

    /**
     * 根据ID查询轮播图
     *
     * @param id 轮播图ID
     * @return 轮播图详情
     */
    public Result<Carousel> getById(Long id) {
        LOGGER.info("Getting carousel by id: {}", id);
        Carousel carousel = carouselMapper.selectById(id);
        if (carousel == null) {
            return Result.error("-1", "轮播图不存在");
        }
        return Result.success(carousel);
    }

    /**
     * 添加轮播图
     *
     * @param carousel 轮播图信息
     * @return 操作结果
     */
    @Transactional
    public Result<?> add(Carousel carousel) {
        LOGGER.info("Adding new carousel: {}", carousel);
        
        // 如果未设置排序号，设置为当前最大排序号+1
        if (carousel.getSortOrder() == null) {
            Integer maxSortOrder = getMaxSortOrder();
            carousel.setSortOrder(maxSortOrder + 1);
        }
        
        // 设置创建和更新时间
        carousel.setCreatedAt(LocalDateTime.now());
        carousel.setUpdatedAt(LocalDateTime.now());
        
        carouselMapper.insert(carousel);
        return Result.success();
    }

    /**
     * 更新轮播图
     *
     * @param carousel 轮播图信息
     * @return 操作结果
     */
    @Transactional
    public Result<?> update(Carousel carousel) {
        LOGGER.info("Updating carousel: {}", carousel);
        Carousel original = carouselMapper.selectById(carousel.getId());
        if (original == null) {
            return Result.error("-1", "轮播图不存在");
        }
        
        // 更新时间
        carousel.setUpdatedAt(LocalDateTime.now());
        
        if (carouselMapper.updateById(carousel) <= 0) {
            return Result.error("-1", "轮播图不存在");
        }
        return Result.success();
    }

    /**
     * 删除轮播图
     *
     * @param id 轮播图ID
     * @return 操作结果
     */
    @Transactional
    public Result<?> delete(Long id) {
        LOGGER.info("Deleting carousel: {}", id);
        if (carouselMapper.deleteById(id) <= 0) {
            return Result.error("-1", "轮播图不存在");
        }
        return Result.success();
    }

    /**
     * 批量删除轮播图
     *
     * @param ids 轮播图ID列表
     * @return 操作结果
     */
    @Transactional
    public Result<?> batchDelete(List<Long> ids) {
        LOGGER.info("Batch deleting carousels: {}", ids);
        if (ids == null || ids.isEmpty()) {
            return Result.error("-1", "请选择要删除的记录");
        }
        
        carouselMapper.deleteByIds(ids);
        return Result.success();
    }

    /**
     * 获取当前最大的排序号
     * @return 最大排序号，如果没有记录则返回0
     */
    private Integer getMaxSortOrder() {
        LambdaQueryWrapper<Carousel> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Carousel::getSortOrder)
               .last("LIMIT 1");
        
        Carousel carousel = carouselMapper.selectOne(wrapper);
        return carousel != null ? carousel.getSortOrder() : 0;
    }

    /**
     * 调整轮播图排序
     *
     * @param id 轮播图ID
     * @param sortOrder 新的排序号
     * @return 操作结果
     */
    @Transactional
    public Result<?> updateSort(Long id, Integer sortOrder) {
        LOGGER.info("Updating carousel sort order: id={}, sortOrder={}", id, sortOrder);
        Carousel carousel = carouselMapper.selectById(id);
        if (carousel == null) {
            return Result.error("-1", "轮播图不存在");
        }
        
        carousel.setSortOrder(sortOrder);
        carousel.setUpdatedAt(LocalDateTime.now());
        
        carouselMapper.updateById(carousel);
        return Result.success();
    }

    /**
     * 获取所有轮播图列表（按排序号升序）
     *
     * @return 轮播图列表
     */
    public Result<List<Carousel>> listAll() {
        LOGGER.info("Listing all carousels");
        List<Carousel> carousels = carouselMapper.selectList(
            new LambdaQueryWrapper<Carousel>()
                .orderByAsc(Carousel::getSortOrder)
        );
        return Result.success(carousels);
    }
} 