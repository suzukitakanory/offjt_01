# Phase 0: 調査（勤務時間入力）

入力は [spec.md](./spec.md) と既存の `attendance-system-workshop-ja-v1.1.1/docs/design/`。未解消の NEEDS CLARIFICATION はない。

## 1. 更新・作成が必要な設計書

**Decision**: 設計作業開始時に次の 5 件を UC003 の設計作業対象とした。アクター一覧とデプロイメント図は変更せず、5 件の更新・作成は完了している。

| 設計書 | 判定 | 必要な中身 |
| --- | --- | --- |
| [screen-design.md](../../attendance-system-workshop-ja-v1.1.1/docs/design/screen-design.md) | 更新 | SCR-002 のローレベル。項目、HH:MM、前後チェック、呼び出す API、メッセージ、キャンセル。SCR-001 の遷移表は既に SCR-002 を指している |
| [openapi.yaml](../../attendance-system-workshop-ja-v1.1.1/docs/design/openapi.yaml) | 更新 | `POST /api/attendance` と `GET /api/attendance/{employeeId}/{date}` の TODO をスキーマ化する。一覧取得は定義済みなので、UC003 で使う旨の補足に留める |
| [message-definition.md](../../attendance-system-workshop-ja-v1.1.1/docs/design/message-definition.md) | 更新 | コメントアウトされている UC003 の成功・検証メッセージを正式行と詳細定義にする |
| [database-table-definitions.md](../../attendance-system-workshop-ja-v1.1.1/docs/design/database-table-definitions.md) | 更新 | 時刻チェックを「以降」から「より前」へ改める。WORK では両時刻必須、未来日は拒否しない、を備考に書く |
| [SEQ-REQ-UC003-勤務時間入力.md](../../attendance-system-workshop-ja-v1.1.1/docs/design/domain/sequence/) | 新規 | UC001 / UC002 と同じ置き場に、保存・検証失敗・再取得のシーケンスを作る |
| [actors.md](../../attendance-system-workshop-ja-v1.1.1/docs/design/actors.md) | 変更なし | ACT-001 が勤務時間入力と SCR-002 を既に持つ |
| [deployment-diagram.md](../../attendance-system-workshop-ja-v1.1.1/docs/design/deployment-diagram.md) | 変更なし | React、Spring Boot、PostgreSQL、ポートに不足がない |

**Rationale**: 当初、ハイレベル設計は UC001〜UC004 まで反映済みだった一方、UC003 の画面詳細、保存 API、特定日取得、メッセージには未定義箇所があり、テーブルの時刻チェック条件と要件にも不一致があった。これらを設計書へ反映し、UC003 のシーケンスを追加した。

**Alternatives considered**: 設計書を全部作り直す案は、UC001 / UC002 の確定内容を壊すため不採用。スキーマ SQL や `dev-status.md` は設計書ではないため、この一覧には入れない。状態を「ローレベル設計済」にするのは、上記 5 件を実際に更新したあとの管理作業とする。

## 2. 技術構成

**Decision**: 既存の React 18.2 + TypeScript、Spring Boot 3.2 + Java 21、PostgreSQL、Playwright + Cucumber を拡張する。新規サービスや認証は追加しない。

**Rationale**: [deployment-diagram.md](../../attendance-system-workshop-ja-v1.1.1/docs/design/deployment-diagram.md) とスケルトンがこの構成で、UC003 の API パスもコントローラに予告されている。

**Alternatives considered**: 別 API や別テーブルは、既存の `POST /api/attendance` と `attendance_records` と重複するため不採用。

## 3. 保存の意味

**Decision**: 従業員 ID と勤怠日で upsert する。新規なら INSERT、同じ組み合わせなら UPDATE。UC003 の保存リクエストには `status=WORK` を含め、サーバーも `WORK` として保存する。出勤・退勤を両方必須にする。画面に備考欄はないため、更新時も `notes` は変更しない。新規時の `notes` は NULL。

**Rationale**: 受け入れ基準と E2E「既存の勤怠レコードを更新する」が、同日 1 件の上書きを求めている。一意キー `uk_attendance_employee_date` が既にある。

**Alternatives considered**: 同日 2 件目を 409 で拒否する案は、「1 件として保存」と更新シナリオに反するため不採用。休暇行が先にある場合も別レコードにはできない。UC003 の保存はその行を WORK に更新する。休暇ルールの詳細は REQ-UC004 の設計に残す。

## 4. 時刻と比較

**Decision**: 画面と要求は `HH:MM`。保存時に秒は `00` として TIME へ入れる。応答の既存スキーマは `HH:MM:SS` のままでよい。比較は同一日の時刻で、出勤 &lt; 退勤。同刻、逆転、日跨ぎは拒否する。未来日は日付としては許可し、日付の上限チェックは置かない。

**Rationale**: REQ-UC003、REQ-000 の 4.3.1、E2E の文言がこの規則と一致する。DB の `>=` は同刻を許すため設計欠陥である。

**Alternatives considered**: 日跨ぎ勤務を許す案は、現在の TIME 同士の比較と「より前」に合わない。別要件になるまで採用しない。

## 5. メッセージ

**Decision**: E2E が固定している文言をメッセージ ID に割り当てる。成功文言は既存コメントの例を採用する。未入力は E2E に無いが、両時刻必須のため追加する。

