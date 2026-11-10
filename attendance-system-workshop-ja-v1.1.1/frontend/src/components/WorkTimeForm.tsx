import { useEffect, useState, type FormEvent } from 'react'
import {
  AttendanceApiError,
  getAttendanceByDate,
  saveAttendance,
} from '../services/attendanceService'

interface WorkTimeFormProps {
  attendanceDate: string
  onCancel: () => void
  onSaved: () => void
}

const TIME_PATTERN = /^([01]\d|2[0-3]):[0-5]\d$/
const NETWORK_ERROR_MESSAGE = 'ERR99002: ネットワークエラーが発生しました。接続を確認してください。'

const getErrorMessage = (error: unknown): string => {
  if (error instanceof AttendanceApiError) {
    return `${error.errorCode}: ${error.message}`
  }
  return NETWORK_ERROR_MESSAGE
}

/**
 * 勤務時間入力フォームコンポーネント
 * 関連要件: REQ-UC003 - 勤務時間入力
 */
const WorkTimeForm = ({ attendanceDate, onCancel, onSaved }: WorkTimeFormProps) => {
  const [clockInTime, setClockInTime] = useState('')
  const [clockOutTime, setClockOutTime] = useState('')
  const [isLoading, setIsLoading] = useState(true)
  const [isSaving, setIsSaving] = useState(false)
  const [message, setMessage] = useState('')

  useEffect(() => {
    let active = true
    setClockInTime('')
    setClockOutTime('')
    setMessage('')
    setIsLoading(true)

    getAttendanceByDate('EMP001', attendanceDate)
      .then((record) => {
        if (!active || !record) return
        setClockInTime(record.clockInTime?.slice(0, 5) ?? '')
        setClockOutTime(record.clockOutTime?.slice(0, 5) ?? '')
      })
      .catch((error: unknown) => {
        if (active) setMessage(getErrorMessage(error))
      })
      .finally(() => {
        if (active) setIsLoading(false)
      })

    return () => {
      active = false
    }
  }, [attendanceDate])

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    if (!clockInTime || !clockOutTime) {
      setMessage('ERR03003: 出勤時刻と退勤時刻を入力してください')
      return
    }
    if (!TIME_PATTERN.test(clockInTime) || !TIME_PATTERN.test(clockOutTime)) {
      setMessage('ERR03002: 有効な時刻形式で入力してください')
      return
    }
    if (clockInTime >= clockOutTime) {
      setMessage('ERR03001: 出勤時刻は退勤時刻より前である必要があります')
      return
    }

    setIsSaving(true)
    setMessage('')
    try {
      await saveAttendance({
        employeeId: 'EMP001',
        attendanceDate,
        clockInTime,
        clockOutTime,
        status: 'WORK',
      })
      onSaved()
    } catch (error: unknown) {
      setMessage(getErrorMessage(error))
    } finally {
      setIsSaving(false)
    }
  }

  return (
    <div className="work-time-backdrop">
      <section
        className="work-time-dialog"
        role="dialog"
        aria-modal="true"
        aria-labelledby="work-time-title"
      >
        <header className="work-time-dialog-header">
          <h2 id="work-time-title">勤務時間入力</h2>
          <button type="button" aria-label="閉じる" onClick={onCancel} disabled={isSaving}>
            ×
          </button>
        </header>
        <p className="work-time-date">対象日: {attendanceDate}</p>
        <form onSubmit={handleSubmit} noValidate>
          <label className="work-time-field">
            出勤時刻
            <input
              type="time"
              step="60"
              required
              value={clockInTime}
              onChange={(event) => setClockInTime(event.target.value)}
              disabled={isLoading || isSaving}
            />
          </label>
          <label className="work-time-field">
            退勤時刻
            <input
              type="time"
              step="60"
              required
              value={clockOutTime}
              onChange={(event) => setClockOutTime(event.target.value)}
              disabled={isLoading || isSaving}
            />
          </label>
          {isLoading && <p role="status">勤怠情報を読み込んでいます...</p>}
          {message && (
            <p className="work-time-message" role="alert">
              {message}
            </p>
          )}
          <div className="work-time-actions">
            <button type="button" onClick={onCancel} disabled={isSaving}>
              キャンセル
            </button>
            <button type="submit" disabled={isLoading || isSaving}>
              {isSaving ? '保存中...' : '保存'}
            </button>
          </div>
        </form>
      </section>
    </div>
  )
}

export default WorkTimeForm
