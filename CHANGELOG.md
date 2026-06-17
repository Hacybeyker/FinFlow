# Changelog

All notable changes to **FinFlow** are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/)
and this project adheres to [Semantic Versioning](https://semver.org/).

> **Change types:** `Added` (feature), `Fixed` (fix), `Changed` / `Enhancement` (improvement),
> `Deprecated`, `Removed`, `Security`.

## [Unreleased]

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
