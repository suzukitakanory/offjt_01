import type {
  AttendanceApiErrorResponse,
  AttendanceRecord,
  AttendanceStatus,
  WorkTimeRequest,
} from '../types/attendance'

const API_BASE_URL = '/api/attendance'

export class AttendanceApiError extends Error {
  constructor(
    public readonly errorCode: string,
    message: string,
    public readonly timestamp: string,
    public readonly status: number
  ) {
    super(message)
    this.name = 'AttendanceApiError'
  }
}

const isAttendanceRecord = (value: unknown): value is AttendanceRecord => {
  if (typeof value !== 'object' || value === null) return false
  const record = value as Record<string, unknown>
  const statuses: AttendanceStatus[] = ['WORK', 'PAID_LEAVE', 'SICK_LEAVE', 'UNPAID_LEAVE']
  return (
    typeof record.id === 'number' &&
    typeof record.employeeId === 'string' &&
    typeof record.attendanceDate === 'string' &&
    (typeof record.clockInTime === 'string' || record.clockInTime === null) &&
    (typeof record.clockOutTime === 'string' || record.clockOutTime === null) &&
    typeof record.status === 'string' &&
    statuses.some((status) => status === record.status) &&
    (typeof record.notes === 'string' || record.notes === null) &&
    typeof record.createdAt === 'string' &&
    typeof record.updatedAt === 'string'
  )
}

const isAttendanceApiError = (value: unknown): value is AttendanceApiErrorResponse => {
  if (typeof value !== 'object' || value === null) return false
  const error = value as Record<string, unknown>
  return (
    typeof error.error === 'string' &&
    typeof error.message === 'string' &&
    typeof error.timestamp === 'string'
  )
}

const throwApiError = async (response: Response): Promise<never> => {
  let payload: unknown
  try {
    payload = await response.json()
  } catch {
    throw new Error(`勤怠APIでエラーが発生しました (HTTP ${response.status})`)
  }
  if (!isAttendanceApiError(payload)) {
    throw new Error(`勤怠APIから不正なエラー応答を受信しました (HTTP ${response.status})`)
  }
  throw new AttendanceApiError(payload.error, payload.message, payload.timestamp, response.status)
}

const readAttendanceRecord = async (response: Response): Promise<AttendanceRecord> => {
  const payload: unknown = await response.json()
  if (!isAttendanceRecord(payload)) {
    throw new Error('勤怠APIから不正な勤怠レコードを受信しました')
  }
  return payload
}

export const saveAttendance = async (request: WorkTimeRequest): Promise<AttendanceRecord> => {
  const response = await fetch(API_BASE_URL, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(request),
  })
  if (!response.ok) return throwApiError(response)
  return readAttendanceRecord(response)
}

export const getAttendanceByDate = async (
  employeeId: string,
  date: string
): Promise<AttendanceRecord | null> => {
  const response = await fetch(
    `${API_BASE_URL}/${encodeURIComponent(employeeId)}/${encodeURIComponent(date)}`
  )
  if (response.status === 404) {
    return null
  }
  if (!response.ok) return throwApiError(response)
  return readAttendanceRecord(response)
}

export const getAttendancesByEmployeeId = async (
  employeeId: string,
  startDate?: string,
  endDate?: string
): Promise<AttendanceRecord[]> => {
  const query = new URLSearchParams()
  if (startDate !== undefined) query.set('startDate', startDate)
  if (endDate !== undefined) query.set('endDate', endDate)
  const queryString = query.toString()
  const suffix = queryString ? `?${queryString}` : ''
  const response = await fetch(`${API_BASE_URL}/${encodeURIComponent(employeeId)}${suffix}`)
  if (!response.ok) return throwApiError(response)
  const payload: unknown = await response.json()
  if (!Array.isArray(payload) || !payload.every(isAttendanceRecord)) {
    throw new Error('勤怠APIから不正な勤怠一覧を受信しました')
  }
  return payload
}
