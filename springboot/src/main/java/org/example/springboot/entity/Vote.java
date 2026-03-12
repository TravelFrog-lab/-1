package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("vote")
@Schema(description = "投票/表决")
public class Vote {

    @TableId(type = IdType.AUTO)
    @Schema(description = "ID")
    private Long id;

    @Schema(description = "投票主题")
    private String title;

    @Schema(description = "详细描述")
    private String description;

    @Schema(description = "投票类型：单选/多选")
    private VoteType voteType;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "投票资格：仅业主/全部")
    private Eligibility eligibility;

    @Schema(description = "指定楼栋ID列表(JSON)，为空表示不限制楼栋")
    private String eligibilityBuildingIds;

    @Schema(description = "创建人ID")
    private Long creatorId;

    @Schema(description = "是否截止后才显示结果")
    private Boolean resultAfterEndOnly;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    @Schema(description = "选项列表")
    private List<VoteOption> options;

    public enum VoteType {
        SINGLE,
        MULTIPLE
    }

    public enum Eligibility {
        OWNER,
        ALL
    }
}
