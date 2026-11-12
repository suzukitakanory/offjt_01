import { Before, Given, When, Then } from '@cucumber/cucumber';
import { expect, APIRequestContext } from '@playwright/test';
import { World } from './world';

/**
 * REQ-UC004: 休暇登録のステップ定義
 * 期待文言は docs/design/message-definition.md に合わせる。
 */

const UI_BASE_URL = 'http://localhost:5173';
const API_BASE_URL = 'http://localhost:8080';
const LEAVE_TYPES = ['Sick Leave', 'Personal Leave', 'Business Trip'];
const API_URL = `${API_BASE_URL}/api/leaves`;

/** Dateを休暇APIで使用する YYYY-MM-DD 形式に変換する。 */
const formatDateKey = (date: Date): string => {
  const year = date.getFullYear();
  const month = `${date.getMonth() + 1}`.padStart(2, '0');
  const day = `${date.getDate()}`.padStart(2, '0');
  return `${year}-${month}-${day}`;
};

/** 休暇日と勤務日の連続したテスト日を作るため、指定日数を加算する。 */
const addDays = (date: string, days: number): string => {
  const result = new Date(`${date}T00:00:00`);
  result.setDate(result.getDate() + days);
  return formatDateKey(result);
};

/** カレンダー見出しから年月を読み取り、判読できない場合は null を返す。 */
const extractYearMonth = (title: string | null): { year: number; month: number } | null => {
  const match = title?.match(/(\d{4})年\s*(\d{1,2})月/);
  if (!match) {
    return null;
  }
  return { year: Number(match[1]), month: Number(match[2]) };
};

/** カレンダーを対象月へ移動し、日付を選択してシナリオ状態に保存する。 */
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

/** 休暇登録ダイアログを取得する。 */
const leaveDialog = (world: World) => world.page.getByRole('dialog', { name: '休暇登録' });

/** 休暇登録ダイアログ内の休暇区分入力欄を取得する。 */
const leaveTypeField = (world: World) => leaveDialog(world).getByLabel('休暇区分');

/** 休暇登録ダイアログ内のメモ入力欄を取得する。 */
const memoField = (world: World) => leaveDialog(world).getByLabel('メモ');

/** シナリオのページからAPIリクエストコンテキストを取得する。 */
const request = (world: World): APIRequestContext => world.page.request;

/** 休暇登録対象の日付をカレンダーで選択する。 */
Given('従業員が休暇登録対象の日付 {string} を選択している', async function (this: World, date: string) {
  await selectDate(this, date);
});

/** 指定の休暇をAPIで事前登録し、対象日をシナリオ状態に保存する。 */
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

/** 指定の勤務時間をAPIで事前登録し、対象日をシナリオ状態に保存する。 */
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

/** 休暇登録フォームを開き、ダイアログが表示されたことを確認する。 */
When('従業員が休暇登録を開始する', async function (this: World) {
  await this.page.getByRole('button', { name: '休暇登録' }).click();
  await expect(leaveDialog(this)).toBeVisible();
});

/** フォームで指定された休暇区分を選択する。 */
When('従業員が休暇区分 {string} を選択する', async function (this: World, leaveType: string) {
  await leaveTypeField(this).selectOption({ label: leaveType });
});

/** フォームのメモ欄に指定された文字列を入力する。 */
When('従業員がメモ {string} を入力する', async function (this: World, memo: string) {
  await memoField(this).fill(memo);
});

/** メモ欄に501文字を入力し、上限超過の入力を作る。 */
When('従業員がメモ {string} を501文字入力する', async function (this: World, character: string) {
  await memoField(this).fill(character.repeat(501));
});

/** メモ欄が空のままであることを確認する。 */
When('従業員がメモを入力しない', async function (this: World) {
  await expect(memoField(this)).toHaveValue('');
});

/** 休暇登録フォームの登録または保存ボタンを押す。 */
When('従業員が休暇登録フォームの保存ボタンをクリックする', async function (this: World) {
  await leaveDialog(this).getByRole('button', { name: /^(登録|保存)$/ }).click();
});

/** 勤務時刻を削除する確認ダイアログで置換を承認する。 */
When('従業員が勤務時刻の削除を確認する', async function (this: World) {
  const confirmation = this.page.getByRole('dialog').filter({
    hasText: 'この日付には勤務時間が登録されています。休暇にすると出勤・退勤時刻は削除されます',
  });
  await confirmation.getByRole('button', { name: '確認' }).click();
});

