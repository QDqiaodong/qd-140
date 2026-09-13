<template>
  <div class="dashboard">
    <div class="stats-row">
      <el-card class="stat-card">
        <div class="stat-icon">🏗️</div>
        <div class="stat-info">
          <div class="stat-value">{{ totalBrackets }}</div>
          <div class="stat-label">总支架数</div>
        </div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-icon">👥</div>
        <div class="stat-info">
          <div class="stat-value">{{ totalGroups }}</div>
          <div class="stat-label">总组别数</div>
        </div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-icon">🔗</div>
        <div class="stat-info">
          <div class="stat-value">{{ totalBindings }}</div>
          <div class="stat-label">绑定关系数</div>
        </div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-icon">📏</div>
        <div class="stat-info">
          <div class="stat-value">{{ distanceCount }}</div>
          <div class="stat-label">竞速距离类型</div>
        </div>
      </el-card>
    </div>

    <div class="filter-row">
      <el-form :inline="true" :model="filterForm">
        <el-form-item label="距离区间">
          <el-input-number v-model="filterForm.minDistance" placeholder="最小距离" :min="0" />
        </el-form-item>
        <el-form-item>
          <span class="range-separator">-</span>
        </el-form-item>
        <el-form-item>
          <el-input-number v-model="filterForm.maxDistance" placeholder="最大距离" :min="0" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleFilter">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="chart-section">
      <el-card>
        <template #header>
          <span>竞速距离区间统计</span>
        </template>
        <div ref="chartRef" class="chart"></div>
      </el-card>
    </div>

    <div class="table-section">
      <el-card>
        <template #header>
          <span>距离分组配套支架明细</span>
        </template>
        <el-table :data="stats" border stripe>
          <el-table-column prop="distance" label="竞速距离" width="120">
            <template #default="{ row }">{{ row.distanceLabel }}</template>
          </el-table-column>
          <el-table-column prop="bracketCount" label="配套支架数" width="120" />
          <el-table-column prop="groupCount" label="关联组别数" width="120" />
          <el-table-column prop="brackets" label="配套支架" min-width="200">
            <template #default="{ row }">
              <el-tag v-for="bracket in row.brackets" :key="bracket.id" size="small" class="bracket-tag">
                {{ bracket.bracketCode }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="groups" label="关联组别" min-width="200">
            <template #default="{ row }">
              <el-tag v-for="group in row.groups" :key="group.id" size="small" type="success" class="group-tag">
                {{ group.groupName }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button size="small" @click="viewDetail(row)">查看详情</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>

    <el-dialog v-model="detailDialogVisible" :title="`${selectedStat?.distanceLabel} 详情`" width="600px">
      <div v-if="selectedStat">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="竞速距离">{{ selectedStat.distanceLabel }}</el-descriptions-item>
          <el-descriptions-item label="配套支架数">{{ selectedStat.bracketCount }}</el-descriptions-item>
          <el-descriptions-item label="关联组别数">{{ selectedStat.groupCount }}</el-descriptions-item>
        </el-descriptions>
        <div class="detail-section">
          <h4>配套支架列表</h4>
          <el-table :data="selectedStat.brackets" border>
            <el-table-column prop="bracketCode" label="支架编号" />
            <el-table-column prop="loadCapacity" label="承重(kg)" />
            <el-table-column prop="minDistance" label="最小距离" />
            <el-table-column prop="maxDistance" label="最大距离" />
          </el-table>
        </div>
        <div class="detail-section">
          <h4>关联组别列表</h4>
          <el-table :data="selectedStat.groups" border>
            <el-table-column prop="groupName" label="组别名称" />
            <el-table-column prop="groupCode" label="组别编码" />
            <el-table-column prop="racingDistance" label="竞速距离" />
          </el-table>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'
import { statApi, bracketApi, groupApi, bindingApi, type DistanceStat } from '@/api'

const chartRef = ref<HTMLElement>()
let chartInstance: echarts.ECharts | null = null

const stats = ref<DistanceStat[]>([])
const totalBrackets = ref(0)
const totalGroups = ref(0)
const totalBindings = ref(0)
const distanceCount = ref(0)

const filterForm = ref({
  minDistance: null as number | null,
  maxDistance: null as number | null
})

const detailDialogVisible = ref(false)
const selectedStat = ref<DistanceStat>()

const initChart = () => {
  if (!chartRef.value) return
  chartInstance = echarts.init(chartRef.value)
  updateChart()
}

const updateChart = () => {
  if (!chartInstance) return
  const option: echarts.EChartsOption = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    legend: {
      data: ['支架数', '组别数']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: stats.value.map(s => s.distanceLabel)
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '支架数',
        type: 'bar',
        data: stats.value.map(s => s.bracketCount),
        itemStyle: { color: '#4f81bd' }
      },
      {
        name: '组别数',
        type: 'bar',
        data: stats.value.map(s => s.groupCount),
        itemStyle: { color: '#9bbb59' }
      }
    ]
  }
  chartInstance.setOption(option)
}

const fetchData = async () => {
  try {
    if (filterForm.value.minDistance !== null && filterForm.value.maxDistance !== null) {
      stats.value = await statApi.getDistanceStatsByRange(filterForm.value.minDistance, filterForm.value.maxDistance)
    } else {
      stats.value = await statApi.getAllDistanceStats()
    }
    distanceCount.value = stats.value.length

    const [brackets, groups, bindings] = await Promise.all([
      bracketApi.list(),
      groupApi.list(),
      bindingApi.getAllActive()
    ])
    totalBrackets.value = brackets.length
    totalGroups.value = groups.length
    totalBindings.value = bindings.length

    updateChart()
  } catch (error) {
    console.error('获取数据失败:', error)
  }
}

const handleFilter = () => {
  fetchData()
}

const resetFilter = () => {
  filterForm.value = { minDistance: null, maxDistance: null }
  fetchData()
}

const viewDetail = (stat: DistanceStat) => {
  selectedStat.value = stat
  detailDialogVisible.value = true
}

onMounted(() => {
  fetchData()
  initChart()
  window.addEventListener('resize', () => chartInstance?.resize())
})
</script>

<style scoped>
.dashboard {
  padding: 0;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  font-size: 40px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #1a365d;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

.filter-row {
  background: white;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.range-separator {
  margin: 0 8px;
  color: #999;
}

.chart-section {
  margin-bottom: 20px;
}

.chart {
  height: 350px;
}

.table-section {
  margin-bottom: 20px;
}

.bracket-tag,
.group-tag {
  margin-right: 8px;
  margin-bottom: 4px;
}

.detail-section {
  margin-top: 20px;
}

.detail-section h4 {
  margin-bottom: 12px;
  font-size: 14px;
  font-weight: bold;
}
</style>