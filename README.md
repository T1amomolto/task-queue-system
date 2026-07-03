# 分布式任务队列系统 (Task Queue System)

一个基于 Spring Boot + Redis + MySQL 的轻量级分布式任务队列系统，适合处理异步任务、延时任务和定时任务。

## 仓库信息

- 仓库: T1amomolto/task-queue-system
- 仓库 ID: 1287805666
- 默认分支: main
- 主要语言：Java（后端）
- 前端：Vue 3 / JavaScript（位于 web/ 目录）

> 说明：语言检测基于仓库文件结构与元数据；后端以 Java 为主，前端位于 web 目录使用 Vue 3 / JavaScript。

## 📌 项目特性

### 核心功能
- ✅ **异步任务处理** - 基于 Redis 的分布式消息队列
- ✅ **优先级队列** - 支持 URGENT、HIGH、MEDIUM、LOW 四个优先级
- ✅ **自动重试机制** - 可配置最大重试次数和退避策略
- ✅ **任务监控** - 实时监控任务执行状态和队列深度
- ✅ **完整的日志系统** - 记录每个任务的执行流程
- ✅ **RESTful API** - 完整的 HTTP 接口供外部调用

### 技术栈
- **后端框架**: Spring Boot 3.1.0
- **数据库**: MySQL 8.0
- **缓存队列**: Redis
- **ORM**: Spring Data JPA
- **构建工具**: Maven
- **Java 版本**: 17

## 🚀 快速开始

### 前置条件
- Java 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+

### 环境配置

```bash
# 1. 创建 MySQL 数据库
mysql -u root -p -e "CREATE DATABASE task_queue CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 2. 初始化表结构
mysql -u root -p task_queue < src/main/resources/db/schema.sql

# 3. 克隆项目
git clone https://github.com/T1amomolto/task-queue-system.git
cd task-queue-system

# 4. 编译和启动
mvn clean package
java -jar target/task-queue-system-1.0.0.jar
```

应用启动后访问: `http://localhost:8080/api`

## 📚 API 文档

### 创建任务
```http
POST /api/tasks
Content-Type: application/json

{
  "title": "发送邮件",
  "description": "向用户发送通知",
  "payload": "{\"email\": \"user@example.com\"}",
  "priority": "HIGH",
  "maxRetries": 3
}
```

### 获取任务详情
```http
GET /api/tasks/{taskId}
```

### 列表查询
```http
GET /api/tasks?page=0&size=20
```

### 按状态查询
```http
GET /api/tasks/status/PENDING?page=0&size=20
```

### 更新状态
```http
PUT /api/tasks/{taskId}/status?status=PROCESSING
```

### 获取统计
```http
GET /api/tasks/stats/summary
```

## 🔧 核心组件

- **TaskService** - 业务逻辑层
- **TaskQueue** - Redis 队列管理
- **TaskWorker** - 后台异步处理
- **TaskMonitor** - 监控告警模块
- **TaskController** - REST API 接口

## web 管理后台 (web/)

仓库包含一个基于 Vue 3 + Element Plus 的管理后台，位于 `web/` 目录。更多说明见 `web/README.md`。

## 📝 许可证

MIT License
