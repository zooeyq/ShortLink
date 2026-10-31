# ShortLink 短链接服务

基于 Spring Boot、MyBatis 和 MySQL 的短链接后端学习项目，目前实现创建短链接、数据库保存和访问跳转。

## 已实现功能

- `POST /api/links`：接收 JSON，校验原网址和有效期，生成 7 位 Base62 短码并保存数据库。
- `GET /s/{shortCode}`：查询映射并返回 302 重定向；不存在、停用、过期分别返回 404、403、410。
- 创建和错误响应使用统一的 `Result`；重定向成功时通过 `Location` 响应头跳转，没有 JSON 响应体。
- 数据库唯一索引防止短码重复，发生冲突时最多尝试 5 次。

## 本地运行

1. 安装 JDK 17 或以上、Maven 和 MySQL 8.0.16 或以上。
2. 创建 `shortlink` 数据库，并在该数据库中执行 `src/main/resources/db/schema.sql`。
3. 在 `application.yml` 中核对数据库地址及用户名，在启动环境中设置 `DB_PASSWORD`。不要将真实密码提交到仓库。
4. 运行 `ShortLinkApplication`，服务默认监听 `http://localhost:8080`。

使用自定义域名时，通过 `short-link.base-url` 配置生成短链接的地址前缀。

创建请求示例：

```http
POST /api/links
Content-Type: application/json
```

```json
{
  "originalUrl": "https://example.com",
  "expiresAt": null
}
```

`expiresAt` 可以省略或为 `null`，表示长期有效；非空时格式为 `yyyy-MM-dd HH:mm:ss`，按服务器本地时间解释，必须晚于当前时间。

## 验证

```powershell
mvn "-Dtest=CreateLinkTests,RedirectTests" test
```

这些测试验证参数校验、响应结构、短码重试和跳转行为，使用测试替身，不连接实际数据库。完整应用启动测试需要可用的数据库配置。

## 当前范围

项目尚未实现登录鉴权、管理页面、访问统计和限流，适合本地开发学习，公开部署前需要进一步完善。
