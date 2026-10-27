import { AttendanceRecord, AttendanceRequest } from '../types/attendance'

const API_BASE_URL = '/api/attendance'

export class AttendanceServiceError extends Error {
  constructor(
    public readonly code: string,
    message: string
  ) {
    super(message)
    this.name = 'AttendanceServiceError'
  }
}

/**
 * 勤怠レコードを保存する。
 * @since 1.0.0
 */
export const saveAttendance = async (request: AttendanceRequest): Promise<AttendanceRecord> => {
  let response: Response
  try {
    response = await fetch(API_BASE_URL, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(request),
    })
  } catch {
    throw new AttendanceServiceError(
      'ERR99002',
      'ネットワークエラーが発生しました。接続を確認してください。'
    )
  }

  if (!response.ok) {
    throw await readApiError(response)
  }
  return response.json() as Promise<AttendanceRecord>
}

/**
 * 指定日の勤怠レコードを取得する。
 * @since 1.0.0
 */
export const getAttendanceByDate = async (
  employeeId: string,
  date: string
): Promise<AttendanceRecord | null> => {
  let response: Response
  try {
    response = await fetch(`${API_BASE_URL}/${employeeId}/${date}`)
  } catch {
    throw new AttendanceServiceError(
      'ERR99002',
      'ネットワークエラーが発生しました。接続を確認してください。'
    )
  }

  if (response.status === 404) {
    return null
  }
  if (!response.ok) {
    throw await readApiError(response)
  }
  return response.json() as Promise<AttendanceRecord>
}

async function readApiError(response: Response): Promise<AttendanceServiceError> {
  const body: unknown = await response.json()
  if (
    typeof body === 'object' &&
    body !== null &&
    'error' in body &&
    'message' in body &&
    typeof body.error === 'string' &&
    typeof body.message === 'string'
  ) {
    return new AttendanceServiceError(body.error, body.message)
  }
  throw new AttendanceServiceError(
    'ERR99001',
    `APIエラーが発生しました（HTTP ${response.status}）`
  )
}
