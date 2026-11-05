---
agent: 'agent'
description: 'マイクロ要件ごとの製造・試験の実施'
tools: ['execute/getTerminalOutput', 'execute/runInTerminal', 'read/terminalLastCommand', 'read/terminalSelection', 'edit', 'search', 'search/changes']
---

# Implementation and Testing for Micro Requirements

## 目的

マイクロ要件に対応する技術設計ドキュメントに基づいて、コードの実装および単体テスト、E2E テストを実施する。

## タスクの位置づけ

以下のフェーズおよびステップに該当：

- フェーズ 2: マイクロ要件ごとの作業
  - ステップ 2: 製造・試験

## 入力ソース

- システム全体要件仕様書: `docs/requirements/REQ-000-System-Requirements-Specification.md` (全機能の総合要件定義)
- 個別要件書: `docs/requirements/REQ-XXX-*.md`（対象のマイクロ要件のみ）
- E2E テスト仕様: `e2e/features/REQ-XXX-*.feature`（対象のマイクロ要件のみ）
- ハイレベル設計書:
  - アクター一覧: `docs/design/actors.md`
  - デプロイメント図: `docs/design/deployment-diagram.md`
  - 画面設計書: `docs/design/screen-design.md`
  - テーブル定義書: `docs/design/database-table-definitions.md`
  - API設計書: `docs/design/openapi.yaml`
  - メッセージ定義書: `docs/design/message-definition.md`
- ローレベル設計書（対象のマイクロ要件に対応）:
  - シーケンス図: `docs/design/domain/sequence/SEQ-REQ-XXX-シナリオ名.md`
  - 画面設計書: `docs/design/screen-design.md`（ローレベル設計部分）
  - テーブル定義書: `docs/design/database-table-definitions.md`（個別テーブル詳細）
  - API設計書: `docs/design/openapi.yaml`（リクエスト/レスポンススキーマ詳細）
  - メッセージ定義書: `docs/design/message-definition.md`（必要に応じて）

## 成果物

- バックエンドコード: `backend/src/` 内の新規・更新ファイル
- フロントエンドコード: `frontend/src/` 内の新規・更新ファイル
- 単体テストコード: `backend/src/test/` 内の新規・更新ファイル
- E2Eテストコード: `e2e/features/step_definitions/` 内の新規・更新ファイル
- 更新された `docs/managements/dev-status.md`（各マイクロ要件の状態）

## 前提条件

1. 対象のマイクロ要件が特定されていること
2. 対象のマイクロ要件に該当する`docs/requirements/REQ-XXX-*.md` が存在すること
3. 対象のマイクロ要件に該当する`e2e/features/REQ-XXX-*.feature` が存在すること
4. ハイレベル設計書（アクター、デプロイメント図、画面設計、テーブル定義、API設計、メッセージ定義）が存在すること
5. 対象のマイクロ要件に対応するローレベル設計書が完成していること
   - シーケンス図: `docs/design/domain/sequence/SEQ-REQ-XXX-シナリオ名.md`
   - 画面設計書のローレベル部分: `docs/design/screen-design.md`
   - テーブル定義書のローレベル部分: `docs/design/database-table-definitions.md`
   - API設計書のローレベル部分: `docs/design/openapi.yaml`

## 実行手順（自動実行）

### ステップ1: 既存設計の確認

1. 対象のマイクロ要件に該当する`docs/requirements/REQ-XXX-*.md` を読み込む
2. 対象のマイクロ要件に該当する`e2e/features/REQ-XXX-*.feature` を読み込む
3. ハイレベル設計書を読み込む
   - アクター一覧: `docs/design/actors.md`
   - デプロイメント図: `docs/design/deployment-diagram.md`
   - 画面設計書: `docs/design/screen-design.md`
   - テーブル定義書: `docs/design/database-table-definitions.md`
   - API設計書: `docs/design/openapi.yaml`
   - メッセージ定義書: `docs/design/message-definition.md`
4. ローレベル設計書を読み込む
   - シーケンス図: `docs/design/domain/sequence/SEQ-REQ-XXX-シナリオ名.md`
   - 画面設計書のローレベル部分: `docs/design/screen-design.md`
   - テーブル定義書のローレベル部分: `docs/design/database-table-definitions.md`
   - API設計書のローレベル部分: `docs/design/openapi.yaml`
   - メッセージ定義書: `docs/design/message-definition.md`（必要に応じて）
5. 未作成または更新が必要なコードおよびテストコードを特定
6. 作業対象リストを生成して表示

### ステップ2: ソースコードおよびテストコード生成（マイクロ要件ごと）

1. バックエンドおよびフロントエンドのコードを実装・更新する
   - ハイレベル設計書およびローレベル設計書（特にシーケンス図、API設計、テーブル定義）を参照して実装すること
2. 単体テストコードを実装・更新する
   - ローレベル設計書に基づいて、各機能の単体テストを実装すること
3. E2Eテストコードを実装・更新する
   - E2Eテストシナリオ（`e2e/features/REQ-XXX-*.feature`）を参照して実装すること
4. 実装およびテストコードの保存先は次を既定とする（なければ作成する）  
   - バックエンド: `backend/src/main/java/`
   - バックエンド単体テスト: `backend/src/test/java/`
   - フロントエンド: `frontend/src/`
   - E2Eテストコード: `e2e/features/step_definitions/`
5. 生成時は「入力ソース」の参照順に基づき整合性を確保すること
   - システム全体要件仕様書 → 個別要件書 → ハイレベル設計書 → ローレベル設計書の順

### ステップ3: テストと開発状態の更新

1. 実装したコードに対して単体テストを実行し、全てのテストが成功することを確認する
2. E2Eテストを実行し、全てのシナリオが成功することを確認する
3. 不具合が発生した場合は、コードを修正し、再度テストを実行する
4. 完了後、開発状態ドキュメント`docs/managements/dev-status.md`の「各マイクロ要件の状態」セクションにおいて、対応するマイクロ要件の状態を「実装・試験完了」に更新すること