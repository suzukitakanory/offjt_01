# 開発ガイド

勤怠管理システムの開発とコントリビュート方法

## 目次

- [開発環境の準備](#開発環境の準備)
- [開発フロー](#開発フロー)
- [Issue ベース保守開発](#issue-ベース保守開発)
- [ビルドとテスト](#ビルドとテスト)
- [バックエンド開発のノウハウ](#バックエンド開発のノウハウ)

## 開発環境の準備

### WSL2 環境のセットアップ

#### 1. WSL2 の導入

```bash
# WSL2 をデフォルトに設定
wsl --set-default-version 2

# Ubuntu をインストール
wsl --install -d Ubuntu

# WSL を最新バージョンに更新（日本語パス問題回避のため 2.5.9 以上推奨）
wsl --update --pre-release
```

#### WSL2 設定ファイルの作成

Windows のユーザーフォルダー（`C:\Users\<YourUsername>`）配下に `.wslconfig`
を作成します。

```ini
[wsl2]
nestedVirtualization=true
networkingMode=mirrored
autoProxy=false

[experimental]
hostAddressLoopback=false
```

設定を反映するため WSL2 を再起動します。

```bash
wsl --shutdown
```

#### 2. ZScaler 環境向け証明書設定

```bash
# ZScaler の証明書をシステム CA ストアに追加（必要に応じてサポートに問い合わせ）
# 環境に合わせて証明書パスを修正
echo "export SSL_CERT_FILE=<Path to Certificate>/ZscalerRootCA.pem" >> $HOME/.bashrc
echo "export NODE_EXTRA_CA_CERTS=<Path to Certificate>/bundle.pem" >> $HOME/.bashrc
source $HOME/.bashrc
```

### 必須ツール

- WSL2（Ubuntu）
- Java 21、Node.js 18 以上、pnpm 8 以上、Maven 3.8 以上、Git 2.30 以上、Docker &
  Docker Compose
- VS Code + Remote-WSL 拡張機能

### ツールのインストール

#### 1. 基本ツール

```bash
# パッケージ更新
sudo apt update && sudo apt upgrade -y

# Java 21 をインストール
sudo apt install openjdk-21-jdk -y

# Maven をインストール
sudo apt install maven -y

# Git をインストール（通常は既に導入済み）
sudo apt install git -y
```

#### 2. Node.js と pnpm

```bash
# nvm を利用して Node.js をインストール
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.0/install.sh | bash
source ~/.bashrc
nvm install 18
nvm use 18

# pnpm をインストール
npm install -g pnpm

# Vite をグローバルインストール
npm install -g vite
```

#### 3. Docker

```bash
# Docker 公式リポジトリを追加し Docker をインストール
# 詳細: https://docs.docker.com/engine/install/ubuntu/
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg

echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

sudo apt update
# 最新版（29系）にバグがあるため、バージョンを 28.5.2 に固定
sudo apt-get install -y \
  docker-ce=5:28.5.2-1~ubuntu.24.04~noble \
  docker-ce-cli=5:28.5.2-1~ubuntu.24.04~noble \
  containerd.io=1.7.27-1 \
  docker-buildx-plugin docker-compose-plugin

# sudo なしで実行するため Docker グループに追加
sudo usermod -aG docker $USER
newgrp docker
```

### 初期セットアップ

```bash
# 1. リポジトリをクローン
git clone <repository-url>
cd attendance-system-ja

# 2. 依存関係をインストール
pnpm install
cd backend && mvn clean install && cd ..

# 3. E2E テスト環境の初期設定（初回のみ）
cd e2e && pnpm exec playwright install --with-deps && cd ..
```

### システムの起動

```bash
# バックエンドを起動（PostgreSQL は自動起動）
cd backend && mvn spring-boot:run &

# フロントエンドを起動
cd frontend && pnpm dev
```

### VS Code 開発環境

```bash
# VS Code を WSL2 に接続
# 1. VS Code に Remote-WSL 拡張機能をインストール
# 2. Ubuntu（WSL2）側でプロジェクトディレクトリへ移動
# 3. `code .` で VS Code を起動
# 4. GitHub Copilot などの拡張機能が利用可能
```

## 開発フロー

### 基本的な開発プロセス

1. **要件定義**: `docs/requirements/` 配下にユーザーストーリーを作成
2. **実装**: フロントエンド／バックエンドを修正しテストを実行
3. **統合テスト**: E2E テストで機能全体を確認

AI Native 開発の標準フローは `.github/prompts/1_1-entire-requirement.prompt.md` から `.github/prompts/3_1-merge.prompt.md` までで段階的に進めます。全体像は `docs/managements/dev-status.md`、体験用の導線は `サンプルアプリケーションの体験ガイド.md` を参照してください。

### 1. ユーザーストーリーの作成

```bash
# 要件ドキュメントを作成
cd docs/requirements
# REQ-XXX-FeatureName.md の形式でファイルを作成
# ユーザーストーリー、受け入れ条件、API 仕様を記載
```

### 2. ブランチの作成

```bash
git checkout main && git pull origin main
git checkout -b feature/REQ-FeatureName
```

### 3. 実装とテスト

```bash
# バックエンドの実装とテスト
cd backend
mvn test

# フロントエンドの実装とテスト
cd frontend
pnpm build && pnpm lint
```

### 4. E2E テストの実行

```bash
# システム全体の振る舞いを確認
cd e2e
pnpm test
```

### 5. プルリクエストの作成

```bash
git add . && git commit -m "feat: REQ-XXX - Feature Description"
gh pr create --title "feat: REQ-XXX - Feature Description" --body "Implementation details"
```

## Issue ベース保守開発

要件起点の新規開発に加えて、追加保守開発では Issue ベースでの docs-first 運用を行います。

### Issue 起票

1. `docs/managements/issue-status.md` を確認し、既存 Issue か新規 Issue かを判定する
2. 新規 Issue の場合は `docs/templates/issues/ISS-XXX-issue.template.md` をベースに `docs/issues/ISS-XXX.md` を作成する
3. `.github/prompts/4_1-create-issue.prompt.md` を使い、Issue 本文とステータス一覧を同時に更新する

### Issue 解決

1. `.github/prompts/4_2-resolve-issue.prompt.md` を使い、関連ドキュメントを最優先で調査する
2. 必要な設計書、要件、API、E2E シナリオ、コードを更新する
3. `docs/issues/ISS-XXX.md` の Resolution Log に調査内容、原因、対応内容、変更ファイルを記録する
4. `docs/managements/issue-status.md` のステータスを `Open` から `In Progress`、`Resolved` へ更新する

### 運用ルール

- 振る舞いが変わる場合は、コード変更前に関連ドキュメントを更新する
- ドキュメント変更が不要な場合も、Issue の Resolution Log に理由を残す
- コードやテストを更新した場合は、該当する単体テストや E2E テストを実行する

## ビルドとテスト

### 開発サーバーの起動

```bash
# 同時に起動
cd backend && mvn spring-boot:run &
cd frontend && pnpm dev &
```

### テストの実行

```bash
# バックエンド
cd backend && mvn verify

# フロントエンド
cd frontend && pnpm build && pnpm lint

# E2E
cd e2e && pnpm test
```

## バックエンド開発のノウハウ

### Docker Compose 統合（開発環境）

本プロジェクトでは、Spring Boot Docker Compose サポートを利用して、開発環境で PostgreSQL データベースコンテナを自動起動・停止します。

#### 1. Maven 依存関係の追加

`pom.xml`に Spring Boot Docker Compose の依存関係を追加：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-docker-compose</artifactId>
    <scope>runtime</scope>
    <optional>true</optional>
</dependency>
```

#### 2. compose.yaml の作成

プロジェクトルート（通常は`backend/compose.yaml`）に作成：

```yaml
services:
  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: your_db
      POSTGRES_USER: your_user
      POSTGRES_PASSWORD: your_password
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./init-db:/docker-entrypoint-initdb.d  # 初期化SQLスクリプト（オプション）
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U your_user -d your_db"]
      interval: 10s
      timeout: 5s
      retries: 5

volumes:
  postgres_data:
```

#### 3. application.yml の設定

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/your_db
    username: your_user
    password: your_password

  docker:
    compose:
      enabled: true
      file: compose.yaml
      lifecycle-management: start_and_stop
```

#### 4. 使い方

Spring Boot アプリを起動するだけ：

```bash
mvn spring-boot:run
```

**動作：**
1. Spring Boot が `compose.yaml` を検出
2. PostgreSQL コンテナを起動（未起動の場合）
3. ヘルスチェック完了を待機
4. データソースを自動構成
5. アプリ終了時にコンテナを停止

#### 5. データベース初期化

開発環境では`init-db`ディレクトリで SQL スクリプトを配置：

```
backend/
├── compose.yaml
└── init-db/
    ├── 01-schema.sql
    └── 02-master-data.sql
```

`compose.yaml`でマウント：
```yaml
volumes:
  - ./init-db:/docker-entrypoint-initdb.d
```

### Testcontainers 統合（テスト環境）

本プロジェクトでは、Testcontainers を利用してバックエンド統合テスト用の PostgreSQL コンテナを起動します。

#### 1. Maven 依存関係の追加

`pom.xml`に Testcontainers 関連の依存関係を追加：

```xml
<dependencies>
    <!-- Testcontainers for Spring Boot -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-testcontainers</artifactId>
        <scope>test</scope>
    </dependency>

    <!-- PostgreSQL Testcontainer -->
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>postgresql</artifactId>
        <scope>test</scope>
    </dependency>

    <!-- JUnit 5 integration -->
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>junit-jupiter</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

#### 2. 統合テストクラスの作成

`@ServiceConnection`を使ったアプローチ（Spring Boot 3.1+）：

```java
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
@Rollback
class YourApplicationTests {

    @Container
    @ServiceConnection  // ⭐ 重要: データソースプロパティを自動構成
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void testDatabaseConnection() throws Exception {
        mockMvc.perform(get("/api/your-endpoint"))
                .andExpect(status().isOk());
    }
}
```

**重要なアノテーション:**

- `@Testcontainers`: Testcontainers のライフサイクル管理を有効化
- `@Container`: 管理対象のコンテナとしてマーク
- `@ServiceConnection`: コンテナから Spring Boot のデータソースプロパティを自動設定
- `@Transactional` + `@Rollback`: 各テストがトランザクション内で実行され、ロールバックされる
