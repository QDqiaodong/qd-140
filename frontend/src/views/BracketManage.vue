<template>
  <div class="bracket-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>支架列表</span>
          <el-button type="primary" @click="openAddDialog">新增支架</el-button>
        </div>
      </template>

      <el-form :inline="true" :model="queryForm" class="query-form">
        <el-form-item label="支架编号">
          <el-input v-model="queryForm.bracketCode" placeholder="请输入编号" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable>
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" border stripe>
        <el-table-column prop="bracketCode" label="支架编号" width="140" />
        <el-table-column prop="loadCapacity" label="承重(kg)" width="120" />
        <el-table-column prop="minDistance" label="最小适配距离(m)" width="140" />
        <el-table-column prop="maxDistance" label="最大适配距离(m)" width="140" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" />
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" @click="openEditDialog(row)">编辑</el-button>
            <el-button size="small" :type="row.status === 1 ? 'warning' : 'success'" @click="toggleStatus(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="queryForm.pageNum"
        v-model:page-size="queryForm.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleQuery"
        @current-change="handleQuery"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑支架' : '新增支架'" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="支架编号" prop="bracketCode">
          <el-input v-model="form.bracketCode" placeholder="请输入支架编号" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="承重(kg)" prop="loadCapacity">
          <el-input-number v-model="form.loadCapacity" :min="0.01" :step="0.1" placeholder="请输入承重" />
        </el-form-item>
        <el-form-item label="最小适配距离(m)" prop="minDistance">
          <el-input-number v-model="form.minDistance" :min="1" placeholder="请输入最小距离" />
        </el-form-item>
        <el-form-item label="最大适配距离(m)" prop="maxDistance">
          <el-input-number v-model="form.maxDistance" :min="1" placeholder="请输入最大距离" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { bracketApi, type Bracket } from '@/api'
import type { FormInstance, FormRules } from 'element-plus'

const tableData = ref<Bracket[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()

const queryForm = reactive({
  bracketCode: '',
  status: undefined as number | undefined,
  pageNum: 1,
  pageSize: 10
})

const form = reactive({
  id: 0,
  bracketCode: '',
  loadCapacity: 0,
  minDistance: 0,
  maxDistance: 0,
  remark: ''
})

const rules: FormRules = {
  bracketCode: [{ required: true, message: '请输入支架编号', trigger: 'blur' }],
  loadCapacity: [{ required: true, message: '请输入承重', trigger: 'blur' }],
  minDistance: [{ required: true, message: '请输入最小适配距离', trigger: 'blur' }],
  maxDistance: [{ required: true, message: '请输入最大适配距离', trigger: 'blur' }]
}

const handleQuery = async () => {
  try {
    const result = await bracketApi.page(queryForm)
    tableData.value = result.data
    total.value = result.total
  } catch (error) {
    console.error('查询失败:', error)
  }
}

const resetQuery = () => {
  queryForm.bracketCode = ''
  queryForm.status = undefined
  queryForm.pageNum = 1
  handleQuery()
}

const openAddDialog = () => {
  isEdit.value = false
  Object.assign(form, {
    id: 0,
    bracketCode: '',
    loadCapacity: 0,
    minDistance: 0,
    maxDistance: 0,
    remark: ''
  })
  dialogVisible.value = true
}

const openEditDialog = (row: Bracket) => {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    bracketCode: row.bracketCode,
    loadCapacity: row.loadCapacity,
    minDistance: row.minDistance,
    maxDistance: row.maxDistance,
    remark: row.remark || ''
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate()
  try {
    if (isEdit.value) {
      await bracketApi.update(form)
    } else {
      await bracketApi.create(form)
    }
    dialogVisible.value = false
    handleQuery()
  } catch (error) {
    console.error('操作失败:', error)
  }
}

const toggleStatus = async (row: Bracket) => {
  try {
    await bracketApi.update({ id: row.id, status: row.status === 1 ? 0 : 1 })
    handleQuery()
  } catch (error) {
    console.error('操作失败:', error)
  }
}

const handleDelete = (row: Bracket) => {
  if (confirm(`确定删除支架 ${row.bracketCode} 吗？`)) {
    bracketApi.delete(row.id).then(() => {
      handleQuery()
    }).catch(error => {
      console.error('删除失败:', error)
    })
  }
}

handleQuery()
</script>

<style scoped>
.bracket-manage {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.query-form {
  margin-bottom: 20px;
}

.el-pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>