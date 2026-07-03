# 分布式任务队列系统 - 面试回答稿

> 这份文档包含了秋招面试中最可能被问到的问题及详细回答。建议反复阅读，形成自己的理解。

---

## 📌 Part 1: 项目介绍（2-3分钟）

### Q1: 请介绍一下你的项目

**标准回答：**

"我开发了一个分布式异步任务队列系统，这是一个中等规模的个人项目，耗时2周左右完成。

**技术栈是 Spring Boot 3.1 + Redis + MySQL**，主要用来处理邮件发送、文件转换、短信通知等后台异步任务。

**系统的核心设计是：**
- 用户请求创建任务时，任务立即存入数据库并入队 Redis，立即返回任务 ID
- 后台有一个 TaskWorker 线程持续从 Redis 队列中取出任务，按照优先级处理
- 处理成功就标记为成功，失败就自动重试，超过重试次数后标记为失败

**主要功能包括：**
1. 优先级队列 - 支持 4 个优先级（URGENT、HIGH、MEDIUM、LOW）
2. 自动重试机制 - 失败自动重新入队，最多重试 3 次
3. 完整的监控系统 - 实时监控任务执行情况和队列深度
4. 9 个 RESTful API - 完整的任务管理接口
5. 生产级部署 - 支持 Docker 和 Kubernetes

**整个项目从需求分析、系统设计、代码实现到 Docker 部署都完全闭合。"

---

### Q2: 为什么要做这个项目？

**标准回答：**

"有几个原因：

1. **学习分布式系统** - 任务队列系统是分布式系统的经典案例，涉及并发、消息队列、分布式事务等核心概念

2. **掌握生产级技术** - Redis、MySQL、Docker 这些都是企业必备技能

3. **完整的项目经验** - 从 0 到 1 搭建一个完整系统，包括设计、开发、测试、部署全流程

4. **面试准备** - 这是一个很好的项目展示自己后端开发能力的机会"

---

## 🏗️ Part 2: 系统架构（面试常问）

### Q3: 请讲一下你的系统架构设计

**标准回答（配合画图）：**

```
┌─────────────────────────────────────────────────────┐
│                 Client / API                         │
└────────────────┬────────────────────────────────────┘
                 │
┌────────────────▼────────────────────────────────────┐
│            TaskController (REST API)                 │
└────────────────┬────────────────────────────────────┘
                 │
┌────────────────▼────────────────────────────────────┐
│              TaskService (业务逻辑)                   │
│  - createTask     - updateStatus                     │
│  - getTask        - handleFailure                    │
│  - listTasks      - handleSuccess                    │
└────────────────┬────────────────────────────────────┘
                 │
        ┌────────┴────────┬──────────────┐
        │                 │              │
        ▼                 ▼              ▼
┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│  MySQL DB    │  │ Redis Queue  │  │ TaskWorker   │
│              │  │              │  │ (异步线程)   │
│  tasks表     │  │ 4个优先级队列 │  │ - 取任务     │
│  task_logs表 │  │ 处理中任务   │  │ - 执行任务   │
└──────────────┘  └──────────────┘  │ - 记录结果   │
                                      └──────────────┘
                                            │
                                    ┌───────┴────────┐
                                    │                │
                              ┌─────▼─────┐  ┌─────▼─────┐
                              │  Success  │  │  Failed   │
                              │  Retry    │  │  Cancel   │
                              └───────────┘  └───────────┘
```

"具体来说：

1. **API 层** - TaskController 提供 9 个 REST 接口

2. **业务逻辑层** - TaskService 处理任务的 CRUD 和状态管理

3. **数据访问层** - TaskRepository 操作 MySQL 数据库

4. **消息队列** - TaskQueue 基于 Redis 的 List 实现 4 个优先级队列

5. **异步处理** - TaskWorker 后台线程持续从队列取任务并执行

6. **监控告警** - TaskMonitor 定期检查系统状态

**关键设计点：**
- 写入数据库 + 入队 Redis，保证可靠性
- 优先级队列确保紧急任务优先处理
- 失败自动重试，重试超限后标记为失败
- 整个系统支持水平扩展，多个实例可共享 Redis 和 MySQL"

---

### Q4: 为什么选择 Redis 而不是 RabbitMQ 或 Kafka？

**标准回答：**

"这是一个很好的问题。我来对比一下：

| 方案 | 优点 | 缺点 | 适用场景 |
|------|------|------|--------|
| Redis | 简单快速、低延迟、开发快 | 持久化不如专业 MQ、集群复杂 | 中小型异步任务 |
| RabbitMQ | 可靠性高、支持事务 | 部署复杂、性能一般 | 对可靠性要求高 |
| Kafka | 高吞吐量、分布式、持久化 | 复杂、学习成本高 | 流计算、日志处理 |

**我选择 Redis 的原因：**

1. **项目规模适中** - 这是个中等规模的个人项目，不需要 Kafka 这么强大的工具

2. **开发效率** - Redis 开箱即用，5 分钟就能跑起来，而 Kafka 需要 30 分钟

