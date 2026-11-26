# 后端（Spring Boot 3）

## 一、初始化数据库（只做一次）

在项目根目录执行，会提示输入 MySQL root 密码：

```bash
mysql -u root -p < backend/src/main/resources/db/init.sql
```

脚本会做四件事：建 `car_beauty` 库、建应用账号 `beauty` / `Beauty@2026`、建 12 张表、插入 4 条演示服务项目。

验证：

```bash
mysql -u beauty -pBeauty@2026 -e "USE car_beauty; SHOW TABLES;"
```

## 二、启动

方式一：IntelliJ IDEA 打开 `backend/`，运行 `BeautyApplication`。

方式二：命令行（Windows 用 `mvnw.cmd`，Git Bash 用 `./mvnw`）：

```bash
cd backend
./mvnw spring-boot:run
```

启动后监听 `http://localhost:8081`（本机 8080 被 Jenkins 占用，所以改用 8081）。

首次启动会自动创建三个演示账号（表里已有用户则跳过）：

| 账号 | 密码 | 角色 |
|---|---|---|
| admin | admin123 | 管理员 |
| staff001 | staff123 | 门店员工 |
| owner001 | owner123 | 车主 |

> 演示账号写死在 `common/config/DataInitializer.java`，答辩前记得改掉。
> 首次启动还会给 owner001 建一辆演示车辆：`苏A12345`（比亚迪 汉），否则订单没法关联车辆；
> 同时初始化 3 条演示公告、2 个演示活动、4 个积分商品，并给 owner001 发 500 演示积分，
> 保证小程序首页和积分商城都能直接演示（表里已有数据则跳过）。

## 三、接口（阶段 2）

