import axios from 'axios'

const service = axios.create({
  baseURL: '/api',
  timeout: 10000
})

service.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code !== 200) {
      return Promise.reject(new Error(res.message || 'Error'))
    }
    return res.data
  },
  (error) => {
    return Promise.reject(error)
  }
)

export interface Bracket {
  id: number
  bracketCode: string
  loadCapacity: number
  minDistance: number
  maxDistance: number
  status: number
  remark: string
  createdAt: string
  updatedAt: string
}

export interface Group {
  id: number
  groupName: string
  groupCode: string
  racingDistance: number
  description: string
  planFileName: string | null
  createdAt: string
  updatedAt: string
}

export interface Binding {
  id: number
  bracketId: number
  bracketCode: string
  bracketMinDistance: number | null
  bracketMaxDistance: number | null
  bracketStatus: number | null
  groupId: number
  groupName: string
  groupCode: string | null
  racingDistance: number | null
  bindingTime: string
  status: number
}

/** 组别改距离后被当场拆掉的绑定明细 */
export interface UnboundBinding {
  bindingId: number
  bracketId: number
  bracketCode: string
  bracketMinDistance: number
  bracketMaxDistance: number
  groupId: number
  groupName: string
  groupCode: string
  previousDistance: number
  newDistance: number
}

export interface GroupUpdateResult {
  group: Group
  distanceChanged: boolean
  previousDistance: number | null
  newDistance: number | null
  unboundBindings: UnboundBinding[]
  unboundCount: number
}

export interface ChangeLog {
  id: number
  bracketId: number
  bracketCode: string
  groupId: number
  groupName: string
  changeType: string
  previousDistance: number | null
  newDistance: number | null
  changeReason: string
  operator: string
  changedAt: string
}

export interface DistanceStat {
  distance: number
  distanceLabel: string
  bracketCount: number
  groupCount: number
  brackets: Bracket[]
  groups: Group[]
}

/** 训练课次（排课）。valid/overloaded 由后端按支架“当前承重”实时判定 */
export interface Session {
  id: number
  sessionDate: string
  bracketId: number
  bracketCode: string | null
  loadCapacity: number | null
  bracketStatus: number | null
  groupId: number | null
  groupName: string | null
  groupCode: string | null
  expectedPersonCount: number
  remark: string | null
  createdAt: string
  updatedAt: string
  valid: boolean
  overloaded: boolean
}

export interface SessionFormPayload {
  id?: number
  sessionDate: string
  bracketId: number
  groupId?: number | null
  expectedPersonCount: number
  remark?: string
}

export interface PageResult<T> {
  data: T[]
  total: number
  pageNum: number
  pageSize: number
}

export const bracketApi = {
  create: (data: Omit<Bracket, 'id' | 'status' | 'createdAt' | 'updatedAt'>) =>
    service.post('/bracket', data) as unknown as Promise<Bracket>,
  update: (data: Partial<Bracket> & { id: number }) =>
    service.put('/bracket', data) as unknown as Promise<Bracket>,
  delete: (id: number) => service.delete(`/bracket/${id}`) as unknown as Promise<void>,
  getById: (id: number) => service.get(`/bracket/${id}`) as unknown as Promise<Bracket>,
  list: () => service.get('/bracket/list') as unknown as Promise<Bracket[]>,
  page: (params: {
    bracketCode?: string
    minDistance?: number
    maxDistance?: number
    status?: number
    pageNum?: number
    pageSize?: number
  }) => service.get('/bracket/page', { params }) as unknown as Promise<PageResult<Bracket>>,
  getByDistance: (distance: number) => service.get(`/bracket/distance/${distance}`) as unknown as Promise<Bracket[]>
}

