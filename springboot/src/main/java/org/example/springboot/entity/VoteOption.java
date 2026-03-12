package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@TableName("vote_option")
@Schema(description = "投票选项")
public class VoteOption {

    @TableId(type = IdType.AUTO)
    @Schema(description = "ID")
    private Long id;

    @Schema(description = "投票ID")
    private Long voteId;

    @Schema(description = "选项文案")
    private String optionText;

    @Schema(description = "排序")
    private Integer sortOrder;
}
