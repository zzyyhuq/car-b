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

除 `/api/auth/**` 外，所有请求都要带请求头：

```
Authorization: Bearer <token>
```

响应统一为 `{ code, message, data }`，`code = 200` 表示成功。

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
- 微信登录的 `wx.appid` / `wx.secret` 在阶段 3 填，也可以用环境变量 `WX_APPID` / `WX_SECRET`。
- Redis 已引入依赖和配置，阶段 4 做登录态/缓存时启用，现在不启动 Redis 也不影响运行。
