# Changelog

All notable changes to **FinFlow** are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/)
and this project adheres to [Semantic Versioning](https://semver.org/).

> **Change types:** `Added` (feature), `Fixed` (fix), `Changed` / `Enhancement` (improvement),
> `Deprecated`, `Removed`, `Security`.

## [Unreleased]

### Added
- `Money` domain value class in `core/domain`: exact monetary amounts stored as `Long` minor units
  (no floating point), with `+`/`-`/unary-minus operators, `Comparable`, sign helpers and `ZERO`.
- `TransactionType` enum (`INCOME` / `EXPENSE`) in `feature/transactions/domain`.
- `Category` domain model (`id`, `name`) in `feature/transactions/domain`.
- `Transaction` domain model (embeds its `Category`, exposes `signedAmount()`), the
  `TransactionRepository` interface (reactive `Flow` reads + `add`), and the `GetBalance`,
  `GetTransactionsByMonth` and `AddTransaction` use cases (with validation), each unit-tested with an
  in-memory fake repository.
- **Room persistence** for transactions (SSOT): `FinFlowDatabase` in `core/database` with its Hilt
  module, plus the feature's `TransactionEntity`/`TransactionDao` (primitive columns, no
  TypeConverters), entity↔domain mapper (unit-tested) and `RoomTransactionRepository` wired with Hilt.
  The category is stored as a denormalized snapshot for now (normalized table arrives in the categories
  slice).
- **Home UI (MVI):** `HomeScreen` with a balance card and the current month's transaction list, driven
  by `HomeViewModel` (`StateFlow` combining balance + list into `Loading`/`Empty`/`Content`),
  unit-tested with Turbine. Reactive: a new transaction shows up instantly.
- **Add-transaction screen:** full `AddTransactionScreen` (amount, income/expense, category, date
  picker, note) reached from a Home FAB via a new Nav3 `AddTransaction` route, backed by
  `AddTransactionViewModel` (MVI intents, amount/category validation), unit-tested.
- Shared UI: `AmountText` component (income green / expense coral + `+`/`−` sign, accessibility) and a
  `MoneyFormatter` (device locale/currency by default, overridable via `LocalMoneyFormatter` for a
  future currency setting) with an exact `BigDecimal` amount parser.

### Changed
- Adopted **Vertical Slice Architecture** (feature-first packaging): moved existing code to
  `core/ui/theme/`, `feature/transactions/ui/` and `navigation/`, keeping Clean Architecture's
  per-slice dependency rule (`ui → domain ← data`). Pure restructure, no behavior change.

## [0.2.0] - 2026-06-17

### Added
- **Hilt dependency injection** wired with KSP: `FinFlowApplication` (`@HiltAndroidApp`),
  `@AndroidEntryPoint` on `MainActivity`, and `hilt-navigation-compose` for `hiltViewModel()`.
- Clean Architecture package skeleton: `domain/` and `data/` (alongside the existing `ui/`).
- **Navigation 3** wiring under `ui/navigation/`: an app-owned, saveable back stack (`@Serializable`
  `NavKey`s) rendered by `NavDisplay`, plus an empty themed `HomeScreen` as the start destination.
- **Continuous Integration** (`.github/workflows/ci.yml`): a GitHub Actions workflow that, on every
  push and pull request to `main`, runs read-only static analysis (`codeQuality`: ktlintCheck +
  detekt + lint) followed by unit tests on JDK 17 with Gradle caching.

### Fixed
- Hilt's aggregating processor could not read Kotlin 2.4.0 class metadata; pinned
  `kotlin-metadata-jvm` (tied to the `kotlin` catalog version) on the annotation-processor classpath.

## [0.1.0] - 2026-06-16

### Added
- **Design system** (`DESIGN.md`) defining the visual contract: a calm, muted indigo palette with
  dedicated income/expense semantics, full Material 3 type scale, 4dp spacing scale and shape scale,
  with light/dark support and accessibility (WCAG AA) rules.
- Design tokens under `ui/theme/`: brand + neutral color roles (`Color.kt`), semantic
  `FinanceColors` exposed as `MaterialTheme.financeColors`, a `Spacing` scale
  (`MaterialTheme.spacing`), `FinFlowShapes` and a complete typography scale.

### Changed
- `FinFlowTheme` now assembles the full light/dark color schemes from tokens, provides the spacing
  and finance-color `CompositionLocal`s, and defaults `dynamicColor` to **off** to keep the brand
  palette consistent (dynamic color remains opt-in).
- `AGENTS.md` and `README.md` reference `DESIGN.md` and enforce a tokens-only, no-drift UI rule.

## [0.0.1] - 2026-06-16

### Added
- **MIT** license (`LICENSE`) and a reference to it in the `README.md`.
- Project documentation: `README.md`, `CHANGELOG.md` and `AGENTS.md`.
- `AGENTS.md` refocused on implementation standards: Clean Architecture, SOLID, the MVI pattern,
    unit testing and screenshot testing, removing the phased build workflow.
- Aggregate Gradle tasks `codeQuality` and `formatAndAnalyze` for code verification.
- Initial project setup: Gradle with Version Catalog (`libs.versions.toml`),
  `.editorconfig` and code quality tools (ktlint, detekt, Android Lint).
- Configuration of `compileSdk 37`, `targetSdk 36`, `minSdk 26`.
- Base structure of the Android project (Kotlin + Jetpack Compose, Material 3).
