import { Given, When, Then } from '@cucumber/cucumber';
import { expect, APIRequestContext } from '@playwright/test';
import { World } from './world';

/**
 * REQ-UC004: 休暇登録のステップ定義
 * 期待文言は docs/design/message-definition.md に合わせる。
 */

const UI_BASE_URL = 'http://localhost:5173';
const API_BASE_URL = 'http://localhost:8080';
const LEAVE_TYPES = ['Sick Leave', 'Personal Leave', 'Business Trip'];

const formatDateKey = (date: Date): string => {
  const year = date.getFullYear();
  const month = `${date.getMonth() + 1}`.padStart(2, '0');
  const day = `${date.getDate()}`.padStart(2, '0');
  return `${year}-${month}-${day}`;
};

const extractYearMonth = (title: string | null): { year: number; month: number } | null => {
  const match = title?.match(/(\d{4})年\s*(\d{1,2})月/);
  if (!match) {
    return null;
  }
  return { year: Number(match[1]), month: Number(match[2]) };
};

const selectDate = async (world: World, date: string) => {
  const { page } = world;
  const target = new Date(`${date}T00:00:00`);
  const current = extractYearMonth(await page.locator('.calendar-title').textContent());
  if (current) {
    const monthDiff = (target.getFullYear() - current.year) * 12 + (target.getMonth() + 1 - current.month);
    const button = monthDiff > 0
      ? page.locator('.calendar-nav-button').last()
      : page.locator('.calendar-nav-button').first();
    for (let index = 0; index < Math.abs(monthDiff); index += 1) {
      await button.click();
    }
  }
  const dateCell = page.locator(`.calendar-day[data-date="${date}"]`);
  await dateCell.click();
  world.context.selectedDate = date;
};

const leaveDialog = (world: World) => world.page.getByRole('dialog', { name: '休暇登録' });

const leaveTypeField = (world: World) => leaveDialog(world).getByLabel('休暇区分');

const memoField = (world: World) => leaveDialog(world).getByLabel('メモ');

const request = (world: World): APIRequestContext => world.page.request;

Given('従業員が日付 {string} を選択している', async function (this: World, date: string) {
  await selectDate(this, date);
});

Given('従業員 {string} が日付 {string} に {string} を登録済みである', async function (
  this: World,
  employeeId: string,
  date: string,
  leaveType: string,
) {
  expect(employeeId).toBe('EMP001');
  expect(LEAVE_TYPES).toContain(leaveType);
  const response = await request(this).post(`${API_BASE_URL}/api/leaves`, {
    data: { date, leaveType, memo: '登録済み' },
  });
  expect(response.status()).toBe(201);
  this.context.selectedDate = date;
});

Given('従業員 {string} が日付 {string} に勤務時間を登録済みである', async function (
  this: World,
  employeeId: string,
  date: string,
) {
  const response = await request(this).post(`${API_BASE_URL}/api/attendance`, {
    data: {
      employeeId,
      attendanceDate: date,
      clockInTime: '09:00',
      clockOutTime: '18:00',
      status: 'WORK',
    },
  });
  expect(response.ok()).toBeTruthy();
  this.context.selectedDate = date;
  this.context.workDate = date;
});

When('従業員が休暇登録を開始する', async function (this: World) {
  await this.page.getByRole('button', { name: '休暇登録' }).click();
  await expect(leaveDialog(this)).toBeVisible();
});

When('従業員が休暇区分 {string} を選択する', async function (this: World, leaveType: string) {
  await leaveTypeField(this).selectOption({ label: leaveType });
});

When('従業員がメモ {string} を入力する', async function (this: World, memo: string) {
  await memoField(this).fill(memo);
});

When('従業員がメモ {string} を501文字入力する', async function (this: World, character: string) {
  await memoField(this).fill(character.repeat(501));
});

When('従業員がメモを入力しない', async function (this: World) {
  await expect(memoField(this)).toHaveValue('');
});

When('従業員が保存ボタンをクリックする', async function (this: World) {
  await leaveDialog(this).getByRole('button', { name: /^(登録|保存)$/ }).click();
});

When('従業員が勤務時刻の削除を確認する', async function (this: World) {
  const confirmation = this.page.getByRole('dialog').filter({
    hasText: 'この日付には勤務時間が登録されています。休暇にすると出勤・退勤時刻は削除されます',
  });
  await confirmation.getByRole('button', { name: '確認' }).click();
});

Then('休暇登録ダイアログが表示される', async function (this: World) {
  await expect(leaveDialog(this)).toBeVisible();
  await expect(leaveDialog(this).getByRole('heading', { name: '休暇登録' })).toBeVisible();
});

Then('休暇日に {string} が表示される', async function (this: World, date: string) {
  await expect(leaveDialog(this).getByText(date, { exact: true })).toBeVisible();
  expect(formatDateKey(new Date(`${date}T00:00:00`))).toBe(date);
});

Then('休暇区分の入力欄が表示される', async function (this: World) {
  await expect(leaveTypeField(this)).toBeVisible();
});

Then('メモの入力欄が表示される', async function (this: World) {
  await expect(memoField(this)).toBeVisible();
});

Then('休暇区分の選択肢は {string} と {string} と {string} だけである', async function (
  this: World,
  first: string,
  second: string,
  third: string,
) {
  const labels = (await leaveTypeField(this).locator('option').allTextContents())
    .map((label) => label.trim())
    .filter((label) => label.length > 0);
  expect(labels).toEqual([first, second, third]);
});

Then('{string} が表示される', async function (this: World, message: string) {
  await expect(this.page.getByText(message, { exact: true })).toBeVisible();
});

Then('休暇区分に {string} が表示される', async function (this: World, leaveType: string) {
  await expect(leaveTypeField(this)).toHaveValue(leaveType);
});

Then('メモに {string} が表示される', async function (this: World, memo: string) {
  await expect(memoField(this)).toHaveValue(memo);
});

Then('メモが空である', async function (this: World) {
  const date = this.context.selectedDate;
  await expect(memoField(this)).toHaveValue('');
  const response = await request(this).get(`${API_BASE_URL}/api/leaves/${date}`);
  expect(response.ok()).toBeTruthy();
  expect((await response.json()).memo).toBe('');
});

Then('休暇が保存されない', async function (this: World) {
  await expect(this.page.getByText('休暇を登録しました', { exact: true })).toHaveCount(0);
  const response = await request(this).get(`${API_BASE_URL}/api/leaves/${this.context.selectedDate}`);
  expect(response.status()).toBe(404);
});

Then('勤務時刻が残っている', async function (this: World) {
  const response = await request(this).get(`${API_BASE_URL}/api/attendance/EMP001/${this.context.workDate}`);
  expect(response.ok()).toBeTruthy();
  const body = await response.json();
  expect(body.clockInTime).toBeTruthy();
  expect(body.clockOutTime).toBeTruthy();
  expect(body.status).toBe('WORK');
});

Then('勤務時刻が削除されている', async function (this: World) {
  const response = await request(this).get(`${API_BASE_URL}/api/attendance/EMP001/${this.context.workDate}`);
  expect(response.ok()).toBeTruthy();
  const body = await response.json();
  expect(body.clockInTime ?? null).toBeNull();
  expect(body.clockOutTime ?? null).toBeNull();
});
