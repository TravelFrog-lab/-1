package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("vote_record")
@Schema(description = "投票记录")
public class VoteRecord {

    @TableId(type = IdType.AUTO)
    @Schema(description = "ID")
    private Long id;

    @Schema(description = "投票ID")
    private Long voteId;

    @Schema(description = "选项ID")
    private Long optionId;

    @Schema(description = "投票人用户ID")
    private Long userId;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