3. **功能足够** - 对于邮件、短信、文件处理这类任务，Redis 完全够用

4. **学习成本** - 大多数学生都用过 Redis，容易上手

**但如果生产环境需要更高的可靠性，我会：**
- 配置 Redis 持久化（RDB + AOF）
- 使用 Redis Cluster 提高高可用性
- 或者迁移到 RabbitMQ/Kafka（核心逻辑不变，只需要改队列实现）

这也是这个项目的优点 - **架构解耦，可以轻松替换 MQ 实现。**"

---

## 💾 Part 3: 核心技术（深度面试）

### Q5: 讲一下你的数据库设计

**标准回答：**

"我设计了两个核心表：

**1. tasks 表**
```sql
CREATE TABLE tasks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255),              -- 任务标题
    status VARCHAR(50),              -- 任务状态（6种）
    priority VARCHAR(50),            -- 优先级（4种）
    payload VARCHAR(500),            -- 任务数据
    retry_count INT,                 -- 已重试次数
    max_retries INT,                 -- 最大重试次数
    created_at DATETIME,             -- 创建时间
    updated_at DATETIME,             -- 更新时间
    started_at DATETIME,             -- 开始时间
    completed_at DATETIME,           -- 完成时间
    error_message TEXT,              -- 错误信息
    execution_time BIGINT,           -- 执行耗时（ms）
    
    INDEX idx_status (status),       -- 查询待处理任务
    INDEX idx_priority (priority),   -- 查询不同优先级
    INDEX idx_created_at (created_at) -- 查询日期范围
);
```

**2. task_logs 表**
```sql
CREATE TABLE task_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id BIGINT FOREIGN KEY,      -- 关联任务
    level VARCHAR(50),               -- 日志级别
    message TEXT,                    -- 日志消息
    created_at DATETIME,
    
    INDEX idx_task_id (task_id),     -- 快速查询某任务的日志
    INDEX idx_created_at (created_at)
);
```

**为什么这样设计：**

1. **索引策略**
   - status 索引：支持快速查询待处理任务（SELECT * FROM tasks WHERE status='PENDING'）
   - priority 索引：支持按优先级查询
   - created_at 索引：支持日期范围查询

2. **冗余字段**
   - retry_count、max_retries：避免每次重试都查日志表
   - execution_time：记录执行耗时，便于性能分析

3. **分离 logs 表**
   - 任务表保持精简，查询快速
   - 日志表单独管理，可以定期归档

**优化空间：**
- 如果数据量大（百万级），可以按日期分区
- 可以定期将完成的任务归档到历史表
- 使用 JSON 存储 payload，支持更复杂的数据"

---

### Q6: 讲一下你的 Redis 队列实现

**标准回答：**

"我用 Redis 的 List 数据结构实现了优先级队列。

**关键代码思路：**
```java
// 1. 入队
public void enqueue(Task task) {
    String key = \"task:queue:\" + task.getPriority().ordinal();
    // URGENT=0, HIGH=1, MEDIUM=2, LOW=3
    String value = objectMapper.writeValueAsString(task);
    redisTemplate.opsForList().rightPush(key, value); // 右插
}

// 2. 出队（按优先级）
public String dequeue(Task.TaskPriority priority) {
    String key = \"task:queue:\" + priority.ordinal();
    return redisTemplate.opsForList().leftPop(key); // 左取，FIFO
}

// 3. 标记为处理中
public void markProcessing(Long taskId, String taskData) {
    String key = \"task:processing:\" + taskId;
    redisTemplate.opsForValue().set(key, taskData, 86400, TimeUnit.SECONDS);
}
```

**为什么这样设计：**

1. **为什么用 List 而不是 Queue?**
   - List 支持阻塞操作（blpop），可以实现高效的消费者模式
   - List 有左右两端，rightPush + leftPop = FIFO 队列

2. **为什么分开 4 个队列?**
   - 分离优先级，确保高优���级任务优先处理
   - URGENT 的任务不会被 LOW 优先级任务阻塞

3. **处理中任务的作用**
   - 记录正在处理的任务，防止重复处理
   - 宕机恢复时，可以找到那些还没完成的任务

4. **Redis 的优势**
   - 单线程，天然支持并发
   - 原子操作，不用考虑分布式锁
   - 内存存储，性能极高（10w+ TPS）

**缺点和改进：**
- Redis 重启数据丢失？配置 RDB 持久化，定期备份
- 想要更强的可靠性？改用 Kafka 或 RabbitMQ
- 单点故障？配置 Redis Sentinel 或 Cluster"

---

### Q7: 讲一下任务的失败重试机制

**标准回答：**

"这是系统最关键的部分，保证了任务的可靠性。

**重试流程：**
```
任务执行
    ↓
[是否异常?]
    ├─ 否 → 标记 SUCCESS，任务完成
    └─ 是 → retry_count++
           ↓
       [retry_count < maxRetries?]
           ├─ 是 → 状态改为 RETRYING，入重试队列（HIGH优先级）
           └─ 否 → 状态改为 FAILED，完成
```

