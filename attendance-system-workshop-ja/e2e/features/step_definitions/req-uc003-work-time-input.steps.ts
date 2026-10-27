import { Given, Then, When } from '@cucumber/cucumber'
import { expect } from '@playwright/test'
import { World } from './world'

const formatDateKey = (date: Date): string => {
  const year = date.getFullYear()
  const month = `${date.getMonth() + 1}`.padStart(2, '0')
  const day = `${date.getDate()}`.padStart(2, '0')
  return `${year}-${month}-${day}`
}

const parseCalendarTitle = (title: string | null): { year: number; month: number } => {
  const match = title?.match(/(\d{4})年\s*(\d{1,2})月/)
  if (!match) {
    throw new Error(`カレンダーの年月を読み取れません: ${title ?? ''}`)
  }
  return { year: Number(match[1]), month: Number(match[2]) }
}

const selectDate = async (world: World, date: string): Promise<void> => {
  const targetDate = new Date(`${date}T00:00:00`)
  const title = await world.page.locator('.calendar-title').textContent()
  const current = parseCalendarTitle(title)
  const monthDifference =
    (targetDate.getFullYear() - current.year) * 12 +
    (targetDate.getMonth() + 1 - current.month)
  const navigationButton = world.page.locator('.calendar-nav-button').nth(monthDifference < 0 ? 0 : 1)

  for (let month = 0; month < Math.abs(monthDifference); month += 1) {
    await navigationButton.click()
  }

  const dateCell = world.page.locator(`.calendar-day[data-date="${date}"]`)
  await expect(dateCell).toBeVisible()
  await dateCell.click()
  world.context.selectedDate = date
}

const ensureRecordExists = async (world: World, employeeId: string, date: string): Promise<void> => {
  const response = await world.page.request.post('http://localhost:8080/api/attendance', {
    data: {
      employeeId,
      attendanceDate: date,
      clockInTime: '09:00',
      clockOutTime: '18:00',
      status: 'WORK',
    },
  })
  expect(response.status()).toBe(200)
}

Given('従業員が日付 {string} を選択している', async function (this: World, date: string) {
  await selectDate(this, date)
})

Given('従業員が未来の日付を選択している', async function (this: World) {
  const futureDate = new Date()
  futureDate.setDate(futureDate.getDate() + 1)
  await selectDate(this, formatDateKey(futureDate))
})

Given(
  '従業員 {string} が日付 {string} に勤怠レコードを登録済みである',
  async function (this: World, employeeId: string, date: string) {
    await ensureRecordExists(this, employeeId, date)
  }
)

Given(
  '従業員 {string} が日付 {string} に勤怠レコードを登録している',
  async function (this: World, employeeId: string, date: string) {
    await ensureRecordExists(this, employeeId, date)
  }
)

When('従業員が勤務時間入力フォームを開く', async function (this: World) {
  await this.page.getByTestId('btn-work-time').click()
  await expect(this.page.getByTestId('work-time-dialog')).toBeVisible()
  await expect(this.page.getByTestId('save-attendance')).toBeEnabled()
})

When('従業員が出勤時刻 {string} を入力する', async function (this: World, time: string) {
  await this.page.getByTestId('clock-in-time').fill(time)
})

When('従業員が退勤時刻 {string} を入力する', async function (this: World, time: string) {
  await this.page.getByTestId('clock-out-time').fill(time)
})

When('従業員が保存ボタンをクリックする', async function (this: World) {
  this.context.saveRequestCount = 0
  this.page.on('request', (request) => {
    if (request.method() === 'POST' && request.url().endsWith('/api/attendance')) {
      this.context.saveRequestCount = (this.context.saveRequestCount ?? 0) + 1
    }
  })
  await this.page.getByTestId('save-attendance').click()
})

When(
  'システムが従業員 {string} の日付 {string} の勤怠レコードを取得する',
  async function (this: World, employeeId: string, date: string) {
    const response = await this.page.request.get(
      `http://localhost:8080/api/attendance/${employeeId}/${date}`
    )
    this.context.attendanceResponse = {
      status: response.status(),
      body: await response.json(),
    }
  }
)

Then('バリデーションエラーが表示される', async function (this: World) {
  await expect(this.page.getByRole('alert')).toBeVisible()
})

Then('エラーメッセージに {string} が含まれる', async function (this: World, message: string) {
  await expect(this.page.getByRole('alert')).toContainText(message)
})

Then('勤怠情報が保存されない', async function (this: World) {
  expect(this.context.saveRequestCount).toBe(0)
  await expect(this.page.getByTestId('work-time-dialog')).toBeVisible()
})

Then('勤怠情報が正常に保存される', async function (this: World) {
  await expect(this.page.getByTestId('save-success')).toContainText(
    '勤怠情報が正常に保存されました'
  )
})

Then('保存成功メッセージが表示される', async function (this: World) {
  await expect(this.page.getByTestId('save-success')).toBeVisible()
})

Then('既存の勤怠レコードが更新される', async function (this: World) {
  await expect(this.page.getByTestId('save-success')).toBeVisible()
  const employeeId = 'EMP001'
  const date = this.context.selectedDate
  expect(date).toBeDefined()
  const response = await this.page.request.get(
    `http://localhost:8080/api/attendance/${employeeId}/${date}`
  )
  expect(response.status()).toBe(200)
  const record: unknown = await response.json()
  expect(record).toMatchObject({
    clockInTime: '08:30:00',
    clockOutTime: '17:30:00',
  })
})

Then('勤怠レコードが正常に取得される', async function (this: World) {
  expect(this.context.attendanceResponse?.status).toBe(200)
})

Then('レコードに出勤時刻と退勤時刻が含まれる', async function (this: World) {
  const body = this.context.attendanceResponse?.body
  expect(body).toMatchObject({
    clockInTime: expect.any(String),
    clockOutTime: expect.any(String),
  })
})
