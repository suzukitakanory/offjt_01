import { Given, When, Then } from '@cucumber/cucumber';
import { expect } from '@playwright/test';
import { World } from './world';

/**
 * E2Eテストステップ定義: REQ-UC002 - カレンダー操作
 * 
 * 関連要件: REQ-UC002
 * 関連画面: SCR-001 (メイン画面)
 * 関連シーケンス図:
 * - SEQ-UC002-001: カレンダー日付選択
 * - SEQ-UC002-002: カレンダー月切り替え
 * - SEQ-UC002-003: カレンダー異なる日付選択
 */

const formatDateKey = (date: Date): string => {
  const year = date.getFullYear();
  const month = `${date.getMonth() + 1}`.padStart(2, '0');
  const day = `${date.getDate()}`.padStart(2, '0');
  return `${year}-${month}-${day}`;
};

const extractYearMonth = (title: string | null): { year: number; month: number } | null => {
  if (!title) {
    return null;
  }
  const match = title.match(/(\d{4})年\s*(\d{1,2})月/);
  if (!match) {
    return null;
  }
  return {
    year: Number(match[1]),
    month: Number(match[2]),
  };
};

const navigateToTargetMonth = async (world: World, date: string) => {
  const { page } = world;
  const targetDate = new Date(date);
  const currentTitle = await page.locator('.calendar-title').textContent();
  const current = extractYearMonth(currentTitle);

  if (!current) {
    return;
  }

  const targetYear = targetDate.getFullYear();
  const targetMonth = targetDate.getMonth() + 1;
  const monthDiff = (targetYear - current.year) * 12 + (targetMonth - current.month);

  if (monthDiff === 0) {
    return;
  }

  const button = monthDiff > 0 ? page.locator('.calendar-nav-button').last() : page.locator('.calendar-nav-button').first();
  for (let i = 0; i < Math.abs(monthDiff); i += 1) {
    await button.click();
    await page.waitForTimeout(100);
  }
};

const ensureDateCellVisible = async (world: World, date: string) => {
  const { page } = world;
  let dateCell = page.locator(`.calendar-day[data-date="${date}"]`);
  const isVisible = await dateCell.isVisible().catch(() => false);

  if (isVisible) {
    return dateCell;
  }

  await navigateToTargetMonth(world, date);
  dateCell = page.locator(`.calendar-day[data-date="${date}"]`);
  return dateCell;
};

// ==============================================
// 前提条件 (Given)
// ==============================================

Given('カレンダーが表示されている', async function (this: World) {
  // カレンダーコンポーネントが表示されていることを確認
  const calendar = this.page.locator('.calendar');
  await expect(calendar).toBeVisible();
  
  // カレンダーグリッドが表示されていることを確認
  const calendarGrid = this.page.locator('.calendar-grid');
  await expect(calendarGrid).toBeVisible();
  
  // 日付セルが表示されていることを確認（最低でも28日分）
  const dateCells = this.page.locator('.calendar-day');
  const count = await dateCells.count();
  expect(count).toBeGreaterThanOrEqual(28);
});

Given('日付 {string} が選択されている', async function (this: World, date: string) {
  const dateCell = await ensureDateCellVisible(this, date);
  await dateCell.click();
  this.context.selectedDate = date;
  await expect(dateCell).toHaveClass(/selected/);
});

// ==============================================
// 操作 (When)
// ==============================================

When('従業員が任意の日付を選択する', async function (this: World) {
  // カレンダーから当月の最初の有効な日付を選択
  const currentMonthDays = this.page.locator('.calendar-day:not(.other-month)');
  const firstDay = currentMonthDays.first();
  
  // クリック前の日付を記録
  const dateAttr = await firstDay.getAttribute('data-date');
  this.context.selectedDate = dateAttr || undefined;
  
  // 日付セルをクリック
  await firstDay.click();
});

When('従業員が次月ボタンをクリックする', async function (this: World) {
  // 現在の年月を記録
  const currentTitle = await this.page.locator('.calendar-title').textContent();
  this.context.previousMonthTitle = currentTitle || undefined;
  
  // 次月ボタンをクリック
  const nextButton = this.page.locator('.calendar-nav-button').last();
  await nextButton.click();
  
  // カレンダーが更新されるまで少し待機
  await this.page.waitForTimeout(100);
});

When('従業員が日付 {string} を選択する', async function (this: World, date: string) {
  const dateCell = await ensureDateCellVisible(this, date);
  await dateCell.click();
  this.context.selectedDate = date;
});

// ==============================================
// 検証 (Then)
// ==============================================

Then('選択した日付がハイライトされる', async function (this: World) {
  // 選択された日付のセルがハイライト表示されていることを確認
  const selectedDate = this.context.selectedDate;
  expect(selectedDate).toBeDefined();
  
  const dateCell = this.page.locator(`.calendar-day[data-date="${selectedDate}"]`);
  await expect(dateCell).toHaveClass(/selected/);
});

