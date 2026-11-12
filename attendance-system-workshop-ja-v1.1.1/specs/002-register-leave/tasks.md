---
description: "休暇登録のタスク一覧"
---

# タスク: 休暇登録

**入力**: `/specs/002-register-leave/` の設計書

**前提**: plan.md（必須）、spec.md（ユーザーストーリーに必須）、research.md、data-model.md、contracts/、quickstart.md。実装の正本は `offjt_01/attendance-system-workshop-ja-v1.1.1/docs/design/` のローレベル設計。

**テスト**: 仕様の各ストーリーに独立テストと受け入れシナリオがあり、plan.md が JUnit 5、MockMvc、Testcontainers、Cucumber、Playwright での検証を定めているため、テストタスクを含める。新しいテストライブラリは追加しない。

**構成**: タスクはユーザーストーリー単位に分ける。各ストーリーは、用意したデータだけで独立して検証できる。

**憲章**: 同時に進めるタスクは2件まで。1タスクにつき担当者とブランチは1つ。1つの PR は400行以内、8ファイル以内、この機能以外の変更を含めない。本番データベースへ接続しない。テストを無効化する起動オプションは使わない。設計書の人間レビューが終わるまで実装を始めない。

## 書式: `[ID] [P?] [Story] 説明`

- **[P]**: 別ファイルを扱い、未完了タスクに依存しない場合だけ付ける
- **[Story]**: ユーザーストーリーフェーズだけに付ける（US1、US2、US3、US4）
- 説明には実装するファイルのパスを含める

## パスの規約

- アプリ: `offjt_01/attendance-system-workshop-ja-v1.1.1/`
- 契約: `specs/002-register-leave/contracts/leave-api.yaml`
- 画面: `offjt_01/attendance-system-workshop-ja-v1.1.1/docs/design/screen-design.md` の SCR-003
- シーケンス: `offjt_01/attendance-system-workshop-ja-v1.1.1/docs/design/domain/sequence/SEQ-REQ-UC004-休暇登録.md`
- API: `offjt_01/attendance-system-workshop-ja-v1.1.1/docs/design/openapi.yaml`
- メッセージ: `offjt_01/attendance-system-workshop-ja-v1.1.1/docs/design/message-definition.md`
- パスはワークスペースルートからの相対パス

## フェーズ 1: セットアップ（共有基盤）

**目的**: 休暇 API が使うデモ従業員 ID を、呼び出し側から指定できない設定にする

- [X] T001 `offjt_01/attendance-system-workshop-ja-v1.1.1/backend/src/main/resources/application.yml` に `attendance.demo-employee-id: EMP001` を追加する。既存のローカル PostgreSQL 設定は変えず、本番接続先は追加しない。

---

## フェーズ 2: 基盤（後続を止める前提）

**目的**: 全ストーリーが使う休暇区分、保存制約、要求、応答、メッセージ ID のエラー変換を先に置く

**⚠️ 重要**: このフェーズが終わるまでユーザーストーリーへ進まない

