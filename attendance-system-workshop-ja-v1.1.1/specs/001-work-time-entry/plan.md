# 実装計画: 勤務時間入力（REQ-UC003）

**Branch**: `001-work-time-entry` | **Date**: 2026-11-06 | **Spec**: [spec.md](./spec.md)

**Input**: [REQ-UC003-勤務時間入力.md](../../attendance-system-workshop-ja-v1.1.1/docs/requirements/REQ-UC003-勤務時間入力.md) と更新済みローレベル設計。設計書と計画成果物の契約・検証条件を再照合した。

## Summary

従業員が選んだ日付の出勤・退勤を検証し、`status=WORK` として従業員・日付ごとに 1 件の勤怠を保存・再取得できるようにする。既存の一覧 API は任意の日付範囲条件を維持する。

更新済みの画面、API、メッセージ、DB、シーケンス設計を現行の UC003 成果物と再照合した。主な同期点は保存リクエストの必須 `status=WORK`、一覧取得の任意 `startDate` / `endDate`、従業員 ID・日付の形式エラーである。技術方針は既存の React + Spring Boot + PostgreSQL を拡張し、新規サービスを追加しない。

## Technical Context

**Language/Version**: Java 21、TypeScript 5.2

**Primary Dependencies**: Spring Boot 3.2、Spring Data JPA、Maven 3.8 以上、React 18.2、Vite 5、Playwright、Cucumber

**Storage**: PostgreSQL。テーブル `attendance_records`。開発時は Docker Compose

**Testing**: JUnit 5、Testcontainers、JaCoCo レポート生成、Playwright + Cucumber。フロントエンドは ESLint

**Target Platform**: 最新の Chrome / Firefox / Safari / Edge。開発サーバーはフロントエンド 5173、API 8080

**Project Type**: web-service（既存の frontend + backend + e2e）

**Performance Goals**: REQ-000 の「実用的な時間」を維持する。UC003 固有の数値目標・負荷試験基準は定義されていないため、新たな SLA や負荷試験を本計画の受け入れゲートにしない

**Constraints**: UC003 画面の保存リクエストは `status=WORK` を含み、保存サービスは WORK として登録・更新する。出勤時刻は退勤時刻より前。時刻は HH:MM。未来日付可。従業員と日付で 1 件。日跨ぎ不可。一覧 API は任意の開始日・終了日を含む絞り込みを維持する。認証は対象外で画面の従業員 ID は `EMP001`。PR は 400 行・8 ファイル以内、同時進行は 2 件まで

**Scale/Scope**: 画面は 1 ダイアログ、API は保存・特定日取得・一覧取得、永続化エンティティは共有の `attendance_records`。休暇登録の画面・操作とカレンダーの勤怠バッジは対象外

## Constitution Check

*GATE: 設計前に評価し、Phase 1 のあと再評価する。憲章は `.specify/memory/constitution.md` 1.0.0。*

| ゲート | 設計前 | 設計後 |
| --- | --- | --- |
| 仕様駆動。実装前に要件と受入条件がある | 合格。REQ-UC003 と E2E 文言を spec に写した。実装はしない | 合格。更新対象の設計書と、書き戻す契約・データ・確認手順が揃った |
| 小さくテスト可能なタスク。PR は 400 行・8 ファイル以内 | 合格条件として記録。本成果は計画文書であり実装 PR に混ぜない | 合格。設計書の更新と実装は別作業にする |
| 1 タスク 1 ブランチ 1 担当。同時進行は 2 件まで | 合格条件として記録 | 合格。設計書更新を実装と並列にしない |
| AI レビューと実装者以外の人レビュー。自己承認禁止。PR 本文に AI 利用を記録 | 計画段階では該当なし | 実装 PR ごとに AI レビュー、人レビュー、AI 利用記録を行う |
| 機密情報を増やさない。テストを緩めて通さない | 合格。接続情報は転記していない | 合格 |
| 説明できない構成を増やさない | 合格。既存 3 モジュールと既存設計置き場を使う | 合格。設計書の新設はシーケンス 1 件だけ |

正当化が必要な違反はない。未解消の確認事項はない。

## 設計書の再確認結果

