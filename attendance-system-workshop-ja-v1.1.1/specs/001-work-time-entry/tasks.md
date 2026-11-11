# Tasks: 勤務時間入力（REQ-UC003）

**Input**: 設計文書は `attendance-system-workshop-ja-v1.1.1/specs/001-work-time-entry/` の `plan.md`、`spec.md`、`research.md`、`data-model.md`、`contracts/attendance-uc003.openapi.yaml`、`quickstart.md`

**前提**: 既存の Maven / Spring Boot、React / Vite、Playwright / Cucumber の構成を使う。プロジェクト初期化と依存追加は不要。仕様と既存 E2E シナリオに合わせ、実装前にテストを具体化する。

**作業単位**: 憲章の PR 上限（400 行・8 ファイル）を守る。基盤、バックエンド、フロントエンド、E2E の変更が上限を超える場合は、独立して検証可能な PR に分割する。

**作業ゲート**: 作業開始時に憲章の開始チェックを行い、終了時に差分・テスト・lint・未コミット変更を確認する。最大 2 件まで並列に進める。各 PR で AI レビューの後に実装者以外の人レビューを受け、PR 本文に AI 利用を記録する。

## フェーズ1: セットアップ（既存環境の再利用）

既存の `attendance-system-workshop-ja-v1.1.1/backend/pom.xml`、`frontend/package.json`、`e2e/package.json` に必要な依存関係と実行スクリプトがあるため、新しいセットアップタスクはない。

## フェーズ2: 基盤（全ストーリーの前提）

**目的**: データベース制約と共通 API エラー応答を整備し、UC003 の保存・取得で同一の契約を使えるようにする。

- [X] T001 `attendance-system-workshop-ja-v1.1.1/backend/init-db/01-schema.sql` に設計済み制約を反映する。`employee_id` と `attendance_date` の一意性を維持し、`status` は `WORK`、`PAID_LEAVE`、`SICK_LEAVE`、`UNPAID_LEAVE`、`PERSONAL_LEAVE`、`BUSINESS_TRIP` を許可し、時刻制約は `clock_in_time IS NULL OR clock_out_time IS NULL OR clock_out_time > clock_in_time` とする。
- [X] T002 `attendance-system-workshop-ja-v1.1.1/backend/src/main/java/com/attendance/exception/ApiErrorResponse.java` と `attendance-system-workshop-ja-v1.1.1/backend/src/main/java/com/attendance/exception/GlobalExceptionHandler.java` を作成し、エラー応答 `{error, message, timestamp}` と HTTP 400 / 404 の日本語メッセージ対応を実装する。UC003 のコードは ERR03001〜ERR03005、ERR02001 とし、予期しない例外は既存共通定義 ERR99001 にする。

**チェックポイント**: DB 初期化 SQL が新規環境で適用でき、すべての API エラーを OpenAPI の `ErrorResponse` 形式で返せる。

## フェーズ3: ユーザーストーリー1 - 勤務時間を記録する（優先度: P1 / MVP）

**目標**: 従業員が日付と出退勤時刻を入力し、検証後に保存・再取得できる。同じ従業員と日付の組み合わせは常に 1 件にする。

**独立したテスト基準**:

- 正常な `HH:MM` と `status=WORK` の新規保存と同日再保存で、同じ勤怠レコードが更新される。
- 未来日の保存が成功し、出勤時刻・退勤時刻は応答で `HH:MM:SS`（秒 `00`）となる。
- 未入力、形式不正、同刻または出勤が退勤以降の場合は日本語エラーになり、既存レコードを含め DB を変更しない。
- 特定日取得は存在時に 200、未登録時に 404 / ERR02001。従業員 ID / 日付の形式不正は 400 / ERR03004 / ERR03005。従業員別一覧取得は任意の開始日・終了日を含む絞り込みを行い、200 と配列を返す。
- SCR-002 から保存・再取得でき、キャンセルで保存せず、既存 UC001 / UC002 の操作を壊さない。

### テスト（実装より先に追加）

