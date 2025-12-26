# PhotoDisposeSystem - Smart Image Processing Platform

本项目为“Smart Image Processing Platform”课程项目的 Spring Boot 实现版本，采用前后端分离架构设计，后端提供 REST API。AI 推理服务默认通过 HTTP 调用（不在本项目内实现）。

## 技术栈
- **后端**：Spring Boot 3 (Java 17)
- **数据库**：H2 内存数据库
- **架构**：REST API + 前端独立部署（未包含在本仓库）

## 预设账号
系统启动时会自动注入以下账号（密码均为 `password123`）：
- `student_a` (LEADER)
- `student_b` (MEMBER)
- `student_c` (MEMBER)

## 本地运行
```bash
mvn spring-boot:run
```

默认端口：`http://localhost:8080`

## H2 控制台
- 地址：`http://localhost:8080/h2-console`
- JDBC URL：`jdbc:h2:mem:photo_dispose_db`
- 用户名：`sa`
- 密码：`(空)`

## 核心 API 示例
### F1 用户注册/登录
- `POST /api/auth/register`
- `POST /api/auth/login`

请求示例：
```json
{
  "username": "new_user",
  "password": "password123"
}
```

### F2 图片上传/管理
- `POST /api/images/upload?userId=1` (multipart/form-data, file)
- `GET /api/images?userId=1`
- `GET /api/images/{imageId}/download`

### F3 智能增强
- `POST /api/ai/enhance?userId=1&imageId=1`

### F4 风格迁移
- `POST /api/ai/style-transfer?userId=1&imageId=1&style=van-gogh`

### F5 背景替换
- `POST /api/ai/background-replace?userId=1&imageId=1&template=studio`

### F6 压缩 & 格式转换
- `POST /api/tasks/compress?userId=1&imageId=1`
- `POST /api/tasks/convert?userId=1&imageId=1&format=png`

### F7 历史记录 & 批处理
- `GET /api/tasks/history?userId=1`
- `POST /api/tasks/batch`

批处理请求示例：
```json
{
  "userId": 1,
  "tasks": [
    { "imageId": 1, "taskType": "ENHANCE" },
    { "imageId": 2, "taskType": "CONVERT", "option": "jpg" }
  ]
}
```

## AI 推理服务配置
默认 AI 服务地址为 `http://localhost:8081`。如需修改，请在 `application.yml` 中调整：
```yml
app:
  ai-service-url: http://localhost:8081
```

AI 服务不可用时，系统会返回可读的错误信息，不会导致整体流程崩溃。

## 说明
- 本项目仅提供后端 REST API 示例，不包含前端实现。
- AI 推理服务默认模拟调用，**不实现模型本体**。
