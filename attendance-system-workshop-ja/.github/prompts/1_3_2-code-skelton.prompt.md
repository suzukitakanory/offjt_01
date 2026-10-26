---
agent: 'agent'
description: 'ハイレベル設計からコードスケルトンの生成'
tools: ['execute/getTerminalOutput', 'execute/runInTerminal', 'read/terminalLastCommand', 'read/terminalSelection', 'edit', 'search', 'search/changes']
---

# High-Level Design to Code Skeleton Generation

## 目的

確認済みのハイレベル設計をもとに、コードスケルトンを生成・更新する。

## タスクの位置づけ

以下のフェーズおよびステップに該当：

- フェーズ 1: 全体作業
  - ステップ 3: ハイレベル設計
    - 項目 2: コードスケルトン生成

## 入力ソース

- システム全体要件仕様書: `docs/requirements/REQ-000-System-Requirements-Specification.md` (全機能の総合要件定義)
- マイクロ要件仕様書: `docs/requirements/REQ-XXX-*.md` (各マイクロ要件)
- E2Eテストシナリオ: `e2e/features/REQ-XXX-*.feature` (各マイクロ要件のテストシナリオ)
- ハイレベル設計ドキュメント: `docs/design/` (アーキテクチャ設計、API設計、データベース設計)

## 成果物

- バックエンドコードスケルトン（`backend/` 内の新規ファイル）
- フロントエンドコードスケルトン（`frontend/` 内の新規ファイル）
- 更新された `docs/managements/dev-status.md`（ハイレベル設計セクション）

## 前提条件

1. 全体の要件に該当する`docs/requirements/REQ-000-System-Requirements-Specification.md` が存在すること
2. マイクロ要件仕様書 `docs/requirements/REQ-XXX-*.md` が存在すること
3. E2Eテストシナリオ `e2e/features/REQ-XXX-*.feature` が存在すること
4. ハイレベル設計ドキュメントが`docs/design/` に存在すること

## 実行手順（自動実行）

### ステップ1: 既存設計の確認

1. システム全体要件仕様書`docs/requirements/REQ-000-System-Requirements-Specification.md` を読み込む
2. マイクロ要件仕様書 `docs/requirements/REQ-XXX-*.md` を読み込む
3. E2Eテストシナリオ `e2e/features/REQ-XXX-*.feature` を読み込む
4. `docs/design/` のハイレベル設計ドキュメントを読み込む
5. 未作成または更新が必要なコードスケルトンを特定
6. 作業対象リストを生成して表示

### ステップ2: コードスケルトンの生成

#### 2.1 バックエンドコードスケルトンの生成

ハイレベル設計（API設計、データベース設計など）に基づき、以下のファイルを生成する：

1. プロジェクト設定ファイル（`pom.xml`, `application.yml`, `compose.yaml`）
2. アプリケーションコード（Controller, Service, Repository, Entity など）
3. テストコード（統合テスト、単体テストの骨格）

バックエンドコード要件：
- ファイルはすべて作成するが、各クラス・関数は空実装とする（関数シグネチャは仮のもので良い）
- 各関数には TODO コメントで実装予定の要件IDと説明を記載
  - 形式例: `// TODO: REQ-XXX - 説明`
- テストコードは常に成功するダミーテスト（例: `assertTrue(true)`）を含める
- コードスケルトンのみでビルドが成功すること
- ビルドコマンドは `CONTRIBUTING.md` など開発ガイドを参照

開発環境・テスト環境の設定：
- 開発環境: Docker Composeを使用したPostgreSQLの自動起動設定（`CONTRIBUTING.md` の「Docker Compose 統合（開発環境）」セクションを参照）
- テスト環境: Testcontainersを使用した統合テスト設定（`CONTRIBUTING.md` の「Testcontainers 統合（テスト環境）」セクションを参照）
- これらの設定により、開発者はローカル環境でデータベースを手動セットアップすることなく、アプリケーションの起動とテストが可能

