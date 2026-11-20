package com.example.beauty.module.car.service;

import com.example.beauty.module.car.dto.CarSaveDTO;
import com.example.beauty.module.car.entity.Car;

import java.util.List;

public interface CarService {

    /** 当前车主的车辆 */
    List<Car> myCars();

    /** 指定车主的车辆，供到店开单选择 */
    List<Car> carsOfUser(Long userId);

    Long create(CarSaveDTO dto);

    void update(Long id, CarSaveDTO dto);

    /** 已被订单引用的车辆不能删除 */
    void delete(Long id);
}
