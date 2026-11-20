package com.example.beauty.module.car.controller;

import com.example.beauty.common.Result;
import com.example.beauty.common.constant.Role;
import com.example.beauty.common.security.RequireRole;
import com.example.beauty.module.car.dto.CarSaveDTO;
import com.example.beauty.module.car.entity.Car;
import com.example.beauty.module.car.service.CarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "车辆", description = "车主车辆管理，订单必须关联车辆")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/api/car")
public class CarController {

    private final CarService carService;

    public CarController(CarService carService) {
        this.carService = carService;
    }

    @Operation(summary = "我的车辆（车主）")
    @GetMapping("/my")
    @RequireRole({Role.OWNER})
    public Result<List<Car>> my() {
        return Result.ok(carService.myCars());
    }

    @Operation(summary = "查看指定车主的车辆（管理员/员工）", description = "到店开单时选择车辆用")
    @GetMapping("/user/{userId}")
    @RequireRole({Role.ADMIN, Role.STAFF})
    public Result<List<Car>> byUser(@PathVariable Long userId) {
        return Result.ok(carService.carsOfUser(userId));
    }

    @Operation(summary = "新增车辆（车主）")
    @PostMapping
    @RequireRole({Role.OWNER})
    public Result<Long> create(@RequestBody @Valid CarSaveDTO dto) {
        return Result.ok(carService.create(dto));
    }

    @Operation(summary = "修改车辆（车主，只能改自己的）")
    @PutMapping("/{id}")
    @RequireRole({Role.OWNER})
    public Result<Void> update(@PathVariable Long id, @RequestBody @Valid CarSaveDTO dto) {
        carService.update(id, dto);
        return Result.ok();
    }

    @Operation(summary = "删除车辆（车主）", description = "已有订单的车辆不能删除")
    @DeleteMapping("/{id}")
    @RequireRole({Role.OWNER})
    public Result<Void> delete(@PathVariable Long id) {
        carService.delete(id);
        return Result.ok();
    }
}
