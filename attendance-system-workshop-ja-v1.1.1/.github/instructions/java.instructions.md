---
applyTo: "**/*.{java}"
---
- 変数・メソッドは camelCase、クラスは PascalCase
- パッケージ構成: `controller`, `service`, `repository`, `model`, `config`
- public/protectedなクラス・メソッドには、日本語のJavadocを付ける。
- 複雑な業務ロジックにもJavadocを付け、目的や前提条件を説明する。
- メソッドでは必要に応じて `@param`、`@return`、`@throws` を記載する。
- 自明なprivateメソッドにはJavadocを付けない。
- 実装後は、古いTODOや実装予定の記述を更新または削除する。