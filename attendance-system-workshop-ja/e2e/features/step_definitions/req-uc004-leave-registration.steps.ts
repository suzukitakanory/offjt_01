import { Given, Then, When } from '@cucumber/cucumber'
import { expect } from '@playwright/test'
import { World } from './world'

const API_BASE_URL = 'http://localhost:8080/api/attendance'
const LEAVE_STATUSES = {
  'Sick Leave': 'SICK_LEAVE',
  'Personal Leave': 'PERSONAL_LEAVE',
  'Business Trip': 'BUSINESS_TRIP',
} as const

const formatDateKey = (date: Date): string => {
  const year = date.getFullYear()
  const month = `${date.getMonth() + 1}`.padStart(2, '0')
  const day = `${date.getDate()}`.padStart(2, '0')
  return `${year}-${month}-${day}`
}

const selectDate = async (world: World, date: string): Promise<void> => {
  const targetDate = new Date(`${date}T00:00:00`)
  const title = await world.page.locator('.calendar-title').textContent()
  const match = title?.match(/(\d{4})年\s*(\d{1,2})月/)
  if (!match) {
    throw new Error(`カレンダーの年月を読み取れません: ${title ?? ''}`)
  }

  const displayedYear = Number(match[1])
  const displayedMonth = Number(match[2])
  const monthDifference =
    (targetDate.getFullYear() - displayedYear) * 12 +
    (targetDate.getMonth() + 1 - displayedMonth)
  const navigationButton = world.page.locator('.calendar-nav-button').nth(monthDifference < 0 ? 0 : 1)

  for (let month = 0; month < Math.abs(monthDifference); month += 1) {
    await navigationButton.click()
  }

  const dateCell = world.page.locator(`.calendar-day[data-date="${date}"]`)
  await expect(dateCell).toBeVisible()
  await dateCell.click()
  world.context.selectedDate = date
}

interface AttendanceRecord {
  id: number
  status: string
  notes: string | null
}

const statusForLeaveLabel = (label: string): string => {
  if (!(label in LEAVE_STATUSES)) {
    throw new Error(`未対応の休暇種別です: ${label}`)
  }
  return LEAVE_STATUSES[label as keyof typeof LEAVE_STATUSES]
}

const parseAttendanceRecord = (value: unknown): AttendanceRecord => {
  if (
    typeof value !== 'object' ||
    value === null ||
    !('id' in value) ||
    typeof value.id !== 'number' ||
    !('status' in value) ||
    typeof value.status !== 'string'
  ) {
    throw new Error('勤怠APIのレスポンス形式が不正です')
  }

  const notes = 'notes' in value ? value.notes : null
  if (notes !== null && typeof notes !== 'string') {
    throw new Error('勤怠APIのメモ形式が不正です')
  }

  return { id: value.id, status: value.status, notes }
}

const createLeaveRecord = async (
  world: World,
  employeeId: string,
  date: string,
  leaveLabel: string,
  notes: string | null = null
): Promise<AttendanceRecord> => {
  const response = await world.page.request.post(API_BASE_URL, {
    data: {
      employeeId,
      attendanceDate: date,
      clockInTime: null,
      clockOutTime: null,
      status: statusForLeaveLabel(leaveLabel),
      notes,
    },
  })

  expect(response.status(), '休暇レコードの前提データを作成できること').toBe(200)
  return parseAttendanceRecord(await response.json())
}

const openLeaveDialog = async (world: World): Promise<void> => {
  await world.page.getByTestId('btn-leave').click()
  await expect(world.page.getByTestId('leave-dialog')).toBeVisible()
  await expect(world.page.getByTestId('leave-type')).toBeEnabled()
}

const fetchAttendance = async (world: World, employeeId = 'EMP001'): Promise<AttendanceRecord> => {
  const date = world.context.selectedDate
  if (!date) {
    throw new Error('選択中の日付が記録されていません')
  }

  const response = await world.page.request.get(`${API_BASE_URL}/${employeeId}/${date}`)
  expect(response.status(), '指定日の勤怠レコードが取得できること').toBe(200)
  return parseAttendanceRecord(await response.json())
}

Given(
  '従業員 {string} が日付 {string} に {string} を登録済みである',
  async function (this: World, employeeId: string, date: string, leaveLabel: string) {
    const record = await createLeaveRecord(this, employeeId, date, leaveLabel, '登録済みの休暇')
    this.context.leaveRecordId = record.id
  }
)

Given(
  '従業員 {string} が日付 {string} に休暇を登録済みである',
  async function (this: World, employeeId: string, date: string) {
    const record = await createLeaveRecord(this, employeeId, date, 'Sick Leave')
    this.context.leaveRecordId = record.id
  }
)

Given('従業員が休暇登録用の未来の日付を選択している', async function (this: World) {
  const futureDate = new Date()
  futureDate.setDate(futureDate.getDate() + 7)
  await selectDate(this, formatDateKey(futureDate))
})

When('従業員が休暇種別 {string} を選択する', async function (this: World, leaveLabel: string) {
  await openLeaveDialog(this)
  await this.page.getByTestId('leave-type').selectOption({ label: leaveLabel })
})

When('従業員がメモ {string} を入力する', async function (this: World, notes: string) {
  await this.page.getByTestId('leave-notes').fill(notes)
})

When('従業員がメモを入力しない', async function (this: World) {
  await this.page.getByTestId('leave-notes').fill('')
})

Then('休暇情報が正常に保存される', async function (this: World) {
  await expect(this.page.getByTestId('save-success'))
    .toContainText('勤怠情報が正常に保存されました')
})

Then('勤怠ステータスが {string} に設定される', async function (this: World, leaveLabel: string) {
  const record = await fetchAttendance(this)
  expect(record.status).toBe(statusForLeaveLabel(leaveLabel))
})

Then('既存の休暇レコードが更新される', async function (this: World) {
  await expect(this.page.getByTestId('save-success')).toBeVisible()
  const previousRecordId = this.context.leaveRecordId
  if (previousRecordId === undefined) {
    throw new Error('更新前の休暇レコードIDがありません')
  }

  const record = await fetchAttendance(this)
  expect(record.id).toBe(previousRecordId)
  expect(record.notes).toBe('私用に変更')
})

When('従業員が削除ボタンをクリックする', async function (this: World) {
  await openLeaveDialog(this)
  await this.page.getByTestId('delete-leave').click()
})

When('従業員が削除確認ダイアログで確認する', async function (this: World) {
  await expect(this.page.getByRole('alertdialog')).toBeVisible()
  await this.page.getByTestId('confirm-delete').click()
})

Then('休暇レコードが削除される', async function (this: World) {
  await expect(this.page.getByTestId('save-success')).toBeVisible()
  const date = this.context.selectedDate
  if (!date) {
    throw new Error('選択中の日付が記録されていません')
  }

  const response = await this.page.request.get(`${API_BASE_URL}/EMP001/${date}`)
  expect(response.status()).toBe(404)
})

Then('削除成功メッセージが表示される', async function (this: World) {
  await expect(this.page.getByTestId('save-success'))
    .toContainText('休暇情報が正常に削除されました')
})

Then('メモが空の状態で保存される', async function (this: World) {
  const record = await fetchAttendance(this)
  expect(record.notes ?? '').toBe('')
})
