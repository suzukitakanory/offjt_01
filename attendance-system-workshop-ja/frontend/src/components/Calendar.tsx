import React, { useState, useMemo, useRef } from 'react'

/**
 * カレンダーコンポーネント
 * 関連要件: REQ-UC001 - システム起動, REQ-UC002 - カレンダー操作
 * 関連画面項目: SCR001-2 (年月表示), SCR001-3 (前月ボタン), SCR001-4 (次月ボタン),
 *              SCR001-5 (曜日ヘッダー), SCR001-6 (日付セル), SCR001-7 (カレンダーグリッド),
 *              SCR001-8 (年月選択モーダル)
 */

interface CalendarProps {
  onDateSelect?: (date: Date) => void
}

const formatDateKey = (date: Date): string => {
  const year = date.getFullYear()
  const month = `${date.getMonth() + 1}`.padStart(2, '0')
  const day = `${date.getDate()}`.padStart(2, '0')
  return `${year}-${month}-${day}`
}

const selectableYears = Array.from({ length: 201 }, (_, index) => 1900 + index)
const selectableMonths = Array.from({ length: 12 }, (_, index) => index + 1)

const Calendar: React.FC<CalendarProps> = ({ onDateSelect }) => {
  const [currentDate, setCurrentDate] = useState(new Date())
  const [selectedDate, setSelectedDate] = useState<Date | null>(null)
  const [draftYear, setDraftYear] = useState(currentDate.getFullYear())
  const [draftMonth, setDraftMonth] = useState(currentDate.getMonth() + 1)
  const todayKey = useMemo(() => formatDateKey(new Date()), [])
  const selectedDateKey = useMemo(() => (selectedDate ? formatDateKey(selectedDate) : null), [selectedDate])
  const monthPickerRef = useRef<HTMLDialogElement>(null)
  const monthPickerTriggerRef = useRef<HTMLButtonElement>(null)

  // 現在表示中の年月を取得
  const year = currentDate.getFullYear()
  const month = currentDate.getMonth()

  // カレンダーの日付セルを生成（useMemoでパフォーマンス最適化）
  const calendarDays = useMemo(() => {
    // 月の最初の日と最後の日を取得
    const firstDay = new Date(year, month, 1)
    const lastDay = new Date(year, month + 1, 0)

    // カレンダーグリッドの開始日（月の最初の日が属する週の日曜日）
    const startDate = new Date(firstDay)
    startDate.setDate(startDate.getDate() - startDate.getDay())

    // カレンダーグリッドの終了日（月の最後の日が属する週の土曜日）
    const endDate = new Date(lastDay)
    endDate.setDate(endDate.getDate() + (6 - endDate.getDay()))

    // カレンダーの日付セルを生成
    const days: Date[] = []
    const currentDay = new Date(startDate)
    while (currentDay <= endDate) {
      days.push(new Date(currentDay))
      currentDay.setDate(currentDay.getDate() + 1)
    }
    return days
  }, [year, month])

  // 前月へ移動 (REQ-UC002: SEQ-UC002-002)
  const goToPreviousMonth = () => {
    setCurrentDate(new Date(year, month - 1, 1))
  }

  // 次月へ移動 (REQ-UC002: SEQ-UC002-002)
  const goToNextMonth = () => {
    setCurrentDate(new Date(year, month + 1, 1))
  }

  const openMonthPicker = () => {
    setDraftYear(year)
    setDraftMonth(month + 1)
    const dialog = monthPickerRef.current
    if (dialog && !dialog.open) {
      dialog.showModal()
    }
  }

  const handleMonthPickerSubmit = (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setCurrentDate(new Date(draftYear, draftMonth - 1, 1))
    monthPickerRef.current?.close()
  }

  const restoreMonthPickerFocus = () => {
    monthPickerTriggerRef.current?.focus()
  }

  // 日付セルクリックハンドラー (REQ-UC002: SEQ-UC002-001, SEQ-UC002-003)
  const handleDateClick = (date: Date) => {
    // 選択日付を更新
    setSelectedDate(date)
    
    // 親コンポーネントに通知
    if (onDateSelect) {
      onDateSelect(date)
    }
  }

  // 日付セルのクラス名を決定
  const getDayClassName = (date: Date): string => {
    const classes: string[] = ['calendar-day']
    const dayKey = formatDateKey(date)
    const dayOfWeek = date.getDay()

    // 当月以外の日付
    if (date.getMonth() !== month) {
      classes.push('other-month')
    }

    // 本日
    if (dayKey === todayKey) {
      classes.push('today')
    }

    // 選択中の日付 (REQ-UC002: 選択状態のハイライト)
    if (selectedDateKey && dayKey === selectedDateKey) {
      classes.push('selected')
    }

    if (dayOfWeek === 0) {
      classes.push('sunday')
    } else if (dayOfWeek === 6) {
      classes.push('saturday')
    }

    return classes.join(' ')
  }

  // 曜日ヘッダー
  const weekDays = ['日', '月', '火', '水', '木', '金', '土']

  return (
    <div className="calendar">
      {/* 年月表示とナビゲーション (SCR001-2, SCR001-3, SCR001-4) */}
      <div className="calendar-header">
        <button
          className="calendar-nav-button"
          onClick={goToPreviousMonth}
          aria-label="前月へ"
        >
          &lt;
        </button>
        <h2 className="calendar-title">
          <button
            ref={monthPickerTriggerRef}
            className="calendar-title-button"
            type="button"
            aria-label="年月を選択"
            aria-haspopup="dialog"
            onClick={openMonthPicker}
          >
            {year}年 {month + 1}月
          </button>
        </h2>
        <button
          className="calendar-nav-button"
          onClick={goToNextMonth}
          aria-label="次月へ"
        >
          &gt;
        </button>
      </div>

      <dialog
        ref={monthPickerRef}
        className="calendar-month-picker"
        aria-labelledby="calendar-month-picker-title"
        onClose={restoreMonthPickerFocus}
      >
        <h2 id="calendar-month-picker-title">年月を選択</h2>
        <form onSubmit={handleMonthPickerSubmit}>
          <div className="calendar-month-picker-fields">
            <label htmlFor="calendar-month-picker-year">年</label>
            <select
              id="calendar-month-picker-year"
              value={draftYear}
              onChange={(event) => setDraftYear(Number(event.target.value))}
            >
              {selectableYears.map((selectableYear) => (
                <option key={selectableYear} value={selectableYear}>
                  {selectableYear}年
                </option>
              ))}
            </select>
            <label htmlFor="calendar-month-picker-month">月</label>
            <select
              id="calendar-month-picker-month"
              value={draftMonth}
              onChange={(event) => setDraftMonth(Number(event.target.value))}
            >
              {selectableMonths.map((selectableMonth) => (
                <option key={selectableMonth} value={selectableMonth}>
                  {selectableMonth}月
                </option>
              ))}
            </select>
          </div>
          <div className="calendar-month-picker-actions">
            <button type="button" onClick={() => monthPickerRef.current?.close()}>
              キャンセル
            </button>
            <button type="submit">確定</button>
          </div>
        </form>
      </dialog>

      {/* カレンダーグリッド (SCR001-7) */}
      <div className="calendar-grid">
        {/* 曜日ヘッダー (SCR001-5) */}
        {weekDays.map((day, index) => (
          <div key={`weekday-${index}`} className="calendar-weekday">
            {day}
          </div>
        ))}

        {/* 日付セル (SCR001-6) */}
        {calendarDays.map((date, index) => (
          <div
            key={`day-${index}`}
            className={getDayClassName(date)}
            data-date={formatDateKey(date)}
            onClick={() => handleDateClick(date)}
            role="button"
            tabIndex={0}
            aria-label={`${date.getFullYear()}年${date.getMonth() + 1}月${date.getDate()}日`}
            onKeyDown={(e) => {
              if (e.key === 'Enter' || e.key === ' ') {
                e.preventDefault()
                handleDateClick(date)
              }
            }}
          >
            {date.getDate()}
          </div>
        ))}
      </div>
    </div>
  )
}

export default Calendar
