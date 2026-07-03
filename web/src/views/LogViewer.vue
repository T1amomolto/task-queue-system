<template>
  <div class="log-viewer">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>日志查看</span>
        </div>
      </template>

      <div class="log-filters" style="margin-bottom: 20px">
        <el-input v-model="taskId" placeholder="输入任务 ID" style="width: 150px" />
        <el-button type="primary" @click="loadLogs" :loading="loading">查询</el-button>
        <el-button @click="clearLogs">清空</el-button>
      </div>

      <div v-if="logs.length === 0 && !loading" class="no-data">
        <el-empty description="暂无日志" />
      </div>

      <div v-else class="logs-container">
        <div v-for="(log, index) in logs" :key="index" class="log-item">
          <div class="log-header">
            <span class="log-time">{{ formatTime(log.createdAt) }}</span>
            <span :class="['log-level', `level-${log.level}`]">{{ log.level }}</span>
          </div>
          <div class="log-message">{{ log.message }}</div>
        </div>
      </div>

      <div v-if="loading" style="text-align: center; padding: 20px">
        <el-skeleton :rows="5" animated />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { format } from 'date-fns'

const taskId = ref('')
const logs = ref([])
const loading = ref(false)

const formatTime = (dateString) => {
  if (!dateString) return ''
  return format(new Date(dateString), 'yyyy-MM-dd HH:mm:ss')
}

const loadLogs = async () => {
  if (!taskId.value) {
    ElMessage.warning('请输入任务 ID')
    return
  }

  loading.value = true
  try {
    const response = await axios.get(`/api/tasks/${taskId.value}`)
    // 这里需要后端提供获取日志的 API
    // 目前展示任务详情
    ElMessage.info('功能开发中，请在任务详情页面查看日志')
  } catch (error) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const clearLogs = () => {
  logs.value = []
  taskId.value = ''
}
</script>

<style scoped lang="scss">
.log-viewer {
  .log-filters {
    display: flex;
    gap: 10px;
    align-items: center;
  }

  .logs-container {
    background: #f5f5f5;
    border-radius: 4px;
    padding: 15px;
    max-height: 600px;
    overflow-y: auto;
    font-family: 'Courier New', monospace;
    font-size: 12px;
  }

  .log-item {
    margin-bottom: 10px;
    padding: 8px;
    background: white;
    border-left: 3px solid #ddd;
    border-radius: 2px;

    .log-header {
      display: flex;
      gap: 10px;
      margin-bottom: 5px;
      align-items: center;
    }

    .log-time {
      color: #999;
      font-size: 11px;
    }

    .log-level {
      display: inline-block;
      padding: 2px 6px;
      border-radius: 2px;
      font-size: 11px;
      font-weight: bold;
    }

    .level-INFO {
      background: #d1f2eb;
      color: #0f766e;
    }

    .level-WARN {
      background: #fef3c7;
      color: #b45309;
    }

    .level-ERROR {
      background: #fee2e2;
      color: #991b1b;
    }

    .level-DEBUG {
      background: #dbeafe;
      color: #0c4a6e;
    }

    .log-message {
      color: #333;
      word-break: break-all;
      white-space: pre-wrap;
    }
  }

  .no-data {
    text-align: center;
    padding: 40px 20px;
  }
}
</style>
