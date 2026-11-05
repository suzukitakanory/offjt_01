---
agent: 'agent'
description: '全体要件からマイクロ要件仕様書の生成'
tools: ['execute/getTerminalOutput', 'execute/runInTerminal', 'read/terminalLastCommand', 'read/terminalSelection', 'edit', 'search', 'search/changes']
---

# Micro Requirement Specification Generation from Overall Requirements

## 目的

システム全体要件仕様書（REQ-000）に記載されたユースケース・機能・ユーザーストーリーごとに、マイクロ要件仕様書およびE2Eテストシナリオを生成する。

## タスクの位置づけ

以下のフェーズおよびステップに該当：

- フェーズ 1: 全体作業
  - ステップ 2: 要件の分割

## 入力ソース

- **システム全体要件仕様書**: `docs/requirements/REQ-000-System-Requirements-Specification.md`
  - このドキュメントに記載された、ID が振られたユースケース/機能/ユーザーストーリー単位でマイクロ要件を切り出す

## 成果物

- **マイクロ要件仕様書**: `docs/requirements/REQ-XXX-ストーリー名.md`
- **E2Eテストシナリオ**: `e2e/features/REQ-XXX-ストーリー名.feature`
- **開発状況管理ドキュメント**: `docs/managements/dev-status.md` の「各マイクロ要件」テーブルへの追加

## 前提条件

1. `docs/requirements/REQ-000-System-Requirements-Specification.md` が存在し、確認済みであること
2. REQ-000 内のユースケース/機能/ユーザーストーリーに一意のID（UC001、UC002 など）が振られていること

## 実行手順（自動実行）

### ステップ1: 全体要件仕様書の確認と検証

1. システム全体要件仕様書 `docs/requirements/REQ-000-System-Requirements-Specification.md` を読み込む
2. ドキュメント内に記載されたユースケース/機能/ユーザーストーリーのリストを抽出する
3. 各項目に一意のID（例: UC001、UC002）が振られているかを確認する
4. もし適切なIDが振られていない場合は作業を中断する
   - ユーザーに以下を報告する:
     - 「REQ-000 にユースケース/機能/ユーザーストーリーのIDが不足しています」
     - 不足している項目を列挙する
     - 「IDを追加してから再度実行してください」と伝える

### ステップ2: マイクロ要件仕様書とE2Eシナリオの生成

1. 以下のテンプレートファイルを読み込む:
   - マイクロ要件仕様書: [Micro Requirement Spec Template](../../docs/templates/requirements/REQ-XXX-micro-requirement.template.md)
   - E2Eテストシナリオ: [E2E Test Scenario Template](../../docs/templates/requirements/REQ-XXX-e2e-test-scenario.template.md)

2. REQ-000 から抽出した各ID（UC001、UC002 など）に対して:
   - テンプレートをコピーし、プレースホルダーを実際の内容に置換
   - ファイル名は `REQ-XXX-ストーリー名.md` および `REQ-XXX-ストーリー名.feature` の形式とする
     - XXX は UC001、UC002 などのIDが入る
   - 保存先:
     - マイクロ要件仕様書: `docs/requirements/`
     - E2Eテストシナリオ: `e2e/features/`

3. 各マイクロ要件は、REQ-000 の対応する項目と整合性を保つこと:
   - 必要に応じて要件の詳細化を行う

4. E2Eテストシナリオには、マイクロ要件の受け入れ条件をもとに具体的なテストケースを記述する

### ステップ3: 開発状況管理ドキュメントの更新

1. `docs/managements/dev-status.md` を開く
2. 「各マイクロ要件」テーブルに、生成した各マイクロ要件を追加する:
   - 要件ID（REQ-XXX）
   - ストーリー名
   - ステータス（未着手）
   - 対応するE2Eシナリオファイル名

## 注意事項

- マイクロ要件はREQ-000に記載されたID単位で1:1で切り出すこと
- 生成したマイクロ要件は、後続のローレベル設計・実装フェーズの入力となるため、明確で実装可能な粒度であることを確認すること