**核心代码：**
```java
@Transactional
public void handleTaskFailure(Long taskId, String errorMessage) {
    Task task = taskRepository.findById(taskId).orElseThrow();
    
    task.setErrorMessage(errorMessage);
    task.setRetryCount(task.getRetryCount() + 1);
    
    if (task.getRetryCount() < task.getMaxRetries()) {
        // 还有重试机会
        task.setStatus(TaskStatus.RETRYING);
        taskQueue.moveToRetry(task); // 入队，HIGH优先级
        taskLogService.addLog(taskId, \"第\" + task.getRetryCount() + \"次重试\", \"WARN\");
    } else {
        // 重试已满
        task.setStatus(TaskStatus.FAILED);
        task.setCompletedAt(LocalDateTime.now());
        taskLogService.addLog(taskId, \"失败，放弃重试\", \"ERROR\");
    }
    
    taskRepository.save(task);
}
```

**设计要点：**

1. **为什么入 HIGH 优先级队列？**
   - 重试任务应该比新任务优先处理
   - 否则老任务一直重试不了，新任务反复入队

2. **为什么保存到数据库？**
   - Redis 重启数据丢失
   - 数据库持久化，宕机后可以恢复

3. **完整的日志记录**
   - 追踪每次重试的原因
   - 便于后期分析和调试

4. **可配置的重试策略**
   - maxRetries 字段可配置
   - 不同类型的任务可以有不同的重试次数

**改进方向：**
- 增加指数退避策略：第 1 次立即重试，第 2 次等 1 秒，第 3 次等 2 秒...
- 添加死信队列：重试失败的任务转移到死信队列，人工处理
- 支持错误分类：网络错误重试，业务错误不重试"

---

### Q8: 如何保证任务不丢失？

**标准回答（三层保障）：**

"这是分布式系统最重要的问题。我采用了三层保障：

**第一层：数据库持久化**
```java
// 任务创建时立即存入数据库
Task savedTask = taskRepository.save(task); // 持久化到 MySQL
taskQueue.enqueue(savedTask);              // 再入 Redis 队列
return TaskResponse.fromEntity(savedTask);
```
好处：
- 即使 Redis 全部宕机，任务也不会丢
- 宕机重启后可以扫描 PENDING 状态的任务重新入队

**第二层：Redis 持久化**
```yaml
# application.yml 配置 Redis 持久化
spring:
  redis:
    # 内存到磁盘持久化，防止宕机丢数据
```
好处：
- 减少内存 → 磁盘的同步延迟
- Redis 重启前数据保存在磁盘

**第三层：失败重试机制**
```
任务失败 → 自动重试（最多3次）
```
好处：
- 暂时的网络抖动不会导致任务失败
- 提高整体任务的完成率

**三层保障的效果：**
```
成功率 = (直接成功概率) + (一次失败后恢复概率) + (数据库恢复概率)
      = 95% + (5% × 95%) + (5% × 5% × 恢复成功率)
      ≈ 99.7% 左右
```

**实测场景：**
- 正常情况：99%+ 任务成功
- Redis 宕机：数据库中的任务不丢，待恢复后可重新处理
- 应用宕机：Redis 中的队列保留，重启后继续处理

**进一步优化：**
- 配置 Redis Sentinel：主从切换，自动故障转移
- 配置 Redis Cluster：分布式存储，容错能力强
- 添加死信队列：失败多次的任务转移，人工介入"

---

### Q9: 如何防止重复消费？

**标准回答：**

"这是一个经典的分布式问题。我从几个层面保证幂等性：

**第一层：任务 ID 唯一性（数据库约束）**
```java
@Entity
@Table(name = \"tasks\", uniqueConstraints = {
    @UniqueConstraint(columnNames = \"id\")
})
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 数据库自增 ID，全局唯一
    // ...
}
```

**第二层：执行前检查**
```java
private void executeTask(Task task) {
    Long taskId = task.getId();
    
    // 检查任务是否已被其他实例处理
    String processingKey = \"task:processing:\" + taskId;
    String currentOwner = redisTemplate.opsForValue().get(processingKey);
    if (currentOwner != null) {
        log.warn(\"Task {} is being processed by another instance\", taskId);
        return; // 已有其他实例在处理，退出
    }
    
    // 标记为处理中
    taskQueue.markProcessing(taskId, \"instance-\" + getInstanceId());
    
    try {
        // 执行任务
        simulateTaskExecution(task);
        taskService.handleTaskSuccess(taskId);
    } catch (Exception e) {
        taskService.handleTaskFailure(taskId, e.getMessage());
    } finally {
        taskQueue.removeProcessing(taskId);
    }
}
```

**第三层：业务逻辑幂等性**
```java
// 对于邮件发送这种操作，需要业务层面的幂等性设计
@Transactional
public void sendEmail(String email, String subject) {
    // 检查是否已发送过
    EmailRecord record = emailRepository.findByEmailAndSubject(email, subject);
    if (record != null && record.getSentAt() != null) {
        log.info(\"Email already sent to {}\", email);
        return; // 幂等返回
    }
    
    // 发送邮件
    emailService.send(email, subject);
    
    // 记录发送
    record.setSentAt(LocalDateTime.now());
    emailRepository.save(record);
}
```

