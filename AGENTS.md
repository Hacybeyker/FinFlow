# AGENTS.md

Guide for AI agents (Claude Code, Gemini in Android Studio, Cursor, Copilot, etc.) that
contribute to **FinFlow**. Follows the [agents.md](https://agents.md/) standard.

This file defines **the standards, architecture and best practices** used to implement any change
in the repository. For the product vision and user-facing commands, see [README.md](README.md).

---

## Project overview

FinFlow is an Android app for **offline-first personal finance**: logging income/expenses by
category, balance and charts, with a **locally encrypted database** and **biometric** unlock.
**There's no backend**: the Room database is the **single source of truth** (SSOT).

- **Language:** Kotlin · **UI:** Jetpack Compose + Material 3 · **DI:** Hilt
- **Architecture:** Clean Architecture + **MVI** (one immutable state per screen)
- **Reactive state:** Coroutines + Flow / StateFlow
- **Data:** Room (+ KSP) over SQLCipher · **Preferences:** DataStore
- **Security:** Android Keystore + `androidx.biometric`
- **System:** Glance (widget) + WorkManager (background)
- **package:** `com.hacybeyker.finflow`

---

## Clean Architecture

Three layers with a **strict dependency rule**: dependencies point **toward the domain**.

```
app/src/main/java/com/hacybeyker/finflow/
├── domain/   # Models, use cases and repository interfaces. PURE Kotlin (no Android).
├── data/     # Room (DB, DAOs, entities), entity↔domain mappers, repository implementations.
└── ui/       # Compose screens, ViewModels (MVI), theme/. Orchestrates use cases.
```

Dependency rule: **`ui → domain ← data`**.

- The **domain knows nothing** about Android, Room, Compose or Hilt; it imports no framework code.
- The **UI depends on the domain** (use cases and interfaces), **never** on `data` directly.
- **`data` implements** the interfaces declared in `domain` (dependency inversion).
- The entity↔domain **mappers** live in `data`. A domain model never exposes an `@Entity`.
- Each layer has its **own model**: entity (data) ↔ domain model ↔ UI state. Entities are never
  leaked to the UI.

---

## SOLID principles (how they apply here)

- **S — Single responsibility:** one use case = one business operation (`AddTransaction`,
  `GetBalance`). ViewModels orchestrate UI state; they hold no persistence logic. Composables
  only render state and emit intents.
- **O — Open/closed:** extend via new use cases or new interface implementations, without modifying
  existing ones. Avoid giant `when` statements over types that keep growing.
- **L — Liskov substitution:** repository fakes/mocks must honor the contract of the domain
  interface so tests stay reliable.
- **I — Interface segregation:** small, cohesive repositories
  (`TransactionRepository`, `CategoryRepository`, `PreferencesRepository`), not a mega-repository.
- **D — Dependency inversion:** the domain defines interfaces; `data` implements them; Hilt wires
  them. The UI depends on abstractions (use cases/interfaces), not on implementations.

---

## Implementation patterns and rules (mandatory)

- **MVI per screen:** each screen exposes a single **immutable** `StateFlow<XxxUiState>` from the
  ViewModel. Interactions come in as **intents** (`sealed interface`), not by calling loose
  ViewModel methods ad-hoc. Explicit states: `loading / empty / content / error`.
- **SSOT with Room:** DAOs return `Flow`. The UI **reacts** to the Flow; it doesn't refresh by hand
  or keep caches parallel to the DB.
- **Business logic in `domain`:** calculations (balance, aggregates) and validations live in use
  cases or the ViewModel, **never in Composables**. Form validation goes in the ViewModel.
- **Coroutines:** use `viewModelScope`; expose state with `StateFlow`; combine flows with
  `combine` / `flatMapLatest`. **Inject** the `CoroutineDispatcher` (don't hardcode `Dispatchers.IO`)
  so it can be tested.
- **Hilt:** modules in `data` for the DB, DAOs and repositories. Domain interfaces bound with
  `@Binds`. ViewModels with `@HiltViewModel` + `@Inject`.
- **Versioned Room migrations.** **Never** `fallbackToDestructiveMigration` in real code.
- **Security:** the SQLCipher passphrase is generated and stored in the **Keystore** /
  EncryptedSharedPreferences; never hardcoded or in plain text. Don't log sensitive data.
- **Charts with Compose Canvas**, no charting libraries (a deliberate decision: lightweight APK).
- **Immutability:** domain models and UI state as immutable `data class`es; collections as
  read-only `List`s. Avoid shared mutable state.

---

## Workflow to implement a feature / fix / enhancement

1. **Locate the right layer.** Is it a business rule? → `domain`. Persistence/mapping? → `data`.
   Presentation? → `ui`. Respect `ui → domain ← data`.
2. **Model the domain first** (when applicable): model + use case + repository interface in pure
   Kotlin, with its unit test.
3. **Implement in `data`:** entity/DAO/migration + mapper + repository implementation; wire it up
   with Hilt.
4. **Connect the UI:** define/extend `UiState` and intents; the ViewModel orchestrates the use cases
   and exposes `StateFlow`; the Composable consumes state and emits intents.
5. **Tests:** unit tests for the new logic (use case and/or ViewModel). Screenshot test if there's
   relevant visual UI (e.g. the chart).
6. **Verify:** `./gradlew formatAndAnalyze` and `./gradlew test` green.
7. **Document:** add an entry to `CHANGELOG.md` under `[Unreleased]` with the type
   (`Added` / `Fixed` / `Changed` / `Enhancement` / `Security`).

Keep the change **focused and atomic**: one feature/fix at a time, without touching unrelated code
along the way.

---

## Code conventions

- ktlint `android_studio` style, **`max_line_length = 120`**, **4-space** indentation,
  `end_of_line = lf` (see `.editorconfig`).
- **Wildcard imports** (`import x.*`) and **trailing commas** are **forbidden**. ktlint enforces this
  in the build, not just the editor.
- `@Composable`/`@Preview` functions in **PascalCase** (exception already configured). All other
  functions in camelCase; classes in PascalCase.
- Descriptive, intention-revealing names; no cryptic abbreviations.
- Comment only the **non-obvious** (the why), not the obvious.
- Dependencies **always** in the Version Catalog (`gradle/libs.versions.toml`), referenced with
  `libs.*`. Never inline versions in `build.gradle.kts`.

---

## Testing

### Unit tests (mandatory for new logic)
- **Use cases and ViewModels** with **MockK** + **Turbine** (for `Flow`/`StateFlow`) +
  `kotlinx-coroutines-test` (use an injected `TestDispatcher`).
- The **domain is tested without an emulator** (pure Kotlin, faked repositories that honor the contract).
- **Arrange–Act–Assert** pattern; one behavior per test; names that describe the case.
- All new business or ViewModel logic **must** be accompanied by its test.

### Screenshot testing
- Key visual components (especially the **Canvas charts**) are covered with **Roborazzi**.
- Workflow: generate the baseline (`recordRoborazziDebug`) and verify on every change
  (`verifyRoborazziDebug`). Baseline images are versioned; any visual diff must be intentional and
  reviewed.
- Use deterministic data and pin dimensions/theme so the render is reproducible.

### Pre-close verification
```bash
./gradlew formatAndAnalyze   # ktlintFormat → ktlintCheck + detekt + lint
./gradlew test               # JVM unit tests
```

---

## Commands

| Action                       | Command                          |
|------------------------------|----------------------------------|
| Format                       | `./gradlew ktlintFormat`         |
| Check style                  | `./gradlew ktlintCheck`          |
| Static analysis              | `./gradlew detekt`               |
| Android Lint                 | `./gradlew lint`                 |
| Lint + ktlint + detekt       | `./gradlew codeQuality`          |
| Format + verify everything   | `./gradlew formatAndAnalyze`     |
| Unit tests (JVM)             | `./gradlew test`                 |
| Instrumented tests           | `./gradlew connectedAndroidTest` |
| Build (debug)                | `./gradlew assembleDebug`        |

> On Windows: `gradlew.bat`. Config: `compileSdk 37`, `targetSdk 36`, `minSdk 26`, **JDK 11**.

---

## Do's and don'ts

**Do:**
- Respect Clean Architecture and the `ui → domain ← data` flow.
- Apply SOLID: single responsibility, small interfaces, dependencies toward abstractions.
- A single immutable `UiState` per screen with explicit states.
- Accompany new logic with unit tests and, if there's visual UI, a screenshot test.
- Centralize dependencies in the Version Catalog and justify each addition.
- Keep `formatAndAnalyze` and the tests green, and update the `CHANGELOG.md`.

**Don't:**
- Don't put business logic or validation in Composables.
- Don't leak Room `@Entity` types or `data` details into `ui`.
- Don't use `fallbackToDestructiveMigration`, wildcard imports or trailing commas.
- Don't hardcode secrets, keys or passphrases; don't log financial data.
- Don't add heavy charting libraries (charts are drawn with Canvas).
- Don't mix several features/fixes in a single change.
