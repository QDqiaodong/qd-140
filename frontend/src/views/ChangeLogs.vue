<template>
  <div class="change-logs">
    <el-card>
      <template #header>
        <span>绑定变更日志</span>
      </template>

      <el-form :inline="true" :model="queryForm" class="query-form">
        <el-form-item label="支架编号">
          <el-input v-model="queryForm.bracketCode" placeholder="请输入编号" clearable />
        </el-form-item>
        <el-form-item label="组别名称">
          <el-input v-model="queryForm.groupName" placeholder="请输入名称" clearable />
        </el-form-item>
        <el-form-item label="变更类型">
          <el-select v-model="queryForm.changeType" placeholder="全部" clearable>
            <el-option label="绑定" value="BIND" />
            <el-option label="解绑" value="UNBIND" />
            <el-option label="更新" value="UPDATE" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" border stripe>
        <el-table-column prop="bracketCode" label="支架编号" width="140" />
        <el-table-column prop="groupName" label="组别名称" width="160" />
        <el-table-column prop="changeType" label="变更类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTagType(row.changeType)">
              {{ getChangeTypeName(row.changeType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="previousDistance" label="变更前距离(m)" width="140">
          <template #default="{ row }">{{ row.previousDistance || '-' }}</template>
        </el-table-column>
        <el-table-column prop="newDistance" label="变更后距离(m)" width="140">
          <template #default="{ row }">{{ row.newDistance || '-' }}</template>
        </el-table-column>
        <el-table-column prop="changeReason" label="变更原因" />
        <el-table-column prop="operator" label="操作人" width="100" />
        <el-table-column prop="changedAt" label="变更时间" width="180" />
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { bindingApi, type ChangeLog } from '@/api'

const tableData = ref<ChangeLog[]>([])
const total = ref(0)

const queryForm = reactive({
  bracketCode: '',
  groupName: '',
  changeType: '',
  pageNum: 1,
  pageSize: 10
})

const handleQuery = async () => {
  try {
    const result = await bindingApi.queryLogs(queryForm)
    tableData.value = result.data
    total.value = result.total
  } catch (error) {
    console.error('查询失败:', error)
  }
}

const resetQuery = () => {
  queryForm.bracketCode = ''
  queryForm.groupName = ''
  queryForm.changeType = ''
  queryForm.pageNum = 1
  handleQuery()
}

const getChangeTypeName = (type: string) => {
  const map: Record<string, string> = {
    'BIND': '绑定',
    'UNBIND': '解绑',
    'UPDATE': '更新'
  }
  return map[type] || type
}

const getTagType = (type: string) => {
  const map: Record<string, string> = {
    'BIND': 'success',
    'UNBIND': 'danger',
    'UPDATE': 'warning'
  }
  return map[type] || 'info'
}

handleQuery()
</script>

<style scoped>
.change-logs {
  padding: 0;
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