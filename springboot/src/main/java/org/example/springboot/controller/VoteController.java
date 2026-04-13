package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.springboot.common.Result;
import org.example.springboot.entity.User;
import org.example.springboot.entity.Vote;
import org.example.springboot.service.VoteService;
import org.example.springboot.util.JwtTokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 固定路径须先于 /{id}：/page、/{id}/result、/batch、/front/** 等。
 */
@RestController
@RequestMapping("/votes")
@Tag(name = "投票表决", description = "投票/表决管理及业主端接口")
public class VoteController {

    @Autowired
    private VoteService voteService;

    @GetMapping("/page")
    @Operation(summary = "分页查询投票列表")
    public Result<Page<Vote>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String title) {
        return Result.success(voteService.list(pageNum, pageSize, title));
    }

    // ---------- 业主端（须在 GET /{id} 之前，避免 /votes/front 被当成 id） ----------
    @GetMapping("/front/list")
    @Operation(summary = "业主端-投票列表（进行中、已结束）")
    public Result<List<Vote>> listForFront(@RequestParam(required = false) String status) {
        return Result.success(voteService.listForFront(status));
    }

    @GetMapping("/front/{id}")
    @Operation(summary = "业主端-投票详情（含是否已投、是否可看结果）")
    public Result<Map<String, Object>> getDetailForFront(@PathVariable Long id) {
        User user = JwtTokenUtils.getCurrentUser();
        Long userId = user != null ? user.getId() : null;
        return voteService.getDetailForFront(id, userId);
    }

    @PostMapping("/front/{id}/submit")
    @Operation(summary = "业主端-提交投票")
    public Result<?> submitVote(@PathVariable Long id, @RequestBody Map<String, List<Long>> body) {
        User user = JwtTokenUtils.getCurrentUser();
        if (user == null) return Result.error("-1", "请先登录");
        List<Long> optionIds = body != null ? body.get("optionIds") : null;
        return voteService.submitVote(id, optionIds, user.getId());
    }

    // ---------- 管理端 ----------
    @GetMapping("/{id}/result")
    @Operation(summary = "获取投票统计结果")
    public Result<List<Map<String, Object>>> getResult(@PathVariable Long id) {
        return Result.success(voteService.buildResult(id));
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询投票")
    public Result<Vote> getById(@PathVariable Long id) {
        return voteService.getById(id);
    }

    @PostMapping
    @Operation(summary = "新增投票")
    public Result<?> add(@RequestBody Vote vote) {
        User user = JwtTokenUtils.getCurrentUser();
        Long creatorId = user != null ? user.getId() : null;
        return voteService.add(vote, creatorId);
    }

    @PutMapping
    @Operation(summary = "更新投票")
    public Result<?> update(@RequestBody Vote vote) {
        return voteService.update(vote);
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除投票")
    public Result<?> batchDelete(@RequestBody List<Long> ids) {
        return voteService.deleteBatch(ids);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除投票")
    public Result<?> delete(@PathVariable Long id) {
        return voteService.delete(id);
    }
}
