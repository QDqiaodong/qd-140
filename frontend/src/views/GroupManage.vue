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
        <el-table-column label="训练计划" width="160">
          <template #default="{ row }">
            <el-link
              v-if="row.planFileName"
              type="primary"
              :href="groupApi.planDownloadUrl(row.id)"
              target="_blank"
            >
              <el-icon><Paperclip /></el-icon>
              <span class="plan-link-text">{{ row.planFileName }}</span>
            </el-link>
            <span v-else class="plan-empty">未上传</span>
          </template>
        </el-table-column>
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
        <el-form-item label="训练计划" required>
          <div class="plan-uploader">
            <el-upload
              ref="planUploadRef"
              :show-file-list="false"
              :before-upload="beforePlanUpload"
              :http-request="handlePlanUpload"
              :disabled="planUploading || submitting"
              accept=".pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.txt"
            >
              <el-button type="primary" :loading="planUploading">
                {{ form.planFileName ? '重新上传' : '上传训练计划' }}
              </el-button>
              <template #tip>
                <div class="el-upload__tip">
                  支持 PDF、Word、Excel、PPT、TXT 格式，大小不超过 20MB
                </div>
              </template>
            </el-upload>
            <div v-if="planUploading" class="plan-status">附件上传中…</div>
            <div v-else-if="form.planFileName" class="plan-current">
              <el-icon><Paperclip /></el-icon>
              <span class="plan-name" :title="form.planFileName">{{ form.planFileName }}</span>
              <el-link
                v-if="savedPlanId"
                type="primary"
                :href="groupApi.planDownloadUrl(savedPlanId)"
                target="_blank"
              >下载</el-link>
            </div>
            <div v-else-if="planRejected" class="plan-error">
              附件格式不是基地允许的类型，请更换文件
            </div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false" :disabled="planUploading || submitting">取消</el-button>
        <el-button type="primary" :loading="planUploading || submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Paperclip } from '@element-plus/icons-vue'
import type { UploadRequestOptions } from 'element-plus'
import { groupApi, type Group, type GroupUpdateResult } from '@/api'
import type { FormInstance, FormRules } from 'element-plus'

const ALLOWED_PLAN_EXTENSIONS = ['pdf', 'doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx', 'txt']
const MAX_PLAN_SIZE = 20 * 1024 * 1024

const tableData = ref<Group[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()
const planUploadRef = ref()

const planUploading = ref(false)
/** 附件是否已通过校验并成功上传（先做附件通过，再做保存组别） */
const planPassed = ref(false)
const planRejected = ref(false)
/** 已保存组别且有附件时，其组别 id，用于下载链接 */
const savedPlanId = ref<number>(0)
/** 新增组别时，校验通过、待建档后上传的附件 */
const pendingFile = ref<File | null>(null)
const submitting = ref(false)

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
  description: '',
  planFileName: '' as string | null
})

const rules: FormRules = {
  groupName: [{ required: true, message: '请输入组别名称', trigger: 'blur' }],
  groupCode: [{ required: true, message: '请输入组别编码', trigger: 'blur' }],
  racingDistance: [{ required: true, message: '请输入竞速距离', trigger: 'blur' }]
}

const getExtension = (fileName: string) => {
  const index = fileName.lastIndexOf('.')
  return index < 0 ? '' : fileName.slice(index + 1).toLowerCase()
}

const beforePlanUpload = (file: File) => {
  planRejected.value = false
  if (!ALLOWED_PLAN_EXTENSIONS.includes(getExtension(file.name))) {
    planRejected.value = true
    planPassed.value = false
    ElMessage.error('训练计划格式不是基地允许的类型，请更换文件')
    return false
  }
  if (file.size > MAX_PLAN_SIZE) {
    planPassed.value = false
    ElMessage.error('训练计划大小不能超过20MB，请压缩或更换文件')
    return false
  }
  return true
}

