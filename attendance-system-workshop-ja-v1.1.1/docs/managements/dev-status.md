# Development Status

このドキュメントは、プロジェクトの開発状況を要件IDごとに管理します。

## マイクロ要件共通

### 要件定義

要件定義書（REQ-000）: **作成済**

要件定義書（REQ-000）へ反映済みの要件ID一覧は以下：

- UC001-UC004

### ハイレベル設計

作成済みドキュメント:

- `docs/design/actors.md` - アクター一覧
- `docs/design/deployment-diagram.md` - デプロイメント図
- `docs/design/screen-design.md` - 画面設計書（ハイレベル部分）
- `docs/design/database-table-definitions.md` - テーブル定義書（ハイレベル部分）
- `docs/design/openapi.yaml` - API設計書（ハイレベル部分）
- `docs/design/message-definition.md` - メッセージ定義書（構造のみ）

ハイレベル設計ドキュメントへ反映済みの要件ID：

- UC001-UC004（全要件のハイレベル設計完了）

### コードスケルトン生成

**状態**: 完了（2025-11-18）

生成済みコードスケルトン:

**バックエンド** (`backend/`):
- プロジェクト設定: `pom.xml`, `compose.yaml`, `application.yml`
- データベース初期化: `init-db/01-schema.sql`, `init-db/02-master-data.sql`
- アプリケーションコード:
  - `AttendanceSystemApplication.java` - メインアプリケーション
  - `controller/AttendanceController.java` - REST APIエンドポイント
  - `service/AttendanceService.java` - ビジネスロジック層
  - `repository/AttendanceRecordRepository.java` - データアクセス層
  - `entity/AttendanceRecord.java` - JPAエンティティ
  - `dto/AttendanceRequest.java`, `dto/AttendanceResponse.java` - DTO
- テストコード:
  - `AttendanceSystemApplicationTests.java` - アプリケーション起動テスト
  - `controller/AttendanceControllerTest.java` - コントローラーテスト
  - `service/AttendanceServiceTest.java` - サービステスト
  - `repository/AttendanceRecordRepositoryTest.java` - リポジトリテスト
- ドキュメント: `backend/README.md`

**フロントエンド** (`frontend/`):
- プロジェクト設定: `package.json`, `vite.config.ts`, `tsconfig.json`, `.eslintrc.cjs`
- アプリケーションコード:
  - `main.tsx` - エントリーポイント
  - `App.tsx` - ルートコンポーネント
  - `pages/CalendarPage.tsx` - メイン画面
  - `components/Calendar.tsx` - カレンダーコンポーネント
  - `components/WorkTimeForm.tsx` - 勤務時間入力フォーム
  - `components/LeaveForm.tsx` - 休暇登録フォーム
  - `services/attendanceService.ts` - API通信サービス
  - `types/attendance.ts` - 型定義
- ドキュメント: `frontend/README.md`

**ビルド・テスト確認済み**:
- バックエンド: `mvn clean compile` ✓, `mvn test` ✓ (全9テスト成功)
- フロントエンド: `pnpm install` ✓, `pnpm build` ✓

コードスケルトンへ反映済みの要件ID：

- UC001-UC004（全要件のスケルトンコード生成完了）

## 各マイクロ要件

以下は `docs/requirements/` および対応する E2E シナリオに基づく最新のユースケース管理テーブルです。

| 要件ID    | 名前           | 状態   | 対応E2Eシナリオ                               | 他マイクロ要件の依存関係 |
| --------- | -------------- | ------ | --------------------------------------------- | ------------------------ |
| REQ-UC001 | システム起動   | 完了   | e2e/features/REQ-UC001-システム起動.feature   | -                        |
| REQ-UC002 | カレンダー操作 | 完了   | e2e/features/REQ-UC002-カレンダー操作.feature | REQ-UC001                |
| REQ-UC003 | 勤務時間入力   | 完了   | e2e/features/REQ-UC003-勤務時間入力.feature   | REQ-UC001, REQ-UC002     |
| REQ-UC004 | 休暇登録       | 完了   | e2e/features/REQ-UC004-休暇登録.feature       | REQ-UC001, REQ-UC002     |

### 状態定義

#### REQ-XXX: 各マイクロ要件

- **未着手**: マイクロ要件仕様書および E2E テストシナリオが生成・更新されている状態
- **ローレベル設計済**: ローレベル設計ドキュメントが生成・更新されている状態
- **コードスケルトン済**: コードスケルトンが生成・更新されている状態
- **実装・試験済**: コード実装およびテストコードが生成・更新され、単体テストおよび E2E テストが完了している状態
- **完了**: マージ済みですべての関連ドキュメントとコードが最終確認されている状態

