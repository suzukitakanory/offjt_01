---
agent: 'agent'  
description: 'プロジェクト規約に従って新規Issueを作成する'  
tools: ['execute', 'read', 'edit', 'search', 'web', 'agent', 'todo']  
---

# Create a New Issue Document

## 目的

ユーザーの入力や既存のドキュメントに基づいて、プロジェクトの規約に従って新しい Issue ドキュメントを作成する。

## 🔁 Workflow（作業手順）

### 1. Issue の特定

- ユーザーからの入力や関連ドキュメントを確認し、Issue の内容を特定します。
- 例: バグ修正、機能追加、ドキュメント更新など

### 2. Issue の採番

- 既存の Issue ステータスドキュメント（`docs/managements/issue-status.md`）を確認し、既存のIssue であるか、新規のIssue であるかを判断します。
- 新規 Issue の場合、次の連番を取得します。
  - フォーマット：`ISS-XXX`
  - 例：`ISS-006`
- 既存 Issue の場合、該当する Issue ID を特定します。

---

### 3. Issue ファイルの作成

- 新規 Issue の場合、`docs/issues/` ディレクトリに新しい Markdown ファイルを作成します。
- ファイル名は以下の形式で命名します。
  - フォーマット：`ISS-XXX.md`
  - 例：`ISS-006.md`
- ベーステンプレート：`docs/templates/issues/ISS-XXX-issue.template.md`

---

### 4. Issue 詳細の記入

テンプレートに従って、各項目を記入してください：
---

### 5. Issue ステータスの初期設定

`docs/managements/issue-status.md` に新規エントリを追加する。

記載内容：

- Issue ID（例：`ISS-006`）
- Title（Issueのタイトル）
- Status（初期値は `Open`）
- Priority（例：`High`）
- Category（例：`Bug`）
- Related Use Cases（関連するユースケースがあれば記載）

---