package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.example.springboot.common.Result;
import org.example.springboot.dto.LoginRequest;
import org.example.springboot.entity.User;
import org.example.springboot.entity.UserPasswordUpdate;
import org.example.springboot.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 固定路径（/page、/forget、/search 等）必须在 /{id} 之前，否则 /user/page 会被当成 id=page → 500。
 */
@Tag(name = "用户管理接口")
@RestController
@RequestMapping("/user")
public class UserController {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);
    @Autowired
    private UserService userService;

    @Operation(summary = "登录")
    @PostMapping("/login")
    public Result<?> login(@RequestBody LoginRequest req) {
        if (req == null || StringUtils.isBlank(req.getUsername()) || req.getPassword() == null) {
            return Result.error("-1", "用户名或密码不能为空");
        }
        User user = new User();
        user.setUsername(req.getUsername().trim());
        user.setPassword(req.getPassword());
        return userService.login(user);
    }

    @Operation(summary = "忘记密码")
    @GetMapping("/forget")
    public Result<?> forgetPassword(@RequestParam String phone, @RequestParam String newPassword) {
        boolean success = userService.forgetPassword(phone, newPassword);
        if (success) {
            return Result.success();
        } else {
            return Result.error("-1", "忘记密码操作失败");
        }
    }

    @Operation(summary = "分页查询用户")
    @GetMapping("/page")
    public Result<?> getUsersByPage(
            @RequestParam(defaultValue = "") String username,
            @RequestParam(defaultValue = "") String sex,
            @RequestParam(defaultValue = "") String name,
            @RequestParam(defaultValue = "") String role,
            @RequestParam(defaultValue = "") String currentRole,
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<User> page = userService.getUsersByPage(username, sex, name, role, currentRole, currentPage, size);
        return Result.success(page);
    }

    @Operation(summary = "搜索用户")
    @GetMapping("/search")
    public Result<?> searchUsers(@RequestParam String keyword) {
        if (StringUtils.isBlank(keyword)) {
            return Result.success(List.of());
        }
        List<User> users = userService.searchUsers(keyword);
        return Result.success(users);
    }

    @Operation(summary = "根据username获取用户信息")
    @GetMapping("/username/{username}")
    public Result<?> getUserByUsername(@PathVariable String username) {
        User user = userService.getByUsername(username);
        if (user != null) {
            return Result.success(user);
        } else {
            return Result.error("-1", "未找到用户");
        }
    }

    @Operation(summary = "根据角色获取用户列表")
    @GetMapping("/role/{role}")
    public Result<?> getUserByRole(@PathVariable String role) {
        List<User> users = userService.getUserByRole(role);
        if (users != null && !users.isEmpty()) {
            return Result.success(users);
        } else {
            return Result.error("-1", "未找到该角色的用户");
        }
    }

    @Operation(summary = "根据角色枚举获取用户列表")
    @GetMapping("/role-enum/{role}")
    public Result<?> getUsersByRole(@PathVariable User.Role role) {
        List<User> users = userService.getUsersByRole(role);
        if (users != null && !users.isEmpty()) {
            return Result.success(users);
        } else {
            return Result.error("-1", "未找到该角色的用户");
        }
    }

    @Operation(summary = "获取所有用户")
    @GetMapping
    public Result<?> getAllUsers() {
        List<User> users = userService.getAllUsers();
        if (users != null && !users.isEmpty()) {
            return Result.success(users);
        } else {
            return Result.error("-1", "未找到用户");
        }
    }

    @Operation(summary = "根据id获取用户信息")
    @GetMapping("/{id}")
    public Result<?> getUserById(@PathVariable int id) {
        User user = userService.getUserById(id);
        if (user != null) {
            return Result.success(user);
        } else {
            return Result.error("-1", "未找到用户");
        }
    }

    @Operation(summary = "密码修改")
    @PutMapping("/password/{id}")
    public Result<?> updatePassword(@PathVariable int id, @RequestBody UserPasswordUpdate userPasswordUpdate) {
        boolean success = userService.updatePassword(id, userPasswordUpdate);
        if (success) {
            return Result.success();
        } else {
            return Result.error("-1", "密码修改失败");
        }
    }

    @Operation(summary = "批量删除用户")
    @DeleteMapping("/deleteBatch")
    public Result<?> deleteBatch(@RequestParam List<Integer> ids) {
        return userService.deleteBatch(ids);
    }

    @Operation(summary = "创建新用户")
    @PostMapping("/add")
    public Result<?> createUser(@RequestBody User user) {
        int res = userService.createUser(user);
        if (res == -1) return Result.error("-1", "用户名已存在！");
        if (res == -2) return Result.error("-1", "手机号已存在！");
        if (res > 0) {
            return Result.success(user);
        } else {
            return Result.error("-1", "创建用户失败");
        }
    }

    @Operation(summary = "更新用户信息")
    @PutMapping("/{id}")
    public Result<?> updateUser(@PathVariable Long id, @RequestBody User user) {
        boolean success = userService.updateUser(id, user);
        if (success) {
            return Result.success(user);
        } else {
            return Result.error("-1", "更新失败");
        }
    }

    @Operation(summary = "仅更新用户头像")
    @PutMapping("/{id}/avatar")
    public Result<?> updateAvatar(@PathVariable Long id, @RequestBody java.util.Map<String, String> body) {
        String avatar = body != null ? body.get("avatar") : null;
        boolean success = userService.updateAvatar(id, avatar);
        if (success) {
            return Result.success();
        }
        return Result.error("-1", "更新头像失败");
    }

    @Operation(summary = "根据id删除用户")
    @DeleteMapping("/delete/{id}")
    public Result<?> deleteUserById(@PathVariable int id) {
        return userService.deleteUserById(id);
    }

    @PostMapping("/reset-password")
    public Result<?> resetPassword(@RequestParam Long userId, @RequestParam String newPassword) {
        if (userService.resetPassword(userId, newPassword)) {
            return Result.success();
        }
        return Result.error("-1", "重置密码失败");
    }
}
