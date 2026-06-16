<div align="center">

# 💸 FinFlow

### Your finances, in your pocket. No cloud, no accounts, no excuses.

**Offline-first personal finance manager, encrypted at rest and with a home-screen widget.**

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=flat&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=flat&logo=jetpackcompose&logoColor=white)
![Material 3](https://img.shields.io/badge/Material%203-757575?style=flat&logo=materialdesign&logoColor=white)
![Min SDK 26](https://img.shields.io/badge/minSdk-26-3DDC84?style=flat&logo=android&logoColor=white)
![Offline-First](https://img.shields.io/badge/Offline--First-✓-success?style=flat)
![Encrypted](https://img.shields.io/badge/SQLCipher-Encrypted-critical?style=flat&logo=securitytrustcorp&logoColor=white)
![License MIT](https://img.shields.io/badge/License-MIT-yellow?style=flat)

</div>

---

FinFlow tracks **income and expenses while working 100% offline**. You log transactions by category, see your balance and trends in charts drawn with Canvas, and check the month's summary from a **widget** without opening the app. The local database is the **single source of truth** —there's no backend— and, since it's about money, it's **encrypted at rest** and protected with **biometrics**.

## ✨ What sets it apart

| | |
|---|---|
| 🔒 **Real privacy** | Your finances **never leave the device**. Local DB encrypted with SQLCipher; the passphrase lives in the Android Keystore. |
| ⚡ **Always works** | _Offline-first_: zero loading screens, zero network errors. The UI reacts instantly thanks to Room + Flow. |
| 📊 **Clear picture** | Monthly balance, spending by category (donut) and month-over-month trend (bars), all in **Canvas** for a lightweight APK. |
| 🏠 **Present beyond the app** | A **Glance** widget and **WorkManager** reminders so you never forget to log an expense. |

> **In one sentence:** keep control of your money in a way that's **private, fast, and free of any internet or server dependency.**

## 🛠️ Stack

| Layer | Technologies |
|-------|--------------|
| **Language & UI** | Kotlin · Jetpack Compose (Material 3 + dynamic color) · Navigation 3 · Canvas for charts (no charting libs) |
| **Architecture** | Lightweight Clean Architecture (`domain` / `data` / `ui`) · **MVI** (one immutable state per screen) · Coroutines + Flow / StateFlow · Hilt |
| **Data & security** | Room (+ KSP) as the SSOT with `Flow` DAOs · SQLCipher · Android Keystore / `security-crypto` · `androidx.biometric` · DataStore |
| **System** | Jetpack Glance (widget) · WorkManager (reminders and deferred tasks) |
| **Quality & tests** | JUnit · MockK · Turbine · coroutines-test · Roborazzi · ktlint · detekt · Android Lint |

## ⚡ Commands

> **Requirements:** JDK 11 · Android Studio (AGP `9.2.1`) · `compileSdk 37` · `targetSdk 36` · `minSdk 26`. All versions live in the Version Catalog (`gradle/libs.versions.toml`).

| Action | Command |
|--------|---------|
| Build (debug) | `./gradlew assembleDebug` |
| Install on device | `./gradlew installDebug` |
| Release build | `./gradlew assembleRelease` |
| Format (ktlint) | `./gradlew ktlintFormat` |
| Quality (lint + ktlint + detekt) | `./gradlew codeQuality` |
| **Format + verify everything** ⭐ | `./gradlew formatAndAnalyze` |
| Unit tests (JVM) | `./gradlew test` |
| Instrumented tests | `./gradlew connectedAndroidTest` |

> 💡 Before every commit: `./gradlew formatAndAnalyze`.

## 🗂️ Structure

```
FinFlow/
├── app/src/main/java/com/hacybeyker/finflow/
│   ├── domain/   # Entities, use cases and repository interfaces (pure Kotlin)
│   ├── data/     # Room, DAOs, mappers and repository implementations
│   └── ui/       # Compose screens, ViewModels (MVI) and theme
├── config/detekt/             # detekt configuration
├── gradle/libs.versions.toml  # Version Catalog (dependencies SSOT)
├── lint.xml · .editorconfig   # Lint rules and code style
├── CHANGELOG.md               # Version history
└── AGENTS.md                  # Standards and guide for AI assistants
```

## 🤝 Contributing · 🤖 For AI assistants

The **architecture, SOLID standards, MVI patterns and implementation rules** live in **[AGENTS.md](AGENTS.md)** — read it before touching any code (human or AI).

In short: ktlint `android_studio` style, `max_line_length = 120`, 4-space indentation, **no wildcard imports or trailing commas**, dependencies always via the Version Catalog. Keep `formatAndAnalyze` and the tests green, and update the `CHANGELOG.md`.

## 📄 License

Distributed under the **MIT** license — see [LICENSE](LICENSE).

Copyright © 2026 Carlos Osorio ([hacybeyker](https://github.com/hacybeyker)).
