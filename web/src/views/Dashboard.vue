<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <div class="stats-grid">
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-number">{{ stats.pending }}</div>
          <div class="stat-label">待处理</div>
          <div class="stat-icon">⏳</div>
        </div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-number">{{ stats.processing }}</div>
          <div class="stat-label">处理中</div>
          <div class="stat-icon">⚙️</div>
        </div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-number">{{ stats.success }}</div>
          <div class="stat-label">成功</div>
          <div class="stat-icon">✅</div>
        </div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-number">{{ stats.failed }}</div>
          <div class="stat-label">失败</div>
          <div class="stat-icon">❌</div>
        </div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-number">{{ stats.retrying }}</div>
          <div class="stat-label">重试中</div>
          <div class="stat-icon">🔄</div>
        </div>
      </el-card>
    </div>

    <!-- 图表 -->
    <div class="charts-row">
      <!-- 任务分布饼图 -->
      <el-card class="chart-card">
        <template #header>
          <div class="card-header">
            <span>任务状态分布</span>
          </div>
        </template>
        <div ref="pieChartRef" style="height: 300px"></div>
      </el-card>

      <!-- 优先级分布 -->
      <el-card class="chart-card">
        <template #header>
          <div class="card-header">
            <span>优先级分布</span>
          </div>
        </template>
        <div ref="barChartRef" style="height: 300px"></div>
      </el-card>
    </div>

    <!-- 快速操作 -->
    <el-card>
      <template #header>
        <div class="card-header">
          <span>快速操作</span>
        </div>
      </template>
      <div class="quick-actions">
        <el-button type="primary" @click="goToCreate">➕ 创建新任务</el-button>
        <el-button @click="goToTasks">📋 查看所有任务</el-button>
        <el-button @click="refreshStats">🔄 刷新数据</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useTaskStore } from '../stores/taskStore'
import * as echarts from 'echarts'

const router = useRouter()
const taskStore = useTaskStore()
const stats = ref(taskStore.stats)
const pieChartRef = ref()
const barChartRef = ref()

const initCharts = () => {
  // 饼图
  const pieChart = echarts.init(pieChartRef.value)
  const pieOption = {
    tooltip: { trigger: 'item' },
    legend: { orient: 'vertical', left: 'left' },
    series: [
      {
        name: '任务数',
        type: 'pie',
        radius: '50%',
        data: [
          { value: stats.value.pending, name: '待处理' },
          { value: stats.value.processing, name: '处理中' },
          { value: stats.value.success, name: '成功' },
          { value: stats.value.failed, name: '失败' },
          { value: stats.value.retrying, name: '重试中' },
        ],
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)',
          },
        },
      },
    ],
  }
  pieChart.setOption(pieOption)

  // 柱状图
  const barChart = echarts.init(barChartRef.value)
  const barOption = {
    xAxis: {
      type: 'category',
      data: ['URGENT', 'HIGH', 'MEDIUM', 'LOW'],
    },
    yAxis: {
      type: 'value',
    },
    series: [
      {
        data: [12, 28, 45, 15],
        type: 'bar',
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#83bff6' },
            { offset: 0.5, color: '#188df0' },
            { offset: 1, color: '#188df0' },
          ]),
        },
      },
    ],
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true,
    },
  }
  barChart.setOption(barOption)
}

const refreshStats = async () => {
  await taskStore.fetchStats()
  stats.value = taskStore.stats
  initCharts()
}

const goToCreate = () => router.push('/create')
const goToTasks = () => router.push('/tasks')

onMounted(async () => {
  await taskStore.fetchStats()
  stats.value = taskStore.stats
  initCharts()
})
</script>

<style scoped lang="scss">
.dashboard {
  .stats-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 20px;
    margin-bottom: 30px;

    .stat-card {
      position: relative;
      overflow: hidden;

      .stat-content {
        position: relative;
        z-index: 2;
        padding: 10px 0;

        .stat-number {
          font-size: 32px;
          font-weight: bold;
          color: #333;
          margin-bottom: 8px;
        }

        .stat-label {
          font-size: 14px;
          color: #666;
          margin-bottom: 15px;
        }

        .stat-icon {
          font-size: 40px;
          opacity: 0.3;
        }
      }
    }
  }

  .charts-row {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 20px;
    margin-bottom: 20px;

    @media (max-width: 1200px) {
      grid-template-columns: 1fr;
    }

    .chart-card {
      :deep(.el-card__body) {
        padding: 20px;
      }
    }
  }

  .quick-actions {
    display: flex;
    gap: 10px;
    flex-wrap: wrap;
  }
}
</style>
