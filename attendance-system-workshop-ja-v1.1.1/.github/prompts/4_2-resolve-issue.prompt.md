---
agent: "agent"
description: "docs-first アプローチで Issue を解決する"
tools: ["execute", "read", "edit", "search", "web", "agent", "todo"]
---

# Resolve Issue with Docs-First Approach

## 目的

指定された Issue を解決するために、関連ドキュメントを最優先で更新し、その後コード変更とテストを実施する。

## 手順

### 1. 調査

- 指定された Issue ドキュメントを読み、問題の内容を把握する
- 関連するソースコードおよびドキュメントを分析する
- 根本原因を特定する
- 変更を行う前に、影響を受けるドキュメント（要件定義、設計書、API 仕様、シーケンス図、画面設計）をすべて特定する

### 2. ドキュメント更新（最優先）

- Issue のステータスを `"In Progress"` に更新する
- 調査で特定した、影響を受けるすべてのドキュメント（要件定義、設計書、API 仕様、シーケンス図、画面設計）を更新する  
- ドキュメント変更が不要な場合は、Resolution Log に `"Docs unchanged: <reason>"` と明記する
- Resolution Log を記入する:
  - **Investigation**: 確認・分析した内容
  - **Root Cause**: 特定した原因
  - **Solution Applied**: 実施した修正内容
  - **Files Modified**: 変更したファイル一覧の表（更新したドキュメントも含む）

### 3. コード変更

- 必要に応じてコードベースに修正を実装する
- 新規または更新されたコードに合わせて E2E テストシナリオを更新する
- 必要に応じて、不要になったコードやコンポーネントを削除する
- コンパイルエラーがないことを確認する

### 4. テスト実行（必須）

- ソースコードやテストシナリオに変更を加えた場合は、必ずテストを実行して変更の妥当性を確認すること
  - バックエンドのユニットテストを実行する:  
    `cargo test --manifest-path src-tauri/Cargo.toml`
  - E2E テストを実行する:
    1. ビルド: `trunk build`
    2. 別ターミナルでテストサーバーを起動:  
      `Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '<project-path>'; npm run serve:test"`
    3. 少し待ってからテストを実行:  
      `Start-Sleep -Seconds 3; npx playwright test --project=chromium`
  - **いずれかのテストが失敗した場合は、Issue を "Resolved" にしてはいけない**
  - 修正に伴ってテストの更新が必要な場合は、解決作業の一部として必ず更新する

### 5. 関連ドキュメント

- 振る舞いが変わった場合は、設計書（`screen-design.md`、シーケンス図など）を更新する
- インターフェースが変わった場合は、API 仕様を更新する
- 削除したコンポーネントへの参照を削除する

### 6. Issue ステータスの更新

- すべての変更と必要なテストが完了したら、Issue ドキュメントのステータスを `"Resolved"` に更新する
- `docs/managements/issue-status.md` の該当 Issue のステータスも更新する

### 7. 最終チェックリスト（クローズ前に必ず確認）

- [ ] Issue ドキュメントのステータスを更新した（In Progress → Resolved）
- [ ] Resolution Log を記入し、ドキュメント更新内容を記載した
- [ ] 設計書／ドキュメントを更新した、または未変更である理由を明記した
- [ ] コマンドやインターフェース変更がある場合、API 仕様を更新した
- [ ] すべてのテストが成功している（バックエンドのユニットテスト + E2E テスト）、またはテストの実施が不要な理由を明記した
- [ ] Issue ステータスを更新した

## Output

- チャット欄に以下を含む要約を出力する:
  - 根本原因
  - 実施した変更内容
  - 変更したファイル