- [X] T002 [P] `offjt_01/attendance-system-workshop-ja-v1.1.1/backend/init-db/01-schema.sql` を `offjt_01/attendance-system-workshop-ja-v1.1.1/docs/design/database-table-definitions.md` に合わせ、`status` に `SICK_LEAVE`、`PERSONAL_LEAVE`、`BUSINESS_TRIP` を含む CHECK を追加する。休暇行は出勤・退勤の両方 NULL を許し、両方ある場合だけ退勤が出勤より後であることを制約する。新しいテーブルは作らない。
- [X] T003 [P] `offjt_01/attendance-system-workshop-ja-v1.1.1/backend/src/main/java/com/attendance/entity/LeaveType.java` に、Sick Leave、Personal Leave、Business Trip と保存コード `SICK_LEAVE`、`PERSONAL_LEAVE`、`BUSINESS_TRIP` の対応を追加する。`WORK` は休暇区分に含めない。
- [X] T004 [P] `offjt_01/attendance-system-workshop-ja-v1.1.1/backend/src/main/java/com/attendance/dto/CreateLeaveRequest.java` に、必須の日付と休暇区分、任意で最大500文字のメモを検証する作成要求を追加する。従業員 ID は受け取らない。
- [X] T005 [P] `offjt_01/attendance-system-workshop-ja-v1.1.1/backend/src/main/java/com/attendance/dto/UpdateLeaveRequest.java` に、必須の休暇区分と任意で最大500文字のメモを検証する更新要求を追加する。日付は受け取らない。
- [X] T006 [P] `offjt_01/attendance-system-workshop-ja-v1.1.1/backend/src/main/java/com/attendance/dto/LeaveResponse.java` に、日付、表示名の休暇区分、メモを返す応答を追加する。メモなしは空文字にする。
- [X] T007 [P] `offjt_01/attendance-system-workshop-ja-v1.1.1/backend/src/main/java/com/attendance/dto/ApiErrorResponse.java` に、`ERR02002`、`ERR02003`、`ERR02005`、`ERR03004`、`ERR03005`、`ERR03006`、`ERR03007`、`ERR03008` と `offjt_01/attendance-system-workshop-ja-v1.1.1/docs/design/message-definition.md` の利用者向け文言を持つエラー応答を追加する。内部例外の詳細は返さない。
- [X] T008 `offjt_01/attendance-system-workshop-ja-v1.1.1/backend/src/main/java/com/attendance/controller/LeaveExceptionHandler.java` に、日付・区分不足・メモ上限・期間の片方指定を400、未対応区分を400、同日重複を409、休暇不存在を404へ変換する処理を追加する。`error` は T007 のメッセージ ID にする。

**確認地点**: 休暇区分、デモ従業員 ID、DDL、要求、応答、メッセージ ID が揃い、ストーリー実装を始められる

---

## フェーズ 3: ユーザーストーリー 1 - 休暇を登録する（優先度: P1）🎯 MVP

**目標**: 従業員が任意の日付、対応する休暇区分、任意のメモで休暇を新規登録し、同じ内容を再取得できる

**独立テスト**: 未登録の日付に対応する区分で保存し、同じ日付の取得結果が日付、区分、メモと一致する。未対応区分、不足項目、501文字、同日の休暇の2件目は保存されない。勤務日は確認なしでは時刻が残り、確認後は同じ行が休暇になり時刻は空になる。

### ユーザーストーリー 1 のテスト ⚠️

> **注意: これらのテストを先に書き、実装前に失敗することを確認する**

- [X] T009 [P] [US1] `offjt_01/attendance-system-workshop-ja-v1.1.1/backend/src/test/java/com/attendance/service/LeaveServiceTest.java` に、正常登録、空メモ、空白のみのメモ、未対応区分、501文字、日付または区分の不足、既存休暇の追加拒否、確認なしでは勤務時刻を消さないこと、確認後は同じ行を休暇にして時刻を空にすることを Testcontainers の PostgreSQL で検証するテストを追加する。
- [X] T010 [P] [US1] `offjt_01/attendance-system-workshop-ja-v1.1.1/backend/src/test/java/com/attendance/controller/LeaveControllerTest.java` に、`POST /api/leaves` の201、確認済み勤務日の200、`GET /api/leaves/{date}` の200、`ERR03004` から `ERR03007` の400、休暇なしの404 `ERR02002`、既存休暇の409 `ERR02003`、確認なし勤務日の409 `ERR02005` を `specs/002-register-leave/contracts/leave-api.yaml` に合わせて検証するテストを追加する。
- [X] T011 [P] [US1] `offjt_01/attendance-system-workshop-ja-v1.1.1/e2e/features/REQ-UC004-休暇登録.feature` と `offjt_01/attendance-system-workshop-ja-v1.1.1/e2e/features/step_definitions/req-uc004-leave-registration.steps.ts` に、登録開始時の入力欄、3区分だけ、保存と再取得、空メモ、不正入力の `@REQ-UC004` シナリオを追加する。期待文言は `offjt_01/attendance-system-workshop-ja-v1.1.1/docs/design/message-definition.md` の SUCCESS02002、ERR03005、ERR03007、ERR02003、ERR02005 に合わせる。勤務日は確認後だけ登録できるシナリオを含める。

