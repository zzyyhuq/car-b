package com.example.beauty.module.car.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.beauty.common.exception.BusinessException;
import com.example.beauty.common.security.UserContext;
import com.example.beauty.module.car.dto.CarSaveDTO;
import com.example.beauty.module.car.entity.Car;
import com.example.beauty.module.car.mapper.CarMapper;
import com.example.beauty.module.car.service.CarService;
import com.example.beauty.module.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class CarServiceImpl implements CarService {

    private final CarMapper carMapper;
    private final OrderService orderService;

    public CarServiceImpl(CarMapper carMapper, OrderService orderService) {
        this.carMapper = carMapper;
        this.orderService = orderService;
    }

    @Override
    public List<Car> myCars() {
        return listByUser(UserContext.getId());
    }

    @Override
    public List<Car> carsOfUser(Long userId) {
        return listByUser(userId);
    }

    @Override
    public Long create(CarSaveDTO dto) {
        Long userId = UserContext.getId();
        String plateNo = normalizePlate(dto.getPlateNo());
        checkDuplicate(userId, plateNo, null);

        Car car = new Car();
        BeanUtils.copyProperties(dto, car);
        car.setPlateNo(plateNo);
        car.setUserId(userId);
        carMapper.insert(car);
        log.info("车主新增车辆 carId={} userId={} plateNo={}", car.getId(), userId, plateNo);
        return car.getId();
    }

    @Override
    public void update(Long id, CarSaveDTO dto) {
        getOwned(id);
        String plateNo = normalizePlate(dto.getPlateNo());
        checkDuplicate(UserContext.getId(), plateNo, id);

        Car car = new Car();
        BeanUtils.copyProperties(dto, car);
        car.setId(id);
        car.setPlateNo(plateNo);
        carMapper.updateById(car);
    }

    @Override
    public void delete(Long id) {
        getOwned(id);
        if (orderService.hasOrderByCar(id)) {
            throw new BusinessException(400, "该车辆已有订单，不能删除");
        }
        carMapper.deleteById(id);
        log.info("车主删除车辆 carId={} userId={}", id, UserContext.getId());
    }

    private List<Car> listByUser(Long userId) {
        return carMapper.selectList(new LambdaQueryWrapper<Car>()
                .eq(Car::getUserId, userId)
                .orderByDesc(Car::getId));
    }

    private Car getOwned(Long id) {
        Car car = carMapper.selectById(id);
        if (car == null || !Objects.equals(car.getUserId(), UserContext.getId())) {
            throw new BusinessException(404, "车辆不存在或不属于当前用户");
        }
        return car;
    }

    private String normalizePlate(String plateNo) {
        if (!StringUtils.hasText(plateNo)) {
            throw new BusinessException(400, "请输入车牌号");
        }
        return plateNo.trim().toUpperCase();
    }

    /** 同一个车主不允许重复添加同一车牌；修改时排除自身 */
    private void checkDuplicate(Long userId, String plateNo, Long excludeId) {
        LambdaQueryWrapper<Car> wrapper = new LambdaQueryWrapper<Car>()
                .eq(Car::getUserId, userId)
                .eq(Car::getPlateNo, plateNo)
                .ne(excludeId != null, Car::getId, excludeId);
        if (carMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(400, "该车牌已添加过");
        }
    }
}
