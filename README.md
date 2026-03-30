⭐ If this project helps you, please star the repository

# WebAuthn localhost デモ（Java 8 / レガシー対応）
# WebAuthn localhost demo (Java 8 / legacy-friendly)

本プロジェクトは、既存のレガシーシステムにも組み込み可能な形で  
WebAuthn（パスキー認証）を最小構成で体験・検証できるサンプルです。

This project demonstrates WebAuthn (passkey authentication) in a minimal form  
that can be integrated into existing legacy systems.

---

## 🎯 目的 / Purpose

- パスキー認証を最短で理解する  
- Windows Hello にパスキーを登録する  
- 既存システムへの組み込みイメージを持つ  

---

## 🧭 設計方針 / Design Principles

- Java 8（レガシー対応）
- Servletベース（フレームワーク非依存）
- Yubico OSS 利用
- インストール作業の排除
- localhost 前提

---

## 🚀 クイックスタート / Quick Start

```bash
build.bat
start.bat

# webauthn-servlet

Java 8 / Windows 向けの Servlet + 埋め込み Tomcat 9 + Yubico `java-webauthn-server` サンプルです。

## 画面フロー

1. `app/login.html` で `demo / password123` でパスワードログイン
2. `app/menu.html` で同一ユーザーとしてパスキー登録
3. `api/logout` で `HttpSession.invalidate()` を実行してログアウト
4. `app/login.html` に戻る
5. パスキーで再ログイン

## 重要事項

- この checkpoint は **オフライン build 用のソース一式** です。
- `build.bat` は Maven を `-o` で実行します。
- 依存 jar は `MAVEN_REPO` で指定する **事前投入済みのローカル Maven repository** に存在している前提です。
- `run.bat` は `runtime\jre\bin\java.exe` があればそれを優先し、なければ `JAVA_HOME` を使います。

## 既定値

- `JAVA_HOME=C:\java\jdk-8u202`
- `MAVEN_HOME=C:\java\apache-maven-3.9.14`
- `MAVEN_REPO=%SCRIPT_DIR%offline-m2\repository` または `%USERPROFILE%\.m2\repository`

## ビルド

```bat
build.bat
```

## 起動

```bat
run.bat
```

## 補足

静的ファイルは `src/main/resources/app` と `src/main/resources/js` にあります。
起動時は `app.home\src\main\resources` が存在すればそこを web root として使い、存在しなければプロジェクト直下を web root として使います。


## 2026-03-30 transports 修正

登録完了時に送信する JSON からトップレベルの `transports` を除去しました。


## 任意ユーザー / 固定パスワード

初回ログインは任意のユーザー名と `passwd` を使います。未登録ユーザーは初回パスワードログイン時に自動作成されます。
