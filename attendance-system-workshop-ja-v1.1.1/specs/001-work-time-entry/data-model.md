# データモデル: 勤務時間入力

物理テーブルは既存の `attendance_records` を使う。新規テーブルは作らない。設計書の更新箇所は [research.md](./research.md) の一覧どおり [database-table-definitions.md](../../attendance-system-workshop-ja-v1.1.1/docs/design/database-table-definitions.md)。

## エンティティ: 勤怠レコード（AttendanceRecord）

| フィールド | 物理名 | 型 | UC003 での規則 |
| --- | --- | --- | --- |
| id | id | BIGINT | 採番。要求側は送らない |
| employeeId | employee_id | VARCHAR(50) | 必須。今回は `EMP001`。形式 `^EMP[0-9]{3}$` |
| attendanceDate | attendance_date | DATE | 必須。`YYYY-MM-DD`。未来日可。過去日も拒否しない |
| clockInTime | clock_in_time | TIME | WORK では必須。入力は `HH:MM`、保存は秒 `00` |
| clockOutTime | clock_out_time | TIME | WORK では必須。出勤より後。同刻不可。日跨ぎ不可 |
| status | status | VARCHAR(50) | 共有テーブルの許可値は `WORK`、`PAID_LEAVE`、`SICK_LEAVE`、`UNPAID_LEAVE`、`PERSONAL_LEAVE`、`BUSINESS_TRIP`。UC003 は `WORK` として保存 |
| notes | notes | TEXT | 画面項目にしない。新規は NULL。更新では変更しない |
| createdAt | created_at | TIMESTAMP | 新規時に設定。更新しない |
| updatedAt | updated_at | TIMESTAMP | JPA の `@PreUpdate` によりレコードが更新されたときに更新。同値の再送で DB 更新が発生しない場合は更新されない |

## 関係

- 従業員マスタは無い。`employee_id` は文字列のまま。
- 休暇（REQ-UC004）と同じ行を共有する。一意キーは `(employee_id, attendance_date)` の 1 件。

## 検証

1. 従業員 ID が `EMP` + 数字 3 桁でない場合は 400。
2. 日付が `YYYY-MM-DD` 形式の有効な暦日でない場合は 400。未来日だけでは 400 にしない。
3. 出勤または退勤が無い場合は ERR03003。
4. 時刻が `HH:MM`（00:00〜23:59）でない場合は ERR03002。
5. 出勤 &lt; 退勤でない場合は ERR03001。同刻を含む。
6. 保存 API のリクエストは `employeeId`、`attendanceDate`、`clockInTime`、`clockOutTime`、`status` を必須とする。画面は `status=WORK` を送信し、サービスは `WORK` として保存する。
7. 上記を通過した保存は、同じ従業員と日付なら更新、無ければ作成。
8. 特定日取得で行が無い場合は 404 と ERR02001。画面は未登録として空欄にする。検証失敗では行を書かない。
9. 従業員 ID 形式不正は ERR03004、日付形式不正は ERR03005。複数の Bean Validation 不備は従業員 ID、日付、未入力時刻、時刻形式の順に返す。出勤・退勤順序はリクエスト形式検証後に ERR03001 で検証する。

## 一覧取得

- `startDate` と `endDate` は任意。指定した境界日を含めて絞り込む
- 両方指定し、`startDate > endDate` の場合は該当レコードなしとして HTTP 200 と空配列を返す
- 絞り込み無しでも従業員の全勤怠を HTTP 200 と配列で返す
- 結果の並び順は保証しない
- 休暇レコードの時刻は NULL になり得るため、API 応答時刻も nullable とする

## 状態

UC003 の画面が送る状態は `WORK` のみで、保存サービスも `WORK` として永続化する。共有テーブルでは休暇側の状態も保存される。

```text
なし --保存成功--> WORK
WORK --再保存成功--> WORK（時刻を上書き。notes は維持）
休暇系 --UC003 の保存成功--> WORK（時刻を設定。notes は維持）
```

休暇系から WORK への遷移は、一意キーが 1 件しか許せないための帰結である。休暇画面の削除や種別変更は本データモデルの操作にしない。

## 設計書へ書き戻す制約

`chk_attendance_clock_times` は次の意味に改める。

```text
clock_in_time IS NULL OR clock_out_time IS NULL OR clock_out_time > clock_in_time
```

NULL 比較を残すのは、休暇行が時刻 NULL であるため。WORK の両方必須はアプリ検証とし、休暇行をこのチェックだけでは壊さない。

**実装上の注意**: DB は休暇状態として `PERSONAL_LEAVE` と `BUSINESS_TRIP` も許可するが、フロントエンドの `AttendanceStatus` と API 応答検証は `WORK`、`PAID_LEAVE`、`SICK_LEAVE`、`UNPAID_LEAVE` のみを許可する。そのため、前二者を含む勤怠一覧応答は現行フロントエンドで不正応答として拒否される。UC003 画面は一覧 API を呼び出さない。
