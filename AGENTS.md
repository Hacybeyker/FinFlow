# AGENTS.md

Guide for AI agents (Claude Code, Gemini in Android Studio, Cursor, Copilot, etc.) that
contribute to **FinFlow**. Follows the [agents.md](https://agents.md/) standard.

This file defines **the standards, architecture and best practices** used to implement any change
in the repository. For the product vision and user-facing commands, see [README.md](README.md); for
the visual design system (colors, typography, spacing, components), see [DESIGN.md](DESIGN.md).

---

## Project overview

FinFlow is an Android app for **offline-first personal finance**: logging income/expenses by
category, balance and charts, with a **locally encrypted database** and **biometric** unlock.
**There's no backend**: the Room database is the **single source of truth** (SSOT).

- **Language:** Kotlin · **UI:** Jetpack Compose + Material 3 · **DI:** Hilt
- **Architecture:** **Vertical Slice Architecture** (feature-first packaging) + Clean Architecture
  dependency rule + **MVI** (one immutable state per screen)
- **Reactive state:** Coroutines + Flow / StateFlow
- **Data:** Room (+ KSP) over SQLCipher · **Preferences:** DataStore
- **Security:** Android Keystore + `androidx.biometric`
- **System:** Glance (widget) + WorkManager (background)
- **package:** `com.hacybeyker.finflow`

---

## Architecture — Vertical Slice + Clean dependency rule

Two **orthogonal** concerns, both mandatory:

1. **Packaging = Vertical Slice Architecture (feature-first).** Code is grouped **by feature**, not by
   technical layer. Everything a feature needs (its three layers) lives together under `feature/<name>/`.
2. **Dependency rule = Clean Architecture.** Inside every slice, dependencies point **toward the
   domain**: **`ui → domain ← data`**.

These do not compete: VSA decides *how files are grouped*; Clean decides *who may depend on whom*.

```
app/src/main/java/com/hacybeyker/finflow/
├── core/                     # SHARED across features (only what ≥2 features truly need)
│   ├── domain/               #   cross-cutting domain types (e.g. Money value class)
│   ├── database/             #   FinFlowDatabase (@Database aggregates all DAOs), Room/SQLCipher, Hilt DB module
│   └── ui/theme/             #   design tokens (Color, Spacing, Type, Shape, Theme)
├── feature/
│   └── <feature>/            # one vertical slice per business capability
│       ├── domain/           #   models, use cases, repository interfaces. PURE Kotlin (no Android).
│       ├── data/             #   @Entity + DAO (contributed to core DB), mappers, repository impl, Hilt module
│       └── ui/               #   Compose screens, ViewModels (MVI), UiState/intents
└── navigation/               # cross-feature NavHost + nav keys
```

Dependency rule (**per slice**): **`ui → domain ← data`**.

- The **domain knows nothing** about Android, Room, Compose or Hilt; it imports no framework code.
- The **UI depends on the domain** (use cases and interfaces), **never** on `data` directly.
- **`data` implements** the interfaces declared in `domain` (dependency inversion).
- The entity↔domain **mappers** live in `data`. A domain model never exposes an `@Entity`.
- Each layer has its **own model**: entity (data) ↔ domain model ↔ UI state. Entities are never
  leaked to the UI.

**VSA rules:**
- A *feature* is a **business capability** (transactions, charts, security…), never a single widget.
- **One feature never imports another feature's internals.** Cross-feature collaboration goes through
  `core/` or the `domain` contracts.
- **Promote to `core/` only when ≥2 features genuinely need it** (YAGNI). Until then, keep it local to
  its feature. Cheap duplication beats premature coupling.
- The Room **`@Database` is inherently cross-feature** (one `.db`, aggregates every DAO, global
  migrations) → it lives in `core/database/`; each feature **contributes** its `@Entity` + DAO.
- Single Gradle module with feature packages (not multi-module) — a deliberate, revisitable choice.

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
- **Hilt:** the DB and shared DAOs are provided from `core/database`; each feature's `data` provides
  its repositories and binds its domain interfaces with `@Binds`. ViewModels with `@HiltViewModel` +
  `@Inject`. A feature's Hilt module lives inside that feature.
- **Versioned Room migrations.** **Never** `fallbackToDestructiveMigration` in real code.
- **Security:** the SQLCipher passphrase is generated and stored in the **Keystore** /
  EncryptedSharedPreferences; never hardcoded or in plain text. Don't log sensitive data.
- **Charts with Compose Canvas**, no charting libraries (a deliberate decision: lightweight APK).
- **Immutability:** domain models and UI state as immutable `data class`es; collections as
  read-only `List`s. Avoid shared mutable state.

---

## Design system (mandatory for any UI)

The full visual contract lives in **[DESIGN.md](DESIGN.md)**; read it before building or changing
any screen or component. Non-negotiable rules:

- **Tokens only.** In a `@Composable`, **never** hardcode `Color(0x…)`, a magic `.dp`, a raw
  `.sp`/`fontSize` or `FontFamily.Default`. Always consume `MaterialTheme.colorScheme.*`,
  `MaterialTheme.financeColors.*`, `MaterialTheme.typography.*`, `MaterialTheme.spacing.*`,
  `MaterialTheme.shapes.*` (defined under `ui/theme/`).
- **Income/expense semantics:** use `MaterialTheme.financeColors` (income = green, expense = coral),
  never `colorScheme.error` or a raw green/red. Pair color with a sign/icon (don't rely on color
  alone).
- **Shared components:** reuse (or add) "dumb" components in `ui/components/` instead of re-creating
  visual patterns per screen. Each MVI screen renders explicit `loading / empty / content / error`
  via shared state scaffolds.
- **Light & dark:** every screen/component works in both themes, with `@Preview` for each.
- **No drift:** `DESIGN.md` and the `ui/theme/` tokens are kept in sync in the same commit. The
  runtime source of truth is the Kotlin theme.

---

## Workflow to implement a feature / fix / enhancement (Vertical Slice)

**Every change is a vertical slice**: pick the **feature** first, then build only the classes that
feature needs, top-down through its own layers — never "all the domain of the app, then all the data".

1. **Locate the feature, not the layer.** New capability → new `feature/<name>/`. Change to an
   existing one → its folder. Only genuinely shared pieces touch `core/`.
2. **Model the slice's domain first:** model + use case + repository interface in pure Kotlin under
   `feature/<name>/domain`, each with its unit test. Only what *this* feature needs.
3. **Implement the slice's `data`:** `@Entity` + DAO (contributed to `core` DB) + mapper + repository
   implementation + the feature's Hilt module.
4. **Connect the slice's UI:** define/extend `UiState` and intents; the ViewModel orchestrates the use
   cases and exposes `StateFlow`; the Composable consumes state and emits intents.
5. **Tests:** unit tests for the new logic (use case and/or ViewModel). Screenshot test if there's
   relevant visual UI (e.g. the chart).
6. **Verify:** `./gradlew formatAndAnalyze` and `./gradlew test` green.
7. **Document & version:** add a `CHANGELOG.md` entry under `[Unreleased]` with the type
   (`Added` / `Fixed` / `Changed` / `Enhancement` / `Security`). When the **slice** is done, cut a
   version per the **Versioning** section.

**Commit granularity — group by functional unit, not by file.** "Short/simple" does **not** mean one
commit per class. Group the classes **needed to deliver something functional** toward the objective —
typically by layer (`domain` / `data` / `ui`) or by sub-objective. Each commit must **compile and pass
`codeQuality` + tests**. If a feature involves many classes (say ~15), analyze it and split into ~2
coherent commits, not 15. One feature/fix at a time; don't touch unrelated code along the way.

---

## Versioning

The project follows [Semantic Versioning](https://semver.org/) `MAJOR.MINOR.PATCH`. The app is
**not released yet**, so it stays in the **`0.y.z`** range until the first public release, which
becomes `1.0.0`. A version identifies a **cut/milestone**, never a single commit.

**When to bump (matched to the phased plan):**
- **PATCH** (`0.1.1`, `0.1.2`, …) — each **self-contained step** within a phase (e.g. "add the
  `Transaction` entity", "add the `GetBalance` use case"). Bump when the step works on its own.
- **MINOR** (`0.2.0`) — when a **phase / user-visible milestone** is completed.
- **MAJOR** (`1.0.0`) — the **first public release**.

**`versionName` vs `versionCode`** (in `app/build.gradle.kts`):
- `versionName` is the human SemVer string above.
- `versionCode` is a monotonically increasing integer. **Increment it by 1 on every `versionName`
  bump** (keeps it future-proof for distribution).

**Release loop (do this for every bump, so `[Unreleased]` never piles up):**
1. While working, add entries under `## [Unreleased]` in `CHANGELOG.md` (grouped by `Added` /
   `Fixed` / `Changed` / `Enhancement` / `Security`).
2. To cut the version: rename `[Unreleased]` to `## [x.y.z] - YYYY-MM-DD` and add a fresh empty
   `## [Unreleased]` above it.
3. Bump `versionName` (and `versionCode`) in `app/build.gradle.kts`.
4. Tag the commit: `git tag vX.Y.Z`.

Keep `[Unreleased]` small: promote it to a numbered version as soon as a step/phase is done, rather
than accumulating a large backlog of unreleased changes.

---

## Code conventions

- ktlint `android_studio` style, **`max_line_length = 120`**, **4-space** indentation,
  `end_of_line = lf` (see `.editorconfig`).
- **Wildcard imports** (`import x.*`) and **trailing commas** are **forbidden**. ktlint enforces this
  in the build, not just the editor.
- `@Composable`/`@Preview` functions in **PascalCase** (exception already configured). All other
  functions in camelCase; classes in PascalCase.
- Descriptive, intention-revealing names; no cryptic abbreviations.
- Comment only the **non-obvious** (the why), not the obvious. **Default to no comment**: don't add
  KDoc/comments that just restate what the class/function name already says (e.g. "Provides the app
  database" on a Hilt module). Comment **only** when it adds real value: a non-obvious *why*, a design
  decision, a warning, or something counterintuitive (e.g. "no dispatcher because Room already threads").
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
- Group code **by feature** (`feature/<name>/{domain,data,ui}`); keep only truly shared pieces in
  `core/`. Respect the per-slice `ui → domain ← data` dependency rule.
- Apply SOLID: single responsibility, small interfaces, dependencies toward abstractions.
- A single immutable `UiState` per screen with explicit states.
- Build UI from design tokens (`MaterialTheme.*`) and shared `ui/components/`; support light & dark.
- Accompany new logic with unit tests and, if there's visual UI, a screenshot test.
- Centralize dependencies in the Version Catalog and justify each addition.
- Keep `formatAndAnalyze` and the tests green, and update the `CHANGELOG.md`.

**Don't:**
- Don't put business logic or validation in Composables.
- Don't leak Room `@Entity` types or `data` details into `ui`.
- Don't use `fallbackToDestructiveMigration`, wildcard imports or trailing commas.
- Don't hardcode secrets, keys or passphrases; don't log financial data.
- Don't add heavy charting libraries (charts are drawn with Canvas).
- Don't hardcode colors, sizes or fonts in Composables, or use `colorScheme.error` for expenses.
- Don't let `DESIGN.md` and the `core/ui/theme/` tokens drift apart.
- Don't mix several features/fixes in a single change, and **don't dump many files into one commit** —
  keep commits short and coherent.
- Don't import another feature's internals from a feature; collaborate via `core/` or domain contracts.
- Don't promote code to `core/` speculatively (YAGNI) — wait until ≥2 features need it.
