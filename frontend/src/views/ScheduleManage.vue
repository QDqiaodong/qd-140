<template>
  <div class="schedule-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>训练课次排课</span>
          <el-button type="primary" @click="openAddDialog">新增排课</el-button>
        </div>
      </template>

      <el-form :inline="true" :model="queryForm" class="query-form">
        <el-form-item label="训练日期">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            value-format="YYYY-MM-DD"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" border stripe :row-class-name="rowClassName">
        <el-table-column prop="sessionDate" label="训练日期" width="120" />
        <el-table-column prop="bracketCode" label="支架编号" width="120">
          <template #default="{ row }">{{ row.bracketCode || '-' }}</template>
        </el-table-column>
        <el-table-column label="当前承重" width="100">
          <template #default="{ row }">
            {{ row.loadCapacity != null ? row.loadCapacity : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="预计上艇人数" width="120">
          <template #default="{ row }">
            <span :class="{ 'overload-text': row.overloaded }">{{ row.expectedPersonCount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="groupName" label="训练组别" width="160">
          <template #default="{ row }">{{ row.groupName || '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="250">
          <template #default="{ row }">
            <el-tag :type="row.valid ? 'success' : 'danger'">
              {{ row.overloaded
                ? `超载（承重${row.loadCapacity}人/已排${row.expectedPersonCount}人）`
                : (row.valid ? '可上' : '不可上') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="120" />
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button size="small" @click="openEditDialog(row)">改课</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-alert
        class="overload-tip"
        type="warning"
        :closable="false"
        show-icon
        title="超载课次不能上：支架承重被场务调小后，已排人数压过新承重的课次会自动标为超载，须把人数降到新承重以内（或更换支架）后才可正常上艇。"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '修改课次' : '新增排课'" width="520px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="训练日期" prop="sessionDate">
          <el-date-picker
            v-model="form.sessionDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="请选择训练日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="停靠支架" prop="bracketId">
          <el-select
            v-model="form.bracketId"
            placeholder="请选择支架"
            style="width: 100%"
            @change="onBracketChange"
          >
            <el-option
              v-for="bracket in brackets"
              :key="bracket.id"
              :label="`${bracket.bracketCode}（当前承重 ${bracket.loadCapacity} 人）`"
              :value="bracket.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="训练组别">
          <el-select v-model="form.groupId" placeholder="可空" clearable style="width: 100%">
            <el-option
              v-for="group in groups"
              :key="group.id"
              :label="`${group.groupName} (${group.groupCode})`"
              :value="group.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="预计上艇人数" prop="expectedPersonCount">
          <el-input-number v-model="form.expectedPersonCount" :min="1" :step="1" :precision="0" placeholder="必填" />
          <span v-if="selectedBracket" class="capacity-hint">
            该支架当前承重 {{ selectedBracket.loadCapacity }} 人，人数不得超过
          </span>
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
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { sessionApi, bracketApi, groupApi, type Session, type Bracket, type Group } from '@/api'
import type { FormInstance, FormRules } from 'element-plus'

const tableData = ref<Session[]>([])
const brackets = ref<Bracket[]>([])
const groups = ref<Group[]>([])
const dateRange = ref<[string, string] | null>(null)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()

const queryForm = reactive({
  startDate: '',
  endDate: ''
})

const form = reactive({
  id: 0,
  sessionDate: '',
  bracketId: 0 as number,
  groupId: null as number | null,
  expectedPersonCount: null as number | null,
  remark: ''
})

const selectedBracket = computed(() =>
  brackets.value.find(b => b.id === form.bracketId)
)

const rules = computed<FormRules>(() => ({
  sessionDate: [{ required: true, message: '请选择训练日期', trigger: 'change' }],
  bracketId: [{ required: true, message: '请选择停靠支架', trigger: 'change' }],
  expectedPersonCount: [
    { required: true, message: '预计上艇人数不能为空，人数空着不能排课', trigger: 'blur' },
    {
      validator: (_rule, value: number | null, callback: (err?: Error) => void) => {
        if (value == null) {
          callback(new Error('预计上艇人数不能为空，人数空着不能排课'))
          return
        }
        const bracket = selectedBracket.value
        if (bracket && Number(value) > Number(bracket.loadCapacity)) {
          callback(new Error(`超过该支架当前承重：承重 ${bracket.loadCapacity} 人，本次排了 ${value} 人`))
          return
        }
        callback()
      },
      trigger: 'change'
    }
  ]
}))

const fetchData = async () => {
  try {
    const params: { startDate?: string; endDate?: string } = {}
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    const [sessions, bracketList, groupList] = await Promise.all([
      sessionApi.list(params),
      bracketApi.list(),
      groupApi.list()
    ])
    tableData.value = sessions
    brackets.value = bracketList
    groups.value = groupList
  } catch (error) {
    console.error('获取排课数据失败:', error)
  }
}

const handleQuery = () => {
  if (dateRange.value && dateRange.value.length === 2) {
    queryForm.startDate = dateRange.value[0]
    queryForm.endDate = dateRange.value[1]
  } else {
    queryForm.startDate = ''
    queryForm.endDate = ''
  }
  fetchData()
}

const resetQuery = () => {
  dateRange.value = null
  queryForm.startDate = ''
  queryForm.endDate = ''
  fetchData()
}

const onBracketChange = () => {
  // 换支架后承重上限变了，重新校验人数
  formRef.value?.validateField('expectedPersonCount').catch(() => {})
}

const openAddDialog = () => {
  isEdit.value = false
  Object.assign(form, {
    id: 0,
    sessionDate: '',
    bracketId: 0,
    groupId: null,
    expectedPersonCount: null,
    remark: ''
  })
  dialogVisible.value = true
}

const openEditDialog = (row: Session) => {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    sessionDate: row.sessionDate,
    bracketId: row.bracketId,
    groupId: row.groupId,
    expectedPersonCount: row.expectedPersonCount,
    remark: row.remark || ''
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  try {
    const payload = {
      sessionDate: form.sessionDate,
      bracketId: form.bracketId,
      groupId: form.groupId,
      expectedPersonCount: form.expectedPersonCount as number,
      remark: form.remark
    }
    if (isEdit.value) {
      await sessionApi.update({ id: form.id, ...payload })
      ElMessage.success('课次修改成功')
    } else {
      await sessionApi.create(payload)
      ElMessage.success('排课成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (error) {
    // 后端按支架当前承重兜底拦截：承重、本次人数都在返回消息里写明
    ElMessage.error(error instanceof Error ? error.message : '保存失败，请稍后重试')
  }
}

const handleDelete = (row: Session) => {
  if (confirm(`确定删除 ${row.sessionDate} 支架 ${row.bracketCode || row.bracketId} 的这堂课吗？`)) {
    sessionApi.delete(row.id).then(() => {
      ElMessage.success('删除成功')
      fetchData()
    }).catch(error => {
      ElMessage.error(error instanceof Error ? error.message : '删除失败，请稍后重试')
    })
  }
}

const rowClassName = ({ row }: { row: Session }) => (row.valid ? '' : 'overload-row')

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.schedule-manage {
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

.capacity-hint {
  margin-left: 12px;
  color: #909399;
  font-size: 12px;
}

.overload-text {
  color: #f56c6c;
  font-weight: bold;
}

.overload-tip {
  margin-top: 16px;
}

:deep(.overload-row) {
  background-color: #fef0f0;
}

:deep(.overload-row:hover) > td {
  background-color: #fde2e2 !important;
}
</style>