- [X] T003 [P] [US1] `attendance-system-workshop-ja-v1.1.1/backend/src/test/java/com/attendance/service/AttendanceServiceTest.java` に、WORK 新規保存、同日更新、`notes` と `createdAt` の維持、入力検証、ERR03004 / ERR03005、特定日未登録、一覧の日付範囲を含む単体テストを追加し、実装前に失敗を確認する。
- [X] T004 [P] [US1] `attendance-system-workshop-ja-v1.1.1/backend/src/test/java/com/attendance/controller/AttendanceControllerTest.java` に、POST 200 / 400、特定日 GET 200 / 400 / 404、一覧 GET 200 / 400 とエラー応答形式、`startDate` / `endDate` の受け渡しを確認する MockMvc テストを追加し、実装前に失敗を確認する。
- [X] T005 [P] [US1] `attendance-system-workshop-ja-v1.1.1/backend/src/test/java/com/attendance/repository/AttendanceRecordRepositoryTest.java` に、従業員・日付検索、開始日・終了日を含む一覧範囲検索、逆転範囲の空結果、一意性、同刻拒否、NULL 時刻を持つ休暇行の許可を確認する PostgreSQL Testcontainers テストを追加し、実装前に失敗を確認する。

### バックエンドの実装

- [X] T006 [US1] `attendance-system-workshop-ja-v1.1.1/backend/src/main/java/com/attendance/dto/AttendanceRequest.java` を UC003 の入力契約に合わせる。`employeeId` は必須かつ `^EMP[0-9]{3}$`、`attendanceDate` は必須の `YYYY-MM-DD`、出勤・退勤は必須の `HH:MM`、`status` は必須かつ UC003 では `WORK` とする。UC004 用の共有 DTO であるため `notes` と休暇ステータスを排除しない。
- [X] T007 [P] [US1] `attendance-system-workshop-ja-v1.1.1/backend/src/main/java/com/attendance/repository/AttendanceRecordRepository.java` に、一覧の任意 `startDate` / `endDate` 絞り込みで使う検索メソッドを追加する。従業員 ID と日付の既存検索および一意キーと整合させる。
- [X] T008 [US1] `attendance-system-workshop-ja-v1.1.1/backend/src/main/java/com/attendance/service/AttendanceService.java` に保存・特定日取得・一覧取得を実装する。UC003 は `employeeId` が `EMP` + 数字 3 桁、日付が有効な暦日、出退勤が両方必須、時刻が `HH:MM`（00:00〜23:59）、出勤 `<` 退勤、未来日許可とする。形式不正は ERR03004 / ERR03005 とする。時刻は秒 `00` に変換し、従業員 ID と日付で upsert する。新規 `notes` は NULL、更新時 `notes` と `createdAt` は維持する。`updatedAt` は JPA がレコードを更新した場合に設定する。検証失敗時は保存しない。一覧は指定した開始日・終了日を含めて絞り込み、逆転範囲は空結果とする。
- [X] T009 [US1] `attendance-system-workshop-ja-v1.1.1/backend/src/main/java/com/attendance/controller/AttendanceController.java` の POST と GET を実装する。POST はリクエスト検証後に 200 と勤怠レコードを返す。特定日 GET は未登録を 404 / ERR02001、従業員 ID / 日付形式不正を 400 / ERR03004 / ERR03005 とし、一覧 GET は任意 `startDate` / `endDate` を受け取り、0 件でも 200 と空配列を返す。

### フロントエンドの実装

- [X] T010 [P] [US1] `attendance-system-workshop-ja-v1.1.1/frontend/src/types/attendance.ts` に、必須 `status: WORK` を含む UC003 勤務時間リクエスト型と `{error, message, timestamp}` エラー応答型を追加する。時刻入力は `HH:MM`、応答時刻は `HH:MM:SS` として型を区別する。
- [X] T011 [US1] `attendance-system-workshop-ja-v1.1.1/frontend/src/services/attendanceService.ts` の保存・特定日取得・一覧取得を API 契約に合わせる。一覧取得は任意 `startDate` / `endDate` を渡せるようにする。404 は未登録として `null`、その他の非 2xx は日本語メッセージとエラーコードを呼び出し側へ伝え、通信失敗を成功扱いしない。
- [X] T012 [US1] `attendance-system-workshop-ja-v1.1.1/frontend/src/components/WorkTimeForm.tsx` を SCR-002 として実装する。選択日を表示し、ダイアログ表示時に特定日取得、未登録時は空欄、登録済み時は `HH:MM` で初期表示する。出勤・退勤の必須、形式、前後を検証し、成功・エラー表示、読み込み・保存中状態、取得失敗時の再読み込み、キャンセルを実装する。
- [X] T013 [US1] `attendance-system-workshop-ja-v1.1.1/frontend/src/pages/CalendarPage.tsx` から SCR-002 を開閉できるようにする。選択日をフォームへ渡し、勤務時間入力ボタンを有効化し、保存成功時にダイアログを閉じ SUCCESS02001 を表示する。休暇登録は本タスクで実装しない。

