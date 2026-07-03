<template>
  <div class="task-detail">
    <el-card v-if="task">
      <template #header>
        <div class="card-header">
          <span>任务详情 #{{ task.id }}</span>
          <el-button @click="$router.back()">返回</el-button>
        </div>
      </template>

      <el-row :gutter="20">
        <el-col :xs="24" :sm="12">
          <div class="detail-group">
            <label>标题</label>
            <p>{{ task.title }}</p>
          </div>
          <div class="detail-group">
            <label>描述</label>
            <p>{{ task.description || '无' }}</p>
          </div>
          <div class="detail-group">
            <label>状态</label>
            <p>
              <span :class="['status-badge', `status-${task.status}`]">{{ getStatusLabel(task.status) }}</span>
            </p>
          </div>
          <div class="detail-group">
            <label>优先级</label>
            <p>
              <span :class="['priority-badge', `priority-${task.priority}`]">{{ task.priority }}</span>
            </p>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12">
          <div class="detail-group">
            <label>重试次数</label>
            <p>{{ task.retryCount }} / {{ task.maxRetries }}</p>
          </div>
          <div class="detail-group">
            <label>执行时间</label>
            <p>{{ task.executionTime ? `${task.executionTime}ms` : '未执行' }}</p>
          </div>
          <div class="detail-group">
            <label>创建时间</label>
            <p>{{ formatTime(task.createdAt) }}</p>
          </div>
          <div class="detail-group">
            <label>完成时间</label>
            <p>{{ task.completedAt ? formatTime(task.completedAt) : '未完成' }}</p>
          </div>
        </el-col>
      </el-row>

      <el-divider></el-divider>

      <!-- 任务数据 -->
      <div class="detail-group">
        <label>任务数据</label>
        <el-input v-model="task.payload" type="textarea" rows="5" readonly></el-input>
      </div>

      <!-- 错误信息 -->
      <div v-if="task.errorMessage" class="detail-group">
        <label>错误信息</label>
        <el-alert :title="task.errorMessage" type="error" :closable="false"></el-alert>
      </div>

      <!-- 操作按钮 -->
      <div class="actions">
        <el-button v-if="task.status === 'PENDING' || task.status === 'RETRYING'" type="danger" @click="handleCancel">
          取消任务
        </el-button>
        <el-button @click="refreshDetail">刷新</el-button>
      </div>
    </el-card>

    <el-card v-else>
      <el-skeleton :rows="5" animated />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useTaskStore } from '../stores/taskStore'
import { ElMessage, ElMessageBox } from 'element-plus'
import { format } from 'date-fns'

const route = useRoute()
const router = useRouter()
const taskStore = useTaskStore()
const task = ref(null)

const formatTime = (dateString) => {
  if (!dateString) return ''
  return format(new Date(dateString), 'yyyy-MM-dd HH:mm:ss')
}

const getStatusLabel = (status) => {
  const labels = {
    PENDING: '待处理',
    PROCESSING: '处理中',
    SUCCESS: '成功',
    FAILED: '失败',
    RETRYING: '重试中',
    CANCELLED: '已取消',
  }
  return labels[status] || status
}

const refreshDetail = async () => {
  try {
    task.value = await taskStore.fetchTaskDetail(route.params.id)
    ElMessage.success('已刷新')
  } catch (error) {
    ElMessage.error('刷新失败')
  }
}

const handleCancel = () => {
  ElMessageBox.confirm('确定要取消这个任务吗?', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(async () => {
      try {
        await taskStore.cancelTask(route.params.id)
        ElMessage.success('任务已取消')
        await refreshDetail()
      } catch (error) {
        ElMessage.error('取消失败')
      }
    })
    .catch(() => {})
}

onMounted(async () => {
  try {
    task.value = await taskStore.fetchTaskDetail(route.params.id)
  } catch (error) {
    ElMessage.error('加载任务失败')
  }
})
</script>

<style scoped lang="scss">
.task-detail {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    width: 100%;
  }

  .detail-group {
    margin-bottom: 20px;

    label {
      display: block;
      font-weight: 600;
      color: #333;
      margin-bottom: 8px;
      font-size: 14px;
    }

    p {
      margin: 0;
      color: #666;
      word-break: break-all;
    }
  }

  .actions {
    margin-top: 30px;
    display: flex;
    gap: 10px;
  }
}
</style>
