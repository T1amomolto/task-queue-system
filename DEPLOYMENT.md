# 部署指南

## 开发环境部署

### 1. 本地环境准备

#### 安装 MySQL
```bash
brew install mysql
brew services start mysql
mysql -u root -p
```

#### 安装 Redis
```bash
brew install redis
brew services start redis
```

### 2. 启动应用
```bash
git clone https://github.com/T1amomolto/task-queue-system.git
cd task-queue-system
mvn clean package
java -jar target/task-queue-system-1.0.0.jar
```

## Docker 部署

### 使用 docker-compose
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

  redis:
    image: redis:7
    ports:
      - "6379:6379"

  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/task_queue
      SPRING_REDIS_HOST: redis
```

```bash
docker-compose up -d
```

## 性能优化

### JVM 参数
```bash
java -Xms2g -Xmx4g -XX:+UseG1GC -jar task-queue-system-1.0.0.jar
```

### 数据库连接池
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
```

## 监控和日志

配置日志
```yaml
logging:
  level:
    root: INFO
    com.taskqueue: DEBUG
  file:
    name: logs/application.log
```
