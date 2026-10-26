---
applyTo: "e2e/**"
---
# E2Eルール
- Cucumberの Feature は `e2e/features/REQ-XXX-*.feature` に配置。
- シナリオ: Happy path + 代表的な失敗パス（バリデーション）。
- ステップ定義が不足していれば自動生成してからテストを実行。
