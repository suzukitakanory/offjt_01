# Implementation Plan: 休暇登録

**Branch**: `002-register-leave` | **Date**: 2026-11-09 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/002-register-leave/spec.md`

**Note**: This template is filled in by the `/speckit-plan` command; its definition describes the execution workflow.

## Summary

従業員が、自分の休暇を日付ごとに登録、確認、変更、削除できるようにする。既存の `attendance_records` を再利用し、休暇区分は設計書のステータスコード、メモは `notes` に保存する。画面と API では仕様の表示名（Sick Leave、Personal Leave、Business Trip）を使い、境界でコードへ変換する。

作成と更新は分ける。同じ日付に休暇が既にある作成は拒否し、変更または削除へ誘導する。同じ日付に勤務時間がある場合は、確認なしでは時刻を消さない。従業員が削除を確認したときだけ、新しい行を作らず、その行を休暇にして時刻を空にする。存在しない休暇の更新と削除は成功にしない。従業員 ID はサーバー設定のデモ従業員（既定 `EMP001`）に固定し、要求では受け取らない。認証と承認は追加しない。

## Technical Context

**Language/Version**: Java 21 / Spring Boot 3.2.0、TypeScript 5.2 / React 18.2

**Primary Dependencies**: Spring Web、Spring Data JPA、Spring Validation、PostgreSQL、React、Vite、JUnit 5、Testcontainers、Cucumber、Playwright。新規依存関係は追加しない。

**Storage**: 既存 PostgreSQL の `attendance_records`。新しいテーブルは作らない。ローカル開発用コンテナまたは Testcontainers だけを使う。

**Testing**: JUnit 5、MockMvc、Testcontainers PostgreSQL、フロントエンドの TypeScript ビルドと ESLint、Cucumber + Playwright の `@REQ-UC004`。

**Target Platform**: ブラウザー上の React アプリ、Spring Boot のローカル API、ローカル PostgreSQL。

**Project Type**: フルスタック Web アプリケーション（`offjt_01/attendance-system-workshop-ja-v1.1.1/`）。

**Performance Goals**: 独立した応答時間目標は仕様にない。利用者目標は SC-001（日付選択から保存完了まで1分以内）と SC-007（初回利用者の95%以上が案内なしで登録と確認を完了する）。

**Constraints**:

- 1人の従業員と1つの日付につき勤怠レコードは1件。既存の一意制約を維持する。
- 休暇区分は Sick Leave、Personal Leave、Business Trip のみ。メモは任意で最大500文字。
- 従業員 ID はサーバー設定（既定 `EMP001`）だけが決める。呼び出し側に選ばせない。
- 認証、代理登録、承認、残日数、半日休暇、時間単位休暇、複数日一括登録は対象外。
- 本番データベースへ接続しない。機密情報を入力しない。テストを無効化しない。確認を省略する起動オプションを使わない。
- 1つの PR は400行以内、8ファイル以内、1機能とする。この機能の実装は複数 PR に分ける。

**Scale/Scope**: デモ従業員1人の日次休暇。作成、日付取得、期間取得、更新、削除と、既存カレンダーからの操作に限る。

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **II. 仕様駆動**: PASS。仕様 [spec.md](./spec.md) を先に確定し、この計画は実装前の設計である。
- **I. 責任**: PASS。実装者はマージする差分を説明できなければならない。AI 生成分も担当者の成果物とする。
- **III. 粒度**: PASS。実装は単独で検証できる単位に分け、各 PR は400行・8ファイル以内にする。API と画面は必要なら別 PR にする。
- **IV. 並列**: PASS。この機能は1ブランチ、1担当者とする。同時進行は2タスク、クラウドエージェントは2件まで。
- **VI. レビュー**: PASS。実装者以外のレビューを必須とし、AI レビューの後に人のレビューを行う。読む順序は変更ファイル、削除行、テスト、実装、依存追加、理解確認とする。
- **VII. 禁止事項**: PASS。本番 DB、機密情報の入力、テスト無効化、確認省略の起動オプションを計画に含めない。
- **VIII. 中断**: PASS。同じ修正が3回失敗したら人が引き取る。理解できない差分は破棄する。
- **IX. 記録**: PASS。仕様を実装より先に更新済み。実装 PR には AI の利用内容を記載する。
- **例外**: PASS。個別例外は設けない。

**Gate result**: PASS。Phase 0 に進んでよい。

## Project Structure

### Documentation (this feature)

```text
specs/002-register-leave/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── leave-api.yaml
└── tasks.md             # /speckit-tasks が作成する。このコマンドでは作らない