const handlePlanUpload = async (options: UploadRequestOptions) => {
  const file = options.file as File
  if (!isEdit.value || !form.id) {
    // 新增组别尚未建档，先暂存，待组别保存后自动上传
    pendingFile.value = file
    form.planFileName = file.name
    planPassed.value = true
    planRejected.value = false
    ElMessage.info('附件校验通过，将在保存组别后完成上传')
    return
  }

  planUploading.value = true
  try {
    const updated = await groupApi.uploadPlan(form.id, file)
    form.planFileName = updated.planFileName
    savedPlanId.value = form.id
    planPassed.value = true
    planRejected.value = false
    ElMessage.success('训练计划上传成功')
    handleQuery()
  } catch (error: unknown) {
    planPassed.value = false
    planRejected.value = true
    form.planFileName = ''
    ElMessage.error(error instanceof Error ? error.message : '训练计划上传失败，请更换文件')
  } finally {
    planUploading.value = false
  }
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

const resetPlanState = () => {
  pendingFile.value = null
  planPassed.value = false
  planRejected.value = false
  planUploading.value = false
  savedPlanId.value = 0
  planUploadRef.value?.clearFiles?.()
}

const openAddDialog = () => {
  isEdit.value = false
  Object.assign(form, {
    id: 0,
    groupName: '',
    groupCode: '',
    racingDistance: 2000,
    description: '',
    planFileName: null
  })
  resetPlanState()
  dialogVisible.value = true
}

const openEditDialog = (row: Group) => {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    groupName: row.groupName,
    groupCode: row.groupCode,
    racingDistance: row.racingDistance,
    description: row.description || '',
    planFileName: row.planFileName || null
  })
  resetPlanState()
  if (row.planFileName) {
    // 已有附件视为通过；重新上传成功前保存组别不会清空原附件
    planPassed.value = true
    savedPlanId.value = row.id
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate()

  if (!planPassed.value) {
    ElMessage.warning('请先上传训练计划附件，且附件格式需为基地允许的类型')
    return
  }

  submitting.value = true
  try {
    let groupId = form.id
    let updateResult: GroupUpdateResult | null = null
    if (isEdit.value) {
      updateResult = await groupApi.update(form)
    } else {
      // 先做附件通过（已在选择文件时完成校验），再做保存组别
      const created = await groupApi.create(form)
      groupId = created.id
      form.id = groupId
    }

    // 新增组别暂存的附件，在组别建档成功后完成上传
    if (pendingFile.value) {
      planUploading.value = true
      try {
        const updated = await groupApi.uploadPlan(groupId, pendingFile.value)
        form.planFileName = updated.planFileName
        pendingFile.value = null
      } catch (error: unknown) {
        ElMessage.error('组别已保存，但训练计划上传失败，请重新选择附件后上传')
        planRejected.value = true
        planPassed.value = false
        dialogVisible.value = false
        handleQuery()
        return
      } finally {
        planUploading.value = false
      }
    }

    ElMessage.success(isEdit.value ? '组别更新成功' : '组别建档成功')
    dialogVisible.value = false
    handleQuery()

    // 竞速距离变更后，对不上区间的绑定已在服务端拆掉，当场把明细告知场务
    if (updateResult && updateResult.unboundCount > 0) {
      notifyUnboundBindings(updateResult)
    }
  } catch (error) {
    console.error('操作失败:', error)
  } finally {
    submitting.value = false
  }
}

const notifyUnboundBindings = (result: GroupUpdateResult) => {
  const lines = result.unboundBindings.map(item =>
    `${item.bracketCode} × ${item.groupName}（支架适配 ${item.bracketMinDistance}-${item.bracketMaxDistance}m）`
  )
  ElMessageBox.alert(
    `竞速距离已由 ${result.previousDistance}m 改为 ${result.newDistance}m，以下 `
      + `${result.unboundCount} 条绑定因超出支架适配区间已被自动解绑，绑定列表中显示为“失效”：<br/><br/>`
      + lines.map(line => `· ${line}`).join('<br/>'),
    '部分支架绑定已自动解绑',
    {
      confirmButtonText: '我知道了',
      type: 'warning',
      dangerouslyUseHTMLString: true
    }
  ).catch(() => {})
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

.plan-uploader {
  width: 100%;
}

.plan-current,
.plan-status {
  margin-top: 6px;
  display: flex;
  align-items: center;
  gap: 6px;
  color: #606266;
  font-size: 13px;
}

.plan-name,
.plan-link-text {
  max-width: 220px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.plan-error {
  margin-top: 6px;
  color: #f56c6c;
  font-size: 13px;
}

.plan-empty {
  color: #909399;
  font-size: 13px;
}
</style>