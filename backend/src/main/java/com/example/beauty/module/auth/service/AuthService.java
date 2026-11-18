package com.example.beauty.module.auth.service;

import com.example.beauty.module.auth.dto.LoginDTO;
import com.example.beauty.module.auth.dto.WxLoginDTO;
import com.example.beauty.module.auth.vo.LoginVO;

public interface AuthService {

    /** 管理端账号密码登录 */
    LoginVO login(LoginDTO dto);

    /** 小程序 wx.login 换 openid 登录，首次登录自动建档 */
    LoginVO wxLogin(WxLoginDTO dto);
}
