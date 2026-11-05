/**
 * Cucumber + Playwright E2Eテスト設定
 * 
 * このファイルはCucumberのテスト実行設定を定義します。
 */

module.exports = {
  default: {
    // featureファイルのパス
    paths: ['features/**/*.feature'],
    
    // ステップ定義のパス（TypeScriptを優先）
    require: ['features/step_definitions/**/*.ts'],
    
    // TypeScriptサポート
    requireModule: ['ts-node/register/transpile-only'],
    
    // フォーマッター設定
    format: [
      'progress-bar',
      'html:reports/html/cucumber-report.html',
      'json:reports/raw/cucumber-report.json',
      'junit:reports/junit.xml'
    ],
    
    // 並列実行の設定
    parallel: 1,
    
    // タイムアウト設定（60秒）
    timeout: 60000,
  }
};
