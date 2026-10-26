# GitHub Copilot 向け指示書

## プロジェクト構成

```text
attendance-system-ja/
├── backend/              # Spring Boot製API
├── frontend/             # React + TypeScriptアプリケーション
├── e2e/                  # E2Eテスト (Playwright + Cucumber)
│   └── features/         # .featureファイル (E2Eテストシナリオ)
├── docs/                 # プロジェクトドキュメント
│   ├── design/           # 設計ドキュメント (アーキテクチャ、API、データベース、画面など)
│   ├── requirements/     # 要件ドキュメント
│   │   ├── REQ-000-System-Requirements-Specification.md  # 全体要件仕様書
│   │   └── REQ-XXX-*.md  # マイクロ要件仕様書
│   ├── templates/        # ドキュメントテンプレート
│   │   ├── design/       # 設計書テンプレート
│   │   ├── requirements/ # 要件書テンプレート
│   │   └── management/   # 管理ドキュメントテンプレート
│   └── managements/dev-status.md     # 開発状況管理ドキュメント
├── .github/
│   ├── prompts/          # 開発ワークフロー用プロンプト (本指示書の詳細版)
│   ├── instructions/     # 言語・領域別のコーディング指示書
│   └── workflows/        # CI/CDワークフロー
├── CONTRIBUTING.md       # 開発環境セットアップと開発手順
└── README.md             # プロジェクト概要とクイックスタート
```

## 機能開発ワークフロー概要

開発は3つのフェーズで進行します。各ステップの詳細は `.github/prompts/` 配下のプロンプトファイルを参照してください。

### フェーズ 1: 全体作業

システム全体の要件定義と設計を行い、個別の機能（マイクロ要件）に分割します。

1. **全体要件定義** (1_1-entire-requirement.prompt.md)
   - システム全体要件仕様書をレビュー・作成
   - 成果物: `docs/requirements/REQ-000-System-Requirements-Specification.md`

2. **要件の分割** (1_2-micro-requirement.prompt.md)
   - マイクロ要件仕様書とE2Eシナリオの作成
   - 成果物: `docs/requirements/REQ-XXX-*.md`, `e2e/features/REQ-XXX-*.feature`

3. **ハイレベル設計** (1_3_1-highlevel-design.prompt.md, 1_3_2-code-skelton.prompt.md)
   - アーキテクチャ、API、データベース、画面の全体設計
   - コードスケルトンの生成
   - 成果物: `docs/design/` 配下の設計ドキュメント、`backend/`, `frontend/` のスケルトンコード

### フェーズ 2: マイクロ要件ごとの作業

個別機能ごとに設計・実装・テストを実施します。

1. **ローレベル設計** (2_1-lowlevel-design.prompt.md)
   - マイクロ要件に対応する詳細設計 (シーケンス図、画面詳細など)
   - 成果物: `docs/design/` 配下の詳細設計ドキュメント

2. **製造・試験** (2_2-implement-test.prompt.md)
   - フロントエンド/バックエンドの実装
   - 単体テスト、E2Eテストの実装と実行
   - 成果物: 実装コード、テストコード

### フェーズ 3: 全体作業

1. **結合作業** (3_1-merge.prompt.md)
   - PRレビューとマージ
   - 結合テストの実行

## 重要なガイドライン

- **開発フロー**: `CONTRIBUTING.md` に開発環境のセットアップと開発手順を記載
- **ワークフロー詳細**: `.github/prompts/` 配下のプロンプトファイルに各ステップの詳細手順を記載
- **テンプレート**: `docs/templates/` 配下にドキュメント作成用テンプレートを配置
- **コーディング規約**: `.github/instructions/` 配下に言語・領域別の指示書を配置
- **進捗管理**: `docs/managements/dev-status.md` に開発状況を随時記録
- **整合性**: 既存のコードベース構造、API設計、データベース設計との整合性を常に確保

## クイックリファレンス

- **環境構築・開発手順**: [CONTRIBUTING.md](../CONTRIBUTING.md)
- **プロジェクト概要**: [README.md](../README.md)
- **開発ワークフロー詳細**: [.github/prompts/](prompts/)
- **要件ドキュメント**: [docs/requirements/](../docs/requirements/)
- **設計ドキュメント**: [docs/design/](../docs/design/)
- **コーディング指示書**: [.github/instructions/](instructions/)