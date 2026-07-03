import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000,
})

export const useTaskStore = defineStore('task', () => {
  const tasks = ref([])
  const stats = ref({
    pending: 0,
    processing: 0,
    success: 0,
    failed: 0,
    retrying: 0,
  })
  const loading = ref(false)
  const selectedTask = ref(null)

  // 获取任务列表
  const fetchTasks = async (page = 0, size = 20, status = null) => {
    loading.value = true
    try {
      let url = `/tasks?page=${page}&size=${size}`
      if (status) {
        url = `/tasks/status/${status}?page=${page}&size=${size}`
      }
      const response = await api.get(url)
      tasks.value = response.data.data.content || []
      return response.data.data
    } catch (error) {
      console.error('Error fetching tasks:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  // 获取单个任务详情
  const fetchTaskDetail = async (id) => {
    try {
      const response = await api.get(`/tasks/${id}`)
      selectedTask.value = response.data.data
      return response.data.data
    } catch (error) {
      console.error('Error fetching task detail:', error)
      throw error
    }
  }

  // 创建任务
  const createTask = async (taskData) => {
    try {
      const response = await api.post('/tasks', taskData)
      return response.data.data
    } catch (error) {
      console.error('Error creating task:', error)
      throw error
    }
  }

  // 更新任务状态
  const updateTaskStatus = async (id, status) => {
    try {
      const response = await api.put(`/tasks/${id}/status?status=${status}`)
      return response.data.data
    } catch (error) {
      console.error('Error updating task status:', error)
      throw error
    }
  }

  // 取消任务
  const cancelTask = async (id) => {
    try {
      const response = await api.post(`/tasks/${id}/cancel`)
      return response.data.data
    } catch (error) {
      console.error('Error canceling task:', error)
      throw error
    }
  }

  // 获取统计信息
  const fetchStats = async () => {
    try {
      const response = await api.get('/tasks/stats/summary')
      stats.value = response.data.data
      return response.data.data
    } catch (error) {
      console.error('Error fetching stats:', error)
      throw error
    }
  }

  return {
    tasks,
    stats,
    loading,
    selectedTask,
    fetchTasks,
    fetchTaskDetail,
    createTask,
    updateTaskStatus,
    cancelTask,
    fetchStats,
  }
})
