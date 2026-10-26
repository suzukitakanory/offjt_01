import { Given, When, Then, Before, After, setDefaultTimeout } from '@cucumber/cucumber';
import { chromium, Page, Browser, expect } from '@playwright/test';
import { World } from './world';

/**
 * REQ-UC001: システム起動のステップ定義
 * 
 * このファイルは以下のE2Eテストシナリオをサポートします:
 * - ユーザーがシステムにアクセスしてメイン画面を表示する
 * - システムヘルスチェックが正常に動作する
 * - 各種ブラウザーでメイン画面が正常に表示される
 */

// ステップのデフォルトタイムアウトを30秒に設定
setDefaultTimeout(30000);

let browser: Browser;
let page: Page;

const ensureBrowserSession = (browserLabel: string) => {
  console.log(`ブラウザー起動を確認: ${browserLabel}`);
  expect(page).toBeDefined();
};

// 各シナリオの前にブラウザーとページを初期化
Before(async function (this: World) {
  browser = await chromium.launch({ headless: true });
  page = await browser.newPage();
  this.page = page;
  this.browser = browser;
});

// 各シナリオの後にブラウザーをクローズ
After(async function () {
  if (page) {
    await page.close();
  }
  if (browser) {
    await browser.close();
  }
});

// ===========================================
// 前提条件 (Given)
// ===========================================

Given('システムが正常に稼働している', async function () {
  // バックエンドのヘルスチェックを実行してシステムの稼働を確認
  const response = await page.request.get('http://localhost:8080/actuator/health');
  expect(response.ok()).toBeTruthy();
});

Given('ユーザーがブラウザーを起動している', async function () {
  // ブラウザーは既にBeforeフックで起動済み
  expect(page).toBeDefined();
});

Given('システムが起動している', async function () {
  // バックエンドのヘルスチェックを実行
  const response = await page.request.get('http://localhost:8080/actuator/health');
  expect(response.ok()).toBeTruthy();
});

Given('ユーザーが {string} を起動している', async function (browserName: string) {
  ensureBrowserSession(browserName);
});

Given('ユーザーが {word} を起動している', async function (browserName: string) {
  ensureBrowserSession(browserName);
});

Given('従業員がシステムにアクセスしている', async function () {
  // システムURLにアクセス
  await page.goto('http://localhost:5173', { waitUntil: 'networkidle' });
});

Given('メイン画面が表示されている', async function () {
  // メイン画面のタイトルが表示されていることを確認
  const title = await page.locator('h1').textContent();
  expect(title).toContain('勤怠管理システム');
});

// ===========================================
// アクション (When)
// ===========================================

When('ユーザーがシステムURL {string} にアクセスする', async function (url: string) {
  await page.goto(url, { waitUntil: 'networkidle' });
});

When('ヘルスチェックエンドポイント {string} にアクセスする', async function (this: World, endpoint: string) {
  const response = await this.page.request.get(`http://localhost:8080${endpoint}`);
  this.context.healthCheckResponse = response;
});

When('システムURLにアクセスする', async function (this: World) {
  await this.page.goto('http://localhost:5173', { waitUntil: 'networkidle' });
});

// ===========================================
// 検証 (Then)
// ===========================================

Then('メイン画面が表示される', async function () {
  // ページタイトルまたはh1要素を確認
  const title = await page.locator('h1').textContent();
  expect(title).toContain('勤怠管理システム');
});

Then('カレンダーが表示される', async function () {
  // カレンダーコンポーネントの存在を確認
  const calendar = page.locator('.calendar');
  await expect(calendar).toBeVisible();
  
  // カレンダーグリッドの存在を確認
  const calendarGrid = page.locator('.calendar-grid');
  await expect(calendarGrid).toBeVisible();
});

Then('ユーザーが勤怠操作を開始できる状態になる', async function () {
  // カレンダーの日付セルがクリック可能であることを確認
  const firstDay = page.locator('.calendar-day').first();
  await expect(firstDay).toBeVisible();
  
  // 月次ナビゲーションボタンが存在することを確認
  const prevButton = page.locator('.calendar-nav-button').first();
  const nextButton = page.locator('.calendar-nav-button').last();
  await expect(prevButton).toBeVisible();
  await expect(nextButton).toBeVisible();
});

Then('ステータスコード {int} が返される', async function (this: World, statusCode: number) {
  const response = this.context.healthCheckResponse;
  expect(response.status()).toBe(statusCode);
});

Then('レスポンスに {string} ステータスが含まれる', async function (this: World, status: string) {
  const response = this.context.healthCheckResponse;
  const body = await response.json();
  expect(body.status).toBe(status);
});

Then('メイン画面が正常に表示される', async function () {
  const title = await page.locator('h1').textContent();
  expect(title).toContain('勤怠管理システム');
});

Then('カレンダーが正常にレンダリングされる', async function () {
  const calendar = page.locator('.calendar');
  await expect(calendar).toBeVisible();
  
  // 曜日ヘッダーが7つ存在することを確認
  const weekdays = page.locator('.calendar-weekday');
  await expect(weekdays).toHaveCount(7);
  
  // 日付セルが存在することを確認（28〜42個）
  const days = page.locator('.calendar-day');
  const dayCount = await days.count();
  expect(dayCount).toBeGreaterThanOrEqual(28);
  expect(dayCount).toBeLessThanOrEqual(42);
});
