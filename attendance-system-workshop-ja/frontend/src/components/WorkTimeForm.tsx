import React, { FormEvent, useEffect, useState } from 'react'
import { AttendanceServiceError, getAttendanceByDate, saveAttendance } from '../services/attendanceService'

interface WorkTimeFormProps {
  attendanceDate: Date
  onClose: () => void
  onSaved: (message: string) => void
}

const EMPLOYEE_ID = 'EMP001'
const INVALID_TIME_MESSAGE = '有効な時刻形式で入力してください'
const MISSING_TIME_MESSAGE = '出勤時刻と退勤時刻を入力してください'
const INVALID_ORDER_MESSAGE = '出勤時刻は退勤時刻より前である必要があります'

const toDateKey = (date: Date): string => {
  const year = date.getFullYear()
  const month = `${date.getMonth() + 1}`.padStart(2, '0')
  const day = `${date.getDate()}`.padStart(2, '0')
  return `${year}-${month}-${day}`
}

const displayError = (error: unknown): string => {
  if (error instanceof AttendanceServiceError) {
    return error.message
  }
  return '予期しないエラーが発生しました。システム管理者に連絡してください。'
}

const WorkTimeForm: React.FC<WorkTimeFormProps> = ({ attendanceDate, onClose, onSaved }) => {
  const dateKey = toDateKey(attendanceDate)
  const [clockInTime, setClockInTime] = useState('')
  const [clockOutTime, setClockOutTime] = useState('')
  const [message, setMessage] = useState('')
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    let active = true
    setLoading(true)
    setMessage('')

    getAttendanceByDate(EMPLOYEE_ID, dateKey)
      .then((record) => {
        if (!active || !record) {
          return
        }
        setClockInTime(record.clockInTime?.slice(0, 5) ?? '')
        setClockOutTime(record.clockOutTime?.slice(0, 5) ?? '')
      })
      .catch((error: unknown) => {
        if (active) {
          setMessage(displayError(error))
        }
      })
      .finally(() => {
        if (active) {
          setLoading(false)
        }
      })

    return () => {
      active = false
    }
  }, [dateKey])

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setMessage('')

    const timePattern = /^([01]\d|2[0-3]):[0-5]\d$/
    if (
      (clockInTime !== '' && !timePattern.test(clockInTime)) ||
      (clockOutTime !== '' && !timePattern.test(clockOutTime))
    ) {
      setMessage(INVALID_TIME_MESSAGE)
      return
    }
    if (!clockInTime || !clockOutTime) {
      setMessage(MISSING_TIME_MESSAGE)
      return
    }
    if (clockInTime >= clockOutTime) {
      setMessage(INVALID_ORDER_MESSAGE)
      return
    }

    setSaving(true)
    try {
      await saveAttendance({
        employeeId: EMPLOYEE_ID,
        attendanceDate: dateKey,
        clockInTime,
        clockOutTime,
        status: 'WORK',
      })
      onSaved('勤怠情報が正常に保存されました')
    } catch (error: unknown) {
      setMessage(displayError(error))
    } finally {
      setSaving(false)
    }
  }

  return (
    <div
      className="work-time-modal-backdrop"
      onClick={(event) => {
        if (event.target === event.currentTarget) {
          onClose()
        }
      }}
    >
      <section
        className="work-time-modal"
        role="dialog"
        aria-modal="true"
        aria-labelledby="work-time-title"
        data-testid="work-time-dialog"
      >
        <header className="work-time-modal-header">
          <h2 id="work-time-title">勤務時間入力</h2>
          <button type="button" aria-label="閉じる" onClick={onClose}>
            ×
          </button>
        </header>
        <form onSubmit={handleSubmit}>
          <p>勤怠日: <time dateTime={dateKey}>{dateKey}</time></p>
          <label htmlFor="clock-in-time">出勤時刻</label>
          <input
            id="clock-in-time"
            data-testid="clock-in-time"
            type="text"
            inputMode="numeric"
            maxLength={5}
            placeholder="HH:MM"
            value={clockInTime}
            onChange={(event) => setClockInTime(event.target.value)}
          />
          <label htmlFor="clock-out-time">退勤時刻</label>
          <input
            id="clock-out-time"
            data-testid="clock-out-time"
            type="text"
            inputMode="numeric"
            maxLength={5}
            placeholder="HH:MM"
            value={clockOutTime}
            onChange={(event) => setClockOutTime(event.target.value)}
          />
          {message && <p className="form-message" role="alert">{message}</p>}
          {loading && <p role="status">勤怠情報を読み込んでいます...</p>}
          <footer className="work-time-form-actions">
            <button type="button" onClick={onClose}>キャンセル</button>
            <button type="submit" disabled={loading || saving} data-testid="save-attendance">
              保存
            </button>
          </footer>
        </form>
      </section>
    </div>
  )
}

export default WorkTimeForm