export const groupApi = {
  create: (data: Omit<Group, 'id' | 'planFileName' | 'createdAt' | 'updatedAt'>) =>
    service.post('/group', data) as unknown as Promise<Group>,
  update: (data: Partial<Omit<Group, 'planFileName' | 'createdAt' | 'updatedAt'>> & { id: number }) =>
    service.put('/group', data) as unknown as Promise<GroupUpdateResult>,
  delete: (id: number) => service.delete(`/group/${id}`) as unknown as Promise<void>,
  getById: (id: number) => service.get(`/group/${id}`) as unknown as Promise<Group>,
  list: () => service.get('/group/list') as unknown as Promise<Group[]>,
  page: (params: {
    groupName?: string
    groupCode?: string
    racingDistance?: number
    pageNum?: number
    pageSize?: number
  }) => service.get('/group/page', { params }) as unknown as Promise<PageResult<Group>>,
  getByDistance: (distance: number) => service.get(`/group/distance/${distance}`) as unknown as Promise<Group[]>,
  getDistinctDistances: () => service.get('/group/distances') as unknown as Promise<number[]>,
  uploadPlan: (id: number, file: File) => {
    const formData = new FormData()
    formData.append('file', file)
    return service.post(`/group/${id}/plan`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    }) as unknown as Promise<Group>
  },
  planDownloadUrl: (id: number) => `/api/group/${id}/plan`
}

export const bindingApi = {
  bind: (data: { bracketId: number; groupId: number; operator?: string; reason?: string }) =>
    service.post('/binding/bind', data) as unknown as Promise<Binding>,
  unbind: (data: { bracketId: number; groupId: number; operator?: string; reason?: string }) =>
    service.post('/binding/unbind', data) as unknown as Promise<Binding>,
  getById: (id: number) => service.get(`/binding/${id}`) as unknown as Promise<Binding>,
  getByBracketId: (bracketId: number) => service.get(`/binding/bracket/${bracketId}`) as unknown as Promise<Binding[]>,
  getByGroupId: (groupId: number) => service.get(`/binding/group/${groupId}`) as unknown as Promise<Binding[]>,
  getAllActive: () => service.get('/binding/active') as unknown as Promise<Binding[]>,
  getAll: () => service.get('/binding/all') as unknown as Promise<Binding[]>,
  getLogsByBracketId: (bracketId: number) => service.get(`/binding/logs/bracket/${bracketId}`) as unknown as Promise<ChangeLog[]>,
  getLogsByGroupId: (groupId: number) => service.get(`/binding/logs/group/${groupId}`) as unknown as Promise<ChangeLog[]>,
  queryLogs: (params: {
    bracketCode?: string
    groupName?: string
    changeType?: string
    pageNum?: number
    pageSize?: number
  }) => service.get('/binding/logs/page', { params }) as unknown as Promise<PageResult<ChangeLog>>,
  exportLogsUrl: (params: { bracketCode?: string; groupName?: string; changeType?: string }) => {
    const query = new URLSearchParams()
    if (params.bracketCode) query.append('bracketCode', params.bracketCode)
    if (params.groupName) query.append('groupName', params.groupName)
    if (params.changeType) query.append('changeType', params.changeType)
    const qs = query.toString()
    return `/api/binding/logs/export${qs ? `?${qs}` : ''}`
  },
  getRecentLogs: (limit?: number) => service.get('/binding/logs/recent', { params: { limit } }) as unknown as Promise<ChangeLog[]>
}

export const statApi = {
  getAllDistanceStats: () => service.get('/stat/distance/all') as unknown as Promise<DistanceStat[]>,
  getDistanceStat: (distance: number) => service.get(`/stat/distance/${distance}`) as unknown as Promise<DistanceStat>,
  getDistanceStatsByRange: (minDistance: number, maxDistance: number) =>
    service.get('/stat/distance/range', { params: { minDistance, maxDistance } }) as unknown as Promise<DistanceStat[]>
}

export const sessionApi = {
  list: (params: { startDate?: string; endDate?: string } = {}) =>
    service.get('/session/list', { params }) as unknown as Promise<Session[]>,
  create: (data: SessionFormPayload) => service.post('/session', data) as unknown as Promise<Session>,
  update: (data: SessionFormPayload) => service.put('/session', data) as unknown as Promise<Session>,
  delete: (id: number) => service.delete(`/session/${id}`) as unknown as Promise<void>
}