/** 休暇登録ダイアログと見出しが表示されていることを確認する。 */
Then('休暇登録ダイアログが表示される', async function (this: World) {
  await expect(leaveDialog(this)).toBeVisible();
  await expect(leaveDialog(this).getByRole('heading', { name: '休暇登録' })).toBeVisible();
});

/** ダイアログに指定日が表示され、日付形式も正しいことを確認する。 */
Then('休暇日に {string} が表示される', async function (this: World, date: string) {
  await expect(leaveDialog(this).getByText(date, { exact: true })).toBeVisible();
  expect(formatDateKey(new Date(`${date}T00:00:00`))).toBe(date);
});

/** 休暇区分の入力欄が表示されていることを確認する。 */
Then('休暇区分の入力欄が表示される', async function (this: World) {
  await expect(leaveTypeField(this)).toBeVisible();
});

/** メモの入力欄が表示されていることを確認する。 */
Then('メモの入力欄が表示される', async function (this: World) {
  await expect(memoField(this)).toBeVisible();
});

/** 休暇区分の選択肢が指定された3種類だけであることを確認する。 */
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

/** 指定されたメッセージが画面に表示されていることを確認する。 */
Then('{string} が表示される', async function (this: World, message: string) {
  await expect(this.page.getByText(message, { exact: true })).toBeVisible();
});

/** 休暇区分欄に指定された休暇区分が選択されていることを確認する。 */
Then('休暇区分に {string} が表示される', async function (this: World, leaveType: string) {
  await expect(leaveTypeField(this)).toHaveValue(leaveType);
});

/** メモ欄に指定された文字列が表示されていることを確認する。 */
Then('メモに {string} が表示される', async function (this: World, memo: string) {
  await expect(memoField(this)).toHaveValue(memo);
});

/** APIから保存結果を取得し、メモが空文字であることを確認する。 */
Then('メモが空である', async function (this: World) {
  const date = this.context.selectedDate;
  const response = await request(this).get(`${API_BASE_URL}/api/leaves/${date}`);
  expect(response.ok()).toBeTruthy();
  expect((await response.json()).memo).toBe('');
});

/** 成功メッセージがなく、API上にも休暇が保存されていないことを確認する。 */
Then('休暇が保存されない', async function (this: World) {
  await expect(this.page.getByText('休暇を登録しました', { exact: true })).toHaveCount(0);
  const response = await request(this).get(`${API_BASE_URL}/api/leaves/${this.context.selectedDate}`);
  expect(response.status()).toBe(404);
});

/** APIで取得した勤務データに出勤・退勤時刻と勤務状態が残ることを確認する。 */
Then('勤務時刻が残っている', async function (this: World) {
  const response = await request(this).get(`${API_BASE_URL}/api/attendance/EMP001/${this.context.workDate}`);
  expect(response.ok()).toBeTruthy();
  const body = await response.json();
  expect(body.clockInTime).toBeTruthy();
  expect(body.clockOutTime).toBeTruthy();
  expect(body.status).toBe('WORK');
});

/** APIで取得した勤務データの出勤・退勤時刻が削除されたことを確認する。 */
Then('勤務時刻が削除されている', async function (this: World) {
  const response = await request(this).get(`${API_BASE_URL}/api/attendance/EMP001/${this.context.workDate}`);
  expect(response.ok()).toBeTruthy();
  const body = await response.json();
  expect(body.clockInTime ?? null).toBeNull();
  expect(body.clockOutTime ?? null).toBeNull();
});

let scenarioIndex = 0;

const getLeaveDate = (world: World): string => {
  const date = world.context.uc004LeaveDate;
  if (typeof date !== 'string') throw new Error('休暇テスト日が設定されていません');
  return date;
};

/** シナリオ開始時に確保した勤務のみの日付を取得する。 */
const getWorkDate = (world: World): string => {
  const date = world.context.uc004WorkDate;
  if (typeof date !== 'string') throw new Error('勤務テスト日が設定されていません');
  return date;
};

const openLeaveForm = async (world: World): Promise<void> => {
  const date = getLeaveDate(world);
  await selectDate(world, date);
  await world.page.getByRole('button', { name: '休暇登録', exact: true }).click();
  await expect(leaveDialog(world)).toBeVisible();
};

