import React, { useState } from 'react'
import Calendar from '../components/Calendar'

/**
 * メイン画面（カレンダー表示）
 * 関連要件: REQ-UC001 - システム起動, REQ-UC002 - カレンダー操作
 * 関連画面: SCR-001
 */
const CalendarPage: React.FC = () => {
  const [selectedDate, setSelectedDate] = useState<Date | null>(null)

  // 日付選択時のハンドラー (REQ-UC002)
  const handleDateSelect = (date: Date) => {
    setSelectedDate(date)
  }

  // 日付をフォーマット
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
                disabled
              >
                勤務時間入力
              </button>
              <button 
                className="btn-leave"
                data-testid="btn-leave"
                disabled
              >
                休暇登録
              </button>
            </div>
            <p className="info-text">
              ※ 勤務時間入力と休暇登録機能は今後の要件で実装されます
            </p>
          </div>
        )}
      </main>
    </div>
  )
}

export default CalendarPage
