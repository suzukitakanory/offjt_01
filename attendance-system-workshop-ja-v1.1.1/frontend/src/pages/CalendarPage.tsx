import { useState } from 'react'
import Calendar from '../components/Calendar'
import LeaveForm from '../components/LeaveForm'
import WorkTimeForm from '../components/WorkTimeForm'

/** 日付をAPIとフォームで使用する YYYY-MM-DD 形式に変換する。 */
const formatDateKey = (date: Date): string =>
  `${date.getFullYear()}-${`${date.getMonth() + 1}`.padStart(2, '0')}-${`${date.getDate()}`.padStart(2, '0')}`

/**
 * メイン画面（カレンダー表示）
 * 関連要件: REQ-UC001 - システム起動, REQ-UC002 - カレンダー操作
 * 関連画面: SCR-001
 */
const CalendarPage = () => {
  const [selectedDate, setSelectedDate] = useState<Date | null>(null)
  const [isWorkTimeFormOpen, setIsWorkTimeFormOpen] = useState(false)
  const [isLeaveFormOpen, setIsLeaveFormOpen] = useState(false)
  const [successMessage, setSuccessMessage] = useState('')

  /** 選択日を保持し、前回の登録成功メッセージを消去する。 */
  const handleDateSelect = (date: Date) => {
    setSelectedDate(date)
    setSuccessMessage('')
  }

  /** 選択日を画面表示用の年月日形式に変換する。 */
  const formatDate = (date: Date): string => {
    return `${date.getFullYear()}年${date.getMonth() + 1}月${date.getDate()}日`
  }

  return (
    <div className="calendar-page">
      <header className="page-header">
        <h1>勤怠管理システム</h1>
      </header>
      <main className="page-main">
        <Calendar onDateSelect={handleDateSelect} />
        
        {/* 選択日付に応じた操作オプション (REQ-UC002) */}
        {selectedDate && (
          <div className="action-options" data-testid="action-options">
            <h2>選択日: {formatDate(selectedDate)}</h2>
            <div className="action-buttons">
              <button 
                className="btn-work-time"
                data-testid="btn-work-time"
                onClick={() => {
                  setSuccessMessage('')
                  setIsWorkTimeFormOpen(true)
                }}
              >
                勤務時間入力
              </button>
              <button 
                className="btn-leave"
                data-testid="btn-leave"
                onClick={() => {
                  setSuccessMessage('')
                  setIsLeaveFormOpen(true)
                }}
              >
                休暇登録
              </button>
            </div>
          </div>
        )}
        {successMessage && <p className="success-message" role="status">{successMessage}</p>}
      </main>
      {isWorkTimeFormOpen && selectedDate && (
        <WorkTimeForm
          attendanceDate={formatDateKey(selectedDate)}
          onCancel={() => setIsWorkTimeFormOpen(false)}
          onSaved={() => {
            setIsWorkTimeFormOpen(false)
            setSuccessMessage('勤怠情報が正常に保存されました')
          }}
        />
      )}
      {isLeaveFormOpen && selectedDate && (
        <LeaveForm
          date={formatDateKey(selectedDate)}
          onCancel={() => setIsLeaveFormOpen(false)}
          onSaved={(message) => {
            setIsLeaveFormOpen(false)
            setSuccessMessage(message)
          }}
        />
      )}
    </div>
  )
}

export default CalendarPage