Before({ tags: '@leave' }, async function (this: World) {
  const offset = 45 + scenarioIndex * 2;
  scenarioIndex += 1;
  for (let attempt = 0; attempt < 60; attempt += 1) {
    const date = new Date();
    date.setDate(date.getDate() + offset + attempt * 2);
    const dateKey = formatDateKey(date);
    const workDate = addDays(dateKey, 1);
    const dates = [dateKey, workDate];
    // 休暇・勤務データが未登録の日を連続で確保し、シナリオ間の既存データとの衝突を避ける。
    const responses = await Promise.all(dates.flatMap((candidate) => [
      request(this).get(`${API_URL}/${candidate}`),
      request(this).get(`${API_BASE_URL}/api/attendance/EMP001/${candidate}`),
    ]));
    if (responses.every((response) => response.status() === 404)) {
      this.context.uc004LeaveDate = dateKey;
      this.context.uc004WorkDate = workDate;
      return;
    }
    const unexpected = responses.find((response) => response.status() !== 200 && response.status() !== 404);
    if (unexpected) {
      throw new Error(`休暇テスト日の確認に失敗しました (HTTP ${unexpected.status()})`);
    }
  }
  throw new Error('休暇テスト日の連続した2日間を確保できません');
});

Given('従業員のテスト休暇を登録済みにする', async function (this: World) {
  const response = await request(this).post(API_URL, {
    data: {
      date: getLeaveDate(this),
      leaveType: 'Sick Leave',
      memo: '更新前',
    },
  });
  expect(response.status()).toBe(201);
});

Given('期間内に勤務のみの日を用意する', async function (this: World) {
  // 期間一覧が勤務データを休暇として返さないことを確認するため、WORKのみの記録を作る。
  const response = await request(this).post(`${API_BASE_URL}/api/attendance`, {
    data: {
      employeeId: 'EMP001',
      attendanceDate: getWorkDate(this),
      clockInTime: '09:00',
      clockOutTime: '18:00',
      status: 'WORK',
    },
  });
  expect(response.ok()).toBeTruthy();
});

Given('従業員が登録済み休暇の変更フォームを開く', async function (this: World) {
  await openLeaveForm(this);
});

Given('従業員が休暇未登録のテスト日を選択している', async function (this: World) {
  await selectDate(this, getLeaveDate(this));
});

// フォームを閉じた後も、保存済みの休暇内容を再取得して表示できることを確認する。
When('従業員が休暇登録フォームを閉じる', async function (this: World) {
  await leaveDialog(this).getByRole('button', { name: '閉じる' }).click();
  await expect(leaveDialog(this)).toHaveCount(0);
});

When('従業員が選択日の休暇登録フォームを再度開く', async function (this: World) {
  await openLeaveForm(this);
});

// 通信失敗を未登録(404)と区別し、画面側のエラー表示・操作停止を検証する。
When('休暇取得APIを失敗させて休暇登録フォームを開く', async function (this: World) {
  await this.page.route(`**/api/leaves/${getLeaveDate(this)}`, (route) => route.abort('failed'));
  await openLeaveForm(this);
});

When('従業員がテスト期間の休暇一覧を取得する', async function (this: World) {
  this.context.uc004LeaveListResponse = await request(this).get(API_URL, {
    params: { from: getLeaveDate(this), to: getWorkDate(this) },
  });
});

// 期間の片側だけを指定した場合の入力検証レスポンスを確認する。
When('従業員が開始日だけ指定して休暇一覧を取得する', async function (this: World) {
  this.context.uc004LeaveListResponse = await request(this).get(API_URL, {
    params: { from: getLeaveDate(this) },
  });
});

When('従業員が休暇メモ {string} を入力する', async function (this: World, memo: string) {
  await memoField(this).fill(memo);
});

When('保存前に対象休暇を削除する', async function (this: World) {
  const response = await request(this).delete(`${API_URL}/${getLeaveDate(this)}`);
  expect(response.status()).toBe(204);
});

When('従業員が休暇の変更を保存する', async function (this: World) {
  const updateResponse = this.page.waitForResponse((response) =>
    response.request().method() === 'PUT' &&
    response.url().endsWith(`/api/leaves/${getLeaveDate(this)}`),
  );
  await Promise.all([
    updateResponse,
    this.page.getByRole('button', { name: '変更を保存', exact: true }).click(),
  ]);
});

Then('休暇区分が {string}、メモが {string} の休暇を再取得できる', async function (
  this: World,
  leaveType: string,
  memo: string,
) {
  const response = await request(this).get(`${API_URL}/${getLeaveDate(this)}`);
  expect(response.status()).toBe(200);
  expect(await response.json()).toMatchObject({
    date: getLeaveDate(this),
    leaveType,
    memo,
  });
});

Then('休暇変更成功メッセージが表示される', async function (this: World) {
  await expect(leaveDialog(this)).toHaveCount(0);
  await expect(this.page.getByRole('status')).toContainText('休暇を変更しました');
});

