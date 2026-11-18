package com.example.beauty.module.user.service;

import com.example.beauty.common.PageResult;
import com.example.beauty.module.user.dto.ProfileUpdateDTO;
import com.example.beauty.module.user.dto.UserQuery;
import com.example.beauty.module.user.entity.SysUser;

public interface UserService {

    PageResult<SysUser> page(UserQuery query);

    SysUser getById(Long id);

    SysUser getProfile();

    void updateProfile(ProfileUpdateDTO dto);
}
