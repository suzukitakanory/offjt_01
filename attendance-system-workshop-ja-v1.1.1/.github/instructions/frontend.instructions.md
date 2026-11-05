---
applyTo: "frontend/**"
---
# Frontend（React + TypeScript）ルール
- TypeScript strict を維持。`any` は禁止（どうしても必要な場合は型ガードを併記）。
- UIは既存のCSS/コンポーネント指針に合わせる（命名/props/フォルダ構成）。
- APIクライアントは `frontend/src/services/*` に集約。型は OpenAPI から導出/整合。
