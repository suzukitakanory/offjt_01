---
agent: 'agent'
description: '全体要件のレビュー'
tools: ['execute/getTerminalOutput', 'execute/runInTerminal', 'read/terminalLastCommand', 'read/terminalSelection', 'edit', 'search', 'search/changes']
---

# Overall Requirement Review

## 目的

ユーザーが作成したシステム全体要件仕様書をレビューし、曖昧な点や不整合を明確化して改善する。

## タスクの位置づけ

以下のフェーズおよびステップに該当：

- フェーズ 1: 全体作業
  - ステップ 1: 要件定義

## 入力ソース

- ユーザーが作成した `docs/requirements/REQ-000-System-Requirements-Specification.md`
- テンプレート `docs/templates/requirements/REQ-000-System-Requirements-Specification.template.md`

## 成果物

- レビュー・更新された `docs/requirements/REQ-000-System-Requirements-Specification.md`
- 更新された `docs/managements/dev-status.md`（要件定義セクション）

## 前提条件

1. ユーザーが `docs/requirements/REQ-000-System-Requirements-Specification.md` を作成済みであること
2. テンプレートファイル `docs/templates/requirements/REQ-000-System-Requirements-Specification.template.md` が参照可能であること

## 実行手順（自動実行）

### ステップ1: 要件仕様書の読み込み

1. `docs/requirements/REQ-000-System-Requirements-Specification.md` を読み込む
2. テンプレート `docs/templates/requirements/REQ-000-System-Requirements-Specification.template.md` を参照し、必須項目が網羅されているか確認する

### ステップ2: レビューと質問

以下の観点でレビューを実施し、必要に応じてユーザーに質問する：

#### 2.1 曖昧な点の明確化

- 用語の定義が不明確な箇所
- 機能要件の振る舞いが具体的でない箇所
- 非機能要件の具体的な数値や基準が欠けている箇所
- ユースケースの前提条件や事後条件が不明確な箇所
- 実装の解釈が複数考えられる記述

#### 2.2 不整合の検出

- 異なるセクション間での記述の矛盾
- ユースケースと機能要件の不一致
- 制約条件と機能要件の矛盾
- 用語の使い方の不統一
- フロー図や仕様記述の論理的な矛盾

#### 2.3 質問の実施

- 発見した曖昧点や不整合について、ユーザーに具体的な質問を行う
- 質問は一度にまとめて行い、ユーザーの回答を待つ
- 質問形式の例：
  - 「○○について、具体的にはどのような動作を想定していますか？」
  - 「△△と××の記述に矛盾があるように見えますが、どちらが正しいですか？」
  - 「□□の基準値はどの程度を想定していますか？」

### ステップ3: 要件仕様書の更新

1. ユーザーからの回答に基づき、`docs/requirements/REQ-000-System-Requirements-Specification.md` を更新する
2. 曖昧だった点を明確な記述に改善する
3. 不整合を解消し、ドキュメント全体の一貫性を確保する
4. 更新箇所をユーザーに報告する

### ステップ4: 開発状態の更新

1. すべての曖昧な点がユーザーとの対話で明確化されたことを確認する
2. すべての不整合が解消されたことを確認する
3. 更新内容が `docs/requirements/REQ-000-System-Requirements-Specification.md` に反映されたことを確認する
4. ドキュメント全体の一貫性が保たれていることを確認する
5. 完了後、開発状態ドキュメント`docs/managements/dev-status.md`の「マイクロ要件共通」>「要件定義」セクションにおいて、レビュー完了を記録すること