| # | 設計書 | 結果 | UC003 との整合 |
| --- | --- | --- | --- |
| 1 | [screen-design.md](../../attendance-system-workshop-ja-v1.1.1/docs/design/screen-design.md) | 反映済み | SCR-002、初期表示、HH:MM 検証、保存・キャンセル、API とメッセージを定義 |
| 2 | [openapi.yaml](../../attendance-system-workshop-ja-v1.1.1/docs/design/openapi.yaml) | 反映済み・今回補正 | 保存リクエストに `status=WORK` を含む。特定日取得と一覧取得、任意の開始日・終了日条件を定義。形式エラーの例を ERR03004 / ERR03005 に統一 |
| 3 | [message-definition.md](../../attendance-system-workshop-ja-v1.1.1/docs/design/message-definition.md) | 反映済み・今回補正 | SUCCESS02001、ERR02001、ERR03001〜ERR03005 を定義 |
| 4 | [database-table-definitions.md](../../attendance-system-workshop-ja-v1.1.1/docs/design/database-table-definitions.md) | 反映済み | 一意キー、時刻チェック `>`、WORK の時刻必須、未来日許可を定義 |
| 5 | [SEQ-REQ-UC003-勤務時間入力.md](../../attendance-system-workshop-ja-v1.1.1/docs/design/domain/sequence/SEQ-REQ-UC003-勤務時間入力.md) | 反映済み | 保存成功、検証失敗、特定日再取得を定義 |

変更しない設計書:

| 設計書 | 理由 |
| --- | --- |
| [actors.md](../../attendance-system-workshop-ja-v1.1.1/docs/design/actors.md) | ACT-001 が勤務時間入力と関連画面を既に持つ |
| [deployment-diagram.md](../../attendance-system-workshop-ja-v1.1.1/docs/design/deployment-diagram.md) | ブラウザ、React、Spring Boot、PostgreSQL で本機能を配置できる |

設計書ではないため、この一覧に入れないもの:

- `docs/managements/dev-status.md`。上記 5 件を更新したあと、REQ-UC003 を「ローレベル設計済」にする管理作業
- `backend/init-db/01-schema.sql`。チェック制約の実装は設計書更新のあと
- REQ-UC003 と E2E。仕様として既にある

データベースの `status` は VARCHAR(50) で、CHECK 制約は WORK、PAID_LEAVE、SICK_LEAVE、UNPAID_LEAVE、PERSONAL_LEAVE、BUSINESS_TRIP を許可する。UC003 の画面は `WORK` を送信し、保存サービスは `WORK` として保存する。

JaCoCo はテスト実行時にレポートを生成するが、`pom.xml` にカバレッジの閾値チェックはない。UC003 固有の数値性能目標・負荷試験も定義・実装されていない。

## Project Structure

### Documentation (this feature)

```text
specs/001-work-time-entry/
├── plan.md
├── spec.md
├── tasks.md
├── research.md
├── data-model.md
├── quickstart.md
└── contracts/
    └── attendance-uc003.openapi.yaml
```

設計書の更新先（反映済み）:

```text
attendance-system-workshop-ja-v1.1.1/docs/design/
├── screen-design.md
├── openapi.yaml
├── message-definition.md
├── database-table-definitions.md
└── domain/sequence/SEQ-REQ-UC003-勤務時間入力.md
```

### コード配置（リポジトリ内）

```text
attendance-system-workshop-ja-v1.1.1/
├── backend/
│   ├── init-db/01-schema.sql
│   └── src/main/java/com/attendance/
│       ├── controller/AttendanceController.java
│       ├── service/AttendanceService.java
│       ├── repository/AttendanceRecordRepository.java
│       ├── entity/AttendanceRecord.java
│       └── dto/
├── frontend/src/
│   ├── pages/CalendarPage.tsx
│   ├── components/WorkTimeForm.tsx
│   ├── services/attendanceService.ts
│   └── types/attendance.ts
└── e2e/features/REQ-UC003-勤務時間入力.feature
```

**Structure Decision**: ワークショップ既存の frontend / backend / e2e と `docs/design/` を使う。リポジトリルートに新しい `src/` は作らない。

## 実装完了状況

- T001〜T015 は実装・検証完了。完了チェックは [tasks.md](./tasks.md) を参照する
- JaCoCo はレポートを生成するが、閾値チェックは設定されていない
- 性能負荷試験は実装範囲に含まれない
- T016 は実装タスクではなく、実装 PR ごとに適用する憲章上のレビューゲートとして残す

## Complexity Tracking

違反はない。

コードと成果物の照合で確認した詳細と既知の実装制約は [research.md](./research.md) を参照する。
