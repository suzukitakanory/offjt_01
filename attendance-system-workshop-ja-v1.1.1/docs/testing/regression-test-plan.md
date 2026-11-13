# 回帰テスト計画

## 目的

UC003・UC004などの追加・改修後に、それ以前から利用できるUC001・UC002の動作が壊れていないことを確認する。

## 対象と確認項目

| ID | 要件 | 回帰確認項目 | 主な自動テスト |
| --- | --- | --- | --- |
| RT-001 | UC001 システム起動 | メイン画面とカレンダーが表示され、勤怠操作を開始できる。ヘルスチェックが HTTP 200 と `UP` を返す | `e2e/features/REQ-UC001-システム起動.feature`、`backend/src/test/java/com/attendance/actuator/HealthCheckTest.java` |
| RT-002 | UC002 カレンダー操作 | 日付選択とハイライト、選択日に応じた操作項目、月切替、今日の日付表示が正常に動作する | `e2e/features/REQ-UC002-カレンダー操作.feature` |

UC003・UC004の機能確認は、それぞれの機能テストとして実行する。これらの機能がリリースされ、その後の変更で既存機能となった場合は、必要に応じて回帰対象へ追加する。また、変更がUC001・UC002へ影響しないと判断できる場合でも、その判断を変更記録に残す。

## 実行手順

### 1. バックエンドのヘルスチェックテスト

Docker が利用可能な環境で実行する。テスト用 PostgreSQL は Testcontainers が起動する。

```bash
cd backend
mvn -Dtest=HealthCheckTest test
```

### 2. UC001・UC002 のE2Eテスト

テスト用の PostgreSQL、バックエンド（`http://localhost:8080`）、フロントエンド（`http://localhost:5173`）を起動してから実行する。

```bash
cd e2e
pnpm test:regression
```

E2E の実行結果は `e2e/reports/` に出力される。ローカルまたは専用のテスト環境でのみ実行し、本番環境には接続しない。

すべてをまとめて実行する場合は、プロジェクトルートから次を実行する。

```bash
bash scripts/run-regression-tests.sh
```

## 合格条件

- バックエンドのヘルスチェックテストが終了コード `0` で完了する。
- UC001・UC002 のE2Eに失敗、未定義ステップ、スキップがない。
- 失敗時は原因を修正し、失敗したテストと影響範囲の回帰テストを再実行する。