### ユーザーストーリー 1 の実装

- [X] T012 [US1] `offjt_01/attendance-system-workshop-ja-v1.1.1/backend/src/main/java/com/attendance/service/LeaveService.java` に、`attendance.demo-employee-id` と既存の `AttendanceRecordRepository` を使う新規作成と日付取得を実装する。休暇行がある日は作成しない。勤務行は `confirmReplaceWork` が true のときだけ同じ行を休暇にし、出勤・退勤時刻を空にする。空白のみのメモは `notes` を NULL として保存する。取得は休暇ステータスの行だけを成功とする。
- [X] T013 [US1] `offjt_01/attendance-system-workshop-ja-v1.1.1/backend/src/main/java/com/attendance/controller/LeaveController.java` に `POST /api/leaves` と `GET /api/leaves/{date}` を実装する。成功は201と200、契約どおり400、404、409を返す。従業員 ID は要求で受け取らない。
- [X] T014 [P] [US1] `offjt_01/attendance-system-workshop-ja-v1.1.1/frontend/src/types/leave.ts` と `offjt_01/attendance-system-workshop-ja-v1.1.1/frontend/src/services/leaveService.ts` に、3区分の型と休暇作成、日付取得の呼び出しを追加する。従業員 ID は送らない。
- [X] T015 [US1] `offjt_01/attendance-system-workshop-ja-v1.1.1/frontend/src/components/LeaveForm.tsx` と `offjt_01/attendance-system-workshop-ja-v1.1.1/frontend/src/pages/CalendarPage.tsx` に、`offjt_01/attendance-system-workshop-ja-v1.1.1/docs/design/screen-design.md` の SCR-003 と `offjt_01/attendance-system-workshop-ja-v1.1.1/docs/design/domain/sequence/SEQ-REQ-UC004-休暇登録.md` の SEQ-UC004-001、SEQ-UC004-002 に従い、日付、3区分、任意メモ、不足項目と501文字の保存拒否、SUCCESS02002 と ERR02003 の表示を実装する。ERR02005 は確認ダイアログにし、確認したときだけ `confirmReplaceWork: true` で再送する。初期表示の404では ERR02002 を出さない。
- [X] T016 [US1] `offjt_01/attendance-system-workshop-ja-v1.1.1/backend/` で `mvn test` を実行し、`offjt_01/attendance-system-workshop-ja-v1.1.1/e2e/` で `@REQ-UC004` の登録シナリオを実行して、保存した日付、区分、メモを再取得できることを確認する。

**確認地点**: 休暇の新規登録と日付取得が単独で動作し、不正入力と重複は既存レコードを変えない

---

## フェーズ 4: ユーザーストーリー 2 - 登録済みの休暇を確認する（優先度: P2）

**目標**: 従業員が保存済みの休暇をあとから取得し、未登録日では架空の休暇を見ない

**独立テスト**: あらかじめ保存した休暇を取得し、日付、区分、メモが保存時と一致する。未登録日と勤務のみの日は休暇なしと分かり、通信失敗と取り違えない。新規登録をしなくても検証できる。

### ユーザーストーリー 2 のテスト ⚠️

- [X] T017 [US2] `offjt_01/attendance-system-workshop-ja-v1.1.1/e2e/features/REQ-UC004-休暇登録.feature` と `offjt_01/attendance-system-workshop-ja-v1.1.1/e2e/features/step_definitions/req-uc004-leave-registration.steps.ts` に、保存済み休暇の再表示、機能を離れても残ること、未登録日、取得失敗と休暇なしの区別、期間一覧での勤務日の除外、期間片側指定の400を検証する `@REQ-UC004` シナリオを追加する。初期表示の404はエラー表示にしない。

### ユーザーストーリー 2 の実装

