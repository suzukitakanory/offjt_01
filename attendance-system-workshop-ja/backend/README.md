# Attendance System Backend

Spring Boot製の勤怠管理システムバックエンドAPI

## 技術スタック

- **Spring Boot**: 3.2.0
- **Java**: 21
- **Maven**: 3.8+
- **PostgreSQL**: 15
- **Docker Compose**: データベース自動起動
- **Testcontainers**: 統合テスト用データベース

## フォルダ構成

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/attendance/
│   │   │   ├── AttendanceSystemApplication.java  # メインアプリケーション
│   │   │   ├── controller/                       # REST APIエンドポイント
│   │   │   │   └── AttendanceController.java
│   │   │   ├── service/                          # ビジネスロジック層
│   │   │   │   └── AttendanceService.java
│   │   │   ├── repository/                       # データアクセス層
│   │   │   │   └── AttendanceRecordRepository.java
│   │   │   ├── entity/                           # JPA エンティティ
│   │   │   │   └── AttendanceRecord.java
│   │   │   └── dto/                              # データ転送オブジェクト
│   │   │       ├── AttendanceRequest.java
│   │   │       └── AttendanceResponse.java
│   │   └── resources/
│   │       └── application.yml                   # アプリケーション設定
│   └── test/
│       └── java/com/attendance/                  # テストコード
│           ├── AttendanceSystemApplicationTests.java
│           ├── controller/
│           ├── service/
│           └── repository/
├── init-db/                                      # データベース初期化スクリプト
│   ├── 01-schema.sql
│   └── 02-master-data.sql
├── compose.yaml                                  # Docker Compose設定
└── pom.xml                                       # Maven設定
```

## 開発環境のセットアップ

### 前提条件

- Java 21
- Maven 3.8+
- Docker & Docker Compose

### 依存関係のインストール

```bash
cd backend
mvn clean install
```

### データベースの起動

Spring Boot Docker Composeサポートにより、アプリケーション起動時に自動的にPostgreSQLコンテナが起動します。

### アプリケーションの起動

```bash
mvn spring-boot:run
```

アプリケーションは `http://localhost:8080` で起動します。

### ヘルスチェック

```bash
curl http://localhost:8080/actuator/health
```

## ビルド・実行方法

### ビルド

```bash
mvn clean package
```

### 実行

```bash
mvn spring-boot:run
```

または

```bash
java -jar target/attendance-system-backend-1.0.0-SNAPSHOT.jar
```

## テスト実行方法

### 単体テスト

```bash
mvn test
```

### 統合テスト（Testcontainersを使用）

```bash
mvn verify
```

統合テストでは、Testcontainersが自動的にPostgreSQLコンテナを起動してテストを実行します。

### カバレッジレポート

```bash
mvn test jacoco:report
```

レポートは `target/site/jacoco/index.html` に生成されます。

## API エンドポイント

### ヘルスチェック

- `GET /actuator/health` - システムヘルスチェック

### 勤怠管理

- `POST /api/attendance` - 勤怠レコード登録・更新
- `GET /api/attendance/{employeeId}/{date}` - 特定日の勤怠レコード取得
- `GET /api/attendance/{employeeId}` - 従業員の勤怠レコード一覧取得

詳細は `docs/design/openapi.yaml` を参照してください。

## データベース

### 接続情報（開発環境）

- **Host**: localhost
- **Port**: 5432
- **Database**: attendance_db
- **User**: attendance_user
- **Password**: attendance_password

### スキーマ初期化

`init-db/` ディレクトリ内のSQLスクリプトが、Docker Composeによるデータベース初回起動時に自動実行されます。

## 開発ガイドライン

- コーディング規約: `.github/instructions/backend.instructions.md` を参照
- Java規約: `.github/instructions/java.instructions.md` を参照
- 開発フロー: プロジェクトルートの `CONTRIBUTING.md` を参照

## トラブルシューティング

### データベース接続エラー

Docker Composeが正しく起動しているか確認してください：

```bash
docker ps
```

PostgreSQLコンテナが起動していない場合は、手動で起動できます：

```bash
docker compose up -d
```

### ポート競合

8080ポートが既に使用されている場合は、`application.yml` の `server.port` を変更してください。
