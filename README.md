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
[![CI](https://github.com/Hacybeyker/FinFlow/actions/workflows/ci.yml/badge.svg)](https://github.com/Hacybeyker/FinFlow/actions/workflows/ci.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=com.hacybeyker.finflow&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=com.hacybeyker.finflow)
[![Build Scan](https://img.shields.io/badge/Gradle-Build%20Scan®-06A0CE?style=flat&logo=gradle&logoColor=white)](https://github.com/Hacybeyker/FinFlow/actions/workflows/ci.yml)
![License MIT](https://img.shields.io/badge/License-MIT-yellow?style=flat)

</div>

---

FinFlow tracks **income and expenses while working 100% offline**. You log transactions by category, see your balance and trends in charts drawn with Canvas, and check the month's summary from a **widget** without opening the app. The local database is the **single source of truth** —there's no backend— and, since it's about money, it's **encrypted at rest** and protected with **biometrics**.

## ✨ What sets it apart

| | |
|---|---|
| 🔒 **Real privacy** | Your finances **never leave the device**. Local DB encrypted with SQLCipher; the passphrase is generated and sealed with **Tink (AEAD) + Android Keystore**. |
| ⚡ **Always works** | _Offline-first_: zero loading screens, zero network errors. The UI reacts instantly thanks to Room + Flow. |
| 📊 **Clear picture** | Monthly balance, spending by category (donut) and month-over-month trend (bars), all in **Canvas** for a lightweight APK. |
| 🏠 **Present beyond the app** | A **Glance** widget and **WorkManager** reminders so you never forget to log an expense. |
| 📤 **Your data is yours** | One-tap **CSV export** via the Storage Access Framework — no storage permission, RFC 4180-escaped, spreadsheet-ready. |

> **In one sentence:** keep control of your money in a way that's **private, fast, and free of any internet or server dependency.**

## 🏛️ Architecture

**Vertical Slice Architecture (feature-first)**: each feature is a self-contained slice with its own `domain` / `data` / `ui` layers, so a change to *charts* never touches *transactions*. Inside every slice: **MVI** (one immutable `UiState` per screen), unidirectional data flow, and repository contracts owned by `domain`.

```
                        ┌───────────────────────────────┐
                        │  navigation  ·  Navigation 3  │
                        └───────────────┬───────────────┘
                                        │
  feature/* ─ one self-contained slice per feature ─────────────────────────┐
  │       transactions · charts · security · settings · widget · reminders │
  │                                                                        │
  │   ┌──────────────────────┐        ┌──────────────────────────────┐     │
  │   │ ui                   │ ─────▶ │ domain                       │     │
  │   │ Compose + MVI        │ intents│ use cases + contracts        │     │
  │   │ ViewModel            │ /state │ (pure Kotlin, no Android)    │     │
  │   └──────────────────────┘        └──────────────▲───────────────┘     │
  │                                                  │ implements          │
  │   ┌──────────────────────┐                       │                     │
  │   │ data                 │ ──────────────────────┘                     │
  │   │ repository impls     │                                             │
  │   └──────────┬───────────┘                                             │
  └──────────────┼──────────────────────────────────────────────────────── ┘
                 ▼
  ┌──────────────────────────────┐   ┌─────────────────────────────────────┐
  │ core/database                │   │ core/ui · core/domain · core/di     │
  │ Room + SQLCipher             │   │ theme & shared UI · cross-feature   │
  │ (Tink-sealed passphrase)     │   │ contracts · Hilt wiring             │
  └──────────────────────────────┘   └─────────────────────────────────────┘
```

## 🛠️ Stack

| Layer | Technologies |
|-------|--------------|
| **Language & UI** | Kotlin · Jetpack Compose (Material 3, brand palette — dynamic color deliberately off) · Navigation 3 · Canvas for charts (no charting libs) |
| **Architecture** | Vertical Slice (feature-first) with `domain` / `data` / `ui` per slice · **MVI** (one immutable state per screen) · Coroutines + Flow / StateFlow · Hilt |
| **Data & security** | Room (+ KSP) as the SSOT with `Flow` DAOs · SQLCipher · **Tink (AEAD) + Android Keystore** · `androidx.biometric` · DataStore |
| **System** | Jetpack Glance (widget) · WorkManager (reminders and deferred tasks) |
| **Quality & tests** | JUnit · Turbine · coroutines-test · hand-rolled fakes · **Robolectric** (UI flows on the JVM) · **Roborazzi** (screenshot goldens) · **Kover** (≥95% coverage gate) · ktlint · detekt · Android Lint · SonarCloud · Gradle Build Scans |

## 🧪 Quality gates

Every push to `main` runs the full pipeline — and **fails** if any gate breaks:

1. **Static analysis** — ktlint + detekt + Android Lint (`codeQuality`).
2. **Tests** — unit + Robolectric UI flows (93 tests, one JVM run shared by all gates).
3. **Coverage gate** — Kover fails the build under **95%** line coverage on measured classes (domain, data, ViewModels; codegen and framework adapters excluded by design).
4. **Screenshot gate** — Roborazzi diffs the charts against committed goldens; a single changed pixel without re-recording fails CI.
5. **SonarCloud** — Quality Gate blocks the job (`sonar.qualitygate.wait`), with a Build Scan published per run for diagnostics.

The goldens guarding the charts (light/dark, versioned in `app/src/test/screenshots/`):

<div align="center">

<img src="app/src/test/screenshots/com.hacybeyker.finflow.feature.charts.ui.ChartsScreenshotTest.spendingDonut_light.png" alt="Spending donut — light" width="270">&nbsp;<img src="app/src/test/screenshots/com.hacybeyker.finflow.feature.charts.ui.ChartsScreenshotTest.spendingDonut_dark.png" alt="Spending donut — dark" width="270">

<img src="app/src/test/screenshots/com.hacybeyker.finflow.feature.charts.ui.ChartsScreenshotTest.monthlyBarChart_light.png" alt="Monthly bars — light" width="270">&nbsp;<img src="app/src/test/screenshots/com.hacybeyker.finflow.feature.charts.ui.ChartsScreenshotTest.monthlyBarChart_dark.png" alt="Monthly bars — dark" width="270">

</div>

## 🧭 Key decisions

| Decision | Why |
|----------|-----|
| **No backend, ever** | Privacy as a feature: financial data never leaves the device, so there is nothing to breach remotely. |
| **SQLCipher + Tink-sealed passphrase** | Encryption at rest for the whole DB; the passphrase is random, AEAD-encrypted and never stored in plaintext. |
| **Canvas charts, no libraries** | Two charts don't justify a dependency; custom drawing keeps the APK lean and the visuals on-brand. |
| **Robolectric over emulator for UI flows** | Seconds instead of minutes per run, same CI, and Roborazzi reuses the whole setup for screenshot tests. |
| **R8 enabled on AGP 9.x** | Release builds are shrunk and obfuscated using the new `optimization` DSL + `keepRules` source folder. |
| **GitHub Actions pinned to SHA** | Mutable tags (`@v6`) are a supply-chain vector; commits are immutable. Dependabot keeps the pins fresh. |

## ⚡ Commands

> **Requirements:** JDK 17 (build; bytecode targets Java 11) · Android Studio with AGP `9.3` · `compileSdk 37` · `targetSdk 36` · `minSdk 26`. All versions live in the Version Catalog (`gradle/libs.versions.toml`).

| Action | Command |
|--------|---------|
| Build (debug) | `./gradlew assembleDebug` |
| Install on device | `./gradlew installDebug` |
| Release build (R8) | `./gradlew assembleRelease` |
| Format (ktlint) | `./gradlew ktlintFormat` |
| Quality (lint + ktlint + detekt) | `./gradlew codeQuality` |
| **Format + verify everything** ⭐ | `./gradlew formatAndAnalyze` |
| Unit + UI-flow tests (JVM) | `./gradlew testDebugUnitTest` |
| Coverage gate (≥95%) | `./gradlew koverVerifyDebug` |
| Screenshot gate | `./gradlew verifyRoborazziDebug` |
| Re-record goldens (after an intentional visual change) | `./gradlew recordRoborazziDebug` |

> 💡 Before every commit: `./gradlew formatAndAnalyze`.

## 🗂️ Structure

```
FinFlow/
├── app/src/main/java/com/hacybeyker/finflow/
│   ├── core/
│   │   ├── database/ # Room + SQLCipher setup, Tink-sealed passphrase
│   │   ├── di/       # Hilt modules shared across slices
│   │   ├── domain/   # Cross-feature entities and contracts
│   │   └── ui/       # Theme, design tokens, shared composables
│   ├── feature/      # One vertical slice per feature, each with domain/ data/ ui/
│   │   ├── transactions/ · charts/ · security/ · settings/ · widget/ · reminders/
│   └── navigation/   # Navigation 3 graph
├── app/src/test/     # Unit + Robolectric flows + Roborazzi goldens (screenshots/)
├── config/detekt/              # detekt configuration
├── gradle/libs.versions.toml   # Version Catalog (dependencies SSOT)
├── lint.xml · .editorconfig    # Lint rules and code style
├── CHANGELOG.md                # Version history
├── DESIGN.md                   # Design system (colors, typography, spacing, components)
└── AGENTS.md                   # Standards and guide for AI assistants
```

## 🤝 Contributing · 🤖 For AI assistants

The **architecture, SOLID standards, MVI patterns and implementation rules** live in **[AGENTS.md](AGENTS.md)**, and the **visual design system** (colors, typography, spacing, components, light/dark) in **[DESIGN.md](DESIGN.md)** — read both before touching any code (human or AI).

In short: ktlint `android_studio` style, `max_line_length = 120`, 4-space indentation, **no wildcard imports or trailing commas**, dependencies always via the Version Catalog, and UI built only from design tokens (`MaterialTheme.*`). Keep `formatAndAnalyze` and the tests green, and update the `CHANGELOG.md`.

## 📄 License

Distributed under the **MIT** license — see [LICENSE](LICENSE).

Copyright © 2026 Carlos Osorio ([hacybeyker](https://github.com/hacybeyker)).