- [ ] T018 [US2] `offjt_01/attendance-system-workshop-ja-v1.1.1/backend/src/main/java/com/attendance/service/LeaveService.java` と `offjt_01/attendance-system-workshop-ja-v1.1.1/backend/src/main/java/com/attendance/controller/LeaveController.java` に `GET /api/leaves` を実装する。期間の片方だけは400 `ERR03008`、休暇コードのレコードだけを返し、勤務のみの日は含めない。
- [X] T019 [US2] `offjt_01/attendance-system-workshop-ja-v1.1.1/frontend/src/services/leaveService.ts` と `offjt_01/attendance-system-workshop-ja-v1.1.1/frontend/src/components/LeaveForm.tsx` に、SEQ-UC004-003 に従い、選択日の取得、保存値の初期表示、404と通信失敗の区別、未登録日の空フォームを実装する。取得失敗中は登録・変更・削除を実行させない。
- [ ] T020 [US2] `offjt_01/attendance-system-workshop-ja-v1.1.1/e2e/` の `@REQ-UC004` を実行し、フォームを閉じて開き直しても保存値が表示されることを確認する。

**確認地点**: 既存データを取得するだけで、保存内容の確認と休暇なしの判定ができる

---

## フェーズ 5: ユーザーストーリー 3 - 登録済みの休暇を変更する（優先度: P3）

**目標**: 既存休暇の区分またはメモを変更し、存在しない対象は作成しない

**独立テスト**: 用意した休暇の区分またはメモを変えて保存し、再取得結果が変更後と一致する。存在しない日付の更新は成功扱いにならず、レコードは増えない。日付と勤務時刻は変わらない。

### ユーザーストーリー 3 のテスト ⚠️

- [X] T021 [P] [US3] `offjt_01/attendance-system-workshop-ja-v1.1.1/backend/src/test/java/com/attendance/service/LeaveServiceTest.java` に、既存休暇の区分とメモの更新、日付と時刻を変えないこと、存在しない休暇と勤務のみの日を更新しないことを検証するテストを追加する。
- [X] T022 [P] [US3] `offjt_01/attendance-system-workshop-ja-v1.1.1/backend/src/test/java/com/attendance/controller/LeaveControllerTest.java` に、`PUT /api/leaves/{date}` の200、入力不正の400、対象なしの404 `ERR02002` を検証するテストを追加する。
- [X] T023 [P] [US3] `offjt_01/attendance-system-workshop-ja-v1.1.1/e2e/features/REQ-UC004-休暇登録.feature` と `offjt_01/attendance-system-workshop-ja-v1.1.1/e2e/features/step_definitions/req-uc004-leave-registration.steps.ts` に、変更の保存と再取得、存在しない休暇の更新が SUCCESS02003 にならないシナリオを追加する。

### ユーザーストーリー 3 の実装

- [ ] T024 [US3] `offjt_01/attendance-system-workshop-ja-v1.1.1/backend/src/main/java/com/attendance/service/LeaveService.java` に、休暇ステータスの既存行だけを更新する処理を追加する。対象がなければ作成せず、空白のみのメモはメモなしにする。`attendance_date` と出勤・退勤時刻は変更しない。
- [ ] T025 [US3] `offjt_01/attendance-system-workshop-ja-v1.1.1/backend/src/main/java/com/attendance/controller/LeaveController.java` に `PUT /api/leaves/{date}` を実装し、契約の200、400、404を返す。日付はパスだけが決める。
- [ ] T026 [P] [US3] `offjt_01/attendance-system-workshop-ja-v1.1.1/frontend/src/services/leaveService.ts` と `offjt_01/attendance-system-workshop-ja-v1.1.1/frontend/src/components/LeaveForm.tsx` に、SEQ-UC004-004 に従い更新呼び出しを追加し、読み込んだ休暇があるときだけ更新を送る。成功時は SUCCESS02003 を表示して閉じる。
- [ ] T027 [US3] `offjt_01/attendance-system-workshop-ja-v1.1.1/backend/` と `offjt_01/attendance-system-workshop-ja-v1.1.1/e2e/` で US3 のテストを実行し、存在しない更新でレコードが増えないことを確認する。

**確認地点**: 登録済み休暇を訂正でき、対象がない更新は成功にならない

---

## フェーズ 6: ユーザーストーリー 4 - 登録済みの休暇を削除する（優先度: P4）

