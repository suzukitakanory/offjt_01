---
applyTo: "backend/**"
---

# バックエンドコーディング規約

## 技術スタック

- **Spring Boot**: 3.2+
- **Java**: 21
- **ビルドツール**: Maven
- **データベース**: PostgreSQL 15
- **開発環境**: Docker Compose（データベース自動起動）
- **テスト**: JUnit 5, Testcontainers, MockMvc
- **ユーティリティ**: Lombok

## アーキテクチャ

レイヤードアーキテクチャを採用：

```
Controller層 → Service層 → Repository層 → Entity層
```

## パッケージ構成

```
com.example.attendance/
├── controller/       # REST APIエンドポイント
├── service/          # ビジネスロジック
├── repository/       # データアクセス層（Spring Data JPA）
├── entity/           # JPA Entity
├── dto/              # Data Transfer Object（リクエスト/レスポンス）
├── exception/        # カスタム例外クラス
└── config/           # Spring設定クラス
```

## 命名規約

### Java

- **クラス名**: パスカルケース（`AttendanceController`, `AttendanceService`）
- **メソッド名**: キャメルケース（`createAttendance`, `findByEmployeeId`）
- **変数名**: キャメルケース（`employeeId`, `attendanceRecord`）
- **定数**: スネークケース大文字（`MAX_RETRY_COUNT`, `DEFAULT_PAGE_SIZE`）
- **パッケージ名**: すべて小文字（`controller`, `service`, `repository`）

### データベース

- **テーブル名**: スネークケース小文字、複数形（`attendance_records`, `employees`）
- **カラム名**: スネークケース小文字（`employee_id`, `created_at`）
- **主キー**: `id`（Long型、自動採番）
- **外部キー**: `{参照先テーブル単数形}_id`（例: `employee_id`）

### REST API

- **エンドポイント**: ケバブケース、名詞の複数形（`/api/attendance-records`）
- **HTTPメソッド**: 
  - GET: 取得
  - POST: 作成
  - PUT: 更新
  - DELETE: 削除

