package com.example.beauty.module.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.beauty.common.constant.Role;
import com.example.beauty.common.exception.BusinessException;
import com.example.beauty.common.security.JwtUtil;
import com.example.beauty.module.auth.dto.LoginDTO;
import com.example.beauty.module.auth.dto.WxLoginDTO;
import com.example.beauty.module.auth.service.AuthService;
import com.example.beauty.module.auth.vo.LoginVO;
import com.example.beauty.module.user.entity.SysUser;
import com.example.beauty.module.user.mapper.SysUserMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    private static final String CODE2SESSION_URL = "https://api.weixin.qq.com/sns/jscode2session";

    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    @Value("${wx.appid:}")
    private String appid;

    @Value("${wx.secret:}")
    private String secret;

    public AuthServiceImpl(SysUserMapper sysUserMapper, PasswordEncoder passwordEncoder,
                           JwtUtil jwtUtil, ObjectMapper objectMapper) {
        this.sysUserMapper = sysUserMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.objectMapper = objectMapper;
    }

    @Override
    public LoginVO login(LoginDTO dto) {
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, dto.getUsername()));
        // 账号不存在和密码错误返回同一句话，别给攻击者送信息；
        // 服务端日志里记录具体原因，方便排查（不记录密码）
        if (user == null || !StringUtils.hasText(user.getPassword())
                || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            log.warn("登录失败 username={} reason={}", dto.getUsername(),
                    user == null ? "账号不存在" : "密码错误");
            throw new BusinessException(400, "账号或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            log.warn("登录失败 username={} reason=账号已禁用", dto.getUsername());
            throw new BusinessException(403, "账号已被禁用，请联系管理员");
        }
        log.info("登录成功 userId={} username={} role={}", user.getId(), user.getUsername(), user.getRole());
        return buildLoginVO(user);
    }

    @Override
    public LoginVO wxLogin(WxLoginDTO dto) {
        if (!StringUtils.hasText(appid) || !StringUtils.hasText(secret)) {
            throw new BusinessException(500, "后端未配置小程序 AppID/AppSecret，请填写 application.yml 的 wx.appid 与 wx.secret");
        }
        String openid = requestOpenid(dto.getCode());
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getOpenid, openid));
        if (user == null) {
            user = new SysUser();
            user.setOpenid(openid);
            user.setNickname("微信用户");
            user.setRole(Role.OWNER);
            user.setPoints(0);
            user.setStatus(1);
            sysUserMapper.insert(user);
            log.info("微信首次登录，自动建档 userId={}", user.getId());
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            log.warn("微信登录失败 userId={} reason=账号已禁用", user.getId());
            throw new BusinessException(403, "账号已被禁用，请联系门店");
        }
        log.info("微信登录成功 userId={}", user.getId());
        return buildLoginVO(user);
    }

    private String requestOpenid(String code) {
        try {
            String body = RestClient.create()
                    .get()
                    .uri(CODE2SESSION_URL + "?appid={appid}&secret={secret}&js_code={code}&grant_type=authorization_code",
                            appid, secret, code)
                    .retrieve()
                    .body(String.class);
            JsonNode node = objectMapper.readTree(body);
            int errcode = node.path("errcode").asInt(0);
            if (errcode != 0) {
                throw new BusinessException(400, "微信登录失败：" + node.path("errmsg").asText("code 无效"));
            }
            String openid = node.path("openid").asText(null);
            if (!StringUtils.hasText(openid)) {
                throw new BusinessException(400, "微信登录失败：未获取到 openid");
            }
            return openid;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(500, "调用微信接口失败，请检查网络配置");
        }
    }

    private LoginVO buildLoginVO(SysUser user) {
        return LoginVO.builder()
                .token(jwtUtil.generate(user.getId(), user.getRole()))
                .expiresIn(jwtUtil.getExpireSeconds())
                .id(user.getId())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .role(user.getRole())
                .build();
    }
}
