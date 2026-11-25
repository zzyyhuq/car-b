package com.example.beauty.module.activitysignup.service;

import com.example.beauty.module.activitysignup.vo.ActivitySignupVO;

import java.util.List;

public interface ActivitySignupService {

    /** 车主报名，只有 need_signup=1 的进行中活动可以报名 */
    void signup(Long activityId);

    /** 车主取消报名 */
    void cancel(Long activityId);

    /** 当前车主的报名状态，没有记录返回 null */
    ActivitySignupVO mySignup(Long activityId);

    /** 管理端查看某活动的报名名单 */
    List<ActivitySignupVO> listByActivity(Long activityId);
}
