import React from 'react'
import './App.css'
import CalendarPage from './pages/CalendarPage'

/**
 * アプリケーションルートコンポーネント
 * 関連要件: REQ-UC001 - システム起動
 */
const App: React.FC = () => {
  return (
    <div className="App">
      <CalendarPage />
    </div>
  )
}

export default App
