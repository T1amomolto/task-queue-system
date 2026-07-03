# 部署指南

## 开发环境部署

### 1. 本地环境准备

#### 安装 MySQL
```bash
# macOS (使用 Homebrew)
brew install mysql

# 启动 MySQL
brew services start mysql

# 连接 MySQL
mysql -u root -p
```

#### 安装 Redis
```bash
# macOS
brew install redis

# 启动 Redis
brew services start redis
```

#### 创建数据库
```sql
CREATE DATABASE task_queue CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE task_queue;

-- 执行 schema.sql 中的所有 SQL 语句
SOURCE src/main/resources/db/schema.sql;
```

### 2. 启动应用
```bash
# 克隆项目
git clone https://github.com/T1amomolto/task-queue-system.git
cd task-queue-system

# 构建项目
mvn clean package

# 启动应用
java -jar target/task-queue-system-1.0.0.jar
```

应用将在 `http://localhost:8080/api` 启动。

## 生产环境部署

### 1. Docker 部署

#### 创建 Dockerfile
```dockerfile
FROM openjdk:17-slim

WORKDIR /app

COPY target/task-queue-system-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### 构建镜像
```bash
mvn clean package
docker build -t task-queue-system:1.0.0 .
```

#### 运行容器
```bash
docker run -d \
  --name task-queue-system \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/task_queue \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=root \
  -e SPRING_REDIS_HOST=redis \
  task-queue-system:1.0.0
```

### 2. Docker Compose 部署

#### 创建 docker-compose.yml
```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: task_queue
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./src/main/resources/db/schema.sql:/docker-entrypoint-initdb.d/schema.sql

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data

  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/task_queue
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: root
      SPRING_REDIS_HOST: redis
      SPRING_REDIS_PORT: 6379
    depends_on:
      - mysql
      - redis

volumes:
  mysql_data:
  redis_data:
```

#### 启动所有服务
```bash
docker-compose up -d
```

### 3. Kubernetes 部署

#### 创建 Namespace
```bash
kubectl create namespace task-queue
```

#### 创建 Deployment
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: task-queue-system
  namespace: task-queue
spec:
  replicas: 3
  selector:
    matchLabels:
      app: task-queue-system
  template:
    metadata:
      labels:
        app: task-queue-system
    spec:
      containers:
      - name: task-queue-system
        image: task-queue-system:1.0.0
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_DATASOURCE_URL
          value: "jdbc:mysql://mysql-service:3306/task_queue"
        - name: SPRING_REDIS_HOST
          value: "redis-service"
```

#### 创建 Service
```yaml
apiVersion: v1
kind: Service
metadata:
  name: task-queue-service
  namespace: task-queue
spec:
  type: LoadBalancer
  ports:
  - port: 8080
    targetPort: 8080
  selector:
    app: task-queue-system
```

### 4. 性能配置

#### JVM 参数优化
```bash
java -Xms2g -Xmx4g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+PrintGCDetails \
  -Xloggc:gc.log \
  -jar task-queue-system-1.0.0.jar
```

#### 数据库连接池配置
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
```

#### Redis 连接池配置
```yaml
spring:
  redis:
    jedis:
      pool:
        max-active: 50
        max-idle: 25
        min-idle: 10
```

## 监控和日志

### 1. 日志配置
```yaml
logging:
  level:
    root: INFO
    com.taskqueue: DEBUG
  file:
    name: logs/application.log
    max-size: 100MB
    max-history: 30
```

### 2. 健康检查
添加 Spring Boot Actuator：
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

### 3. 监控指标
- 任务队列长度
- 任务处理速率
- 失败率
- 平均响应时间

## 备份和恢复

### 1. MySQL 备份
```bash
# 每日备份
mysqldump -u root -p task_queue > backup_$(date +%Y%m%d).sql
```

### 2. Redis 备份
```bash
# 手动触发持久化
redis-cli BGSAVE
```

## 故障排查

### 1. 检查服务状态
```bash
# 检查 MySQL
mysql -u root -p -e "SELECT 1"

# 检查 Redis
redis-cli ping

# 检查应用
curl http://localhost:8080/api/tasks/stats/summary
```

### 2. 查看日志
```bash
# 实时查看日志
tail -f logs/application.log

# 查看错误日志
grep ERROR logs/application.log
```

### 3. 数据库连接问题
```bash
# 检查连接数
mysql -u root -p -e "SHOW PROCESSLIST;"

# 检查连接限制
mysql -u root -p -e "SHOW VARIABLES LIKE 'max_connections';"
```

## 升级指南

### 版本升级步骤
1. 备份数据库和 Redis
2. 编译新版本
3. 灰度发布（逐个更新实例）
4. 监控应用性能
5. 如出现问题，回滚到前一版本

## 安全加固

- 使用 HTTPS/TLS 通信
- 设置强密码和定期轮换
- 限制数据库和 Redis 访问 IP
- 启用审计日志
- 定期更新依赖和系统补丁
