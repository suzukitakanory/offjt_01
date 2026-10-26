---
agent: 'agent'
description: 'REQに基づくローレベル設計の生成'
tools: ['execute/getTerminalOutput', 'execute/runInTerminal', 'read/terminalLastCommand', 'read/terminalSelection', 'edit', 'search', 'search/changes']
---

# Low-Level Design Document Generation for Micro Requirements

## 目的

確認済みのマイクロ要件仕様書に基づいて、マイクロ要件に対応する技術設計ドキュメントを生成・更新する。

## タスクの位置づけ

以下のフェーズおよびステップに該当：

- フェーズ 2: マイクロ要件ごとの作業
  - ステップ 1: 部分設計

## 入力ソース

- システム全体要件仕様書: `docs/requirements/REQ-000-System-Requirements-Specification.md` (全機能の総合要件定義)
- 個別要件書: `docs/requirements/REQ-XXX-*.md`（対象のマイクロ要件のみ）
- E2E テスト仕様: `e2e/features/REQ-XXX-*.feature`（対象のマイクロ要件のみ）
- ハイレベル設計書:
  - アクター一覧: `docs/design/actors.md`
  - デプロイメント図: `docs/design/deployment-diagram.md`
  - 画面設計書: `docs/design/screen-design.md`（ハイレベル部分）
  - テーブル定義書: `docs/design/database-table-definitions.md`（ハイレベル部分）
  - API設計書: `docs/design/openapi.yaml`（ハイレベル部分）
  - メッセージ定義書: `docs/design/message-definition.md`

## 成果物

以下の順序で作成・更新し、各成果物のレビュー完了後に次へ進む:

1. シーケンス図: `docs/design/domain/sequence/SEQ-REQ-XXX-シナリオ名.md`
2. 画面設計書: `docs/design/screen-design.md`（ローレベル設計部分を追記）
3. テーブル定義書: `docs/design/database-table-definitions.md`（個別テーブル詳細を追記）
4. API設計書: `docs/design/openapi.yaml`（リクエスト/レスポンススキーマ詳細を追記）
5. メッセージ定義書: `docs/design/message-definition.md`（必要に応じて更新）
6. 開発状態: `docs/managements/dev-status.md`（対象マイクロ要件のローレベル設計状態を更新）

## 前提条件

1. 対象のマイクロ要件が特定されていること
2. 対象のマイクロ要件に該当する`docs/requirements/REQ-XXX-*.md` が存在すること
3. 対象のマイクロ要件に該当する`e2e/features/REQ-XXX-*.feature` が存在すること

## 実行手順

### ステップ1: 既存要件と設計の確認

1. 対象のマイクロ要件に該当する`docs/requirements/REQ-XXX-*.md` を読み込む
2. 対象のマイクロ要件に該当する`e2e/features/REQ-XXX-*.feature` を読み込む
3. ハイレベル設計書（アクター、デプロイメント図、画面設計、テーブル定義、API設計、メッセージ定義）を読み込む
4. 未作成または更新が必要な設計書を特定

### ステップ2: ローレベル設計書の生成・更新（成果物ごとにレビューを実施）

以下の各成果物を**1つずつ順番に**作成・更新し、各成果物の完了後に**人間によるレビューを受ける**こと。レビュー完了後、次の成果物に進む。

#### 2.1 シーケンス図の作成

1. [Sequence Diagram Template](../../docs/templates/design/sequence-diagram.template.md)を参照して内容を読み込み、新規ファイルにコピーしてプレースホルダを置換する
2. 対象マイクロ要件のユースケースに基づいてシーケンス図を作成する
3. システム全体の設計書（API設計、テーブル定義等）との整合性を確保する
4. ファイル保存先: `docs/design/domain/sequence/SEQ-REQ-XXX-シナリオ名.md`
5. **レビューポイント**: 作成完了後、人間にレビューを依頼し、承認を得てから次へ進む

#### 2.2 画面設計書の更新（ローレベル部分）

1. [Screen Design Template](../../docs/templates/design/screen-design.template.md)を参照して内容を確認する
2. 既存の `docs/design/screen-design.md` のローレベル設計セクションを更新する
3. 対象マイクロ要件に関連する画面の詳細項目（入力フィールド、ボタン、バリデーション等）を追記する
4. 各画面が呼び出すAPIエンドポイントを明記する
5. ハイレベル設計で作成した画面一覧・画面遷移図との整合性を確保する
6. ファイル保存先: `docs/design/screen-design.md`（既存ファイルに追記）
7. **レビューポイント**: 更新完了後、人間にレビューを依頼し、承認を得てから次へ進む

#### 2.3 テーブル定義書の更新（ローレベル部分）

1. [Database Table Definition Template](../../docs/templates/design/database-table-definition.template.md)を参照して内容を確認する
2. 既存の `docs/design/database-table-definitions.md` のローレベル設計セクションを更新する
3. 対象マイクロ要件に関連するテーブルの詳細定義（カラム名、型、制約、インデックス等）を追記する
4. ハイレベル設計で作成したエンティティ一覧・ER図との整合性を確保する
5. ファイル保存先: `docs/design/database-table-definitions.md`（既存ファイルに追記）
6. **レビューポイント**: 更新完了後、人間にレビューを依頼し、承認を得てから次へ進む

#### 2.4 API設計書の更新（ローレベル部分）

1. 既存の `docs/design/openapi.yaml` を読み込む
2. 対象マイクロ要件に関連するエンドポイントのリクエスト/レスポンススキーマ詳細を追記する
3. パラメータ、ボディスキーマ、レスポンスコード、エラーレスポンス等を定義する
4. ハイレベル設計で作成したエンドポイント一覧との整合性を確保する
5. ファイル保存先: `docs/design/openapi.yaml`（既存ファイルに追記）
6. **レビューポイント**: 更新完了後、人間にレビューを依頼し、承認を得てから次へ進む

#### 2.5 メッセージ定義書の更新（必要に応じて）

1. [Message Definition Template](../../docs/templates/design/message-definition.template.md)を参照して内容を確認する
2. 既存の `docs/design/message-definition.md` を読み込む
3. 対象マイクロ要件で新たに必要となるメッセージがあれば追記する
4. 既存のメッセージ定義との整合性を確保する
5. ファイル保存先: `docs/design/message-definition.md`（既存ファイルに追記）
6. **レビューポイント**: 更新完了後、人間にレビューを依頼し、承認を得てから次へ進む

### ステップ3: 開発状態の更新

1. すべての成果物のレビューが完了したら、開発状態ドキュメント`docs/managements/dev-status.md`の「各マイクロ要件の状態」セクションにおいて、対応するマイクロ要件の状態を「ローレベル設計済」に更新する
2. 各成果物の更新状況を記録する

## レビュープロセス

- 各成果物の作成・更新完了後、必ず人間によるレビューを受けること
- レビューで修正が必要な場合は、修正後に再度レビューを受けること
- レビュー承認後、次の成果物の作成・更新に進むこと
- すべての成果物のレビューが完了するまで、次のステップ（実装）には進まないこと