**目標**: 不要な休暇行だけを削除し、存在しない削除は成功にしない

**独立テスト**: 用意した休暇を削除し、同じ日付の再取得に含まれない。勤務のみの日と未登録日は削除成功にせず、勤務時刻も消えない。削除後の同じ日付へは新しい休暇を登録できる。

### ユーザーストーリー 4 のテスト ⚠️

- [ ] T028 [P] [US4] `offjt_01/attendance-system-workshop-ja-v1.1.1/backend/src/test/java/com/attendance/service/LeaveServiceTest.java` に、休暇行の削除、削除後の404、勤務のみの日を削除しないこと、削除後に同じ日付へ再登録できることを検証するテストを追加する。
- [ ] T029 [P] [US4] `offjt_01/attendance-system-workshop-ja-v1.1.1/backend/src/test/java/com/attendance/controller/LeaveControllerTest.java` に、`DELETE /api/leaves/{date}` の204、日付不正の400 `ERR03004`、対象なしの404 `ERR02002` を検証するテストを追加する。
- [ ] T030 [P] [US4] `offjt_01/attendance-system-workshop-ja-v1.1.1/e2e/features/REQ-UC004-休暇登録.feature` と `offjt_01/attendance-system-workshop-ja-v1.1.1/e2e/features/step_definitions/req-uc004-leave-registration.steps.ts` に、確認後の削除、確認取消で残ること、存在しない削除が SUCCESS02004 にならないシナリオを追加する。

### ユーザーストーリー 4 の実装

- [ ] T031 [US4] `offjt_01/attendance-system-workshop-ja-v1.1.1/backend/src/main/java/com/attendance/service/LeaveService.java` に、休暇ステータスの行だけを削除する処理を追加する。対象がなければ何も削除せず404にする。
- [ ] T032 [US4] `offjt_01/attendance-system-workshop-ja-v1.1.1/backend/src/main/java/com/attendance/controller/LeaveController.java` に `DELETE /api/leaves/{date}` を実装し、成功は204、契約どおり400と404を返す。
- [ ] T033 [P] [US4] `offjt_01/attendance-system-workshop-ja-v1.1.1/frontend/src/services/leaveService.ts` と `offjt_01/attendance-system-workshop-ja-v1.1.1/frontend/src/components/LeaveForm.tsx` に、SEQ-UC004-005 に従い、登録済み状態だけの削除ボタンと確認ダイアログを追加する。確定前は API を呼ばない。成功時は SUCCESS02004 を表示して閉じる。
- [ ] T034 [US4] `offjt_01/attendance-system-workshop-ja-v1.1.1/backend/` と `offjt_01/attendance-system-workshop-ja-v1.1.1/e2e/` で US4 のテストを実行し、勤務行が削除されないことを確認する。

**確認地点**: 休暇の取消ができ、勤務行と存在しない対象は削除成功にならない

---

## フェーズ 7: 仕上げと横断的な確認

**目的**: 設計書との一致と、ローカル検証の完了を確認する

- [ ] T035 [P] `offjt_01/attendance-system-workshop-ja-v1.1.1/README.md` に、休暇機能がデモ従業員 ID `EMP001` だけを扱い、認証や本番データベース接続を含まないことを記載する。
- [ ] T036 [P] `offjt_01/attendance-system-workshop-ja-v1.1.1/docs/managements/dev-status.md` の REQ-UC004 を、T016、T020、T027、T034 が成功したあとだけ「実装・試験済」に更新する。テスト未完了のまま完了にしない。
- [ ] T037 `specs/002-register-leave/quickstart.md` の起動、`mvn test`、フロントエンドの `pnpm build` と `pnpm lint`、`@REQ-UC004` の確認を実行し、無関係な変更が残っていないことを確認する。

---

## 依存関係と実行順序

### フェーズの依存関係

