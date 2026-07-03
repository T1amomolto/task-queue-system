<template>
  <div class="task-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>任务列表</span>
          <el-button type="primary" @click="$router.push('/create')">+ 创建任务</el-button>
        </div>
      </template>

      <!-- 筛选条件 -->
      <div class="filters" style="margin-bottom: 20px">
        <el-select v-model="filterStatus" placeholder="按状态筛选" clearable @change="handleFilterChange">
          <el-option label="全部" value="" />
          <el-option label="待处理" value="PENDING" />
          <el-option label="处理中" value="PROCESSING" />
          <el-option label="成功" value="SUCCESS" />
          <el-option label="失败" value="FAILED" />
          <el-option label="重试中" value="RETRYING" />
          <el-option label="已取消" value="CANCELLED" />
        </el-select>
        <el-input v-model="searchKeyword" placeholder="搜索标题..." style="width: 200px; margin-left: 10px" />
      </div>

      <!-- 表格 -->
      <el-table
        :data="displayTasks"
        stripe
        style="width: 100%"
        :loading="taskStore.loading"
        @row-click="handleRowClick"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="150" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <span :class="['status-badge', `status-${row.status}`]">{{ getStatusLabel(row.status) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="优先级" width="100">
          <template #default="{ row }">
            <span :class="['priority-badge', `priority-${row.priority}`]">{{ row.priority }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="retryCount" label="重试次数" width="100" />
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleDetail(row)">详情</el-button>
            <el-button
              v-if="row.status === 'PENDING' || row.status === 'RETRYING'"
              link
              type="danger"
              @click="handleCancel(row)"
            >
              取消
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div style="margin-top: 20px; text-align: right">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @change="handlePageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useTaskStore } from '../stores/taskStore'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const taskStore = useTaskStore()

const filterStatus = ref('')
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

const displayTasks = computed(() => {
  return taskStore.tasks.filter((task) => task.title.includes(searchKeyword.value))
})

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

const handleFilterChange = async () => {
  currentPage.value = 1
  await loadTasks()
}

const handlePageChange = async () => {
  await loadTasks()
}

const loadTasks = async () => {
  try {
    const response = await taskStore.fetchTasks(currentPage.value - 1, pageSize.value, filterStatus.value || null)
    total.value = response.totalElements || 0
  } catch (error) {
    ElMessage.error('加载任务失败')
  }
}

const handleDetail = (row) => {
  router.push(`/tasks/${row.id}`)
}

const handleCancel = (row) => {
  ElMessageBox.confirm(`确定要取消任务 "${row.title}" 吗?`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(async () => {
      try {
        await taskStore.cancelTask(row.id)
        ElMessage.success('任务已取消')
        await loadTasks()
      } catch (error) {
        ElMessage.error('取消失败')
      }
    })
    .catch(() => {})
}

onMounted(() => {
  loadTasks()
})
</script>

<style scoped lang="scss">
.task-list {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    width: 100%;
  }

  .filters {
    display: flex;
    gap: 10px;
    align-items: center;

    :deep(.el-select) {
      width: 150px;
    }
  }
}
</style>