| ID | 文言 | 用途 |
| --- | --- | --- |
| SUCCESS02001 | 勤怠情報が正常に保存されました | 保存成功時に画面が表示するメッセージ。API の応答本文には含まれない |
| ERR03001 | 出勤時刻は退勤時刻より前である必要があります | 同刻・逆転。HTTP 400 |
| ERR03002 | 有効な時刻形式で入力してください | HH:MM 違反。HTTP 400 |
| ERR03003 | 出勤時刻と退勤時刻を入力してください | どちらか未入力。HTTP 400 |
| ERR03004 | 従業員IDの形式が不正です | `EMP` + 数字 3 桁以外。HTTP 400 |
| ERR03005 | 勤怠日の形式が不正です | 有効な `YYYY-MM-DD` でない。HTTP 400 |
| ERR02001 | 指定した日付の勤怠レコードはありません | 特定日取得の不存在。HTTP 404 |

**Rationale**: E2E はエラー 2 件の部分一致を固定している。コメント例の SUCCESS02001 / ERR03001 と衝突しない。

**Alternatives considered**: 英語メッセージは、E2E と「分かりやすいエラー」に合わないため不採用。

## 6. 取得 API の画面利用

**Decision**: ダイアログを開くとき特定日取得を呼ぶ。無ければ空欄、あれば時刻を初期表示する。一覧取得は UC003 の機能要件として契約に残すが、カレンダーのバッジ表示には使わない。

**Rationale**: 更新シナリオは既存値の再入力を許しており、初期表示があると操作が一致する。一覧のスキーマは UC002 向けに既に書いてある。バッジは UC003 の受け入れ基準に無い。

**Alternatives considered**: ダイアログを常に空欄にする案は、更新時に現在値を確認できない。一覧を UC003 画面の必須表示にする案は範囲を広げる。

一覧 API の既存契約にある `startDate` / `endDate` は任意とし、指定した境界日を含めて検索する。両方を指定して開始日が終了日より後なら、範囲内の行がないものとして HTTP 200 と空配列を返す。これは既存の一覧契約を UC003 の機能別契約にも引き継ぐためである。

## 9. 実装コード照合

- 保存時は従業員 ID、日付、出退勤時刻を検証し、既存行があれば同じ従業員・日付の行を更新する。時刻は `HH:MM` を受け付け、保存・応答時は秒を含む形式にする。更新時に備考は保持される。
- 保存成功時の API 応答は勤怠レコードで、SUCCESS02001 はフロントエンドが表示する。入力検証に対応するエラーは HTTP 400、予期しない例外は HTTP 500 / ERR99001。
- 特定日取得で未登録なら API は HTTP 404 / ERR02001 を返す。画面は未登録として空欄を表示し、404 以外の取得エラーでは入力と保存を無効にして再読み込みを提示する。
- 一覧 API は指定範囲の境界日を含み、開始日が終了日より後なら空配列を返す。結果の並び順は実装で保証していない。
- DB は 6 種類の status を許可するが、フロントエンドの一覧応答検証は 4 種類に限られる。`PERSONAL_LEAVE` または `BUSINESS_TRIP` を含む一覧応答は現状フロントエンドで拒否される。UC003 の画面は一覧 API を呼び出さない。
- JaCoCo の設定はカバレッジレポート生成までで、閾値チェックは行わない。UC003 固有の性能目標・負荷試験も実装されていない。

## 7. 実装上の既知制約

**Decision**: DB が許可する `status` は 6 種類のまま維持する。UC003 の保存状態は `WORK` とし、一覧 API の全 status 値をフロントエンドが扱えることは UC003 のスコープ外とする。

**Rationale**: 共有テーブルの CHECK 制約とエンティティは `WORK`、`PAID_LEAVE`、`SICK_LEAVE`、`UNPAID_LEAVE`、`PERSONAL_LEAVE`、`BUSINESS_TRIP` を許可する一方、フロントエンドの `AttendanceStatus` と一覧応答の検証は最初の 4 種類に限られる。UC003 のダイアログは一覧 API を使用しないが、一覧に `PERSONAL_LEAVE` または `BUSINESS_TRIP` が含まれると現状のフロントエンド検証で拒否される。

`status` の DB 列は初期化 SQL とエンティティの双方で `VARCHAR(50)`。保存 DTO は 4 状態を検証対象としているが、UC003 のサービスは受け付けた値にかかわらず `WORK` を保存する。UC003 の画面は `WORK` を送る。

## 8. 設計書再確認で確定した補正

- 保存リクエストは `status=WORK` を必須とする。API 応答は保存した勤怠レコードであり、SUCCESS02001 はフロントエンドが表示する。
- 特定日取得の未登録は API が 404 / ERR02001 を返し、フロントエンドは空欄として扱う。404 以外の取得エラーはエラー表示と再読み込み操作の対象になる。
- 一覧 API の `startDate` / `endDate` は任意で境界日を含む。開始日が終了日より後なら 200 と空配列を返し、結果の並び順は保証されない。
- 不正な従業員 ID と日付は 400 / ERR03004、ERR03005。予期しない例外は 500 / ERR99001。
- JaCoCo はカバレッジレポートを生成するが、閾値を強制する check はない。UC003 固有の性能目標・負荷試験は実装されていないため、REQ-000 の一般的な性能記述から数値基準を追加しない。
