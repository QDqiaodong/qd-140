<template>
  <div class="group-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>组别列表</span>
          <el-button type="primary" @click="openAddDialog">新增组别</el-button>
        </div>
      </template>

      <el-form :inline="true" :model="queryForm" class="query-form">
        <el-form-item label="组别名称">
          <el-input v-model="queryForm.groupName" placeholder="请输入名称" clearable />
        </el-form-item>
        <el-form-item label="组别编码">
          <el-input v-model="queryForm.groupCode" placeholder="请输入编码" clearable />
        </el-form-item>
        <el-form-item label="竞速距离">
          <el-input-number v-model="queryForm.racingDistance" :min="1" placeholder="距离(m)" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" border stripe>
        <el-table-column prop="groupCode" label="组别编码" width="120" />
        <el-table-column prop="groupName" label="组别名称" width="160" />
        <el-table-column prop="racingDistance" label="竞速距离(m)" width="140" />
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column prop="updatedAt" label="更新时间" width="180" />
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button size="small" @click="openEditDialog(row)">编辑</el-button>
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑组别' : '新增组别'" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="组别名称" prop="groupName">
          <el-input v-model="form.groupName" placeholder="请输入组别名称" />
        </el-form-item>
        <el-form-item label="组别编码" prop="groupCode">
          <el-input v-model="form.groupCode" placeholder="请输入组别编码" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="竞速距离(m)" prop="racingDistance">
          <el-input-number v-model="form.racingDistance" :min="1" placeholder="请输入竞速距离" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" placeholder="请输入描述" />
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
import { groupApi, type Group } from '@/api'
import type { FormInstance, FormRules } from 'element-plus'

const tableData = ref<Group[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()

const queryForm = reactive({
  groupName: '',
  groupCode: '',
  racingDistance: undefined as number | undefined,
  pageNum: 1,
  pageSize: 10
})

const form = reactive({
  id: 0,
  groupName: '',
  groupCode: '',
  racingDistance: 0,
  description: ''
})

const rules: FormRules = {
  groupName: [{ required: true, message: '请输入组别名称', trigger: 'blur' }],
  groupCode: [{ required: true, message: '请输入组别编码', trigger: 'blur' }],
  racingDistance: [{ required: true, message: '请输入竞速距离', trigger: 'blur' }]
}

const handleQuery = async () => {
  try {
    const result = await groupApi.page(queryForm)
    tableData.value = result.data
    total.value = result.total
  } catch (error) {
    console.error('查询失败:', error)
  }
}

const resetQuery = () => {
  queryForm.groupName = ''
  queryForm.groupCode = ''
  queryForm.racingDistance = undefined
  queryForm.pageNum = 1
  handleQuery()
}

const openAddDialog = () => {
  isEdit.value = false
  Object.assign(form, {
    id: 0,
    groupName: '',
    groupCode: '',
    racingDistance: 2000,
    description: ''
  })
  dialogVisible.value = true
}

const openEditDialog = (row: Group) => {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    groupName: row.groupName,
    groupCode: row.groupCode,
    racingDistance: row.racingDistance,
    description: row.description || ''
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate()
  try {
    if (isEdit.value) {
      await groupApi.update(form)
    } else {
      await groupApi.create(form)
    }
    dialogVisible.value = false
    handleQuery()
  } catch (error) {
    console.error('操作失败:', error)
  }
}

const handleDelete = (row: Group) => {
  if (confirm(`确定删除组别 ${row.groupName} 吗？`)) {
    groupApi.delete(row.id).then(() => {
      handleQuery()
    }).catch(error => {
      console.error('删除失败:', error)
    })
  }
}

handleQuery()
</script>

<style scoped>
.group-manage {
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