**在线接口文档（推荐）：** 启动后浏览器打开 <http://localhost:8081/swagger-ui.html>，
所有接口按模块分组，可以直接点「Try it out」调试。先调登录接口拿到 `data.token`，
点右上角 **Authorize** 粘贴 token（不用自己加 `Bearer ` 前缀），之后的请求会自动带上。
JSON 文档地址：<http://localhost:8081/v3/api-docs>。

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| POST | `/api/auth/login` | 公开 | 账号密码登录，返回 JWT |
| POST | `/api/auth/wx-login` | 公开 | 小程序 code 换 openid 登录（需配置 wx.appid/secret） |
| GET | `/api/user/profile` | 已登录 | 当前用户资料 |
| PUT | `/api/user/profile` | 已登录 | 修改昵称/手机号/头像 |
| GET | `/api/user/page` | ADMIN | 用户分页列表（keyword/role/status 筛选） |
| GET | `/api/user/{id}` | ADMIN | 用户详情 |
| GET | `/api/service-type/page` | ADMIN | 服务项目分页（name/category/status 筛选） |
| GET | `/api/service-type/list` | 登录 | 上架中的服务项目列表 |
| GET | `/api/service-type/{id}` | 登录 | 服务项目详情 |
| POST | `/api/service-type` | ADMIN | 新增服务项目 |
| PUT | `/api/service-type/{id}` | ADMIN | 修改服务项目 |
| PUT | `/api/service-type/{id}/status?status=1` | ADMIN | 上架/下架 |
| DELETE | `/api/service-type/{id}` | ADMIN | 逻辑删除 |
| GET | `/api/notice/list` | 登录 | 已发布公告列表（limit 默认 5、最大 20） |
| GET | `/api/notice/page` | ADMIN | 公告分页（标题/状态筛选） |
| GET | `/api/notice/{id}` | 登录 | 公告详情（车主只能看已发布） |
| POST | `/api/notice` | ADMIN | 新增公告 |
| PUT | `/api/notice/{id}` | ADMIN | 修改公告 |
| PUT | `/api/notice/{id}/status?status=1` | ADMIN | 发布/下线 |
| DELETE | `/api/notice/{id}` | ADMIN | 删除公告 |
| GET | `/api/activity/list` | 登录 | 进行中的活动列表（limit 默认 5、最大 20） |
| GET | `/api/activity/page` | ADMIN | 活动分页（标题/状态筛选） |
| GET | `/api/activity/{id}` | 登录 | 活动详情（车主只能看进行中） |
| POST | `/api/activity` | ADMIN | 新增活动 |
| PUT | `/api/activity/{id}` | ADMIN | 修改活动 |
| PUT | `/api/activity/{id}/status?status=1` | ADMIN | 上线/下线 |
| DELETE | `/api/activity/{id}` | ADMIN | 删除活动 |
| POST | `/api/activity/{id}/signup` | OWNER | 报名活动（仅 needSignup=1 的活动） |
| PUT | `/api/activity/{id}/signup/cancel` | OWNER | 取消报名 |
| GET | `/api/activity/{id}/signup/my` | OWNER | 我的报名状态 |
| GET | `/api/activity/{id}/signups` | ADMIN | 报名名单 |
| GET | `/api/stats/overview` | ADMIN | 管理端首页统计（用户/订单/营收/待办） |
| POST | `/api/order` | OWNER | 车主预约下单 |
| POST | `/api/order/admin-create` | ADMIN/STAFF | 到店开单（替车主建单） |
| GET | `/api/order/my` | OWNER | 我的订单（可按 status 筛选） |
| GET | `/api/order/page` | ADMIN/STAFF | 订单分页（订单号/状态/车牌/员工/日期） |
| GET | `/api/order/{id}` | 登录 | 订单详情（车主只能看自己的） |
| PUT | `/api/order/{id}/assign` | ADMIN | 派单给员工 |
| PUT | `/api/order/{id}/cancel` | 登录 | 取消订单（车主只能取消自己的，限待接单/已接单） |
| PUT | `/api/order/{id}/status` | ADMIN/STAFF | 状态流转（取消走取消接口） |
| GET | `/api/service-process/order/{orderId}` | 登录 | 订单服务进度时间线（车主只能看自己的） |
| POST | `/api/service-process` | ADMIN/STAFF | 登记服务进度（节点 + 说明 + 图片） |
| POST | `/api/message/evaluate` | OWNER | 评价已完成订单，订单同步变已评价 |
| POST | `/api/message` | OWNER | 提交留言（默认待审核） |
| GET | `/api/message/my` | OWNER | 我的评价与留言 |
| GET | `/api/message/order/{orderId}` | 登录 | 查询订单评价（没有时 data 为 null） |
| GET | `/api/message/page` | ADMIN | 留言/评价分页（type/status/keyword 筛选） |
| PUT | `/api/message/{id}/reply` | ADMIN | 回复留言/评价 |
| PUT | `/api/message/{id}/audit` | ADMIN | 审核（0 待审核 / 1 通过 / 2 隐藏） |
| GET | `/api/car/my` | OWNER | 我的车辆 |
| GET | `/api/car/user/{userId}` | ADMIN/STAFF | 指定车主的车辆（到店开单用） |
| POST | `/api/car` | OWNER | 新增车辆 |
| PUT | `/api/car/{id}` | OWNER | 修改车辆（只能改自己的） |
| DELETE | `/api/car/{id}` | OWNER | 删除车辆（已有订单的不能删） |
| GET | `/api/points/goods/list` | 登录 | 上架中的积分商品 |
| GET | `/api/points/goods/page` | ADMIN | 积分商品分页（name/status 筛选） |
| POST | `/api/points/goods` | ADMIN | 新增积分商品 |
| PUT | `/api/points/goods/{id}` | ADMIN | 修改积分商品 |
| PUT | `/api/points/goods/{id}/status?status=1` | ADMIN | 上架/下架 |
| DELETE | `/api/points/goods/{id}` | ADMIN | 逻辑删除积分商品 |
| POST | `/api/points/exchange` | OWNER | 兑换积分商品（扣积分 + 扣库存 + 写流水） |
| GET | `/api/points/exchange/my` | OWNER | 我的兑换记录 |
| GET | `/api/points/exchange/page` | ADMIN/STAFF | 兑换单分页（单号/状态/用户/商品筛选） |
| PUT | `/api/points/exchange/{id}/verify` | ADMIN/STAFF | 到店核销（待核销 → 已核销） |
| GET | `/api/points/record/my` | OWNER | 我的积分流水（limit 默认 50、最大 100） |