### E2E テストの接続

- [X] T014 [US1] `attendance-system-workshop-ja-v1.1.1/e2e/features/step_definitions/req-uc003-work-time-entry.steps.ts` を作成し、既存 `REQ-UC003-勤務時間入力.feature` の全ステップを Cucumber / Playwright に接続する。保存成功、前後不正、形式不正、未来日、同日更新、特定日再取得を確認し、各シナリオのデータが互いに影響しないようにする。

**チェックポイント**: US1 は単独で受け入れ基準を満たし、DB・API・画面を通して動作する。

## フェーズ4: 仕上げと横断的な関心事

**目的**: 実装全体を検証し、仕様と手順に沿う状態にする。

- [X] T015 `attendance-system-workshop-ja-v1.1.1/backend/` で `mvn test`、`attendance-system-workshop-ja-v1.1.1/frontend/` で `pnpm lint` と `pnpm build`、`attendance-system-workshop-ja-v1.1.1/e2e/` で `pnpm test --tags @REQ-UC003` を実行し、失敗があれば原因を修正して再実行する。`attendance-system-workshop-ja-v1.1.1/specs/001-work-time-entry/quickstart.md` の正常保存・同日更新・不正時刻・未来日・再取得・一覧日付範囲を確認する。
- [X] T016 実装 PR ごとに `.specify/memory/constitution.md` のレビューゲートを適用する。AI レビューの後に実装者以外の人レビューを受け、PR 本文に AI 利用を記録する。PR は 400 行・8 ファイル以内とし、超える場合は分割する。

## 依存関係と実行順

### フェーズ依存

- Phase 1: 既存スケルトンを再利用するため、新規作業なし。
- Phase 2: T001 と T002 は独立して実行できる。両方の完了後に US1 を開始する。
- Phase 3: T003〜T005 のテスト追加は別ファイルのため並列可能だが、同時に進めるのは最大 2 件。T006〜T009 の API 実装は DTO → Repository / Service → Controller の順。T010 の型定義はバックエンド実装と並列可能。T011 → T012 → T013 の順に画面へ統合する。T014 は API と画面の接続後に実施する。
- Phase 4: T015 は US1 の実装完了後、T016 は検証・PR 準備時に行う。

### ストーリー依存

- US1（P1）: Phase 2 完了後に着手可能。他のユーザーストーリーへの依存はない。

### 並列実行例

```text
Phase 2:
  T001 DB 制約
  T002 API 共通エラー応答

Phase 3 テスト作成:
  先行: T003 サービス単体テスト + T004 コントローラーテスト
  完了後: T005 リポジトリ統合テスト

Phase 3 実装（T006 完了後）:
  T007 Repository 検索 + T010 フロントエンド型定義
```

同時進行は憲章に従い 2 件までとする。同じファイルを変更するタスクは並列化しない。

## 実装方針

### 最小実用製品（MVP、US1）

1. Phase 2 の DB 制約と API エラー応答を整備する。
2. US1 のテストを追加し、失敗することを確認する。
3. 保存・特定日取得 API と SCR-002 を実装して E2E シナリオを満たす。
4. バックエンド、フロントエンド、E2E を検証し、受け入れ基準を満たした段階で MVP とする。

### 段階的な提供

- 最初に DB / 共通 API 基盤をレビュー可能な単位で完了する。
- 次にバックエンドの保存・取得契約を完成させる。
- その後フロントエンドと E2E を接続する。
- どの段階も PR の 400 行・8 ファイル上限を超える場合は分割し、実装者以外のレビューを受ける。
