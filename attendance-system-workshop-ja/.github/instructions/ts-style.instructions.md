---
applyTo: "**/*.{ts,tsx}"
---
- ESLint/Prettier を絶対参照（設定に逆らう提案はしない）。
- 型はできるだけ推論に任せ、`any`/`!` 非推奨。ユニオン型優先、enum禁止（LTD必要時のみ）。
- 公開APIに `@since` と簡潔なJSDocを付与。
