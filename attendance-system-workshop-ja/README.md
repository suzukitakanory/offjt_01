# 勤怠管理システム

Spring Boot と React で構築した従業員向け勤怠管理システムです。

## 主な機能

- **勤怠記録管理**: 出退勤の打刻およびステータス管理（出勤・休暇・病欠 など）
- **休暇管理**: 各種休暇の登録と管理
- **勤務時間入力**: 勤務時間の入力とバリデーション

## ディレクトリ構成

```text
attendance-system-ja/
├── backend/                      # Spring Boot 製 API プロジェクト
│   ├── src/main/java/com/attendance/...   # ドメイン・REST 層
│   ├── init-db/                  # PostgreSQL 初期化 SQL
│   ├── compose.yaml              # 開発用 DB コンテナ定義
├── frontend/                     # React + Vite アプリ
│   ├── src/
│   │   ├── components/
│   │   ├── pages/
│   │   ├── services/             # API クライアント
│   │   └── types/
├── e2e/                          # Playwright + Cucumber テスト
│   ├── features/REQ-UC00X-*.feature
│   ├── step_definitions/
│   └── reports/
├── docs/                         # プロジェクトドキュメント
│   ├── design/
│   │   ├── actors.md
│   │   ├── database-table-definitions.md
│   │   ├── deployment-diagram.md
│   │   ├── domain/sequence/SEQ-REQ-UC00X-*.md
│   │   ├── openapi.yaml
│   │   └── screen-design.md
│   ├── issues/ISS-XXX.md
│   ├── managements/issue-status.md
│   ├── requirements/REQ-UC00X-*.md
│   ├── templates/
│   │   └── issues/ISS-XXX-issue.template.md
│   └── managements/dev-status.md
├── .github/                      # ワークフローと Copilot 指示書
│   ├── copilot-instructions.md
│   ├── instructions/
│   └── prompts/
│       ├── 4_1-create-issue.prompt.md
│       └── 4_2-resolve-issue.prompt.md
└── CONTRIBUTING.md               # 開発ガイド
```

## ドキュメント

- **[開発ガイド](CONTRIBUTING.md)** - セットアップ手順・ワークフロー・コーディング規約
- **[体験ガイド](サンプルアプリケーションの体験ガイド.md)** - AI Native 開発と Issue ベース保守開発の体験手順
- **[要件定義](docs/requirements/)** - REQ-UC00X 系のユーザーストーリー
- **[設計資料](docs/design/)** - `openapi.yaml`、UI/DB/シーケンス図などの詳細設計
- **[Issue 管理](docs/managements/issue-status.md)** - 保守開発向け Issue 一覧とステータス管理
- **[Issue ドキュメント](docs/issues/)** - 個別 Issue の調査・解決記録
- **[テンプレート](docs/templates/)** - 要件・設計・管理系ドキュメントの雛形
- **[開発状況](docs/managements/dev-status.md)** - 進捗サマリ

## 開発の進め方

このリポジトリでは、要件定義から実装までの AI Native 開発フローに加え、追加保守開発向けの Issue ベース運用を扱います。

- **新規開発フロー**: `.github/prompts/1_x` から `.github/prompts/3_1-merge.prompt.md` までを使い、要件定義、設計、実装、マージを段階的に進めます。
- **保守開発フロー**: `.github/prompts/4_1-create-issue.prompt.md` で Issue を起票し、`.github/prompts/4_2-resolve-issue.prompt.md` で docs-first に調査、修正、解決記録を進めます。
- **運用記録**: Issue 一覧は `docs/managements/issue-status.md`、個別の経緯と調査結果は `docs/issues/ISS-XXX.md` に残します。

## サブプロジェクト概要

- **バックエンド (`backend/`)**: Spring Boot 3.2 + Java 21、JPA、Validation、Actuator、Docker Compose による PostgreSQL 連携、Testcontainers ベースの統合テスト。
- **フロントエンド (`frontend/`)**: React 18、TypeScript 5、Vite 5、ESLint による静的解析。`services/` で API クライアントを共通化。
- **E2E テスト (`e2e/`)**: Playwright + Cucumber。`features/REQ-UC00X-*.feature` でマイクロ要件単位のシナリオを管理し、`reports/` 配下に実行ログやスクリーンショットを保存。

## 技術スタック

- **フロントエンド**: React 18 + TypeScript + Vite
- **バックエンド**: Spring Boot 3.2 + JPA + PostgreSQL
- **テスト**: JUnit 5（単体テスト）、Playwright + Cucumber（E2E テスト）
- **開発環境**: Docker Compose（DB 用）、HMR（高速開発）

## クイックスタート

### 環境構築

開発環境のセットアップ（WSL2、Java、Node.js、Docker等）については、**[開発ガイド（CONTRIBUTING.md）](CONTRIBUTING.md)** を参照してください。

### 起動手順

```bash
# 1. リポジトリをクローン
git clone <repository-url>
cd attendance-system-ja

# 2. 依存関係のインストール
cd backend && mvn clean install && cd ..
cd frontend && pnpm install && cd ..
# （E2E テストを実行する場合）
cd e2e && pnpm install && pnpm exec playwright install --with-deps && cd ..

# 3. バックエンドの起動（PostgreSQL は自動で立ち上がります）
cd backend && mvn spring-boot:run

# 4. フロントエンドの起動（別ターミナルで実行）
cd frontend && pnpm dev
```

### アクセス先

- **フロントエンド**: <http://localhost:5173>
- **バックエンド API**: <http://localhost:8080>
- **データベース**: localhost:5432 (attendance_db)
