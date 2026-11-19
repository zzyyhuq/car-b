package com.example.beauty.common.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.beauty.common.constant.Role;
import com.example.beauty.module.car.entity.Car;
import com.example.beauty.module.car.mapper.CarMapper;
import com.example.beauty.module.user.entity.SysUser;
import com.example.beauty.module.user.mapper.SysUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 首次启动时建三个演示账号和一辆演示车辆，省得手动往库里塞数据。
 * 表里有数据就跳过，不会覆盖你改过的内容。
 */
@Slf4j
@Component
public class DataInitializer implements ApplicationRunner {

    private final SysUserMapper sysUserMapper;
    private final CarMapper carMapper;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(SysUserMapper sysUserMapper, CarMapper carMapper, PasswordEncoder passwordEncoder) {
        this.sysUserMapper = sysUserMapper;
        this.carMapper = carMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (sysUserMapper.selectCount(null) == 0) {
            create("admin", "admin123", "系统管理员", Role.ADMIN);
            create("staff001", "staff123", "门店员工小李", Role.STAFF);
            create("owner001", "owner123", "车主小明", Role.OWNER);
            // 日志里不出现明文密码，账号对应密码见 backend/README.md
            log.info("已初始化 3 个演示账号：admin、staff001、owner001");
        }
        initDemoCar();
    }

    /** 车主下单必须选车，没有车辆数据阶段 3 没法联调 */
    private void initDemoCar() {
        if (carMapper.selectCount(null) > 0) {
            return;
        }
        SysUser owner = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, "owner001"));
        if (owner == null) {
            return;
        }
        Car car = new Car();
        car.setUserId(owner.getId());
        car.setPlateNo("苏A12345");
        car.setBrand("比亚迪");
        car.setModel("汉");
        car.setColor("白色");
        carMapper.insert(car);
        log.info("已初始化演示车辆：苏A12345 比亚迪汉（车主 owner001）");
    }

    private void create(String username, String rawPassword, String nickname, String role) {
        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setNickname(nickname);
        user.setRole(role);
        user.setPoints(0);
        user.setStatus(1);
        sysUserMapper.insert(user);
    }
}
