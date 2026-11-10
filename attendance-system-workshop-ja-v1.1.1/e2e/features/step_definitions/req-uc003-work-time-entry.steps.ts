import { Before, Given, Then, When } from '@cucumber/cucumber';
import { expect, APIResponse } from '@playwright/test';
import { World } from './world';

const API_URL = 'http://localhost:8080/api/attendance';
const EMPLOYEE_IDS = ['EMP991', 'EMP992', 'EMP993', 'EMP994', 'EMP995', 'EMP996'];
let scenarioIndex = 0;

const formatDate = (date: Date): string =>
  `${date.getFullYear()}-${`${date.getMonth() + 1}`.padStart(2, '0')}-${`${date.getDate()}`.padStart(2, '0')}`;

const resolveFeatureDate = (world: World, date: string): string =>
  date === '2025-01-15' ? world.context.uc003AttendanceDate : date;

const getSelectedDate = (world: World): string => {
  const date = world.context.selectedDate;
  if (!date) throw new Error('勤務日が選択されていません');
  return date;
};

const navigateToDate = async (world: World, date: string): Promise<void> => {
  const { page } = world;
  const target = new Date(`${date}T00:00:00`);
  const title = await page.locator('.calendar-title').innerText();
  const match = title.match(/(\d{4})年\s*(\d{1,2})月/);
  if (!match) throw new Error(`カレンダーの年月を読み取れません: ${title}`);

  const monthDifference =
    (target.getFullYear() - Number(match[1])) * 12 +
    target.getMonth() + 1 - Number(match[2]);
  const navigationButton = monthDifference > 0
    ? page.getByRole('button', { name: '次月へ' })
    : page.getByRole('button', { name: '前月へ' });
  for (let month = 0; month < Math.abs(monthDifference); month += 1) {
    await navigationButton.click();
  }

  await page.locator(`.calendar-day[data-date="${date}"]`).click();
  world.context.selectedDate = date;
};

const seedAttendance = async (
  world: World,
  date: string,
  clockInTime = '09:00',
  clockOutTime = '18:00'
): Promise<APIResponse> =>
  world.page.request.post(API_URL, {
    data: {
      employeeId: world.context.uc003EmployeeId,
      attendanceDate: date,
      clockInTime,
      clockOutTime,
      status: 'WORK',
    },
  });

const getAttendanceResponse = async (world: World, date: string): Promise<APIResponse> =>
  world.page.request.get(
    `${API_URL}/${world.context.uc003EmployeeId}/${date}`
  );

const installTestEmployeeRoute = async (world: World): Promise<void> => {
  const employeeId = world.context.uc003EmployeeId;
  await world.page.route('**/api/attendance**', async (route) => {
    const url = new URL(route.request().url());
    url.pathname = url.pathname.replace(/\/EMP001(?=\/|$)/, `/${employeeId}`);
    const options: { url: string; postData?: string } = { url: url.toString() };
    if (route.request().method() === 'POST') {
      const body = route.request().postDataJSON() as Record<string, unknown>;
      options.postData = JSON.stringify({ ...body, employeeId });
    }
    await route.continue(options);
  });
};

Before({ tags: '@REQ-UC003' }, function (this: World) {
  const index = scenarioIndex % EMPLOYEE_IDS.length;
  scenarioIndex += 1;
  this.context.uc003EmployeeId = EMPLOYEE_IDS[index];
  this.context.uc003AttendanceDate = formatDate(new Date(2025, 0, 15 + index));
});

Given('従業員が日付 {string} を選択している', async function (this: World, date: string) {
  await installTestEmployeeRoute(this);
  await navigateToDate(this, resolveFeatureDate(this, date));
  await this.page.getByRole('button', { name: '勤務時間入力' }).click();
});

Given('従業員が未来の日付を選択している', async function (this: World) {
  const futureDate = new Date();
  futureDate.setFullYear(futureDate.getFullYear() + 1);
  const date = formatDate(futureDate);
  await installTestEmployeeRoute(this);
  await navigateToDate(this, date);
  await this.page.getByRole('button', { name: '勤務時間入力' }).click();
});