服务项目新增示例：

```json
{
  "name": "轮毂深度清洁",
  "category": "洗车",
  "description": "去除轮毂铁粉与刹车粉尘",
  "price": 88,
  "duration": 45,
  "pointsRate": 1.0,
  "status": 1
}
```

订单状态与请求示例：

```
PENDING 待接单 → ACCEPTED 已接单 → IN_PROGRESS 服务中 → COMPLETED 已完成 → EVALUATED 已评价
PENDING / ACCEPTED 可以取消（CANCELLED）
```

```json
{
  "carId": 1,
  "serviceTypeId": 1,
  "appointmentTime": "2026-11-22T10:00:00",
  "remark": "第一次体验"
}
```

时间字段使用 ISO-8601 格式（`yyyy-MM-ddTHH:mm:ss`），金额由后端按服务项目价格计算，前端传的价格不生效。

除 `/api/auth/**` 外，所有请求都要带请求头：

```
Authorization: Bearer <token>
```

响应统一为 `{ code, message, data }`，`code = 200` 表示成功。
出错时 HTTP 状态码与 `code` 一致（例如 401 / 403 / 404 / 400），前端和日志系统都可以直接按状态码判断。

## 四、Postman 验收步骤

1. `POST /api/auth/login`，Body（JSON）：

   ```json
   { "username": "admin", "password": "admin123" }
   ```

   从响应 `data.token` 复制 token。

2. `GET /api/user/page?page=1&size=10`，Headers 加 `Authorization: Bearer <token>`。
   返回 `data.total = 3`、`data.records` 三条用户即通过。

3. 不带 token 再请求一次，应返回 `code: 401`，说明拦截器生效。

## 五、配置说明

- 数据库账号密码在 `src/main/resources/application.yml`，可用环境变量 `DB_USERNAME` / `DB_PASSWORD` 覆盖。
- JWT 密钥 `jwt.secret` 至少 32 字节，上线前必须换。
### 微信登录配置

1. 小程序 AppID 在 `miniprogram/project.config.json`；AppSecret 在微信公众平台「开发管理 → 开发设置」生成。
2. 在 `backend/src/main/resources/application-local.yml`（已被 git 忽略）中填写：

   ```yaml
   wx:
     appid: wxd3519a64581e566c
     secret: 你的AppSecret
   ```

   或使用环境变量 `WX_APPID` / `WX_SECRET`。
3. 重启后端，`POST /api/auth/wx-login` 用 `wx.login` 的 code 换 openid。
4. 小程序 `miniprogram/config.js`：
   - `USE_WX_LOGIN = true`：使用微信登录
   - `WX_LOGIN_FALLBACK = true`：微信登录失败时回退演示账号（开发期建议 true，正式演示建议 false）
5. 开发者工具「清缓存 → 清除数据缓存」后重新编译。首次微信登录会自动在 `sys_user` 建一个 `OWNER` 账号，昵称「微信用户」。

> 微信登录创建的账号和演示账号 `owner001` 数据不互通，切换登录方式时小程序会自动清掉旧 token。
- Redis 已引入依赖和配置，阶段 4 做登录态/缓存时启用，现在不启动 Redis 也不影响运行。

## 六、日志

- 开发环境（默认）：输出到控制台，`com.example.beauty` 为 DEBUG，能看到 MyBatis SQL。
- 生产环境：启动加 `--spring.profiles.active=prod`，同时写入 `logs/beauty.log`（按天 + 50MB 滚动，保留 30 天，压缩归档），SQL 不再打印。
- 每个请求自动生成 8 位 `traceId`，日志里显示为 `[traceId]`，同时通过响应头 `X-Trace-Id` 返回，排查问题时可把同一次请求的日志筛出来。
- 访问日志只记录 `/api/**` 的方法、路径、耗时、用户、IP，不记录请求体和 token。
