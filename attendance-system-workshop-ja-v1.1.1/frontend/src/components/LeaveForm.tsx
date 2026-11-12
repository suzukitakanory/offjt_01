import { useEffect, useState, type FormEvent } from 'react'
import { LeaveApiError, createLeave, deleteLeave, getLeaveByDate, updateLeave } from '../services/leaveService'
import { LEAVE_TYPES, type LeaveType } from '../types/leave'

interface LeaveFormProps {
  date: string
  onCancel: () => void
  onSaved: (message: string) => void
}

const NETWORK_ERROR_MESSAGE = 'ネットワークエラーが発生しました。接続を確認してください。'

/** 入力値が選択可能な休暇区分かを判定する。 */
const isLeaveType = (value: string): value is LeaveType =>
  LEAVE_TYPES.some((leaveType) => leaveType === value)

/** APIエラーを利用者向け文言にし、それ以外は通信エラー文言にする。 */
const getErrorMessage = (error: unknown): string =>
  error instanceof LeaveApiError ? error.message : NETWORK_ERROR_MESSAGE

/**
 * 休暇登録フォームコンポーネント
 * 関連要件: REQ-UC004 - 休暇登録
 */
const LeaveForm = ({ date, onCancel, onSaved }: LeaveFormProps) => {
  const [leaveType, setLeaveType] = useState<LeaveType | ''>('')
  const [memo, setMemo] = useState('')
  const [isRegistered, setIsRegistered] = useState(false)
  const [isLoading, setIsLoading] = useState(true)
  const [loadFailed, setLoadFailed] = useState(false)
  const [loadRequestId, setLoadRequestId] = useState(0)
  const [isSaving, setIsSaving] = useState(false)
  const [hasExistingLeave, setHasExistingLeave] = useState(false)
  const [isDeleteConfirmationOpen, setIsDeleteConfirmationOpen] = useState(false)
  const [isDeleting, setIsDeleting] = useState(false)
  const [message, setMessage] = useState('')
  const [isConfirmationOpen, setIsConfirmationOpen] = useState(false)

  // 選択日の既存休暇を読み込み、日付変更後に古い応答で状態を上書きしないようにする。
  useEffect(() => {
    let active = true
    setLeaveType('')
    setMemo('')
    setIsRegistered(false)
    setHasExistingLeave(false)
    setIsSaving(false)
    setMessage('')
    setIsLoading(true)
    setLoadFailed(false)

    getLeaveByDate(date)
      .then((record) => {
        if (!active || !record) return
        setIsRegistered(true)
        setLeaveType(record.leaveType)
        setMemo(record.memo)
        setHasExistingLeave(true)
      })
      .catch((error: unknown) => {
        if (!active) return
        setMessage(getErrorMessage(error))
        setLoadFailed(true)
      })
      .finally(() => {
        if (active) setIsLoading(false)
      })

    return () => {
      active = false
    }
  }, [date, loadRequestId])

  /** 休暇を保存し、結果に応じて成功通知または確認・エラー表示を更新する。 */
  const saveLeave = async (type: LeaveType, confirmReplaceWork: boolean) => {
    setIsSaving(true)
    setMessage('')
    try {
      if (isRegistered) {
        await updateLeave(date, { leaveType: type, memo })
        onSaved('休暇を変更しました')
      } else {
        await createLeave({
          date,
          leaveType: type,
          memo,
          confirmReplaceWork,
        })
        onSaved('休暇を登録しました')
      }
    } catch (error: unknown) {
      if (error instanceof LeaveApiError && error.errorCode === 'ERR02005') {
        setIsConfirmationOpen(true)
      } else {
        setMessage(getErrorMessage(error))
      }
    } finally {
      setIsSaving(false)
    }
  }

  /** 入力値を検証して保存し、勤務日の置換確認が必要な場合は保存を保留する。 */
  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    if (isLoading || loadFailed || isSaving) return
    if (!leaveType) {
      setMessage('休暇区分を選択してください')
      return
    }
    if (memo.length > 500) {
      setMessage('メモは500文字以内で入力してください')
      return
    }
    await saveLeave(leaveType, false)
  }

  /** 確認済みの休暇を削除し、成功時にフォームを閉じる。 */
  const handleDelete = async () => {
    if (!hasExistingLeave || isLoading || loadFailed || isSaving || isDeleting) return
    setIsDeleting(true)
    setMessage('')
    try {
      await deleteLeave(date)
      onSaved('休暇を削除しました')
    } catch (error: unknown) {
      setMessage(getErrorMessage(error))
    } finally {
      setIsDeleting(false)
      setIsDeleteConfirmationOpen(false)
    }
  }

  return (
    <div className="work-time-backdrop">
      <section
        className="work-time-dialog"
        role="dialog"
        aria-modal="true"
        aria-labelledby="leave-form-title"
      >
        <header className="work-time-dialog-header">
          <h2 id="leave-form-title">休暇登録</h2>
          <button type="button" aria-label="閉じる" onClick={onCancel} disabled={isSaving}>
            ×
          </button>
        </header>
        <p className="work-time-date">{hasExistingLeave ? `${date} の休暇` : date}</p>
        <form onSubmit={handleSubmit} noValidate>
          <label className="work-time-field">
            休暇区分
            <select
              value={leaveType}
              onChange={(event) => {
                const value = event.target.value
                setLeaveType(isLeaveType(value) ? value : '')
              }}
              disabled={isLoading || loadFailed || isSaving}
            >
              <option value=""></option>
              {LEAVE_TYPES.map((type) => (
                <option key={type} value={type}>{type}</option>
              ))}
            </select>
          </label>
          <label className="work-time-field">
            メモ
            <textarea
              value={memo}
              onChange={(event) => setMemo(event.target.value)}
              disabled={isLoading || loadFailed || isSaving}
            />
          </label>
          {isLoading && <p role="status">休暇情報を読み込んでいます...</p>}
          {message && <p className="work-time-message" role="alert">{message}</p>}
          {loadFailed && (
            <button
              type="button"
              onClick={() => {
                setMessage('')
                setLoadRequestId((requestId) => requestId + 1)
              }}
            >
              再読み込み
            </button>
          )}
          <div className="work-time-actions">
            <button type="button" onClick={onCancel} disabled={isSaving || isDeleting}>
              キャンセル
            </button>
            {hasExistingLeave && (
              <button
                type="button"
                onClick={() => setIsDeleteConfirmationOpen(true)}
                disabled={isLoading || loadFailed || isSaving || isDeleting}
              >
                削除
              </button>
            )}
            <button type="submit" disabled={isLoading || loadFailed || isSaving || isDeleting}>
              {isSaving ? '保存中...' : isRegistered ? '変更を保存' : '登録'}
            </button>
          </div>
        </form>
      </section>
      {isConfirmationOpen && (
        <div className="work-time-backdrop leave-confirmation-backdrop">
          <section
            className="work-time-dialog"
            role="dialog"
            aria-modal="true"
            aria-labelledby="leave-confirmation-title"
          >
            <h2 id="leave-confirmation-title">勤務時間の確認</h2>
            <p>この日付には勤務時間が登録されています。休暇にすると出勤・退勤時刻は削除されます</p>
            <div className="work-time-actions">
              <button type="button" onClick={() => setIsConfirmationOpen(false)} disabled={isSaving}>
                キャンセル
              </button>
              <button
                type="button"
                onClick={() => {
                  setIsConfirmationOpen(false)
                  if (leaveType) void saveLeave(leaveType, true)
                }}
                disabled={isSaving}
              >
                確認
              </button>
            </div>
          </section>
        </div>
      )}
      {isDeleteConfirmationOpen && (
        <div className="work-time-backdrop leave-confirmation-backdrop">
          <section
            className="work-time-dialog"
            role="dialog"
            aria-modal="true"
            aria-labelledby="leave-delete-confirmation-title"
          >
            <h2 id="leave-delete-confirmation-title">削除確認</h2>
            <p>登録済みの休暇を削除しますか？</p>
            <div className="work-time-actions">
              <button
                type="button"
                onClick={() => setIsDeleteConfirmationOpen(false)}
                disabled={isDeleting}
              >
                キャンセル
              </button>
              <button type="button" onClick={() => void handleDelete()} disabled={isDeleting}>
                {isDeleting ? '削除中...' : '削除を確認'}
              </button>
            </div>
          </section>
        </div>
      )}
    </div>
  )
}

export default LeaveForm
