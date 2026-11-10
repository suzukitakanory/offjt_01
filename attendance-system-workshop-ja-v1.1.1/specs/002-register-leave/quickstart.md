# Quickstart: 休暇登録の検証

実装後に、休暇登録が仕様どおり動くことを確認する手順。API の詳細は [contracts/leave-api.yaml](./contracts/leave-api.yaml)、データの制約は [data-model.md](./data-model.md) を参照する。

## 前提

- Java 21、Maven、Node.js、pnpm、Docker が使えること。
- 作業対象は `offjt_01/attendance-system-workshop-ja-v1.1.1/` である。
- デモ従業員 ID は既定 `EMP001` である。認証はない。
- 本番データベースには接続しない。PostgreSQL はローカルの開発用コンテナまたは Testcontainers だけを使う。
- テストを無効化する起動オプションは使わない。

## 起動

バックエンド:

```bash
cd offjt_01/attendance-system-workshop-ja-v1.1.1/backend
mvn spring-boot:run
```

別の端末でフロントエンド:

```bash
cd offjt_01/attendance-system-workshop-ja-v1.1.1/frontend
pnpm install
pnpm dev
```

ブラウザーで `http://localhost:5173` を開く。開発用 PostgreSQL はバックエンドの Compose 設定で起動する。

## 自動検証

バックエンド:

```bash
cd offjt_01/attendance-system-workshop-ja-v1.1.1/backend
mvn test
```

フロントエンド:

```bash
cd offjt_01/attendance-system-workshop-ja-v1.1.1/frontend
pnpm build
pnpm lint
```

画面の受け入れ確認。シナリオ追加後に `@REQ-UC004` を付ける。

```bash
cd offjt_01/attendance-system-workshop-ja-v1.1.1/e2e
pnpm install
pnpm exec cucumber-js --config cucumber.config.js --tags @REQ-UC004
```

## 受け入れ確認

1. 休暇登録を開始すると、日付、Sick Leave、Personal Leave、Business Trip、メモ欄が表示される。
2. 任意の日付と対応する区分を保存すると、成功が分かる。再取得すると日付、区分、メモが一致する。
3. メモを空にしても保存でき、再取得時のメモは空である。
4. 機能を閉じてもう一度開くと、削除していない休暇が残っている。
5. 区分またはメモを変えて保存すると、再取得結果は変更後の内容である。
6. 削除すると、以後の取得結果にその休暇は含まれない。同じ日付へ新しい休暇を登録できる。
7. 未対応の区分、日付なし、区分なし、501文字のメモ、同日の2件目は保存されない。理由が従業員に伝わる。
8. 存在しない休暇の更新と削除は成功表示にならず、記録は増えない。
9. 勤務が入っている日に確認なしで休暇を作成しても、出勤・退勤時刻は残る。時刻の削除を確認した作成では、同じ行が休暇になり、時刻は空になり、再取得できる。

期待する API の成否は [contracts/leave-api.yaml](./contracts/leave-api.yaml) のステータスに従う。
