import { Before, Given, Then, When } from '@cucumber/cucumber';
import { expect } from '@playwright/test';
import { World } from './world';

const API_URL = 'http://localhost:8080/api/leaves';
let scenarioIndex = 0;

const formatDate = (date: Date): string =>
  `${date.getFullYear()}-${`${date.getMonth() + 1}`.padStart(2, '0')}-${`${date.getDate()}`.padStart(2, '0')}`;

const getLeaveDate = (world: World): string => {
  const date = world.context.uc004LeaveDate;
  if (typeof date !== 'string') throw new Error('休暇テスト日が設定されていません');
  return date;
};

const openLeaveForm = async (world: World): Promise<void> => {
  const date = getLeaveDate(world);
  const title = await world.page.locator('.calendar-title').innerText();
  const match = title.match(/(\d{4})年\s*(\d{1,2})月/);
  if (!match) throw new Error(`カレンダーの年月を読み取れません: ${title}`);

  const target = new Date(`${date}T00:00:00`);
  const monthDifference =
    (target.getFullYear() - Number(match[1])) * 12 +
    target.getMonth() + 1 - Number(match[2]);
  const navigationButton = monthDifference > 0
    ? world.page.getByRole('button', { name: '次月へ' })
    : world.page.getByRole('button', { name: '前月へ' });
  for (let month = 0; month < Math.abs(monthDifference); month += 1) {
    await navigationButton.click();
  }

  await world.page.locator(`.calendar-day[data-date="${date}"]`).click();
  world.context.selectedDate = date;
  await world.page.getByRole('button', { name: '休暇登録', exact: true }).click();
  await expect(world.page.getByRole('dialog')).toBeVisible();
};

Before({ tags: '@REQ-UC004-US3' }, function (this: World) {
  const date = new Date();
  date.setDate(date.getDate() + 45 + scenarioIndex);
  scenarioIndex += 1;
  this.context.uc004LeaveDate = formatDate(date);
});

Given('従業員のテスト休暇を登録済みにする', async function (this: World) {
  const response = await this.page.request.post(API_URL, {
    data: {
      date: getLeaveDate(this),
      leaveType: 'Sick Leave',
      memo: '更新前',
    },
  });
  expect(response.status()).toBe(201);
});

Given('従業員が登録済み休暇の変更フォームを開く', async function (this: World) {
  await openLeaveForm(this);
});

When('従業員が休暇区分 {string} を選択する', async function (this: World, leaveType: string) {
  await this.page.getByLabel('休暇区分').selectOption({ label: leaveType });
});

When('従業員が休暇メモ {string} を入力する', async function (this: World, memo: string) {
  await this.page.getByLabel('メモ').fill(memo);
});

When('保存前に対象休暇を削除する', async function (this: World) {
  const response = await this.page.request.delete(`${API_URL}/${getLeaveDate(this)}`);
  expect(response.status()).toBe(204);
});

When('従業員が休暇の変更を保存する', async function (this: World) {
  await this.page.getByRole('button', { name: '変更を保存', exact: true }).click();
});

Then('変更後の休暇が再取得できる', async function (this: World) {
  const response = await this.page.request.get(`${API_URL}/${getLeaveDate(this)}`);
  expect(response.status()).toBe(200);
  expect(await response.json()).toMatchObject({
    date: getLeaveDate(this),
    leaveType: 'Personal Leave',
    memo: '私用に変更',
  });
});

Then('休暇変更成功メッセージが表示される', async function (this: World) {
  await expect(this.page.getByRole('dialog')).toHaveCount(0);
  await expect(this.page.getByRole('status')).toContainText('休暇を変更しました');
});

Then('更新対象がないことが表示され成功扱いにならない', async function (this: World) {
  const response = await this.page.request.get(`${API_URL}/${getLeaveDate(this)}`);
  expect(response.status()).toBe(404);
  await expect(this.page.getByRole('alert')).toContainText('指定した日付の休暇はありません');
  await expect(this.page.getByText('休暇を変更しました', { exact: true })).toHaveCount(0);
});