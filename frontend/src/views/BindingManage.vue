<template>
  <div class="binding-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>绑定关系列表</span>
          <el-button type="primary" @click="openBindDialog">新增绑定</el-button>
        </div>
      </template>

      <el-table :data="tableData" border stripe>
        <el-table-column prop="bracketCode" label="支架编号" width="140" />
        <el-table-column prop="groupName" label="组别名称" width="160" />
        <el-table-column prop="groupCode" label="组别编码" width="120" />
        <el-table-column prop="racingDistance" label="竞速距离(m)" width="130" />
        <el-table-column label="支架适配区间(m)" width="150">
          <template #default="{ row }">
            {{ row.bracketMinDistance != null && row.bracketMaxDistance != null
              ? `${row.bracketMinDistance}-${row.bracketMaxDistance}`
              : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="bindingTime" label="绑定时间" width="180" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '生效' : '失效' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button v-if="row.status === 1" size="small" type="danger" @click="handleUnbind(row)">解绑</el-button>
            <el-button v-else size="small" type="success" @click="handleRebind(row)">重新绑定</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isRebind ? '重新绑定' : '新增绑定'" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="支架" prop="bracketId">
          <el-select v-model="form.bracketId" placeholder="请选择支架" style="width: 100%">
            <el-option v-for="bracket in brackets" :key="bracket.id" :label="`${bracket.bracketCode} (${bracket.minDistance}-${bracket.maxDistance}m)`" :value="bracket.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="组别" prop="groupId">
          <el-select v-model="form.groupId" placeholder="请选择组别" style="width: 100%">
            <el-option v-for="group in groups" :key="group.id" :label="`${group.groupName} (${group.groupCode}) ${group.racingDistance}m`" :value="group.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作人">
          <el-input v-model="form.operator" placeholder="请输入操作人" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.reason" type="textarea" placeholder="请输入备注" />
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { bindingApi, bracketApi, groupApi, type Binding, type Bracket, type Group } from '@/api'
import type { FormInstance, FormRules } from 'element-plus'

const tableData = ref<Binding[]>([])
const brackets = ref<Bracket[]>([])
const groups = ref<Group[]>([])
const dialogVisible = ref(false)
const isRebind = ref(false)
const formRef = ref<FormInstance>()

const form = reactive({
  bracketId: 0,
  groupId: 0,
  operator: '',
  reason: ''
})

const rules: FormRules = {
  bracketId: [{ required: true, message: '请选择支架', trigger: 'change' }],
  groupId: [{ required: true, message: '请选择组别', trigger: 'change' }]
}

const fetchData = async () => {
  try {
    // 拉取全部绑定（含失效），被组别改距自动拆掉的关系刷新后仍可见并显示“失效”
    tableData.value = await bindingApi.getAll()
    brackets.value = await bracketApi.list()
    groups.value = await groupApi.list()
  } catch (error) {
    console.error('获取数据失败:', error)
  }
}

const openBindDialog = () => {
  isRebind.value = false
  Object.assign(form, {
    bracketId: 0,
    groupId: 0,
    operator: '',
    reason: ''
  })
  dialogVisible.value = true
}

const handleRebind = (row: Binding) => {
  isRebind.value = true
  Object.assign(form, {
    bracketId: row.bracketId,
    groupId: row.groupId,
    operator: '',
    reason: ''
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate()
  try {
    await bindingApi.bind(form)
    ElMessage.success(isRebind.value ? '重新绑定成功' : '绑定成功')
    dialogVisible.value = false
    fetchData()
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '绑定失败，请稍后重试')
  }
}

const handleUnbind = (row: Binding) => {
  if (confirm(`确定解绑 ${row.bracketCode} 和 ${row.groupName} 吗？`)) {
    bindingApi.unbind({ bracketId: row.bracketId, groupId: row.groupId }).then(() => {
      ElMessage.success('解绑成功')
      fetchData()
    }).catch(error => {
      ElMessage.error(error instanceof Error ? error.message : '解绑失败，请稍后重试')
    })
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.binding-manage {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>