# ブランチ命名ガイドライン

チーム内でブランチの目的や関連する作業を把握しやすくするため、次の形式でブランチ名を付けます。

## 命名テンプレート

```text
<種別>/<関連ID>-<変更内容>
```

- **種別**: 変更の目的を表す英小文字
- **関連ID**: 要件ID（`REQ-UCXXX`）または GitHub Issue 番号（`issue-123`）
- **変更内容**: 作業内容を表す短い英小文字。単語はハイフン（`-`）で区切る

関連IDがない作業では、次の形式を使用します。

```text
<種別>/<変更内容>
```

## 種別

| 種別 | 用途 |
| --- | --- |
| `feature` | 新機能の追加 |
| `fix` | 不具合の修正 |
| `docs` | ドキュメントの変更 |
| `refactor` | 動作を変えないコードの改善 |
| `test` | テストの追加・修正 |
| `chore` | ビルド、設定、その他の保守作業 |

## 命名例

```text
feature/REQ-UC003-attendance-time-input
fix/issue-123-login-validation
docs/REQ-UC010-api-specification
refactor/REQ-UC003-attendance-service
test/issue-456-attendance-registration
chore/update-dependencies
```

## 運用ルール

- 作業内容が分かる簡潔な名前にし、曖昧な名前（`work`、`update` など）だけにしない
- 英小文字と数字を基本とし、空白や日本語、アンダースコア（`_`）は使わない
- 1つのブランチでは、原則として1つの要件またはIssueに対応する
- ブランチ名に個人名や機密情報を含めない
- 作業開始時に最新のベースブランチから作成し、作業完了後はPull Requestを通じて変更を共有する
