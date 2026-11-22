package com.example.beauty.common.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.beauty.common.constant.Role;
import com.example.beauty.module.activity.entity.Activity;
import com.example.beauty.module.activity.mapper.ActivityMapper;
import com.example.beauty.module.car.entity.Car;
import com.example.beauty.module.car.mapper.CarMapper;
import com.example.beauty.module.notice.entity.Notice;
import com.example.beauty.module.notice.mapper.NoticeMapper;
import com.example.beauty.module.points.entity.PointsGoods;
import com.example.beauty.module.points.entity.PointsRecord;
import com.example.beauty.module.points.mapper.PointsGoodsMapper;
import com.example.beauty.module.points.mapper.PointsRecordMapper;
import com.example.beauty.module.user.entity.SysUser;
import com.example.beauty.module.user.mapper.SysUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 首次启动时建三个演示账号、一辆演示车辆、首页公告与活动，省得手动往库里塞数据。
 * 表里有数据就跳过，不会覆盖你改过的内容。
 */
@Slf4j
@Component
public class DataInitializer implements ApplicationRunner {

    private final SysUserMapper sysUserMapper;
    private final CarMapper carMapper;
    private final NoticeMapper noticeMapper;
    private final ActivityMapper activityMapper;
    private final PointsGoodsMapper pointsGoodsMapper;
    private final PointsRecordMapper pointsRecordMapper;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(SysUserMapper sysUserMapper,
                           CarMapper carMapper,
                           NoticeMapper noticeMapper,
                           ActivityMapper activityMapper,
                           PointsGoodsMapper pointsGoodsMapper,
                           PointsRecordMapper pointsRecordMapper,
                           PasswordEncoder passwordEncoder) {
        this.sysUserMapper = sysUserMapper;
        this.carMapper = carMapper;
        this.noticeMapper = noticeMapper;
        this.activityMapper = activityMapper;
        this.pointsGoodsMapper = pointsGoodsMapper;
        this.pointsRecordMapper = pointsRecordMapper;
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
        initDemoNotices();
        initDemoActivities();
        initDemoPointsGoods();
        initDemoPoints();
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

    /** 小程序首页公告轮播，没有数据首页会空一块 */
    private void initDemoNotices() {
        if (noticeMapper.selectCount(null) > 0) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        noticeMapper.insert(notice("本店营业时间 9:00 - 18:00，节假日正常营业",
                "每日 9:00 开始接待，建议提前在小程序预约。", now.minusDays(2)));
        noticeMapper.insert(notice("新用户首次预约享 8 折优惠",
                "首次到店服务项目享 8 折，详情请咨询门店前台。", now.minusDays(1)));
        noticeMapper.insert(notice("订单完成后自动发放积分，可兑换精洗券",
                "订单完成后按实付金额发放积分，可前往积分商城兑换。", now));
        log.info("已初始化 3 条演示公告");
    }

    private Notice notice(String title, String content, LocalDateTime publishTime) {
        Notice notice = new Notice();
        notice.setTitle(title);
        notice.setContent(content);
        notice.setType("NOTICE");
        notice.setPublishTime(publishTime);
        notice.setStatus(1);
        return notice;
    }

    /** 小程序首页活动卡片 */
    private void initDemoActivities() {
        if (activityMapper.selectCount(null) > 0) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        activityMapper.insert(activity("秋季漆面养护节", "打蜡 / 镀晶项目 8 折",
                now.minusDays(10), now.plusDays(30)));
        activityMapper.insert(activity("积分翻倍月", "本月订单积分 x2",
                now.minusDays(5), now.plusDays(25)));
        log.info("已初始化 2 个演示活动");
    }

    private Activity activity(String title, String content,
                              LocalDateTime startTime, LocalDateTime endTime) {
        Activity activity = new Activity();
        activity.setTitle(title);
        activity.setContent(content);
        activity.setStartTime(startTime);
        activity.setEndTime(endTime);
        activity.setStatus(1);
        return activity;
    }

    /** 积分商城演示商品 */
    private void initDemoPointsGoods() {
        if (pointsGoodsMapper.selectCount(null) > 0) {
            return;
        }
        pointsGoodsMapper.insert(goods("免费精洗券", "凭券免费洗车一次", 300, 20));
        pointsGoodsMapper.insert(goods("玻璃水一瓶", "冬季防冻型玻璃水", 120, 50));
        pointsGoodsMapper.insert(goods("车内除味喷雾", "植物提取，清新除味", 200, 30));
        pointsGoodsMapper.insert(goods("洗车毛巾套装", "超细纤维三件套", 150, 40));
        log.info("已初始化 4 个演示积分商品");
    }

    private PointsGoods goods(String name, String description, int pointsPrice, int stock) {
        PointsGoods goods = new PointsGoods();
        goods.setName(name);
        goods.setDescription(description);
        goods.setPointsPrice(pointsPrice);
        goods.setStock(stock);
        goods.setStatus(1);
        return goods;
    }

    /** 演示车主给 500 初始积分，否则积分商城没法演示；已有流水就不再补 */
    private void initDemoPoints() {
        SysUser owner = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, "owner001"));
        if (owner == null) {
            return;
        }
        Long records = pointsRecordMapper.selectCount(new LambdaQueryWrapper<PointsRecord>()
                .eq(PointsRecord::getUserId, owner.getId()));
        int current = owner.getPoints() == null ? 0 : owner.getPoints();
        if (records > 0 || current > 0) {
            return;
        }

        int points = 500;
        owner.setPoints(points);
        sysUserMapper.updateById(owner);

        PointsRecord record = new PointsRecord();
        record.setUserId(owner.getId());
        record.setChangeAmount(points);
        record.setType("ADMIN_ADJUST");
        record.setRemark("演示初始积分");
        pointsRecordMapper.insert(record);
        log.info("已为 owner001 初始化 {} 演示积分", points);
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