Given(
  '従業員 {string} が日付 {string} に勤怠レコードを登録済みである',
  async function (this: World, _employeeId: string, date: string) {
    const resolvedDate = resolveFeatureDate(this, date);
    const response = await seedAttendance(this, resolvedDate);
    expect(response.status()).toBe(200);
  }
);

Given(
  '従業員 {string} が日付 {string} に勤怠レコードを登録している',
  async function (this: World, _employeeId: string, date: string) {
    const resolvedDate = resolveFeatureDate(this, date);
    const response = await seedAttendance(this, resolvedDate);
    expect(response.status()).toBe(200);
  }
);

When('従業員が出勤時刻 {string} を入力する', async function (this: World, time: string) {
  const input = this.page.getByLabel('出勤時刻');
  await expect(input).toBeEnabled();
  await input.fill(time);
  this.context.uc003ClockInTime = time;
});

When('従業員が退勤時刻 {string} を入力する', async function (this: World, time: string) {
  const input = this.page.getByLabel('退勤時刻');
  await expect(input).toBeEnabled();
  await input.fill(time);
  this.context.uc003ClockOutTime = time;
});

When('従業員が保存ボタンをクリックする', async function (this: World) {
  const date = getSelectedDate(this);
  const before = await getAttendanceResponse(this, date);
  this.context.uc003RecordBeforeSave = before.ok() ? await before.json() : null;
  await this.page.getByRole('button', { name: '保存', exact: true }).click();
});

When(
  'システムが従業員 {string} の日付 {string} の勤怠レコードを取得する',
  async function (this: World, _employeeId: string, date: string) {
    const response = await getAttendanceResponse(this, resolveFeatureDate(this, date));
    this.context.uc003GetResponseStatus = response.status();
    this.context.uc003GetResponseBody = response.ok() ? await response.json() : null;
  }
);

Then('勤怠情報が正常に保存される', async function (this: World) {
  await expect(this.page.getByRole('dialog')).toHaveCount(0);
  const response = await getAttendanceResponse(this, getSelectedDate(this));
  expect(response.status()).toBe(200);
  const record = await response.json();
  expect(record.status).toBe('WORK');
  expect(record.clockInTime).toBe(`${this.context.uc003ClockInTime}:00`);
  expect(record.clockOutTime).toBe(`${this.context.uc003ClockOutTime}:00`);
});

Then('保存成功メッセージが表示される', async function (this: World) {
  await expect(this.page.getByRole('status')).toContainText('勤怠情報が正常に保存されました');
});

Then('バリデーションエラーが表示される', async function (this: World) {
  await expect(this.page.getByRole('alert')).toBeVisible();
});

Then('エラーメッセージに {string} が含まれる', async function (this: World, message: string) {
  await expect(this.page.getByRole('alert')).toContainText(message);
});

Then('勤怠情報が保存されない', async function (this: World) {
  const response = await getAttendanceResponse(this, getSelectedDate(this));
  if (this.context.uc003RecordBeforeSave === null) {
    expect(response.status()).toBe(404);
    return;
  }
  expect(response.status()).toBe(200);
  expect(await response.json()).toEqual(this.context.uc003RecordBeforeSave);
});

Then('既存の勤怠レコードが更新される', async function (this: World) {
  await expect(this.page.getByRole('dialog')).toHaveCount(0);
  const response = await getAttendanceResponse(this, getSelectedDate(this));
  expect(response.status()).toBe(200);
  const record = await response.json();
  expect(record.clockInTime).toBe('08:30:00');
  expect(record.clockOutTime).toBe('17:30:00');
});

Then('勤怠レコードが正常に取得される', async function (this: World) {
  expect(this.context.uc003GetResponseStatus).toBe(200);
  expect(this.context.uc003GetResponseBody).not.toBeNull();
});

Then('レコードに出勤時刻と退勤時刻が含まれる', async function (this: World) {
  expect(this.context.uc003GetResponseBody.clockInTime).toBeTruthy();
  expect(this.context.uc003GetResponseBody.clockOutTime).toBeTruthy();
});
