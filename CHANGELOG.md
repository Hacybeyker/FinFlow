# Changelog

Todos los cambios notables de **FinFlow** se documentan en este archivo.

El formato está basado en [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/)
y el proyecto sigue [Versionado Semántico](https://semver.org/lang/es/).

> **Tipos de cambio:** `Added` (feature), `Fixed` (fix), `Changed` / `Enhancement` (mejora),
> `Deprecated`, `Removed`, `Security`.

## [1.0.0]

### Added
- Licencia **MIT** (`LICENSE`) y referencia a ella en el `README.md`.
- Documentación del proyecto: `README.md`, `CHANGELOG.md` y `AGENTS.md`.
- `AGENTS.md` reenfocado a estándares de implementación: Clean Architecture, SOLID, patrón MVI,
    testing unitario y screenshot testing, eliminando el flujo de construcción por fases.
- Tareas Gradle agregadas `codeQuality` y `formatAndAnalyze` para verificación de código.
- Configuración inicial del proyecto: Gradle con Version Catalog (`libs.versions.toml`),
  `.editorconfig` y herramientas de calidad de código (ktlint, detekt, Android Lint).
- Configuración de `compileSdk 37`, `targetSdk 36`, `minSdk 26`.
- Estructura base del proyecto Android (Kotlin + Jetpack Compose, Material 3).
