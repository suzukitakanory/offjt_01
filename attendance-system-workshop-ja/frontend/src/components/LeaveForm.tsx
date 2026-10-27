import React, { FormEvent, useEffect, useState } from 'react'
import {
  AttendanceServiceError,
  deleteLeaveByDate,
  getAttendanceByDate,
  saveAttendance,
} from '../services/attendanceService'
import { Uc004LeaveStatus } from '../types/attendance'

/**
 * 休暇登録フォームコンポーネント
 * 関連要件: REQ-UC004 - 休暇登録
 */
interface LeaveFormProps {
  attendanceDate: Date
  onClose: () => void
  onSaved: (message: string) => void
}

const EMPLOYEE_ID = 'EMP001'
const LEAVE_OPTIONS: { value: Uc004LeaveStatus; label: string }[] = [
  { value: 'SICK_LEAVE', label: 'Sick Leave' },
  { value: 'PERSONAL_LEAVE', label: 'Personal Leave' },
  { value: 'BUSINESS_TRIP', label: 'Business Trip' },
]

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

const LeaveForm: React.FC<LeaveFormProps> = ({ attendanceDate, onClose, onSaved }) => {
  const dateKey = toDateKey(attendanceDate)
  const [leaveStatus, setLeaveStatus] = useState<Uc004LeaveStatus | ''>('')
  const [notes, setNotes] = useState('')
  const [hasExistingLeave, setHasExistingLeave] = useState(false)
  const [message, setMessage] = useState('')
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [deleting, setDeleting] = useState(false)
  const [confirmingDelete, setConfirmingDelete] = useState(false)

  useEffect(() => {
    let active = true
    setLoading(true)
    setMessage('')
    setLeaveStatus('')
    setNotes('')
    setHasExistingLeave(false)

    getAttendanceByDate(EMPLOYEE_ID, dateKey)
      .then((record) => {
        if (!active || !record) {
          return
        }
        const existingLeave = LEAVE_OPTIONS.find((option) => option.value === record.status)
        if (existingLeave) {
          setLeaveStatus(existingLeave.value)
          setNotes(record.notes ?? '')
          setHasExistingLeave(true)
        }
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

    if (Number.isNaN(attendanceDate.getTime())) {
      setMessage('有効な日付をYYYY-MM-DD形式で指定してください')
      return
    }
    const selectedLeave = LEAVE_OPTIONS.find((option) => option.value === leaveStatus)
    if (!selectedLeave) {
      setMessage('休暇区分を選択してください')
      return
    }

    setSaving(true)
    try {
      await saveAttendance({
        employeeId: EMPLOYEE_ID,
        attendanceDate: dateKey,
        clockInTime: null,
        clockOutTime: null,
        status: selectedLeave.value,
        notes: notes.trim() === '' ? null : notes,
      })
      onSaved('勤怠情報が正常に保存されました')
    } catch (error: unknown) {
      setMessage(displayError(error))
    } finally {
      setSaving(false)
    }
  }

  const handleDelete = async () => {
    setMessage('')
    setDeleting(true)
    try {
      await deleteLeaveByDate(EMPLOYEE_ID, dateKey)
      onSaved('休暇情報が正常に削除されました')
    } catch (error: unknown) {
      setMessage(displayError(error))
      setConfirmingDelete(false)
    } finally {
      setDeleting(false)
    }
  }

  return (
    <div className="leave-modal-backdrop">
      <section
        className="leave-modal"
        role="dialog"
        aria-modal="true"
        aria-labelledby="leave-title"
        aria-busy={loading || saving || deleting}
        data-testid="leave-dialog"
      >
        <header className="leave-modal-header">
          <h2 id="leave-title">休暇登録</h2>
          <button type="button" aria-label="閉じる" onClick={onClose}>
            ×
          </button>
        </header>
        <form onSubmit={handleSubmit}>
          <p>勤怠日: <time dateTime={dateKey}>{dateKey}</time></p>
          <label htmlFor="leave-type">休暇区分</label>
          <select
            id="leave-type"
            data-testid="leave-type"
            value={leaveStatus}
            onChange={(event) => setLeaveStatus(event.target.value as Uc004LeaveStatus | '')}
            disabled={loading || saving || deleting}
            required
          >
            <option value="">選択してください</option>
            {LEAVE_OPTIONS.map((option) => (
              <option key={option.value} value={option.value}>{option.label}</option>
            ))}
          </select>
          <label htmlFor="leave-notes">メモ</label>
          <textarea
            id="leave-notes"
            data-testid="leave-notes"
            value={notes}
            onChange={(event) => setNotes(event.target.value)}
            disabled={loading || saving || deleting}
          />
          {message && <p className="form-message" role="alert">{message}</p>}
          {loading && <p role="status">勤怠情報を読み込んでいます...</p>}
          <footer className="leave-form-actions">
            {hasExistingLeave && (
              <button
                type="button"
                data-testid="delete-leave"
                onClick={() => setConfirmingDelete(true)}
                disabled={loading || saving || deleting}
              >
                削除
              </button>
            )}
            <button type="button" onClick={onClose}>キャンセル</button>
            <button type="submit" disabled={loading || saving || deleting} data-testid="save-attendance">
              保存
            </button>
          </footer>
        </form>
        {confirmingDelete && (
          <div className="leave-confirmation-backdrop">
            <section
              className="leave-confirmation"
              role="alertdialog"
              aria-modal="true"
              aria-labelledby="leave-delete-title"
            >
              <h3 id="leave-delete-title">休暇レコードを削除しますか？</h3>
              <p>この操作は取り消せません。</p>
              <footer className="leave-form-actions">
                <button
                  type="button"
                  data-testid="cancel-delete"
                  onClick={() => setConfirmingDelete(false)}
                  disabled={deleting}
                >
                  キャンセル
                </button>
                <button
                  type="button"
                  data-testid="confirm-delete"
                  onClick={handleDelete}
                  disabled={deleting}
                >
                  削除する
                </button>
              </footer>
            </section>
          </div>
        )}
      </section>
    </div>
  )
}

export default LeaveForm
