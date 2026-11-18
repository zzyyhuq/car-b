package com.example.beauty.common.config;

import com.example.beauty.common.constant.Role;
import com.example.beauty.module.user.entity.SysUser;
import com.example.beauty.module.user.mapper.SysUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 首次启动时建三个演示账号，省得手动往库里塞 BCrypt 密码。
 * 表里有数据就跳过，不会覆盖你改过的密码。
 */
@Slf4j
@Component
public class DataInitializer implements ApplicationRunner {

    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(SysUserMapper sysUserMapper, PasswordEncoder passwordEncoder) {
        this.sysUserMapper = sysUserMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (sysUserMapper.selectCount(null) > 0) {
            return;
        }
        create("admin", "admin123", "系统管理员", Role.ADMIN);
        create("staff001", "staff123", "门店员工小李", Role.STAFF);
        create("owner001", "owner123", "车主小明", Role.OWNER);
        log.info("已初始化演示账号：admin/admin123、staff001/staff123、owner001/owner123");
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