**实现方式对比：**

| 方式 | 原理 | 优点 | 缺点 |
|------|------|------|------|
| 数据库 UNIQUE 索引 | 重复插入报错 | 简单 | 需要捕获异常 |
| 分布式锁 | Redis SET NX | 高效 | 实现复杂 |
| 版本号 | 乐观锁 | 并发高 | 需要额外字段 |
| 业务 ID 去重 | 应用层检查 | 灵活 | 性能依赖查询 |

**我的方案的优点：**
1. 分层设计，多重保护
2. Redis 处理中标记性能高
3. 数据库最终一致性
4. 支持应用宕机恢复"

---

## 🔧 Part 4: 高级问题（笔试 + 深度面试）

### Q10: 你的系统最多能处理多少并发？

**标准回答：**

"这取决于几个因素，让我分别计算：

**单机能力分析：**

1. **TaskWorker 处理能力**
```
配置：
- 线程池大小：10 个线程（可配置）
- 单个任务处理时间：平均 1-6 秒（模拟）
- 每轮批处理：10 个任务（可配置）

计算：
- 单线程 TPS = 1 / 平均耗时 = 1 / 3秒 ≈ 0.33 TPS
- 10 线程 TPS = 0.33 × 10 ≈ 3-5 TPS
- 实际处理能力 = 3-5 TPS（考虑 I/O、网络等）
```

2. **API 接口并发能力**
```
Tomcat 默认配置：
- 默认线程数：200
- 单个请求处理时间：< 100ms（只是入队，不实际处理）
- 数据库连接池：20

计算：
- API 吞吐量 = 200 / 0.1 = 2000 TPS
- 但数据库连接池限制在 20，实际约 200 TPS 入队
```

3. **Redis 处理能力**
```
Redis 单线程模���，官方数据：
- GET/SET 性能：100k+ TPS
- List 操作：50k+ TPS
- 我们的使用场景不会成为瓶颈
```

**综合分析：**
```
单机最大吞吐 ≈ min(入队能力, 处理能力)
              = min(200 TPS 入队, 3-5 TPS 处理)
              = 3-5 TPS 处理能力成为瓶颈
```

**为什么处理慢？**
- 任务执行时间长（1-6秒，这里是模拟）
- 线程池大小限制（10个线程）
- 我/O 阻塞（等待数据库、HTTP 调用等）

**提升吞吐的方法：**

1. **增加线程数**
```yaml
task-queue:
  worker:
    thread-pool-size: 50  # 从 10 增加到 50
```
效果：处理能力 × 5，但 CPU 和内存消耗也增加

2. **异步处理**
```java
// 使用异步回调，不阻塞线程
CompletableFuture.runAsync(() -> {
    // 处理任务
});
```

3. **水平扩展**
```
多个应用实例共享 Redis 和 MySQL：
2个实例 = 单机 × 2 = 10 TPS
10个实例 = 单机 × 10 = 50 TPS
```

4. **数据库优化**
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 50  # 增加连接池
```

5. **优化任务处理逻辑**
```
使用批处理：
- 单个任务 100ms → 批处理 50 个任务 500ms
- TPS：1/0.5 × 线程数 = 更高的吞吐
```

**生产环境建议：**
- 基础配置：50 个线程，连接池 50
- 预期吞吐：10-20 TPS
- 超过 50 TPS：需要多实例分布式部署
- 超过 500 TPS：考虑改用 Kafka + 流处理框架"

---

### Q11: 你遇到过什么问题？怎么解决的？

**标准回答（选择真实的、学到东西的）：**

**问题 1：重复消费**
"在开发 TaskWorker 时，我遇到了一个问题。

现象：同一个任务被多个实例处理了，导致邮件被发送两次。

原因分析：
- TaskWorker 从 Redis 取出任务后
- 但还没来得及标记 'processing'
- 另一个实例也取到了同一个任务
- 结果两个实例都处理了

解决方案：
```java
// 原来的逻辑
String taskData = taskQueue.dequeue(priority); // 从队列取
executeTask(task);

// 改进后
String taskData = taskQueue.dequeue(priority);
String processingKey = \"task:processing:\" + task.getId();

// 用 SET NX 原子操作标记处理中
if (redisTemplate.opsForValue().setIfAbsent(processingKey, instanceId)) {
    // 标记成功，这个任务属于我
    executeTask(task);
} else {
    // 已被其他实例标记，放回队列
    taskQueue.enqueue(task);
}
```

学到：分布式系统要用原子操作保证一致性。"

---

**问题 2：内存泄漏**
"在测试时，应用内存占用不断增长。

原因：
```java
// 原来的代码
while (true) {
    String taskData = taskQueue.dequeue(priority);
    if (taskData == null) {
        // BUG：一直在高频轮询
        continue;
    }
    executeTask(taskData);
}
```

这样会导致：
- 高 CPU 占用
- 无用对象不断创建
- GC 压力大

解决：
```java
// 改进后：用 Redis 的阻塞操作
String taskData = taskQueue.dequeue(priority); // 内部是 BLPOP，阻塞等待
// 没有任务时，线程睡眠，不占用 CPU

