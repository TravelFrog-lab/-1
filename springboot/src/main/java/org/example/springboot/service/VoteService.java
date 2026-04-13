package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.springboot.common.Result;
import org.example.springboot.entity.*;
import org.example.springboot.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class VoteService {

    @Autowired
    private VoteMapper voteMapper;
    @Autowired
    private VoteOptionMapper voteOptionMapper;
    @Autowired
    private VoteRecordMapper voteRecordMapper;
    @Autowired
    private OwnerMapper ownerMapper;

    public static String statusOf(Vote vote) {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(vote.getStartTime())) return "NOT_STARTED";
        if (now.isAfter(vote.getEndTime())) return "ENDED";
        return "IN_PROGRESS";
    }

    public Page<Vote> list(Integer pageNum, Integer pageSize, String title) {
        LambdaQueryWrapper<Vote> wrapper = new LambdaQueryWrapper<>();
        if (title != null && !title.trim().isEmpty()) {
            wrapper.like(Vote::getTitle, title);
        }
        wrapper.orderByDesc(Vote::getCreatedAt);
        Page<Vote> page = voteMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        for (Vote v : page.getRecords()) {
            v.setOptions(voteOptionMapper.selectList(
                new LambdaQueryWrapper<VoteOption>().eq(VoteOption::getVoteId, v.getId()).orderByAsc(VoteOption::getSortOrder)));
            v.setEligibilityBuildingIds(null);
        }
        return page;
    }

    /** 前台：进行中+已结束的列表，带状态 */
    public List<Vote> listForFront(String statusFilter) {
        LambdaQueryWrapper<Vote> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Vote::getCreatedAt);
        List<Vote> list = voteMapper.selectList(wrapper);
        List<Vote> result = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (Vote v : list) {
            String st = statusOf(v);
            if ("NOT_STARTED".equals(st)) continue;
            if (statusFilter != null && !statusFilter.isEmpty() && !statusFilter.equals(st)) continue;
            v.setOptions(voteOptionMapper.selectList(
                new LambdaQueryWrapper<VoteOption>().eq(VoteOption::getVoteId, v.getId()).orderByAsc(VoteOption::getSortOrder)));
            v.setEligibilityBuildingIds(null);
            result.add(v);
        }
        return result;
    }

    public Result<Vote> getById(Long id) {
        Vote vote = voteMapper.selectById(id);
        if (vote == null) return Result.error("-1", "投票不存在");
        vote.setOptions(voteOptionMapper.selectList(
            new LambdaQueryWrapper<VoteOption>().eq(VoteOption::getVoteId, id).orderByAsc(VoteOption::getSortOrder)));
        return Result.success(vote);
    }

    /** 前台详情：可带当前用户是否已投、是否可看结果 */
    public Result<Map<String, Object>> getDetailForFront(Long id, Long userId) {
        Vote vote = voteMapper.selectById(id);
        if (vote == null) return Result.error("-1", "投票不存在");
        vote.setOptions(voteOptionMapper.selectList(
            new LambdaQueryWrapper<VoteOption>().eq(VoteOption::getVoteId, id).orderByAsc(VoteOption::getSortOrder)));
        vote.setEligibilityBuildingIds(null);

        String status = statusOf(vote);
        boolean canVote = "IN_PROGRESS".equals(status) && userId != null;
        boolean alreadyVoted = userId != null && voteRecordMapper.selectCount(
            new LambdaQueryWrapper<VoteRecord>().eq(VoteRecord::getVoteId, id).eq(VoteRecord::getUserId, userId)) > 0;
        boolean showResult = "ENDED".equals(status) || (Boolean.FALSE.equals(vote.getResultAfterEndOnly()));

        Map<String, Object> map = new HashMap<>();
        map.put("vote", vote);
        map.put("status", status);
        map.put("canVote", canVote && !alreadyVoted);
        map.put("alreadyVoted", alreadyVoted);
        map.put("showResult", showResult);
        if (showResult) {
            map.put("result", buildResult(id));
        }
        return Result.success(map);
    }

    @Transactional
    public Result<?> add(Vote vote, Long creatorId) {
        if (vote.getTitle() == null || vote.getTitle().trim().isEmpty()) {
            return Result.error("-1", "请填写投票主题");
        }
        if (vote.getOptions() == null || vote.getOptions().isEmpty()) {
            return Result.error("-1", "请至少添加一个选项");
        }
        vote.setCreatorId(creatorId);
        vote.setCreatedAt(LocalDateTime.now());
        vote.setUpdatedAt(LocalDateTime.now());
        if (vote.getEligibility() == null) vote.setEligibility(Vote.Eligibility.OWNER);
        if (vote.getResultAfterEndOnly() == null) vote.setResultAfterEndOnly(false);
        voteMapper.insert(vote);
        int order = 0;
        for (VoteOption opt : vote.getOptions()) {
            opt.setVoteId(vote.getId());
            opt.setSortOrder(order++);
            voteOptionMapper.insert(opt);
        }
        return Result.success();
    }

    @Transactional
    public Result<?> update(Vote vote) {
        Vote existing = voteMapper.selectById(vote.getId());
        if (existing == null) return Result.error("-1", "投票不存在");
        vote.setUpdatedAt(LocalDateTime.now());
        voteMapper.updateById(vote);

        List<VoteOption> incoming = vote.getOptions() != null ? vote.getOptions() : Collections.emptyList();
        List<VoteOption> existingOpts = voteOptionMapper.selectList(
            new LambdaQueryWrapper<VoteOption>().eq(VoteOption::getVoteId, vote.getId()));

        Set<Long> keepIds = incoming.stream()
            .map(VoteOption::getId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        for (VoteOption eo : existingOpts) {
            if (!keepIds.contains(eo.getId())) {
                voteRecordMapper.delete(new LambdaQueryWrapper<VoteRecord>()
                    .eq(VoteRecord::getVoteId, vote.getId())
                    .eq(VoteRecord::getOptionId, eo.getId()));
                voteOptionMapper.deleteById(eo.getId());
            }
        }

        int order = 0;
        for (VoteOption opt : incoming) {
            opt.setVoteId(vote.getId());
            opt.setSortOrder(order++);
            if (opt.getId() != null) {
                VoteOption dbOpt = voteOptionMapper.selectById(opt.getId());
                if (dbOpt != null && Objects.equals(dbOpt.getVoteId(), vote.getId())) {
                    VoteOption toUpdate = new VoteOption();
                    toUpdate.setId(opt.getId());
                    toUpdate.setVoteId(vote.getId());
                    toUpdate.setOptionText(opt.getOptionText());
                    toUpdate.setSortOrder(opt.getSortOrder());
                    voteOptionMapper.updateById(toUpdate);
                    continue;
                }
            }
            opt.setId(null);
            voteOptionMapper.insert(opt);
        }
        return Result.success();
    }

    @Transactional
    public Result<?> delete(Long id) {
        voteRecordMapper.delete(new LambdaQueryWrapper<VoteRecord>().eq(VoteRecord::getVoteId, id));
        voteOptionMapper.delete(new LambdaQueryWrapper<VoteOption>().eq(VoteOption::getVoteId, id));
        if (voteMapper.deleteById(id) <= 0) return Result.error("-1", "投票不存在");
        return Result.success();
    }

    /** 校验是否有资格投票（业主或全部） */
    public Result<?> checkEligibility(Long voteId, Long userId) {
        Vote vote = voteMapper.selectById(voteId);
        if (vote == null) return Result.error("-1", "投票不存在");
        if (vote.getEligibility() == Vote.Eligibility.OWNER) {
            long ownerCount = ownerMapper.selectCount(
                new LambdaQueryWrapper<Owner>().eq(Owner::getUserId, userId).eq(Owner::getStatus, Owner.ReviewStatus.ENABLED));
            if (ownerCount == 0) return Result.error("-1", "仅业主可参与投票");
        }
        return Result.success();
    }

    @Transactional
    public Result<?> submitVote(Long voteId, List<Long> optionIds, Long userId) {
        Vote vote = voteMapper.selectById(voteId);
        if (vote == null) return Result.error("-1", "投票不存在");
        if (!"IN_PROGRESS".equals(statusOf(vote))) return Result.error("-1", "当前不在投票时间内");
        Result<?> eligibility = checkEligibility(voteId, userId);
        if (!"0".equals(eligibility.getCode())) return eligibility;

        long already = voteRecordMapper.selectCount(
            new LambdaQueryWrapper<VoteRecord>().eq(VoteRecord::getVoteId, voteId).eq(VoteRecord::getUserId, userId));
        if (already > 0) return Result.error("-1", "您已投过票");

        if (optionIds == null || optionIds.isEmpty()) return Result.error("-1", "请选择至少一个选项");
        List<VoteOption> options = voteOptionMapper.selectList(
            new LambdaQueryWrapper<VoteOption>().eq(VoteOption::getVoteId, voteId));
        Set<Long> validIds = options.stream().map(VoteOption::getId).collect(Collectors.toSet());
        if (vote.getVoteType() == Vote.VoteType.SINGLE && optionIds.size() > 1) {
            return Result.error("-1", "该投票为单选");
        }
        for (Long oid : optionIds) {
            if (!validIds.contains(oid)) return Result.error("-1", "无效选项");
        }

        LocalDateTime now = LocalDateTime.now();
        for (Long optionId : optionIds) {
            VoteRecord record = new VoteRecord();
            record.setVoteId(voteId);
            record.setOptionId(optionId);
            record.setUserId(userId);
            record.setCreatedAt(now);
            voteRecordMapper.insert(record);
        }
        return Result.success();
    }

    /** 统计结果：按当前选项统计得票数与占比 */
    public List<Map<String, Object>> buildResult(Long voteId) {
        List<VoteOption> options = voteOptionMapper.selectList(
            new LambdaQueryWrapper<VoteOption>().eq(VoteOption::getVoteId, voteId).orderByAsc(VoteOption::getSortOrder));
        List<Map<String, Object>> list = new ArrayList<>();
        long total = voteRecordMapper.selectCount(new LambdaQueryWrapper<VoteRecord>().eq(VoteRecord::getVoteId, voteId));
        for (VoteOption opt : options) {
            long count = voteRecordMapper.selectCount(
                new LambdaQueryWrapper<VoteRecord>().eq(VoteRecord::getVoteId, voteId).eq(VoteRecord::getOptionId, opt.getId()));
            Map<String, Object> item = new HashMap<>();
            item.put("optionId", opt.getId());
            item.put("optionText", opt.getOptionText());
            item.put("count", count);
            item.put("percent", total > 0 ? Math.round(count * 100.0 / total) : 0);
            list.add(item);
        }
        return list;
    }

    public Result<?> deleteBatch(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return Result.error("-1", "请选择要删除的记录");
        for (Long id : ids) {
            delete(id);
        }
        return Result.success();
    }
}
