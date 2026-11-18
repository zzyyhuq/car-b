-- ============================================================
-- 汽车美容毕设 数据库初始化脚本
-- 用法（在项目根目录执行，会提示输入 MySQL root 密码）：
--   mysql -u root -p < backend/src/main/resources/db/init.sql
-- 说明：创建数据库、应用账号 beauty、12 张业务表和少量演示数据。
--       演示用户（admin/staff001/owner001）由后端首次启动时自动创建。
-- ============================================================
SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS car_beauty
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;

CREATE USER IF NOT EXISTS 'beauty'@'localhost' IDENTIFIED BY 'Beauty@2026';
GRANT ALL PRIVILEGES ON car_beauty.* TO 'beauty'@'localhost';
FLUSH PRIVILEGES;

USE car_beauty;

-- 1. 用户（三种角色共用一张表）
CREATE TABLE IF NOT EXISTS sys_user (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    openid      VARCHAR(64)  NULL COMMENT '微信 openid，管理员/员工为空',
    username    VARCHAR(50)  NULL COMMENT '管理端账号，车主为空',
    password    VARCHAR(100) NULL COMMENT 'BCrypt 密码',
    nickname    VARCHAR(50)  NULL COMMENT '昵称',
    avatar      VARCHAR(255) NULL COMMENT '头像地址',
    phone       VARCHAR(20)  NULL COMMENT '手机号',
    role        VARCHAR(20)  NOT NULL DEFAULT 'OWNER' COMMENT 'ADMIN/STAFF/OWNER',
    points      INT          NOT NULL DEFAULT 0 COMMENT '积分余额',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '1正常 0禁用',
    create_time DATETIME     NULL,
    update_time DATETIME     NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_openid (openid),
    UNIQUE KEY uk_username (username),
    KEY idx_role (role)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='用户表';

-- 2. 车辆
CREATE TABLE IF NOT EXISTS car (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    user_id     BIGINT      NOT NULL COMMENT '车主 id',
    plate_no    VARCHAR(20) NOT NULL COMMENT '车牌号',
    brand       VARCHAR(50) NULL COMMENT '品牌',
    model       VARCHAR(50) NULL COMMENT '型号',
    color       VARCHAR(20) NULL COMMENT '颜色',
    create_time DATETIME    NULL,
    update_time DATETIME    NULL,
    PRIMARY KEY (id),
    KEY idx_user (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='车辆表';

-- 3. 美容订单类型（服务项目）
CREATE TABLE IF NOT EXISTS service_type (
    id          BIGINT        NOT NULL AUTO_INCREMENT,
    name        VARCHAR(50)   NOT NULL COMMENT '项目名称',
    category    VARCHAR(30)   NULL COMMENT '分类：洗车/内饰/漆面/保养',
    description VARCHAR(500)  NULL COMMENT '描述',
    price       DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '价格',
    duration    INT           NULL COMMENT '预计时长（分钟）',
    image       VARCHAR(255)  NULL COMMENT '项目图片',
    points_rate DECIMAL(3, 2) NOT NULL DEFAULT 1.00 COMMENT '积分倍率',
    status      TINYINT       NOT NULL DEFAULT 1 COMMENT '1上架 0下架',
    deleted     TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME      NULL,
    update_time DATETIME      NULL,
    PRIMARY KEY (id),
    KEY idx_status (status)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='服务项目表';

-- 4. 美容订单
CREATE TABLE IF NOT EXISTS service_order (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    order_no         VARCHAR(32)   NOT NULL COMMENT '订单号',
    user_id          BIGINT        NOT NULL COMMENT '车主 id',
    car_id           BIGINT        NOT NULL COMMENT '车辆 id',
    service_type_id  BIGINT        NOT NULL COMMENT '服务项目 id',
    staff_id         BIGINT        NULL COMMENT '负责员工 id',
    appointment_time DATETIME      NULL COMMENT '预约时间',
    status           VARCHAR(20)   NOT NULL DEFAULT 'PENDING' COMMENT '订单状态',
    amount           DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '金额',
    points_deduct    INT           NOT NULL DEFAULT 0 COMMENT '下单抵扣积分',
    remark           VARCHAR(255)  NULL COMMENT '备注',
    cancel_reason    VARCHAR(255)  NULL COMMENT '取消原因',
    finish_time      DATETIME      NULL COMMENT '完成时间',
    create_time      DATETIME      NULL,
    update_time      DATETIME      NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_user_status (user_id, status),
    KEY idx_staff (staff_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='美容订单表';

-- 5. 服务过程记录
CREATE TABLE IF NOT EXISTS service_process (
    id          BIGINT        NOT NULL AUTO_INCREMENT,
    order_id    BIGINT        NOT NULL COMMENT '订单 id',
    staff_id    BIGINT        NULL COMMENT '操作员工 id',
    node        VARCHAR(20)   NOT NULL COMMENT '节点编码',
    content     VARCHAR(500)  NULL COMMENT '备注',
    images      VARCHAR(2000) NULL COMMENT '照片地址，逗号分隔',
    create_time DATETIME      NULL,
    PRIMARY KEY (id),
    KEY idx_order (order_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='服务过程表';

-- 6. 积分商品
CREATE TABLE IF NOT EXISTS points_goods (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    name         VARCHAR(50)  NOT NULL COMMENT '商品名称',
    description  VARCHAR(500) NULL,
    points_price INT          NOT NULL DEFAULT 0 COMMENT '所需积分',
    stock        INT          NOT NULL DEFAULT 0 COMMENT '库存',
    image        VARCHAR(255) NULL,
    status       TINYINT      NOT NULL DEFAULT 1 COMMENT '1上架 0下架',
    deleted      TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_time  DATETIME     NULL,
    update_time  DATETIME     NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='积分商品表';

-- 7. 积分兑换单
CREATE TABLE IF NOT EXISTS points_exchange (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    exchange_no   VARCHAR(32)  NOT NULL COMMENT '兑换单号',
    user_id       BIGINT       NOT NULL,
    goods_id      BIGINT       NOT NULL,
    points_cost   INT          NOT NULL COMMENT '消耗积分',
    status        VARCHAR(20)  NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/VERIFIED/CANCELLED',
    exchange_time DATETIME     NULL,
    verify_time   DATETIME     NULL COMMENT '核销时间',
    create_time   DATETIME     NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_exchange_no (exchange_no),
    KEY idx_user (user_id),
    KEY idx_status (status)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='积分兑换单';

-- 8. 积分明细
CREATE TABLE IF NOT EXISTS points_record (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    user_id       BIGINT       NOT NULL,
    change_amount INT          NOT NULL COMMENT '变动值，加分为正、扣分为负',
    type          VARCHAR(20)  NOT NULL COMMENT 'ORDER_GAIN/EXCHANGE_COST/ADMIN_ADJUST',
    ref_id        BIGINT       NULL COMMENT '关联订单/兑换单 id',
    remark        VARCHAR(255) NULL,
    create_time   DATETIME     NULL,
    PRIMARY KEY (id),
    KEY idx_user (user_id),
    KEY idx_type (type)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='积分明细表';

-- 9. 留言簿（含服务评价）
CREATE TABLE IF NOT EXISTS message (
    id          BIGINT        NOT NULL AUTO_INCREMENT,
    user_id     BIGINT        NOT NULL,
    type        VARCHAR(20)   NOT NULL COMMENT 'EVALUATION/ FEEDBACK',
    order_id    BIGINT        NULL COMMENT '评价时关联订单，留言为空',
    rating      TINYINT       NULL COMMENT '星级 1-5',
    content     VARCHAR(1000) NULL,
    reply       VARCHAR(1000) NULL COMMENT '管理员回复',
    reply_time  DATETIME      NULL,
    status      TINYINT       NOT NULL DEFAULT 1 COMMENT '1通过 0待审核 2隐藏',
    create_time DATETIME      NULL,
    PRIMARY KEY (id),
    KEY idx_type (type),
    KEY idx_order (order_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='留言/评价表';

-- 10. 通知公告
CREATE TABLE IF NOT EXISTS notice (
    id           BIGINT      NOT NULL AUTO_INCREMENT,
    title        VARCHAR(100) NOT NULL,
    content      TEXT        NULL,
    type         VARCHAR(20) NULL COMMENT 'NOTICE/ACTIVITY',
    publish_time DATETIME    NULL,
    status       TINYINT     NOT NULL DEFAULT 0 COMMENT '1已发布 0草稿',
    create_time  DATETIME    NULL,
    update_time  DATETIME    NULL,
    PRIMARY KEY (id),
    KEY idx_status (status)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='通知公告表';

-- 11. 活动
CREATE TABLE IF NOT EXISTS activity (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    title       VARCHAR(100) NOT NULL,
    content     TEXT         NULL,
    cover       VARCHAR(255) NULL COMMENT '封面图',
    start_time  DATETIME     NULL,
    end_time    DATETIME     NULL,
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '1进行中 0已下线',
    create_time DATETIME     NULL,
    update_time DATETIME     NULL,
    PRIMARY KEY (id),
    KEY idx_status (status)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='活动表';

-- 12. 活动报名
CREATE TABLE IF NOT EXISTS activity_signup (
    id          BIGINT   NOT NULL AUTO_INCREMENT,
    activity_id BIGINT   NOT NULL,
    user_id     BIGINT   NOT NULL,
    signup_time DATETIME NULL,
    status      TINYINT  NOT NULL DEFAULT 1 COMMENT '1已报名 0已取消',
    PRIMARY KEY (id),
    UNIQUE KEY uk_activity_user (activity_id, user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='活动报名表';

-- 演示服务项目
INSERT IGNORE INTO service_type (id, name, category, description, price, duration, points_rate, status)
VALUES (1, '标准精洗', '洗车', '高压冲洗、泡沫清洗、轮毂清洁、内饰吸尘', 68.00, 60, 1.00, 1),
       (2, '内饰深度清洁', '内饰', '座椅、地毯、顶棚深度清洁，臭氧除味', 288.00, 120, 1.50, 1),
       (3, '全车打蜡', '漆面', '漆面清洁后手工上蜡，提升光泽度', 198.00, 90, 1.20, 1),
       (4, '漆面镀晶', '漆面', '三层镀晶施工，保护周期约 12 个月', 1280.00, 240, 2.00, 1);

SELECT '数据库初始化完成' AS result;