- セットアップ（フェーズ 1）に依存関係はない
- 基盤（フェーズ 2）はセットアップの後、全ユーザーストーリーの前に完了する
- US1 は基盤の後に開始できる
- US2 は US1 の日付取得が使えると確認しやすいが、保存済みデータを用意すれば取得だけを検証できる
- US3 は更新対象の休暇行が必要である。US1 の作成を使うか、テストデータを用意する
- US4 は削除対象の休暇行が必要である。US1 の作成を使うか、テストデータを用意する
- 仕上げは対象ストーリーの検証が終わってから行う

### ユーザーストーリーの依存関係

- **US1（P1）**: 基盤の後、単独で MVP になる
- **US2（P2）**: 日付取得は US1 と共有する。期間取得と再表示の確認は US2 の増分である
- **US3（P3）**: 作成済み休暇があれば、US2 の画面確認がなくても API 単体で検証できる
- **US4（P4）**: 作成済み休暇があれば、US3 がなくても削除を検証できる

### 各ユーザーストーリーの中

- テストを先に書き、実装前に失敗することを確認する
- サービスをコントローラより先に実装する
- フロントエンドの型と API 呼び出しを画面より先に置く
- ストーリーの検証コマンドを、そのストーリーの実装後に実行する

### 並行できる作業

- 同時実行は2件まで。残りは次に回す
- T002 と T003 は別ファイルなので、基盤の最初の2件にできる
- T004、T005、T006、T007 は別ファイルだが、T003 の休暇区分が決まってから始める。一度に2件まで
- T008 は T007 の後にする
- T009、T010、T011 は別ファイルだが、同時実行は2件まで
- T012 と T014 は別ファイルなので、サービス実装とフロントエンドの型を2件まで並行できる
- T013 は T012 の後、T015 は T014 の後にする
- `LeaveService.java`、`LeaveController.java`、`LeaveForm.tsx`、`REQ-UC004-休暇登録.feature`、`req-uc004-leave-registration.steps.ts` は同時に編集しない
- US3 と US4 のテスト追加は、共有テストファイルを同時に編集しない

## 並行の例: ユーザーストーリー 1

```text
# 同時実行は2件まで。残りは次に回す。
T009 LeaveServiceTest.java の登録テスト
T010 LeaveControllerTest.java の作成・取得テスト

# テストが失敗することを確認したあと:
T012 LeaveService.java の作成と日付取得
T014 leave.ts と leaveService.ts
```

## 実装方針

### MVP を先に作る（ユーザーストーリー 1 のみ）

1. T001 から T008 まで完了する
2. T009 から T016 まで完了する
3. 未登録日への登録、再取得、不正入力、同日競合で既存勤務時刻が残ることを確認して止めてよい

### 段階的な提供

1. 基盤を完了する
2. US1 を完了し、登録と再取得を確認する
3. US2 を完了し、閉じたあとの再表示を確認する
4. US3 を完了し、存在しない更新で行が増えないことを確認する
5. US4 を完了し、勤務行が削除されないことを確認する
6. T035 から T037 で横断確認する

### 並行作業の方針

1人の担当者が1ブランチで進める。並行が必要な場合も同時2タスクまでとし、共有ファイルは分割しない。

### 推奨する PR の分割

各 PR は8ファイル、400行以内に収める。超える場合は次の単位で分ける。

1. T001 から T008: 設定、DDL、区分、DTO、エラー変換
2. T009 から T013: US1 のテストと API
3. T014 から T016: US1 の画面と検証
4. T017 から T020: US2
5. T021 から T027: US3。テストと実装が8ファイルを超える場合は、テスト PR と実装 PR に分ける
6. T028 から T034: US4。同様にテストと実装を分ける
7. T035 から T037: 文書と最終確認

## 注意事項

- エラーコードは `INVALID_INPUT` などの意味コードを使わず、メッセージ ID を使う
- 画面の成功文言は SUCCESS02002、SUCCESS02003、SUCCESS02004 を使う
- 初期表示の404は ERR02002 を画面に出さない。更新と削除の404は成功表示にしない
- 同日の勤務行は、確認後だけ休暇に変える。確認なしでは時刻を消さない
- 本番データベース、機密情報、テスト無効化、確認を省略する起動オプションは使わない
- 同じ修正が3回失敗したら人が引き取る
