<template>
  <div class="create-task">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>创建新任务</span>
        </div>
      </template>

      <el-form ref="formRef" :model="form" label-width="100px" @submit.prevent="handleSubmit">
        <el-form-item label="标题" prop="title" :rules="[{ required: true, message: '请输入标题' }]">
          <el-input v-model="form.title" placeholder="输入任务标题" />
        </el-form-item>

        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" rows="3" placeholder="输入任务描述（可选）" />
        </el-form-item>

        <el-form-item label="优先级" prop="priority" :rules="[{ required: true, message: '请选择优先级' }]">
          <el-select v-model="form.priority" placeholder="选择优先级">
            <el-option label="URGENT（紧急）" value="URGENT" />
            <el-option label="HIGH（高）" value="HIGH" />
            <el-option label="MEDIUM（中）" value="MEDIUM" />
            <el-option label="LOW（低）" value="LOW" />
          </el-select>
        </el-form-item>

        <el-form-item label="最大重试" prop="maxRetries" :rules="[{ required: true, message: '请输入最大重试次数' }]">
          <el-input-number v-model="form.maxRetries" :min="0" :max="10" />
        </el-form-item>

        <el-form-item label="任务数据" prop="payload" :rules="[{ required: true, message: '请输入任务数据' }]">
          <el-input
            v-model="form.payload"
            type="textarea"
            rows="6"
            placeholder='输入 JSON 格式的任务数据，例如：{"email": "user@example.com", "subject": "Hello"}'
          />
          <small>提示：输入有效的 JSON 格式</small>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading">创建任务</el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useTaskStore } from '../stores/taskStore'
import { ElMessage } from 'element-plus'

const router = useRouter()
const taskStore = useTaskStore()
const formRef = ref()
const loading = ref(false)

const form = ref({
  title: '',
  description: '',
  priority: 'MEDIUM',
  maxRetries: 3,
  payload: '{}',
})

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    // 验证 JSON
    try {
      JSON.parse(form.value.payload)
    } catch (error) {
      ElMessage.error('任务数据必须是有效的 JSON 格式')
      return
    }

    loading.value = true
    try {
      await taskStore.createTask({
        title: form.value.title,
        description: form.value.description,
        priority: form.value.priority,
        maxRetries: form.value.maxRetries,
        payload: form.value.payload,
      })
      ElMessage.success('任务创建成功')
      router.push('/tasks')
    } catch (error) {
      ElMessage.error('创建失败：' + error.message)
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped lang="scss">
.create-task {
  :deep(.el-form-item) {
    margin-bottom: 20px;
  }

  small {
    display: block;
    color: #999;
    margin-top: 5px;
  }
}
</style>
