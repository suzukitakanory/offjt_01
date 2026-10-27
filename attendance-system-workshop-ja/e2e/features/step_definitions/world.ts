import { APIResponse, Page, Browser } from '@playwright/test';
import { setWorldConstructor, World as CucumberWorld } from '@cucumber/cucumber';

interface WorldContext {
  selectedDate?: string;
  previousMonthTitle?: string;
  healthCheckResponse?: APIResponse;
  attendanceResponse?: {
    status: number;
    body: unknown;
  };
  saveRequestCount?: number;
}

/**
 * Cucumberワールドインターフェース
 * 
 * テストステップ間でコンテキスト情報を共有するために使用します。
 */
export interface World extends CucumberWorld {
  page: Page;
  browser?: Browser;
  context: WorldContext;
}

/**
 * カスタムワールドコンストラクター
 */
class CustomWorld extends CucumberWorld implements World {
  page!: Page;
  browser?: Browser;
  context: WorldContext = {};
}

setWorldConstructor(CustomWorld);
