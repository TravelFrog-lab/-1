package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.springboot.common.Result;
import org.example.springboot.entity.ParkingSpace;
import org.example.springboot.service.ParkingSpaceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/parking-space")
@Tag(name = "车位管理", description = "车位信息的增删改查接口")
public class ParkingSpaceController {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ParkingSpaceController.class);
    
    @Autowired
    private ParkingSpaceService parkingSpaceService;

    @GetMapping("/page")
    @Operation(summary = "分页查询车位列表")
    public Result<Page<ParkingSpace>> list(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "车位类型") @RequestParam(required = false) ParkingSpace.Type type,
            @Parameter(description = "使用状态") @RequestParam(required = false) String status,
            @Parameter(description = "车位代号") @RequestParam(required = false) String code) {
        return Result.success(parkingSpaceService.list(pageNum, pageSize, code,type,status ));
    }


    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询车位")
    public Result<ParkingSpace> getById(
            @Parameter(description = "车位ID") @PathVariable Long id) {
        return parkingSpaceService.getById(id);
    }

    @PostMapping
    @Operation(summary = "新增车位")
    public Result<?> add(@RequestBody ParkingSpace parkingSpace) {
        return parkingSpaceService.add(parkingSpace);
    }

    @PutMapping
    @Operation(summary = "更新车位信息")
    public Result<?> update(@RequestBody ParkingSpace parkingSpace) {
        return parkingSpaceService.update(parkingSpace);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除车位")
    public Result<?> delete(
            @Parameter(description = "车位ID") @PathVariable Long id) {
        return parkingSpaceService.delete(id);
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除车位")
    public Result<?> batchDelete(
            @Parameter(description = "车位ID列表") @RequestBody List<Long> ids) {
        return parkingSpaceService.batchDelete(ids);
    }
    //getAll
    @GetMapping("/all")
    @Operation(summary = "查询所有车位")
    public Result<List<ParkingSpace>> getAll() {
        return Result.success(parkingSpaceService.list(1,Integer.MAX_VALUE,null,null,null).getRecords());
    }




    @GetMapping("/type/{type}")
    @Operation(summary = "根据类型查询车位列表")
    public Result<List<ParkingSpace>> listByType(
            @Parameter(description = "车位类型") @PathVariable ParkingSpace.Type type) {
        LOGGER.info("Listing parking spaces by type: {}", type);
        return parkingSpaceService.listByType(type);
    }
} 