// 同时设置超时
redisTemplate.opsForList().leftPop(key, 1, TimeUnit.SECONDS);
```

学到：不能用轮询，要用阻塞 API。"

---

**问题 3：数据库连接不足**
"在压测时，创建 1000 个任务后，系统变得很慢。

原因：
```
原配置：
- connection-pool-size: 5
- 10 个 TaskWorker + API 线程竞争连接
- 都在等待获取连接，导致超时
```

解决：
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20  # 从 5 增加到 20
      minimum-idle: 5
      connection-timeout: 30000  # 30 秒超时
```

学到：要根据实际线程数来配置连接池大小。"

---

## 🎯 Part 5: 扩展问题

### Q12: 你的系统能否支持分布式部署？

**标准回答：**

"完全支持，而且这是我设计时就考虑的因素。

**设计原则：**
1. 所有状态存在数据库和 Redis，不在应用内存
2. 任务队列在 Redis，所有实例共享
3. 没有单点故障

**部署方式：**
```
┌─────────────────────────────────────────┐
│         Nginx 负载均衡                   │
└──────────┬──────────────┬────────────────┘
           │              │
    ┌──────▼──┐    ┌─────▼─────┐
    │ App 1   │    │  App 2    │
    │ :8080   │    │  :8081    │
    └──────┬──┘    └──────┬────┘
           │              │
    ┌──────▴──────────────┴──────┐
    │    Shared Redis Queue       │
    │  - task:queue:0 (URGENT)    │
    │  - task:queue:1 (HIGH)      │
    │  - task:queue:2 (MEDIUM)    │
    │  - task:queue:3 (LOW)       │
    └──────┬─────────────────────┘
           │
    ┌──────▴─────────┐
    │  MySQL Master  │
    │  (write)       │
    └────────────────┘
```

**并发处理：**
```
用户请求 → 任意实例创建任务
         → 立即返回
         → 后台 App1/App2 都会竞争该任务
         → 谁取到就谁处理
         → 同一任务只会被处理一次（通过 Redis SET NX）
```

**故障转移：**
```
App1 宕机：
- 它正在处理的任务在 Redis 的 processing:task_id 中
- 超时 24 小时没删除 → 定时任务清理 → 重新入队
- App2 继续处理

MySQL 宕机：
- 应用 SQL 异常，新任务无法创建
- 已入队的任务无法更新状态
- 解决：配置 MySQL 主从复制 + failover
```

**性能分析：**
```
单机：3-5 TPS
2 个实例：6-10 TPS
10 个实例：30-50 TPS

线性扩展成本低，只需要：
1. 多启动几个 App 实例
2. 使用 Docker / K8s 管理
3. 配置负载均衡
```

**生产级方案：**
```yaml
# kubernetes deployment
replicas: 10  # 10 个副本
resources:
  requests:
    memory: 512Mi
    cpu: 500m
  limits:
    memory: 1Gi
    cpu: 1000m
```"

---

### Q13: 如果数据库故障了怎么办？

**标准回答：**

"这是一个重要的容错问题。

**场景分析：**

1. **应��连接不上数据库**
```
症状：org.springframework.dao.DataAccessException

处理：
- 新任务无法创建（入口阻断）
- 已入队的任务可以继续处理
- 处理结果无法存入数据库

应对方案：
- 立即切换到备用数据库
- 使用 MySQL 主从复制 + 故障转移
- 或使用 RDS 自动故障转移
```

