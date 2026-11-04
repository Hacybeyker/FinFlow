# FinFlow

Offline-first personal finance app for Android: income and expenses by category, balance, charts, a
home-screen widget, daily reminders and CSV export. There is **no backend**: the encrypted Room database
on the device is the single source of truth. Product overview in [README.md](README.md); visual design
system in [DESIGN.md](DESIGN.md).

This file is self-contained: everything an agent or a person needs to work on FinFlow is here. The
`hb-*` skills from [Hacybeyker/agent-skills](https://github.com/Hacybeyker/agent-skills) are optional;
when installed, they add the general Android/Kotlin practices of the profile below.

## Stack

- Profile: `hb-stack-mobile-kotlin`
- Variant: `android`
- **Profile exceptions** (this file prevails):
  - **MVI naming:** `XxxIntent` + `onIntent(intent)` instead of `XxxAction` + `onAction(action)`. Same
    contract (one sealed type, one entry point); `Intent` is the vocabulary the project's MVI screens use.
  - **Hilt module in the feature's `data/` package, no `di/`:** the module only binds that feature's data
    implementations to its domain interfaces, so it sits next to what it binds.
  - **Nav keys together in `navigation/`**, next to `FinFlowNavHost`, instead of a `XxxNavKey` /
    `XxxNavigation` pair per feature: with one Gradle module and one NavHost, the whole graph reads in one
    place. Split them per feature if the app is ever modularized.
  - **`core/ui/` instead of `core/designsystem/`:** it holds more than the design system (theme, shared
    components and UI formatting), and `DESIGN.md` refers to these paths.
  - **No trailing commas:** the project's ktlint style forbids them (`.editorconfig`) and the build fails
    on them; the profile's examples use them.
- Kotlin · Jetpack Compose + Material 3 · Hilt · Navigation 3 · Coroutines + Flow · Room (KSP) over
  **SQLCipher** · **Tink** + Android Keystore · DataStore · `androidx.biometric` · **Glance** (widget) ·
  **WorkManager** (reminders) · kotlinx.serialization (nav keys).
- Quality: ktlint, detekt, Android Lint, Kover, Roborazzi, Robolectric; SonarCloud and a Gradle Build
  Scan in CI.
- `compileSdk 37` · `targetSdk 36` · `minSdk 26` · bytecode Java 11 · Gradle daemon on JDK 21
  (`gradle/gradle-daemon-jvm.properties`, auto-provisioned if missing). Package
  `com.hacybeyker.finflow`. Versions live in `gradle/libs.versions.toml`.

## Commands

| For | Command |
| --- | --- |
| Build (debug) | `./gradlew assembleDebug` |
| Install on device | `./gradlew installDebug` |
| Release build (R8) | `./gradlew assembleRelease` |
| Format | `./gradlew ktlintFormat` |
| Lint + ktlint + detekt | `./gradlew codeQuality` |
| Format + verify everything | `./gradlew formatAndAnalyze` |
| Unit + Robolectric UI-flow tests | `./gradlew testDebugUnitTest` |
| Coverage gate (≥ 95 %) | `./gradlew koverVerifyDebug` |
| Screenshot gate | `./gradlew verifyRoborazziDebug` |
| Re-record goldens (intentional visual change) | `./gradlew recordRoborazziDebug` |
| Instrumented tests | `./gradlew connectedAndroidTest` |

## DoD

Before calling a task done, in this order (the same gates as `.github/workflows/ci.yml`):

1. `./gradlew formatAndAnalyze`
2. `./gradlew testDebugUnitTest koverVerifyDebug verifyRoborazziDebug`

A Roborazzi diff is only acceptable if the visual change is intentional: re-record with
`recordRoborazziDebug` and review the new golden. SonarCloud's Quality Gate only runs in CI.

## Architecture

**Vertical Slice Architecture** in a single Gradle module (a deliberate, revisitable choice), with the
Clean Architecture dependency rule inside every slice. Diagram in the README.

- `feature/<name>/{domain,data,ui}` for `transactions`, `charts`, `security`, `settings`, `widget` and
  `reminders`. A feature is a business capability, never a single widget.
- **Dependency rule `ui → domain ← data`:** `domain` is pure Kotlin (models, repository interfaces, use
  cases; `@Inject` is the only annotation allowed). `data` implements the domain interfaces and owns
  entities and mappers; an `@Entity` never leaves `data`. ViewModels talk to use cases, never to
  repositories.
- A feature never imports another feature's internals; they collaborate through `core/`. Promote to
  `core/` only when ≥ 2 features need it.
- `core/`: `database/` (`FinFlowDatabase` aggregates every feature's entities and DAOs; migrations in
  `DatabaseMigrations`; `DatabaseKey`), `domain/` (types and contracts shared by ≥ 2 features, such as
  `Money`), `di/` (`@IoDispatcher` and shared bindings), `ui/theme`, `ui/components` and `ui/format`.
- **MVI per screen:** a single immutable `StateFlow<XxxUiState>`; interactions arrive as a
  `sealed interface XxxIntent` through `onIntent(intent)`, not through loose public ViewModel methods.
  Explicit `loading / empty / content / error` states rendered with shared scaffolds. The ViewModel sends
  no one-off events: transient messages are state the UI acknowledges with an intent.
  - **Known gap (planned refactor):** only `AddTransactionViewModel` follows this contract today. Home,
    Categories, Settings and Security still expose loose methods (Settings also a second `StateFlow`), and
    the shared scaffolds don't exist yet. New screens follow the contract; when you touch an existing one,
    keep it consistent and propose its migration instead of mixing both styles.
- **Room is the SSOT:** DAOs return `Flow` and the UI reacts to it; no manual refresh or caches parallel
  to the database. Writes are `suspend`.
- Dispatchers are injected with `@IoDispatcher`; never a hardcoded `Dispatchers.IO`.
- Room schemas are exported to `app/schemas/`; every database version bump ships its migration in
  `DatabaseMigrations`. Never `fallbackToDestructiveMigration`.

## Project rules

- **Design system:** read [DESIGN.md](DESIGN.md) before building or changing UI.
  - Tokens only: `MaterialTheme.colorScheme`, `.financeColors`, `.typography`, `.spacing`, `.shapes`.
    Never `Color(0x…)`, a magic `.dp`, a raw `.sp` or `FontFamily.Default` in a composable.
  - Income and expense use `MaterialTheme.financeColors` (income green, expense coral), never
    `colorScheme.error` or a raw green/red, and always pair color with a sign or icon.
  - Reuse or add "dumb" components in `core/ui/components/` instead of recreating patterns per screen.
  - Every screen and component works in light and dark, with a `@Preview` for each.
  - `DESIGN.md` and the `core/ui/theme/` tokens change in the same commit; the Kotlin theme is the
    runtime source of truth.
- **No business logic or validation in composables:** calculations live in use cases, form validation in
  the ViewModel.
- **Charts are drawn with Compose Canvas, no charting libraries:** two charts don't justify a dependency,
  and custom drawing keeps the APK lean and the visuals on-brand.
- **Tests:** all new logic (use cases, mappers, repositories, ViewModels) comes with unit tests.
  - Hand-rolled fakes that honor the domain contract, not mocks. Turbine for flows, `runTest` with an
    injected test dispatcher. One behavior per test, with a name that describes the case.
  - Kover gate: ≥ 95 % line coverage over domain, data and ViewModels (generated code and framework
    adapters are excluded by design).
  - UI flows run on the JVM with Robolectric, an in-memory Room without SQLCipher and
    `HiltTestApplication`; chosen over an emulator for speed, and Roborazzi reuses the same setup.
  - Roborazzi goldens are versioned in `app/src/test/screenshots/`, with deterministic data and pinned
    size and theme.
- **Dependencies:** only in `gradle/libs.versions.toml` with `version.ref`, stable versions only, and
  each addition justified. No version literals in `build.gradle.kts`.
- **Style** (ktlint enforces it in the build, see `.editorconfig`): `android_studio`, 120 columns,
  4 spaces, LF. **No wildcard imports and no trailing commas.** `@Composable` / `@Preview` functions in
  PascalCase.
- **Comments:** none by default. Only the non-obvious *why* (a decision, a warning, something
  counterintuitive); no KDoc that restates the name.

## Git

- Short branches from `main`, named after the change type: `feat/…`, `fix/…`, `chore/…`.
- Everything reaches `main` through a PR merged with squash; nothing goes directly to `main`, which is
  protected (PR plus green CI). The PR title follows Conventional Commits, since it becomes the commit.
- Commits: Conventional Commits, in English, no emoji, no AI attribution (`Co-Authored-By`, "Generated
  with") in commits, PRs, tags or releases.
- One feature or fix per PR, without touching unrelated code. Commits group what delivers something
  functional (by layer or sub-goal), not one file per commit; each one compiles and passes `codeQuality`
  and the tests.
- The AI doesn't commit, push or tag unless explicitly asked: it leaves the changes and provides the
  message. Ask for approval before `git push`, creating a tag or release, or publishing.

## Versioning and release

- SemVer. PATCH: a fix or a self-contained step. MINOR: a user-visible feature or milestone. MAJOR: a
  breaking change for users.
- The version lives in `gradle/libs.versions.toml` (`appVersion`, read by `versionName` and by Sonar's
  `projectVersion`) plus `versionCode` in `app/build.gradle.kts`, which goes up by 1 on every release.
- `CHANGELOG.md` in English with [Keep a Changelog 1.1.0](https://keepachangelog.com/en/1.1.0/): every
  change goes into `[Unreleased]`, under the standard types in this order: Added, Changed, Deprecated,
  Removed, Fixed, Security. Comparison links at the bottom.
- Cutting a version (through its own PR, `chore(release): x.y.z`): rename `[Unreleased]` to
  `[x.y.z] - YYYY-MM-DD`, add an empty `[Unreleased]`, update the links, bump `appVersion` and
  `versionCode`.
- After merging, every version gets an annotated tag `vX.Y.Z` on its squash commit in `main` and a
  GitHub Release with its CHANGELOG section as notes.
- Tags and releases are created by the person, or by an agent with their approval. CI doesn't run on
  tags, and no workflow creates tags or publishes.
- Not published to any store yet.

## Security

- No backend and no `INTERNET` permission: financial data never leaves the device. Adding network access
  is a product decision, not an implementation detail.
- The SQLCipher passphrase is random (256 bits), sealed with Tink AES-256-GCM, with the key in the
  Android Keystore (`core/database/DatabaseKey`). Never hardcoded, logged or stored in plaintext.
  Critical writes use `commit()`, not `apply()`, so process death can't orphan the database.
- The database files and `finflow_secure_prefs.xml` are excluded from Auto Backup and device transfer
  (`backup_rules.xml` and `data_extraction_rules.xml`): a restored copy can't be decrypted. Any new file
  with sensitive data is excluded in both.
- Biometrics gate entry to the app; they don't derive the database key, so re-enrolling a fingerprint
  doesn't lose data.
- The widget shows amounts masked by default.
- Never log financial data (amounts, descriptions, categories) or secrets.
- Release builds run R8; keep rules in `app/src/main/keepRules/`. GitHub Actions are pinned to a commit
  SHA, and Dependabot keeps them and the Gradle dependencies up to date with a 7-day cooldown. The Gradle
  wrapper distribution is verified with `distributionSha256Sum`.
