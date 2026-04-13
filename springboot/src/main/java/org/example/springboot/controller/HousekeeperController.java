package org.example.springboot.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.springboot.common.Result;
import org.example.springboot.entity.Housekeeper;
import org.example.springboot.service.HousekeeperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/housekeeper")
@Tag(name = "家政人员管理", description = "家政人员相关接口")
public class HousekeeperController {
    
    @Autowired
    private HousekeeperService housekeeperService;
    
    @GetMapping("/page")
    @Operation(summary = "分页查询家政人员")
    public Result<IPage<Housekeeper>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String status) {
        return housekeeperService.page(pageNum, pageSize, name, phone, status);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询家政人员")
    public Result<Housekeeper> getById(@PathVariable Long id) {
        return housekeeperService.getById(id);
    }
    
    @GetMapping("/user/{userId}")
    @Operation(summary = "根据用户ID查询家政人员")
    public Result<Housekeeper> getByUserId(@PathVariable Long userId) {
        return housekeeperService.getByUserId(userId);
    }
    
    @PostMapping
    @Operation(summary = "新增家政人员")
    public Result<?> save(@RequestBody Housekeeper housekeeper) {
        return housekeeperService.save(housekeeper);
    }
    
    @PutMapping
    @Operation(summary = "更新家政人员")
    public Result<?> update(@RequestBody Housekeeper housekeeper) {
        return housekeeperService.update(housekeeper);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "删除家政人员")
    public Result<?> delete(@PathVariable Long id) {
        return housekeeperService.delete(id);
    }
    
    @GetMapping("/list")
    @Operation(summary = "获取所有家政人员")
    public Result<List<Housekeeper>> list() {
        return housekeeperService.list();
    }
    
    @PutMapping("/{id}/status/{status}")
    @Operation(summary = "更新家政人员状态")
    public Result<?> updateStatus(@PathVariable Long id, @PathVariable String status) {
        return housekeeperService.updateStatus(id, status);
    }
} 