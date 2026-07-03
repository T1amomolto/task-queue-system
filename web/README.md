# 任务队列管理后台

一个基于 Vue 3 + Element Plus 的任务队列系统管理后台。

## 🚀 快速开始

### 安装依赖
```bash
cd web
npm install
```

### 开发模式
```bash
npm run dev
```

访问 http://localhost:5173

### 生产构建
```bash
npm run build
```

## 📁 项目结构

```
web/
├── src/
│   ├── views/           # 页面组件
│   │   ├── Dashboard.vue        # 仪表板
│   │   ├── TaskList.vue         # 任务列表
│   │   ├── TaskDetail.vue       # 任务详情
│   │   ├── CreateTask.vue       # 创建任务
│   │   └── LogViewer.vue        # 日志查看
│   ├── stores/          # Pinia 状态管理
│   │   └── taskStore.js
│   ├── router/          # Vue Router 配置
│   │   └── index.js
│   ├── App.vue          # 应用根组件
│   ├── main.js          # 应用入口
│   └── style.css        # 全局样式
├── index.html           # HTML 模板
├── vite.config.js       # Vite 配置
└── package.json
```

## ✨ 功能特性

- 📊 **仪表板** - 实时任务统计和可视化图表
- 📋 **任务管理** - 列表、创建、编辑、取消任务
- 🔍 **任务详情** - 查看完整的任务信息和执行日志
- 📈 **数据可视化** - 任务状态分布、优先级分布
- 🔄 **实时刷新** - 支持手动刷新数据
- 📱 **响应式设计** - 支持不同屏幕尺寸

## 🔗 API 集成

所有 API 请求都通过 Axios 发送到后端 `http://localhost:8080/api`

### Vite 代理配置

```javascript
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true,
    },
  },
}
```

## 🎨 样式

使用 Element Plus UI 组件库，全局样式在 `src/style.css` 中定义。

### 颜色方案

- PENDING（待处理）: 蓝色
- PROCESSING（处理中）: 黄色
- SUCCESS（成功）: 绿色
- FAILED（失败）: 红色
- RETRYING（重试中）: 粉色
- CANCELLED（已取消）: 灰色

## 📦 依赖

- **Vue 3** - 前端框架
- **Vue Router** - 路由管理
- **Pinia** - 状态管理
- **Element Plus** - UI 组件库
- **Axios** - HTTP 客户端
- **ECharts** - 数据可视化
- **date-fns** - 日期处理

## 🔧 配置

### 环境变量

可以在 `.env` 文件中配置 API 服务器地址：

```
VITE_API_BASE_URL=http://localhost:8080/api
```

### API 服务器地址

Vite 代理会自动转发 `/api` 的请求到后端服务器。

## 📝 使用指南

### 创建任务

1. 点击侧边栏的 "创建任务"
2. 填写任务表单
3. 点击 "创建任务" 按钮

### 查看任务

1. 点击侧边栏的 "任务管理"
2. 查看所有任务列表
3. 支持按状态筛选和搜索
4. 点击任务行可查看详情

### 监控仪表板

1. 点击侧边栏的 "仪表板"
2. 查看实时任务统计
3. 查看任务状态分布和优先级分布

## 🚀 部署

### Docker 部署

```dockerfile
FROM node:18-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

### Nginx 配置

```nginx
server {
    listen 80;
    server_name localhost;

    location / {
        root /usr/share/nginx/html;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

## 📄 许可证

MIT License