#### 2.2 フロントエンドコードスケルトンの生成

ハイレベル設計（画面設計など）に基づき、以下のファイルを生成する：

1. プロジェクト設定ファイル（`package.json`, `vite.config.ts`, `tsconfig.json`）
2. アプリケーションコード（コンポーネント、ページ、サービス層など）

**フロントエンドコード要件：**
- 各コンポーネントは TODO コメントを画面上に表示する最低限の実装とする
  - 形式例: `<div>TODO: REQ-XXX - この画面を実装予定</div>`
- 画面遷移やルーティングは考慮不要（後のステップで実装）
- コードスケルトンのみでビルドが成功すること
- ビルドコマンドは `CONTRIBUTING.md` など開発ガイドを参照

#### 2.3 ビルド・テスト実行確認

生成したコードスケルトンがビルド可能であり、テストが実行可能であることを確認する：

**バックエンド:**
- ビルドコマンドを実行してエラーがないこと
- テストコマンドを実行してダミーテストが成功すること
- Docker Composeによるデータベースの自動起動を確認
- Testcontainersによる統合テストの実行を確認

**フロントエンド:**
- ビルドコマンドを実行してエラーがないこと
- 開発サーバーが正常に起動すること

**参照:**
- 具体的なビルド・テストコマンドは `CONTRIBUTING.md` を参照
- バックエンドの環境設定については `CONTRIBUTING.md` の「バックエンド開発のノウハウ」セクションを参照

この段階でテスト実行が成功することで、各種テストに必要な依存関係が正しく設定されていることを確認できる。

#### 2.4 README.md の作成

各サブプロジェクトの README.md を作成し、フォルダ構成とセットアップ方法を記載する：

**バックエンド（`backend/README.md`）:**
- プロジェクト概要
- 技術スタック（Spring Boot 3.2+, Java 21, Maven, PostgreSQL, Docker Compose, Testcontainers）
- フォルダ構成（パッケージ構造、各層の役割）
- 開発環境のセットアップ方法（Docker Composeによる自動起動）
- ビルド・実行方法
- テスト実行方法（Testcontainersの使用を含む）

**フロントエンド（`frontend/README.md`）:**
- プロジェクト概要
- 技術スタック（React 18, TypeScript, Vite, pnpm）
- フォルダ構成（src/ 配下の構造、各コンポーネントの役割）
- ビルド・実行方法
- テスト実行方法

#### 2.5 開発状態ドキュメントの更新

完了後、開発状態ドキュメント（`docs/managements/dev-status.md` など）のハイレベル設計セクションにおいて、コードスケルトン生成済みの要件ID範囲を更新すること。

## コードスケルトンのサンプル

### バックエンド（API エンドポイント層の例）

API 設計に基づき、各エンドポイントに対応する空の関数を持つクラスを生成する。

```java
package com.example.attendance.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {
    
    // TODO: REQ-001 - 出勤記録の登録API
    @PostMapping("/clock-in")
    public Object clockIn() {
        return null;
    }
    
    // TODO: REQ-002 - 退勤記録の登録API
    @PostMapping("/clock-out")
    public Object clockOut() {
        return null;
    }
    
    // TODO: REQ-003 - 勤怠記録の取得API
    @GetMapping("/{employeeId}")
    public Object getAttendance(@PathVariable String employeeId) {
        return null;
    }
}
```

### フロントエンド（画面コンポーネントの例）

画面設計に基づき、各画面に対応する TODO 表示コンポーネントを生成する。

```tsx
import React from 'react';

export const AttendancePage: React.FC = () => {
  return (
    <div>
      <h2>勤怠記録</h2>
      <p>TODO: REQ-004 - 出勤・退勤ボタンを実装予定</p>
      <p>TODO: REQ-005 - 勤怠履歴一覧を実装予定</p>
    </div>
  );
};
```
