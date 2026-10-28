# Issue管理

本ドキュメントでは、プロジェクトのIssue管理としてissue一覧やissueのステータス管理について記載します。

## issue一覧

| Issue ID | Title                                                      | Status | Priority | Category      | Related Use Cases |
| -------- | ---------------------------------------------------------- | ------ | -------- | ------------- | ----------------- |
| ISS-001  | SCR-002 / SCR-003 のポップアップ表現と画面遷移定義の不整合 | 🟢 Resolved | High     | Documentation | UC003, UC004      |
| ISS-002  | SCR-002 のダイアログ画面外のクリックで画面が閉じない       | 🔴 Open     | Low      | Enhancement   | UC003             |
| ISS-003  | 日付をまたいでもカレンダーの「今日」の表示が更新されない   | 🔴 Open     | Low      | Bug           | UC002             |
| ISS-004  | 休暇登録済みの日に勤務時間を保存すると休暇情報が上書きされる | 🔴 Open     | High     | Bug           | UC003, UC004      |
| ISS-005  | ダイアログでEscapeキー操作とフォーカス管理が不足している   | 🔴 Open     | Medium   | Enhancement   | UC003, UC004      |
| ISS-006  | 従業員別の勤怠一覧APIが常に空の結果を返す                   | 🔴 Open     | Medium   | Bug           | UC002, UC003      |
| ISS-007  | カレンダーに祝日が表示されない                             | 🔴 Open     | Medium   | Feature Request | UC002             |

## issueステータス定義

| ステータス    | 説明                                      |
| ------------- | ----------------------------------------- |
| 🔴 Open        | Issueが作成された状態。対応が必要。       |
| 🟡 In Progress | Issueの対応が進行中の状態。               |
| 🟢 Resolved    | Issueの対応が完了し、レビュー待ちの状態。 |
| 🔵 Closed      | Issueが解決され、クローズされた状態。     |

## issueステータス管理

- Issueのステータスは、対応の進捗に応じて適切に更新してください。
- ステータスの変更は、本ドキュメント内のステータスを更新することで管理します。
- ステータスの変更履歴は、Issueファイル内に記録してください。

**最終更新日**: 2026-10-28