Then('更新対象がないことが表示され成功扱いにならない', async function (this: World) {
  const response = await request(this).get(`${API_URL}/${getLeaveDate(this)}`);
  expect(response.status()).toBe(404);
  await expect(this.page.getByRole('alert')).toContainText('指定した日付の休暇はありません');
  await expect(this.page.getByText('休暇を変更しました', { exact: true })).toHaveCount(0);
});

Then('未登録日の休暇登録フォームは空で利用可能である', async function (this: World) {
  await expect(leaveTypeField(this)).toHaveValue('');
  await expect(memoField(this)).toHaveValue('');
  await expect(leaveDialog(this).getByRole('alert')).toHaveCount(0);
  await expect(leaveDialog(this).getByRole('button', { name: '登録' })).toBeEnabled();
});

// 取得失敗の通知に加えて、入力欄と登録操作が無効化されることを確認する。
Then('休暇取得失敗が表示され操作が無効になる', async function (this: World) {
  await expect(leaveDialog(this).getByRole('alert'))
    .toHaveText('ネットワークエラーが発生しました。接続を確認してください。');
  await expect(leaveTypeField(this)).toBeDisabled();
  await expect(memoField(this)).toBeDisabled();
  await expect(leaveDialog(this).getByRole('button', { name: '登録' })).toBeDisabled();
});

// 期間結果に対象休暇が含まれ、勤務のみの日が除外されていることを確認する。
Then('休暇一覧に登録済み休暇が含まれ勤務日は含まれない', async function (this: World) {
  const response = this.context.uc004LeaveListResponse;
  expect(response.status()).toBe(200);
  const records = await response.json();
  expect(records).toEqual(expect.arrayContaining([expect.objectContaining({
    date: getLeaveDate(this),
    leaveType: 'Sick Leave',
    memo: '更新前',
  })]));
  expect(records.map((record: { date: string }) => record.date)).not.toContain(getWorkDate(this));
});

// 開始日だけの指定が400とERR03008で拒否されることを確認する。
Then('休暇一覧の期間指定エラー ERR03008 が返る', async function (this: World) {
  const response = this.context.uc004LeaveListResponse;
  expect(response.status()).toBe(400);
  expect(await response.json()).toMatchObject({ error: 'ERR03008' });
});

Given('従業員が登録済み休暇を確認している', async function (this: World) {
  const date = getLeaveDate(this);
  const response = await request(this).get(`${API_URL}/${date}`);
  expect(response.status()).toBe(200);
  const leave = await response.json();
  await selectDate(this, date);
  await expect(this.page.getByText(`${date} の休暇`)).toBeVisible();
  await expect(this.page.getByText(leave.leaveType)).toBeVisible();
});

When('従業員が削除ボタンをクリックする', async function (this: World) {
  await this.page.getByRole('button', { name: '削除' }).click();
  await expect(this.page.getByRole('dialog', { name: '削除確認' })).toBeVisible();
});

When('従業員が削除確認ダイアログで確認する', async function (this: World) {
  await this.page.getByRole('button', { name: '削除を確認', exact: true }).click();
});

When('従業員が削除確認ダイアログでキャンセルする', async function (this: World) {
  await this.page.getByRole('button', { name: 'キャンセル' }).click();
});

When('従業員が存在しない休暇を削除しようとする', async function (this: World) {
  const date = getLeaveDate(this);
  await this.page.getByRole('button', { name: '削除' }).click();
});

Then('休暇レコードが削除される', async function (this: World) {
  const response = await request(this).get(`${API_URL}/${getLeaveDate(this)}`);
  expect(response.status()).toBe(404);
});

Then('削除成功メッセージが表示される', async function (this: World) {
  await expect(this.page.getByRole('status')).toContainText('休暇を削除しました');
});

Then('休暇レコードが削除されない', async function (this: World) {
  const response = await request(this).get(`${API_URL}/${getLeaveDate(this)}`);
  expect(response.status()).toBe(200);
});

Then('既存の休暇が変わらず残っている', async function (this: World) {
  const response = await request(this).get(`${API_URL}/${getLeaveDate(this)}`);
  expect(response.status()).toBe(200);
  const leave = await response.json();
  expect(leave.leaveType).toBe('Sick Leave');
  expect(leave.memo).toBe('更新前');
});

Then('削除対象がないことが表示されたままで成功扱いにならない', async function (this: World) {
  await expect(this.page.getByRole('alert')).toContainText('指定した日付の休暇はありません');
  await expect(this.page.getByText('休暇を削除しました', { exact: true })).toHaveCount(0);
});
