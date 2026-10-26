# Attendance System Frontend

React + TypeScript製の勤怠管理システムフロントエンド

## 技術スタック

- **React**: 18.2
- **TypeScript**: 5.2
- **Vite**: 5.0（ビルドツール、開発サーバー）
- **pnpm**: 8+（パッケージマネージャー）

## フォルダ構成

```
frontend/
├── src/
│   ├── main.tsx                    # アプリケーションエントリーポイント
│   ├── App.tsx                     # ルートコンポーネント
│   ├── App.css                     # アプリケーションスタイル
│   ├── index.css                   # グローバルスタイル
│   ├── pages/                      # ページコンポーネント
│   │   └── CalendarPage.tsx        # メイン画面（カレンダー表示）
│   ├── components/                 # 再利用可能なコンポーネント
│   │   ├── Calendar.tsx            # カレンダーコンポーネント
│   │   ├── WorkTimeForm.tsx        # 勤務時間入力フォーム
│   │   └── LeaveForm.tsx           # 休暇登録フォーム
│   ├── services/                   # API通信層
│   │   └── attendanceService.ts    # 勤怠API サービス
│   └── types/                      # TypeScript型定義
│       └── attendance.ts           # 勤怠関連型定義
├── index.html                      # HTMLテンプレート
├── vite.config.ts                  # Vite設定
├── tsconfig.json                   # TypeScript設定
├── tsconfig.node.json              # Node用TypeScript設定
└── package.json                    # npm/pnpm設定
```

## 開発環境のセットアップ

### 前提条件

- Node.js 18+
- pnpm 8+

### 依存関係のインストール

```bash
cd frontend
pnpm install
```

## ビルド・実行方法

### 開発サーバーの起動

```bash
pnpm dev
```

アプリケーションは `http://localhost:5173` で起動します。

### プロダクションビルド

```bash
pnpm build
```

ビルド成果物は `dist/` ディレクトリに生成されます。

### ビルド結果のプレビュー

```bash
pnpm preview
```

### Lint

```bash
pnpm lint
```

## テスト実行方法

TODO: テストフレームワークの導入後に記載

## コンポーネント構成

### ページ

- **CalendarPage**: メイン画面。カレンダーと勤怠操作の起点となる画面

### コンポーネント

- **Calendar**: 月次カレンダー表示コンポーネント
- **WorkTimeForm**: 勤務時間入力フォーム
- **LeaveForm**: 休暇登録フォーム

### サービス

- **attendanceService**: バックエンドAPIと通信するサービス層

## API連携

バックエンドAPIとの通信は `src/services/attendanceService.ts` で実装されています。

Viteのプロキシ設定により、開発環境では `/api` へのリクエストが `http://localhost:8080` に転送されます。

## 開発ガイドライン

- コーディング規約: `.github/instructions/frontend.instructions.md` を参照
- TypeScriptスタイル: `.github/instructions/ts-style.instructions.md` を参照
- 開発フロー: プロジェクトルートの `CONTRIBUTING.md` を参照

## トラブルシューティング

### ポート競合

5173ポートが既に使用されている場合は、`vite.config.ts` の `server.port` を変更してください。

### API接続エラー

バックエンドが起動していることを確認してください：

```bash
curl http://localhost:8080/actuator/health
```
