---
agent: 'agent'
description: '全体要件からハイレベル設計の生成'
tools: ['execute/getTerminalOutput', 'execute/runInTerminal', 'read/terminalLastCommand', 'read/terminalSelection', 'edit', 'search', 'search/changes']
---

# High-Level Design Generation from Overall Requirements

## 目的

全体要件仕様書に基づいて、ハイレベル設計を生成・更新する。

## タスクの位置づけ

以下のフェーズおよびステップに該当：

- フェーズ 1: 全体作業
  - ステップ 3: ハイレベル設計
    - 項目 1: システム設計ドキュメントの作成

## 入力ソース

- システム全体要件仕様書: `docs/requirements/REQ-000-System-Requirements-Specification.md` (全体要件)
- マイクロ要件仕様書: `docs/requirements/REQ-XXX-*.md` (各マイクロ要件)
- E2Eテストシナリオ: `e2e/features/REQ-XXX-*.feature` (各マイクロ要件のテストシナリオ)

## 成果物

以下の順序で作成し、各成果物のレビュー完了後に次へ進む:

1. アクター一覧: `actors.md`
2. デプロイメント図: `deployment-diagram.md`
3. 画面設計書: `screen-design.md`（ハイレベル設計部分のみ）
4. テーブル定義書: `database-table-definitions.md`（ハイレベル設計部分のみ）
5. API設計書: `openapi.yaml`（ハイレベル設計部分のみ）
6. メッセージ定義書: `message-definition.md`（セクション構造のみ）
7. 開発状態: `docs/managements/dev-status.md`（ハイレベル設計セクション更新）

## 前提条件

1. 全体の要件に該当する`docs/requirements/REQ-000-System-Requirements-Specification.md` が存在すること
2. マイクロ要件仕様書 `docs/requirements/REQ-XXX-*.md` が存在すること
3. E2Eテストシナリオ `e2e/features/REQ-XXX-*.feature` が存在すること

## 実行手順

### ステップ1: 既存要件の確認

1. システム全体要件仕様書`docs/requirements/REQ-000-System-Requirements-Specification.md` を読み込む
2. マイクロ要件仕様書 `docs/requirements/REQ-XXX-*.md` を読み込む
3. E2Eテストシナリオ `e2e/features/REQ-XXX-*.feature` を読み込む

### ステップ2: ハイレベル設計書の生成（成果物ごとにレビューを実施）

以下の各成果物を**1つずつ順番に**作成し、各成果物の作成完了後に**人間によるレビューを受ける**こと。レビュー完了後、次の成果物に進む。

#### 2.1 アクター一覧の作成

1. [Actors Template](../../docs/templates/design/actors.template.md)を参照して内容を読み込み、新規ファイルにコピーしてプレースホルダを置換する
2. システムに関与するすべてのアクターを定義する
3. ファイル保存先: `docs/design/actors.md`
4. **レビューポイント**: 作成完了後、人間にレビューを依頼し、承認を得てから次へ進む

#### 2.2 デプロイメント図の作成

1. [Deployment Diagram Template](../../docs/templates/design/deployment-diagram.template.md)を参照して内容を読み込み、新規ファイルにコピーしてプレースホルダを置換する
2. システムのデプロイメント構成を定義する
3. ファイル保存先: `docs/design/deployment-diagram.md`
4. **レビューポイント**: 作成完了後、人間にレビューを依頼し、承認を得てから次へ進む

#### 2.3 画面設計書の作成（ハイレベル部分）

1. [Screen Design Template](../../docs/templates/design/screen-design.template.md)を参照して内容を読み込み、新規ファイルにコピーしてプレースホルダを置換する
2. ハイレベル設計として画面一覧と画面遷移図を定義する
3. 画面一覧には関連要件IDを含めること（システム要件のユースケースIDを記載）
4. 画面項目の詳細と呼び出すAPIはローレベル設計で追加するためこの段階ではドキュメントの目次のみ作成する。ローレベル設計で具体化する旨を TODO コメントとして記載する
5. ファイル保存先: `docs/design/screen-design.md`
6. **レビューポイント**: 作成完了後、人間にレビューを依頼し、承認を得てから次へ進む

#### 2.4 テーブル定義書の作成（ハイレベル部分）

1. [Database Table Definition Template](../../docs/templates/design/database-table-definition.template.md)を参照して内容を読み込み、新規ファイルにコピーしてプレースホルダを置換する
2. ハイレベル設計部分（エンティティ一覧、ER図）のみを作成・更新する
3. ローレベル設計部分（個別テーブルの詳細定義）は後続のローレベル設計フェーズで補完するため段階ではドキュメントの目次のみ作成する。ローレベル設計で具体化する旨を TODO コメントとして記載する
4. ファイル保存先: `docs/design/database-table-definitions.md`
5. **レビューポイント**: 作成完了後、人間にレビューを依頼し、承認を得てから次へ進む

#### 2.5 API設計書の作成（ハイレベル部分）

1. OpenAPI 3.0 仕様テンプレート（YAML形式）に基づいて作成する
2. 全体部分とリクエストマッピング（エンドポイント一覧）まで作成する
3. 各エンドポイントには関連ユースケースIDをコメントで記載すること
4. パラメータ名とHTTPメソッド、パスは定義するが、リクエスト/レスポンスの詳細スキーマは後続のローレベル設計フェーズで補完するため、この段階では作成しない。 ローレベル設計で具体化する旨を TODO コメントとして記載する。
5. ファイル保存先: `docs/design/openapi.yaml`
6. **レビューポイント**: 作成完了後、人間にレビューを依頼し、承認を得てから次へ進む

#### 2.6 メッセージ定義書の作成

1. [Message Definition Template](../../docs/templates/design/message-definition.template.md)を参照して内容を読み込み、新規ファイルにコピーしてプレースホルダを置換する
2. テンプレートに基づいてセクション構造を作成する
3. カテゴリ一覧はシステム要件から抽出し定義する
4. メッセージ詳細はローレベル設計フェーズで追加するため、この段階では作成しない
5. ファイル保存先: `docs/design/message-definition.md`
6. **レビューポイント**: 作成完了後、人間にレビューを依頼し、承認を得てから次へ進む

### ステップ3: 開発状態の更新

1. すべての成果物のレビューが完了したら、開発状態ドキュメント`docs/managements/dev-status.md`の「マイクロ要件共通」>「ハイレベル設計」セクションにおいて、反映済みの要件ID範囲を更新する
2. 各成果物の作成状況を記録する

## レビュープロセス

- 各成果物の作成完了後、必ず人間によるレビューを受けること
- レビューで修正が必要な場合は、修正後に再度レビューを受けること
- レビュー承認後、次の成果物の作成に進むこと
- すべての成果物のレビューが完了するまで、次のステップ（コードスケルトン生成）には進まないこと