```mermaid
stateDiagram-v2
    [*] --> 未着手
    未着手 --> ローレベル設計済 : 2-1 ローレベル設計ドキュメント生成
    ローレベル設計済 --> 実装・試験済 : 2-2 コード実装・テスト完了
    実装・試験済 --> 完了 : 3-1 最終確認・マージ完了
    完了 --> [*]
    note left of 未着手: 1-3 対応するマイクロ要件とE2Eシナリオが存在すること
```

---

### REQ-UC003: 勤務時間入力

#### ローレベル設計（2026-11-09）

設計工程の状態: **ローレベル設計済**。当時は実装前の人間レビューを要する状態だった。

| 成果物 | パス | 更新 |
| ------ | ---- | ---- |
| シーケンス図 | `docs/design/domain/sequence/SEQ-REQ-UC003-勤務時間入力.md` | 新規作成 |
| 画面設計書 | `docs/design/screen-design.md` | SCR-002 のローレベル設計を追記 |
| テーブル定義書 | `docs/design/database-table-definitions.md` | `attendance_records` の REQ-UC003 詳細と制約を追記 |
| API設計書 | `docs/design/openapi.yaml` | 勤務時間保存・特定日取得の契約を追記 |
| メッセージ定義書 | `docs/design/message-definition.md` | SUCCESS02001、ERR02001、ERR03001〜ERR03005 を追加 |

新しいテーブルは追加していない。`status` の長さはテーブル定義書が VARCHAR(20)、SQL / JPA エンティティが VARCHAR(50) だが、UC003 の `WORK` は双方に収まるため変更しないことを確認した。

#### 実装・試験と最終確認（2026-11-11）

状態: **完了**。実装と単体・E2E 試験を完了し、最終確認後に関連 PR をマージした。PR #26 はユーザー報告によると、実装担当者以外によるレビューで問題がないことを確認してからマージした。

| 工程 | 確認内容 |
| ---- | -------- |
| バックエンド | `mvn test` — 39 テスト成功 |
| フロントエンド | `pnpm lint`、`pnpm build` 成功 |
| E2E | `pnpm test --tags @REQ-UC003` — 6 シナリオ・48 ステップ成功 |
| 最終確認・マージ | [PR #21](https://github.com/suzukitakanory/offjt_01/pull/21)（2026-11-10）と [PR #26](https://github.com/suzukitakanory/offjt_01/pull/26)（2026-11-11）はマージ済み。PR #26 はユーザー報告によると、実装担当者以外のレビューで問題がないことを確認後にマージ |

主な変更対象は `backend/src/main/java/com/attendance/exception/GlobalExceptionHandler.java`、`frontend/src/components/WorkTimeForm.tsx`、`e2e/features/step_definitions/req-uc003-work-time-entry.steps.ts`、`e2e/features/REQ-UC003-勤務時間入力.feature`。一覧の日付範囲、入力検証、保存・再取得もバックエンドテストと E2E で確認した。

---

### REQ-UC004: 休暇登録

状態: **完了**（2026-11-13）。ローレベル設計は2026-11-09に完了。

| 成果物 | パス | 更新 |
| ------ | ---- | ---- |
| シーケンス図 | `docs/design/domain/sequence/SEQ-REQ-UC004-休暇登録.md` | 新規作成 |
| 画面設計書 | `docs/design/screen-design.md` | SCR-003 のローレベル設計を追記 |
| テーブル定義書 | `docs/design/database-table-definitions.md` | `attendance_records` の REQ-UC004 詳細と SQL を追記 |
| API設計書 | `docs/design/openapi.yaml` | `/api/leaves` のスキーマ、例、エラーコードを追記 |
| メッセージ定義書 | `docs/design/message-definition.md` | SUCCESS02002〜02004、ERR02002、ERR02003、ERR03004〜03008 を追加 |

新しいテーブルは追加していない。`backend/init-db/01-schema.sql` のステータスと時刻の CHECK 制約はテーブル定義書に合わせて実装済み。

#### 実装・試験（2026-11-13）

| 工程 | 確認内容 |
| ---- | -------- |
| バックエンド | `mvn test` — 84テスト成功 |
| フロントエンド | `pnpm --ignore-workspace run build`、`pnpm --ignore-workspace run lint` 成功 |
| E2E | `@REQ-UC004` — 20シナリオ・155ステップ成功 |

---

**最終更新日**: 2026-11-13
