# Changelog

All notable changes to **FinFlow** are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/)
and this project adheres to [Semantic Versioning](https://semver.org/).

> **Change types:** `Added` (feature), `Fixed` (fix), `Changed` / `Enhancement` (improvement),
> `Deprecated`, `Removed`, `Security`.

## [1.0.0]

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
