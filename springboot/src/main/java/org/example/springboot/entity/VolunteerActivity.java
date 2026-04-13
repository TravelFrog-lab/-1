package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("volunteer_activity")
@Schema(description = "小区活动")
public class VolunteerActivity {
    
    @TableId(type = IdType.AUTO)
    @Schema(description = "ID")
    private Long id;

    @Schema(description = "活动名称")
    private String name;

    @Schema(description = "活动开始时间")
    private LocalDateTime startTime;

    @Schema(description = "活动结束时间")
    private LocalDateTime endTime;

    @Schema(description = "活动地点")
    private String location;

    @Schema(description = "活动描述")
    private String description;

    @Schema(description = "最大人数")
    private Integer maxParticipants;

    @Schema(description = "当前报名人数")
    private Integer currentParticipants;

    @Schema(description = "联系人")
    private String contactPerson;

    @Schema(description = "联系电话")
    private String contactPhone;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
} 