/**
 * 勤怠レコード型定義
 * 関連要件: REQ-UC002 - カレンダー操作, REQ-UC003 - 勤務時間入力, REQ-UC004 - 休暇登録
 */
export type AttendanceStatus =
  | 'WORK'
  | 'PAID_LEAVE'
  | 'SICK_LEAVE'
  | 'UNPAID_LEAVE'
  | 'PERSONAL_LEAVE'
  | 'BUSINESS_TRIP'

export type Uc004LeaveStatus = 'SICK_LEAVE' | 'PERSONAL_LEAVE' | 'BUSINESS_TRIP'

export interface AttendanceRecord {
  id?: number
  employeeId: string
  attendanceDate: string
  clockInTime?: string | null
  clockOutTime?: string | null
  status?: AttendanceStatus
  notes?: string | null
  createdAt?: string
  updatedAt?: string
}

/**
 * 勤怠レコードリクエスト型定義
 */
export interface AttendanceRequest {
  employeeId: string
  attendanceDate: string
  clockInTime?: string | null
  clockOutTime?: string | null
  status: AttendanceStatus
  notes?: string | null
}