offjt_01/attendance-system-workshop-ja-v1.1.1/docs/
├── design/domain/sequence/SEQ-REQ-UC004-休暇登録.md
├── design/screen-design.md
├── design/database-table-definitions.md
├── design/openapi.yaml
├── design/message-definition.md
└── managements/dev-status.md
```

### Source Code (repository root)

```text
offjt_01/attendance-system-workshop-ja-v1.1.1/
├── backend/src/main/java/com/attendance/
│   ├── controller/          # 休暇 API を追加
│   ├── service/
│   ├── dto/
│   └── entity/AttendanceRecord.java
├── backend/src/test/java/com/attendance/
├── frontend/src/
│   ├── components/LeaveForm.tsx
│   ├── pages/CalendarPage.tsx
│   ├── services/
│   └── types/
└── e2e/features/            # @REQ-UC004 を追加
```

**Structure Decision**: 実装先は既存の勤怠ワークショップである。仕様と計画はワークスペース直下の `specs/002-register-leave/` に置く。バックエンドは既存の controller / service / dto / entity 構成を拡張し、フロントエンドは既存の `LeaveForm` とカレンダーから接続する。新しいトップレベルプロジェクトは作らない。

## Complexity Tracking

憲章違反はない。この節に正当化は不要。

## Phase 0: Outline & Research

技術スタック、保存先、テスト手段は既存の実装と設計書から確定した。未解決の `NEEDS CLARIFICATION` はない。判断は [research.md](./research.md) に記録した。

## Phase 1: Design & Contracts

- [data-model.md](./data-model.md): 休暇を既存の日次勤怠として表す項目、コード変換、制約、状態遷移。
- [contracts/leave-api.yaml](./contracts/leave-api.yaml): 作成、一覧、日付取得、更新、削除と、400 / 404 / 409 の契約。エラー区分はメッセージ ID。
- [quickstart.md](./quickstart.md): ローカル起動、自動検証、受け入れ確認。
- ワークショップのローレベル設計（実装コードは含まない）:
  - `offjt_01/attendance-system-workshop-ja-v1.1.1/docs/design/domain/sequence/SEQ-REQ-UC004-休暇登録.md`
  - `offjt_01/attendance-system-workshop-ja-v1.1.1/docs/design/screen-design.md` の SCR-003
  - `offjt_01/attendance-system-workshop-ja-v1.1.1/docs/design/database-table-definitions.md` の REQ-UC004
  - `offjt_01/attendance-system-workshop-ja-v1.1.1/docs/design/openapi.yaml` の `/api/leaves`
  - `offjt_01/attendance-system-workshop-ja-v1.1.1/docs/design/message-definition.md` の REQ-UC004
  - `offjt_01/attendance-system-workshop-ja-v1.1.1/docs/managements/dev-status.md` の REQ-UC004 をローレベル設計済へ更新

実装に入る前に、これらの設計書を人がレビューする。レビュー承認前にアプリケーションコードは変更しない。

### Constitution Check (after Phase 1)

- **機能範囲**: PASS。デモ従業員1人と休暇の記録管理に限定し、認証と承認を足していない。
- **複雑性**: PASS。新テーブル、新ライブラリ、新しい実行基盤はない。既存の一意制約とステータスコードを使う。
- **整合性**: PASS。同日の休暇の追加作成は拒否する。勤務日は確認後だけ同じ行を休暇にし、確認なしでは時刻を消さない。不存在の更新・削除は404にする。
- **検証**: PASS。既存の単体、API、DB、画面検証を使い、テスト無効化はしない。
- **PR 上限**: PASS。設計成果物は実装 PR に混ぜない。実装の分割は `/speckit-tasks` で、各単位が単独テスト可能になるように書く。

**Post-design gate result**: PASS。未解決の確認事項はなく、Phase 2 のタスク分解へ進める。
