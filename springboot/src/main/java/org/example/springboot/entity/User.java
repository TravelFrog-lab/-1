package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

@Data
public class User {
    @TableId(type = IdType.AUTO)
    @Schema(description = "自增主键")
    private Long id;
    @Schema(description = "用户名")
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3到50个字符之间")
    private String username;
    @Schema(description = "密码")
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6到20个字符之间")
    /** 仅反序列化写入，响应 JSON 不输出密码（避免泄漏与序列化问题） */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    @Schema(description = "姓名")
    private String name;
    @Schema(description = "角色")
    private String role;
    @Schema(description = "性别")
    private String sex;
    @Schema(description = "电话")
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    @Schema(description = "头像路径（相对路径或 URL）")
    private String avatar;
    @Schema(description = "创建时间")
    private Timestamp createdAt;
    @Schema(description = "更新时间")
    private Timestamp updatedAt;
    @TableField(exist = false)
    private List<Menu> menuList;
    @TableField(exist = false)
    private String token;

    public enum Role {
        ADMIN,
        OWNER,
        MAINTENANCE,
        KEEPER
    }

    @Schema(description = "账号状态")
    private String status;
}