Then('選択日付に応じた操作オプションが表示される', async function (this: World) {
  // 操作オプションセクションが表示されていることを確認
  const actionOptions = this.page.locator('.action-options');
  await expect(actionOptions).toBeVisible();
  
  // 勤務時間入力ボタンが表示されていることを確認
  const workTimeButton = this.page.locator('[data-testid="btn-work-time"]');
  await expect(workTimeButton).toBeVisible();
  
  // 休暇登録ボタンが表示されていることを確認
  const leaveButton = this.page.locator('[data-testid="btn-leave"]');
  await expect(leaveButton).toBeVisible();
});

Then('カレンダーが次月に切り替わる', async function (this: World) {
  // 現在の年月表示を取得
  const currentTitle = await this.page.locator('.calendar-title').textContent();
  const previousTitle = this.context.previousMonthTitle;
  
  // 年月が変わっていることを確認
  expect(currentTitle).not.toBe(previousTitle);
  
  // 年月が1ヶ月進んでいることを確認
  const prevYear = parseInt(previousTitle!.match(/(\d{4})年/)![1]);
  const prevMonth = parseInt(previousTitle!.match(/(\d{1,2})月/)![1]);
  const currYear = parseInt(currentTitle!.match(/(\d{4})年/)![1]);
  const currMonth = parseInt(currentTitle!.match(/(\d{1,2})月/)![1]);
  
  const expectedMonth = prevMonth === 12 ? 1 : prevMonth + 1;
  const expectedYear = prevMonth === 12 ? prevYear + 1 : prevYear;
  
  expect(currYear).toBe(expectedYear);
  expect(currMonth).toBe(expectedMonth);
});

Then('次月の日付が表示される', async function (this: World) {
  // カレンダーグリッドに日付セルが表示されていることを確認
  const dateCells = this.page.locator('.calendar-day');
  const count = await dateCells.count();
  expect(count).toBeGreaterThanOrEqual(28);
  
  // 少なくとも1つの当月の日付セルが存在することを確認
  const currentMonthDays = this.page.locator('.calendar-day:not(.other-month)');
  const currentMonthCount = await currentMonthDays.count();
  expect(currentMonthCount).toBeGreaterThan(0);
});

Then('日付 {string} がハイライトされる', async function (this: World, date: string) {
  // 指定された日付のセルがハイライト表示されていることを確認
  const dateCell = this.page.locator(`.calendar-day[data-date="${date}"]`);
  await expect(dateCell).toHaveClass(/selected/);
});

Then('以前選択していた日付 {string} のハイライトが解除される', async function (this: World, date: string) {
  // 指定された日付のセルがハイライト解除されていることを確認
  const dateCell = this.page.locator(`.calendar-day[data-date="${date}"]`);
  await expect(dateCell).not.toHaveClass(/selected/);
});

Then('操作オプションが日付 {string} に対応した内容に更新される', async function (this: World, date: string) {
  // 操作オプションが表示されていることを確認
  const actionOptions = this.page.locator('.action-options');
  await expect(actionOptions).toBeVisible();
  
  // 選択日が表示されていることを確認
  const dateText = new Date(date);
  const expectedText = `${dateText.getFullYear()}年${dateText.getMonth() + 1}月${dateText.getDate()}日`;
  await expect(actionOptions).toContainText(expectedText);
});

Then('今日の日付が特別なマーキングで表示される', async function (this: World) {
  const todayStr = formatDateKey(new Date());
  const todayCell = this.page.locator(`.calendar-day[data-date="${todayStr}"]`);
  const isVisible = await todayCell.isVisible().catch(() => false);

  if (isVisible) {
    await expect(todayCell).toHaveClass(/today/);
  }
});

Then('今日の日付を簡単に識別できる', async function (this: World) {
  // 今日の日付を取得
  const todayStr = formatDateKey(new Date());
  const todayCell = this.page.locator(`.calendar-day[data-date="${todayStr}"]`);
  const isVisible = await todayCell.isVisible().catch(() => false);
  
  if (isVisible) {
    await expect(todayCell).toHaveClass(/today/);
    const backgroundColor = await todayCell.evaluate((el) => {
      const doc = el.ownerDocument;
      if (!doc || !doc.defaultView) return '';
      return doc.defaultView.getComputedStyle(el).backgroundColor;
    });
    
    // 背景色が設定されていることを確認（デフォルトのwhiteでないこと）
    expect(backgroundColor).not.toBe('rgba(0, 0, 0, 0)');
    expect(backgroundColor).not.toBe('rgb(255, 255, 255)');
  }
});
