import { LEAVE_TYPES, type CreateLeaveRequest, type LeaveApiErrorResponse, type LeaveRecord } from '../types/leave'

const API_BASE_URL = '/api/leaves'

/** 休暇APIが返すエラーコードとHTTPステータスを保持するエラー。 */
export class LeaveApiError extends Error {
  /** APIエラーの内容を初期化する。 */
  constructor(
    public readonly errorCode: string,
    message: string,
    public readonly status: number
  ) {
    super(message)
    this.name = 'LeaveApiError'
  }
}

/** 値がアプリで扱う休暇区分のいずれかであることを判定する。 */
const isLeaveType = (value: unknown): value is (typeof LEAVE_TYPES)[number] =>
  typeof value === 'string' && LEAVE_TYPES.some((leaveType) => leaveType === value)

/** APIレスポンスが休暇レコードの必須項目と型を満たすか検証する。 */
const isLeaveRecord = (value: unknown): value is LeaveRecord => {
  if (typeof value !== 'object' || value === null) return false
  const record = value as Record<string, unknown>
  return (
    typeof record.date === 'string' &&
    isLeaveType(record.leaveType) &&
    typeof record.memo === 'string'
  )
}

/** APIエラーレスポンスが利用者向けエラー項目を持つか検証する。 */
const isLeaveApiError = (value: unknown): value is LeaveApiErrorResponse => {
  if (typeof value !== 'object' || value === null) return false
  const error = value as Record<string, unknown>
  return typeof error.error === 'string' && typeof error.message === 'string'
}

/** エラー応答を検証し、APIエラーまたは不正応答エラーとして送出する。 */
const throwApiError = async (response: Response): Promise<never> => {
  let payload: unknown
  try {
    payload = await response.json()
  } catch {
    throw new Error(`休暇APIでエラーが発生しました (HTTP ${response.status})`)
  }
  if (!isLeaveApiError(payload)) {
    throw new Error(`休暇APIから不正なエラー応答を受信しました (HTTP ${response.status})`)
  }
  throw new LeaveApiError(payload.error, payload.message, response.status)
}

/** 成功応答のJSONを休暇レコードとして検証して返す。 */
const readLeaveRecord = async (response: Response): Promise<LeaveRecord> => {
  const payload: unknown = await response.json()
  if (!isLeaveRecord(payload)) {
    throw new Error('休暇APIから不正な休暇レコードを受信しました')
  }
  return payload
}

/** 休暇登録APIを呼び出し、登録結果を検証して返す。 */
export const createLeave = async (request: CreateLeaveRequest): Promise<LeaveRecord> => {
  const response = await fetch(API_BASE_URL, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(request),
  })
  if (!response.ok) return throwApiError(response)
  return readLeaveRecord(response)
}

/** 指定日の休暇を取得し、未登録なら null を返す。 */
export const getLeaveByDate = async (date: string): Promise<LeaveRecord | null> => {
  const response = await fetch(`${API_BASE_URL}/${encodeURIComponent(date)}`)
  if (response.status === 404) return null
  if (!response.ok) return throwApiError(response)
  return readLeaveRecord(response)
}
