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

## 共通ルール

- 作業対象は、ユーザーが明示したマイクロ要件ID 1件に限定する。対象IDが未指定または複数に解釈できる場合は、作業を開始せずユーザーに確認する。
- 要件書、E2E仕様、ハイレベル設計、ローレベル設計の間に実装範囲や期待動作の矛盾がある場合、参照順だけで解決を推測しない。矛盾点と影響を提示して停止し、ユーザーの判断を得る。承認済みの対象外・保留範囲は明示して維持する。
- 編集前にリポジトリルートと `git status --short` を確認する。既存の未コミット変更を破棄・上書きしない。変更対象ファイルに既存変更がある場合は内容を確認して統合し、競合や安全に統合できない変更があれば停止して確認する。対象要件と直接関係しない変更は行わない。
- このプロンプトの `tools` front matter は対応する Copilot エージェント環境での利用制約を示す。別環境で使う場合は、その環境で許可された等価ツールのみを利用する。必要な操作を許可ツールで実行できない場合は停止して報告し、未許可ツールで代替しない。
- 実装前に、変更対象に適用される `.github/instructions/` の指示書を読み、従う。

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

1. ユーザーが対象のマイクロ要件IDを1件指定していること
2. 対象のマイクロ要件に該当する`docs/requirements/REQ-XXX-*.md` が存在すること
3. 対象のマイクロ要件に該当する`e2e/features/REQ-XXX-*.feature` が存在すること
4. ハイレベル設計書（アクター、デプロイメント図、画面設計、テーブル定義、API設計、メッセージ定義）が存在すること
5. 対象のマイクロ要件に対応するローレベル設計書が完成していること
   - シーケンス図: `docs/design/domain/sequence/SEQ-REQ-XXX-シナリオ名.md`
   - 画面設計書のローレベル部分: `docs/design/screen-design.md`
   - テーブル定義書のローレベル部分: `docs/design/database-table-definitions.md`
   - API設計書のローレベル部分: `docs/design/openapi.yaml`
6. 設計成果物に未解決の矛盾がなく、必要なレビュー・承認が完了していること

前提条件が満たされない、必要な成果物が不足している、または承認状況・作業範囲が不明な場合は、実装に着手せず不足事項を提示して確認する。

## 実行手順（自動実行）

### ステップ1: 既存設計の確認

0. リポジトリルートと作業前の `git status --short` を確認し、既存変更を記録する
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
5. 変更対象に適用される `.github/instructions/` を読み、未作成または更新が必要なコードおよびテストコードを特定する
6. 要件・E2E・設計間の不整合、保留事項、作業対象外を照合する。不整合が実装判断に影響する場合は、推測で進めずユーザーに確認する
7. 作業対象リストを表示する。対象ID、変更予定ファイル、対象シナリオ、実行予定テスト、保留・対象外項目を含める

### ステップ2: ソースコードおよびテストコード生成（マイクロ要件ごと）

1. バックエンドおよびフロントエンドのコードを実装・更新する
   - ハイレベル設計書およびローレベル設計書（特にシーケンス図、API設計、テーブル定義）を参照して実装すること
   - 承認済みの作業範囲外の要件・API・画面は実装しない
2. 単体テストコードを実装・更新する
   - ローレベル設計書に基づいて、各機能の単体テストを実装すること
3. E2Eテストコードを実装・更新する
   - E2Eテストシナリオ（`e2e/features/REQ-XXX-*.feature`）を参照して実装すること
   - シナリオに設計上必要なUI操作が欠けている場合、操作を暗黙に推測せず、受け入れ条件を変えない範囲の手順補足案を作業対象リストに示す。E2E仕様の変更を伴う場合は、ユーザー承認を得てから変更する
4. 実装およびテストコードの保存先は次を既定とする（なければ作成する）  
   - バックエンド: `backend/src/main/java/`
   - バックエンド単体テスト: `backend/src/test/java/`
   - フロントエンド: `frontend/src/`
   - E2Eテストコード: `e2e/features/step_definitions/`
5. 生成時は「入力ソース」の参照順に基づき整合性を確保すること
   - システム全体要件仕様書 → 個別要件書 → ハイレベル設計書 → ローレベル設計書の順

### ステップ3: テストと開発状態の更新

1. プロジェクトのREADME・開発手順を確認し、専用のテスト用DBと必要なサービスを安全に起動する。共有・本番データをテストに使用しない
2. 次のテストを実行する。リポジトリで指定された別の標準コマンドがある場合はそのコマンドを優先し、実行したコマンドを記録する
   - バックエンド単体・結合テスト: `cd backend && mvn test`
   - フロントエンド型チェック・ビルド・Lint: `cd frontend && pnpm build && pnpm lint`
   - 対象要件のE2E全シナリオ: `cd e2e && pnpm exec cucumber-js --config cucumber.config.js --tags @REQ-XXX`
3. E2Eはdry-runではなく実行し、対象要件の全シナリオが成功したことを確認する。undefined、pending、skipped、失敗がある場合は成功扱いにしない
4. 失敗時は今回の変更に起因する不具合だけを修正して関連テストを再実行する。環境不足または変更前からの失敗と判断される場合は、ログと未実施・失敗範囲を報告して停止する。無関係なコード変更や失敗の隠蔽で完了扱いにしない
5. 「実装・試験完了」へ更新できるのは、承認済みの作業範囲内の要件・機能・テストがすべて完了し、上記テストがすべて成功し、未解決の保留事項がない場合に限る。対象外・保留事項が残る場合はその内容を `dev-status.md` に記録し、ユーザーが要件範囲の変更を明示承認していない限り完了状態にしない
6. 完了条件を満たした場合のみ、`docs/managements/dev-status.md` の「各マイクロ要件の状態」を「実装・試験完了」に更新し、変更対象と実行したテストコマンド・結果を記録する
