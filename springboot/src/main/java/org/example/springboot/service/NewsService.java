package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.springboot.common.Result;
import org.example.springboot.entity.News;
import org.example.springboot.mapper.NewsMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import jakarta.annotation.Resource;

@Service
public class NewsService extends ServiceImpl<NewsMapper, News> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(NewsService.class);
    
    @Resource
    private NewsMapper newsMapper;

    public Result<Page<News>> list(Integer pageNum, Integer pageSize, String title) {
        LOGGER.info("Listing news, pageNum: {}, pageSize: {}, title: {}", 
                pageNum, pageSize, title);
        
        LambdaQueryWrapper<News> wrapper = new LambdaQueryWrapper<>();
        if (title != null && !title.trim().isEmpty()) {
            wrapper.like(News::getTitle, title);
        }
        wrapper.orderByDesc(News::getCreatedAt);
        
        Page<News> page = page(new Page<>(pageNum, pageSize), wrapper);
        return Result.success(page);
    }

    public Result<News> getById(Long id) {
        LOGGER.info("Getting news by id: {}", id);
        News news = newsMapper.selectById(id);
        if (news == null) {
            return Result.error("-1", "新闻不存在");
        }
        return Result.success(news);
    }

    @Transactional
    public Result<?> add(News news) {
        LOGGER.info("Adding new news: {}", news);
        save(news);
        return Result.success();
    }

    @Transactional
    public Result<?> update(News news) {
        LOGGER.info("Updating news: {}", news);
        if (!updateById(news)) {
            return Result.error("-1", "新闻不存在");
        }
        return Result.success();
    }

    @Transactional
    public Result<?> delete(Long id) {
        LOGGER.info("Deleting news: {}", id);
        if (!removeById(id)) {
            return Result.error("-1", "新闻不存在");
        }
        return Result.success();
    }

    @Transactional
    public Result<?> batchDelete(List<Long> ids) {
        LOGGER.info("Batch deleting news: {}", ids);
        if (ids == null || ids.isEmpty()) {
            return Result.error("-1", "请选择要删除的记录");
        }
        
        removeBatchByIds(ids);
        return Result.success();
    }
} 