2. **数据库写入超时**
```
症状：insert/update 变慢，最后超时

处理：
```java
@Transactional(timeout = 10) // 10 秒超时
public void updateTaskStatus(Long taskId, TaskStatus status) {
    Task task = taskRepository.findById(taskId)...;
    task.setStatus(status);
    try {
        taskRepository.save(task); // 可能超时
    } catch (TransactionException e) {
        log.warn(\"Failed to save task status, will retry\", e);
        // 任务继续处理，状态不一致
        // 由定时任务后续修复
    }
}
```

3. **数据库磁盘满了**
```
症状：Insert into xxx error - Disk full

处理：
- 紧急删除旧数据（定期归档）
- 临时扩容磁盘
- 监控告警提前预警
```

**完整的容错方案：**

```java
@Service
public class DatabaseFailoverService {
    
    // 1. 监控数据库连接状态
    @Scheduled(fixedDelay = 5000)
    public void monitorDatabase() {
        try {
            jdbcTemplate.queryForObject(\"SELECT 1\", Integer.class);
        } catch (Exception e) {
            log.error(\"Database connection failed\");
            switchToBackupDatabase();
        }
    }
    
    // 2. 失败转移
    private void switchToBackupDatabase() {
        // 切换 DataSource 到备用 MySQL
        dataSourceRouter.setTarget(\"backup-db\");
        log.info(\"Switched to backup database\");
    }
    
    // 3. 定时修复不一致状态
    @Scheduled(fixedDelay = 60000)
    public void repairInconsistentTasks() {
        List<Task> inProcessing = taskRepository
            .findByStatus(TaskStatus.PROCESSING);
        
        for (Task task : inProcessing) {
            // 检查是否还在处理中
            String processingKey = \"task:processing:\" + task.getId();
            if (!redisTemplate.hasKey(processingKey)) {
                // 不在处理中，说明已完成或失败
                // 根据业务逻辑修复状态
            }
        }
    }
}
```

**最佳实践：**
1. MySQL 主从复制
2. 定期备份（每小时一次）
3. 监控告警（CPU、内存、磁盘）
4. 自动故障转移（MHA/Orchestrator）
5. 定时数据修复任务"

---

### Q14: 如何监控系统健康状态？

**标准回答：**

"我实现了完整的监控系统，包括实时监控和告警。

**1. 实时队列监控**
```java
@Component
public class TaskMonitor {
    
    @Scheduled(fixedDelayString = \"${task-queue.monitor.check-interval:60000}\")
    public void monitorQueueStatus() {
        long pendingCount = taskRepository.countByStatus(PENDING);
        long processingCount = taskRepository.countByStatus(PROCESSING);
        long successCount = taskRepository.countByStatus(SUCCESS);
        long failedCount = taskRepository.countByStatus(FAILED);
        
        log.info(\"STATS - PENDING: {}, PROCESSING: {}, SUCCESS: {}, FAILED: {}\",
            pendingCount, processingCount, successCount, failedCount);
        
        // 关键指标
        long urgentQueue = taskQueue.getQueueSize(URGENT);
        long highQueue = taskQueue.getQueueSize(HIGH);
        
        publishMetrics(\"task.queue.size\", urgentQueue + highQueue);
        publishMetrics(\"task.pending.count\", pendingCount);
    }
}
```

**2. 告警规则**
```java
private void checkAlerts(long pendingCount, long processingCount) {
    // 告警 1：待处理任务过多
    if (pendingCount > 10000) {
        sendAlert(\"ALERT: 待处理任务过多 \" + pendingCount);
    }
    
    // 告警 2：处理线程已死
    if (processingCount == 0 && pendingCount > 0) {
        sendAlert(\"ALERT: 没有实例在处理任务，可能宕机\");
    }
    
    // 告警 3：失败率过高
    long failureRate = failedCount / (successCount + failedCount);
    if (failureRate > 0.05) { // > 5%
        sendAlert(\"ALERT: 任务失败率过高 \" + (failureRate * 100) + \"%\");
    }
}
```

**3. 暴露 Prometheus 指标**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

**4. Grafana 可视化**
```
仪表板面板：
┌─────────────────────────────────┐
│ 任务统计                         │
│ ┌───────────────────────────┐   │
│ │ PENDING: 100              │   │
│ │ PROCESSING: 5             │   │
│ │ SUCCESS: 1000             │   │
│ │ FAILED: 2                 │   │
│ └───────────────────────────┘   │
└─────────────────────────────────┘
┌─────────────────────────────────┐
│ 队列深度趋势                      │
│   |\\ /|                          │
│   | X |                          │
│   |/ \\|                          │
└─────────────────────────────────┘
┌─────────────────────────────────┐
│ 任务完成率                       │
│ 99.5% ✓                         │
└─────────────────────────────────┘
```

**5. 日志聚合**
```
使用 ELK Stack：
- Elasticsearch：存储日志
- Logstash：收集和处理
- Kibana：查询和分析

查询：
- 查找失败的任务：status:FAILED AND error:*
- 查找超时的任务：executionTime > 10000
- 查找重试最多的任务：retryCount:3
```

**监控指标总结：**
| 指标 | 正常范围 | 告警阈值 |
|------|--------|--------|
| PENDING 任务数 | < 1000 | > 10000 |
| 平均处理时间 | 1-10s | > 30s |
| 任务失败率 | < 1% | > 5% |
| 队列延迟 | < 5s | > 60s |
| GC 时间 | < 100ms | > 500ms |"

---

## 📊 Part 6: 场景题 / 算法题

### Q15: 如果要支持任务依赖关系怎么设计？

**标准回答：**

"这是一个很有趣的扩展需求。

**需求分析：**
```
场景：任务 C 依赖于任务 A 和 B
- 必须等 A 和 B 都完成后，C 才能开始
- 如果 A 失败，C 也应该被取消
```

**数据库设计：**
```sql
ALTER TABLE tasks ADD COLUMN (
    depends_on BIGINT,              -- 依赖的任务 ID
    FOREIGN KEY (depends_on) REFERENCES tasks(id)
);

-- 或者用关联表（一对多）
CREATE TABLE task_dependencies (
    task_id BIGINT,
    depends_on_task_id BIGINT,
    PRIMARY KEY (task_id, depends_on_task_id),
    FOREIGN KEY (task_id) REFERENCES tasks(id),
    FOREIGN KEY (depends_on_task_id) REFERENCES tasks(id)
);
```

**处理流程：**
```java
@Service
public class DependencyTaskService {
    
    // 1. 创建任务时检查依赖
    @Transactional
    public TaskResponse createTaskWithDependencies(
            TaskCreateRequest request, 
            List<Long> dependsOn) {
        Task task = Task.builder()
            .title(request.getTitle())
            .status(TaskStatus.BLOCKED)  // 初始状态为 BLOCKED
            .build();
        
        Task savedTask = taskRepository.save(task);
        
        // 保存依赖关系
        for (Long dependTaskId : dependsOn) {
            TaskDependency dep = TaskDependency.builder()
                .taskId(savedTask.getId())
                .dependsOnTaskId(dependTaskId)
                .build();
            taskDependencyRepository.save(dep);
        }
        
        return TaskResponse.fromEntity(savedTask);
    }
    
    // 2. 任务完成时，检查是否有后继任务可以唤醒
    @Transactional
    public void handleTaskSuccess(Long taskId) {
        Task task = taskRepository.findById(taskId).orElseThrow();
        task.setStatus(TaskStatus.SUCCESS);
        taskRepository.save(task);
        
        // 找出依赖于这个任务的其他任务
        List<TaskDependency> dependents = 
            taskDependencyRepository.findByDependsOnTaskId(taskId);
        
        for (TaskDependency dep : dependents) {
            Task dependentTask = taskRepository.findById(dep.getTaskId()).get();
            
            // 检查所有依赖是否都完成
            List<Task> allDependencies = getAllDependencies(dep.getTaskId());
            boolean allCompleted = allDependencies.stream()
                .allMatch(t -> t.getStatus() == TaskStatus.SUCCESS);
            
            if (allCompleted) {
                // 唤醒任务
                dependentTask.setStatus(TaskStatus.PENDING);
                taskQueue.enqueue(dependentTask);
                log.info(\"Task {} dependencies met, ready to process\", 
                    dep.getTaskId());
            }
        }
    }
    
    // 3. 任务失败时，取消所有依赖任务
    @Transactional
    public void handleTaskFailure(Long taskId, String errorMessage) {
        Task task = taskRepository.findById(taskId).orElseThrow();
        task.setStatus(TaskStatus.FAILED);
        task.setErrorMessage(errorMessage);
        taskRepository.save(task);
        
        // 级联取消依赖任务
        List<TaskDependency> dependents = 
            taskDependencyRepository.findByDependsOnTaskId(taskId);
        
        for (TaskDependency dep : dependents) {
            Task dependentTask = taskRepository.findById(dep.getTaskId()).get();
            
            if (dependentTask.getStatus() == TaskStatus.BLOCKED ||
                dependentTask.getStatus() == TaskStatus.PENDING) {
                
                dependentTask.setStatus(TaskStatus.CANCELLED);
                dependentTask.setErrorMessage(\"Dependency task failed: \" 
                    + taskId);
                taskRepository.save(dependentTask);
                
                // 级联取消
                handleTaskFailure(dep.getTaskId(), 
                    \"Upstream task failed\");
            }
        }
    }
}
```

**可视化：**
```
Task A (SUCCESS) ─┐
                  ├─→ Task C (PENDING) → (处理中) → SUCCESS
Task B (SUCCESS) ─┘
```

**进阶：有向无环图 (DAG)**
```
如果要支持复杂的多对多依赖，可以用 DAG：

   A
  / \\
 B   C
  \\ /
   D

实现：用邻接表存储依赖关系，拓扑排序计算执行顺序
```"

---

### Q16: 如何实现优先级队列的公平性？

**标准回答：**

"这是一个关键的设计问题 - 如何保证低优先级任务不会长期饿死。

**当前问题：**
```
严格优先级会导致 LOW 任务一直等不到
│ URGENT: 10个任务
├─ HIGH: 100个任务  ← 新任务不断进来
├─ MEDIUM: 1000个任务
└─ LOW: 10000个任务 ← 可能永不执行
```

**改进方案 1：时间公平调度**
```java
// 采用加权轮转方案
@Component
public class FairTaskWorker {
    
    @Scheduled(fixedDelay = 1000)
    public void processTasks() {
        // 优先级权重分配
        int[] weights = {8, 4, 2, 1}; // URGENT, HIGH, MEDIUM, LOW
        
        int total = sum(weights);
        int urgentQuota = 8 / total;   // 40% 给 URGENT
        int highQuota = 4 / total;     // 20% 给 HIGH
        int mediumQuota = 2 / total;   // 10% 给 MEDIUM
        int lowQuota = 1 / total;      // 5% 给 LOW
        
        processBatch(URGENT, urgentQuota);
        processBatch(HIGH, highQuota);
        processBatch(MEDIUM, mediumQuota);
        processBatch(LOW, lowQuota);
    }
}
```

**改进方案 2：老化机制 (Aging)**
```java
// 任务等待时间越长，优先级越高
@Component
public class AgedTaskQueue {
    
    @Scheduled(fixedDelay = 60000) // 每分钟检查一次
    public void ageTasksPriority() {
        List<Task> allTasks = taskRepository
            .findByStatus(TaskStatus.PENDING);
        
        for (Task task : allTasks) {
            long waitSeconds = 
                ChronoUnit.SECONDS.between(task.getCreatedAt(), now());
            
            // 每等 10 分钟，优先级提升一级
            int priorityBoost = (int) (waitSeconds / 600);
            
            Task.TaskPriority originalPriority = task.getPriority();
            Task.TaskPriority newPriority = upgradePriority(
                originalPriority, priorityBoost);
            
            if (newPriority != originalPriority) {
                // 从旧队列删除，加到新队列
                task.setPriority(newPriority);
                taskRepository.save(task);
                taskQueue.updatePriority(task);
                
                log.info(\"Task {} priority aged: {} -> {}\",
                    task.getId(), originalPriority, newPriority);
            }
        }
    }
    
    private Task.TaskPriority upgradePriority(
            Task.TaskPriority original, int boosts) {
        Task.TaskPriority[] levels = {LOW, MEDIUM, HIGH, URGENT};
        int idx = Arrays.asList(levels).indexOf(original);
        int newIdx = Math.min(idx + boosts, levels.length - 1);
        return levels[newIdx];
    }
}
```

**对比：**
| 方案 | 优点 | 缺点 |
|------|------|------|
| 严格优先级 | 简单，紧急任务快速 | LOW 任务可能永久等待 |
| 加权轮转 | 相对公平 | 需要调整权重参数 |
| 老化机制 | 自动公平 | 实现复杂，需要定期检查 |

**推荐方案：**混合使用老化 + 加权轮转
```java
// 每分钟老化一次，平时用加权轮转
- 0-10分钟：按原优先级
- 10-20分钟：优先级提升一级
- 20+分钟：优先级提升至 URGENT
```"

---

## 💬 Part 7: 收尾问题

### Q17: 你还有什么想补充的？

**标准回答：**

"感谢给我这个机会介绍项目。我想补充几点：

**1. 项目的完整性**
- 这不仅仅是代码，还包括 README、部署文档
- 支持本地开发、Docker 部署、Kubernetes 生产环境
- 可以直接拿来用或作为参考

**2. 学到的东西**
- 深入理解了分布式系统的复杂性
- 学会了如何权衡可靠性、性能、复杂度
- 实践了多种企业级技术栈

**3. 可扩展性**
- 架构解耦，可以轻松替换 Redis → Kafka
- 支持从单机到分布式的平滑升级
- 代码质量好，容易维护和扩展

**4. 后续计划**
- 已准备好的扩展方向：DAG 依赖、流控限流、死信队列
- 可以根据企业需求快速添加功能
- 有相关的技术积累和经验

总的来说，这个项目体现了我的**系统设计、问题解决、工程实践**能力。"

---

### Q18: 你有什么问题想问我们？

**建议提问（体现你的专业性）：**

1. **关于技术栈**
"贵公司目前的后端任务处理是怎样的架构？是用消息队列还是自研方案？"

2. **关于业务规模**
"公司日活、QPS、数据规模大概是多少？这样我可以评估哪些优化是必要的。"

3. **关于团队**
"团队的技术栈是什么？是否有 code review、CI/CD 等工程实践？"

4. **关于职业发展**
"这个职位的职业发展路径是什么？是否有技术深度还是需要转向管理？"

5. **关于具体工作**
"入职后主要会负责哪些项目或模块？"

---

## 🎬 最后的总结

### 完整的项目介绍流程（5 分钟）

1. **背景** (30秒)
   - 为什么做这个项目
   - 解决什么问题

2. **架构** (1分钟)
   - 整体设计（配合画图）
   - 核心组件

3. **技术亮点** (2分钟)
   - 优先级队列
   - 自动重试
   - 监控告警
   - 分布式支持

4. **遇到的问题** (1分钟)
   - 重复消费怎么解决
   - 数据库连接不足怎么解决

5. **性能指标** (30秒)
   - 单机 3-5 TPS
   - 支持水平扩展

---

### 面试中的常见套路

**面试官可能会:**

1. **从简到难逐步深入**
   - 先问基本功能
   - 再问系统设计
   - 最后问极端情况

2. **挑战你的设计**
   - "为什么不用 XX？"
   - "如果 XXX 怎么办？"
   - 目的是看你是否深入思考过

3. **问你没想过的**
   - 要诚实地说"这是个好问题，我没想过，但可以这样..."
   - 千万不要编造答案

4. **问非技术问题**
   - "为什么选择这个职位？"
   - "你最骄傲的是什么？"
   - 要提前准备，显得有思考过

---

### ⭐ 最重要的三点

1. **理解原理，不是死记答案**
   - 面试官能听出你是否真的理解
   - 用自己的语言解释，而不是背诵

2. **承认你的局限**
   - 如果不知道，说"我不清楚，但我可以..."
   - 诚实比装作什么都懂更加分

3. **准备充分的细节**
   - 不仅知道 WHAT，还要知道 WHY 和 HOW
   - 能讲出具体的代码示例更好

---

**祝你秋招顺利！ 🎉**
