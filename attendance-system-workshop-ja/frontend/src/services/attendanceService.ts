import { AttendanceRecord, AttendanceRequest } from '../types/attendance'

const API_BASE_URL = '/api/attendance'

/**
 * 勤怠APIサービス
 * 関連要件: REQ-UC002 - カレンダー操作, REQ-UC003 - 勤務時間入力, REQ-UC004 - 休暇登録
 */

/**
 * 勤怠レコードを保存する
 * TODO: REQ-UC003, REQ-UC004 - API連携実装予定
 */
export const saveAttendance = async (request: AttendanceRequest): Promise<AttendanceRecord> => {
  // TODO: REQ-UC003, REQ-UC004 - 実装予定
  const response = await fetch(API_BASE_URL, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(request),
  })
  return response.json()
}

/**
 * 特定日の勤怠レコードを取得する
 * TODO: REQ-UC003 - API連携実装予定
 */
export const getAttendanceByDate = async (
  employeeId: string,
  date: string
): Promise<AttendanceRecord | null> => {
  // TODO: REQ-UC003 - 実装予定
  const response = await fetch(`${API_BASE_URL}/${employeeId}/${date}`)
  if (response.status === 404) {
    return null
  }
  return response.json()
}

/**
 * 従業員の勤怠レコード一覧を取得する
 * TODO: REQ-UC002 - API連携実装予定
 */
export const getAttendancesByEmployeeId = async (employeeId: string): Promise<AttendanceRecord[]> => {
  // TODO: REQ-UC002 - 実装予定
  const response = await fetch(`${API_BASE_URL}/${employeeId}`)
  return response.json()
}
