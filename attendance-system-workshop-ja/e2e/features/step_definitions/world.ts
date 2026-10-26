import { Page, Browser } from '@playwright/test';
import { setWorldConstructor, World as CucumberWorld } from '@cucumber/cucumber';

/**
 * Cucumberワールドインターフェース
 * 
 * テストステップ間でコンテキスト情報を共有するために使用します。
 */
export interface World extends CucumberWorld {
  page: Page;
  browser?: Browser;
  context: {
    [key: string]: any;
    selectedDate?: string;
    previousMonthTitle?: string;
    healthCheckResponse?: any;
  };
}

/**
 * カスタムワールドコンストラクター
 */
class CustomWorld extends CucumberWorld implements World {
  page!: Page;
  browser?: Browser;
  context: {
    [key: string]: any;
    selectedDate?: string;
    previousMonthTitle?: string;
    healthCheckResponse?: any;
  } = {};
}

setWorldConstructor(CustomWorld);
