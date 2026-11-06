# ブランチ名ガイドライン

チーム内でブランチの目的をすぐに把握できるよう、次の形式で命名します。

## テンプレート

```text
<type>/<ticket-id>-<short-description>
```

- `<type>`: 作業の種類を表す英小文字
- `<ticket-id>`: 関連する Issue 番号または要件 ID
- `<short-description>`: 変更内容を表す短い英小文字の説明

要件 ID を使う場合は、既存の要件書に合わせて `REQ-UCXXX` 形式にします。説明部分は単語をハイフンでつなぎ、空白や日本語は使いません。関連する Issue や要件がない場合は、ID を省略して `<type>/<short-description>` とします。

## 作業種別

| type | 用途 |
| --- | --- |
| `feature` | 新機能の追加 |
| `fix` | 不具合の修正 |
| `docs` | ドキュメントの変更 |
| `refactor` | 外部から見た動作を変えないコード整理 |
| `test` | テストの追加・変更 |
| `chore` | ビルド、依存関係などの保守 |
| `hotfix` | 緊急度の高い本番不具合の修正 |

## 例

```text
feature/REQ-UC003-work-time-entry
fix/REQ-UC003-invalid-time-validation
docs/42-update-contribution-guide
test/REQ-UC002-calendar-navigation
chore/update-node-dependencies
hotfix/108-fix-login-error
```

## チームでの運用

1. 作業開始前に `main` を最新化し、そこから作業ブランチを作成します。

   ```bash
   git checkout main
   git pull origin main
   git checkout -b feature/REQ-UC003-work-time-entry
   ```

2. 1つのブランチでは、原則として1つの Issue または要件に対応する変更を行います。
3. 個人名、機密情報、曖昧な語（`work`、`update` だけなど）はブランチ名に含めません。
4. 作業完了後はプルリクエストを作成し、マージ後に不要なブランチを削除します。
