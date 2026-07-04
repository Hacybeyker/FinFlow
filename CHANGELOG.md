# Changelog

All notable changes to **FinFlow** are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/)
and this project adheres to [Semantic Versioning](https://semver.org/).

> **Change types:** `Added` (feature), `Fixed` (fix), `Changed` / `Enhancement` (improvement),
> `Deprecated`, `Removed`, `Security`.

## [0.7.0] - 2026-07-04

### Added
- **`CategoryAvatar` shared component** (`core/ui/components/`): the category initial on a 48dp
  circle tinted with its `chartColors` accent (stable per category id). Decorative by design — the
  category name always sits next to it.
- **Tonal surface ladder** in the theme: violet-tinted `surfaceContainerLowest…Highest`,
  `surfaceDim`/`surfaceBright` and `inverse*` roles, so cards and sheets separate from the
  background without shadows.

### Changed
- **Visual redesign of the design system** (`DESIGN.md` + `core/ui/theme/`): the brand moves from
  muted indigo to **electric violet** (`#6C3DF4`) with a teal accent over violet-tinted neutrals,
  under a new principle — *one hero per screen* (a single loud element, everything else quiet).
  Typography turns expressive (ExtraBold displays with −0.5 tracking, Bold headlines/titles;
  body/label untouched) and the shape scale grows to 8/12/20/28/36. The chart palette's slice 0 now
  echoes the brand violet and slice 5 becomes blue. **Income/expense semantics are unchanged** —
  money colors don't rebrand.
- **Home redesign:** the balance is now a hero card on solid `primaryContainer` with
  `onPrimaryContainer` content (a `primary → tertiary` gradient was tried and reverted: it read
  oddly and hurt perceived dark-mode legibility, even while passing AA numerically); transaction
  rows become rounded `surfaceContainerLow` cards with a `CategoryAvatar` (the swipe-delete
  background is clipped to the same shape); the FAB is a labeled extended FAB ("Agregar"); and the
  empty state gains an actionable hint.
- **Amounts never wrap:** `AmountText` gains `maxLines`/`autoSize` (plus a `color` override for
  colored surfaces where the semantic green/coral would fail contrast); the hero balance shrinks
  between `displaySmall` and `headlineSmall` to always stay on one line.
- Launcher icon background is now the brand violet (it still shipped the Android template green);
  `res/values/colors.xml` is emptied — colors live only in the Compose theme (single source of
  truth, no parallel XML palette).

## [0.6.0] - 2026-07-02

### Added
- **App lock (security feature):** the whole UI is now gated behind an `AppLock` composable backed by
  `BiometricPrompt` — fingerprint (STRONG on API 30+, WEAK below where the STRONG+credential combo is
  invalid) with device PIN/pattern fallback. It prompts automatically on launch, keeps an explicit
  "Unlock" retry button on the lock screen, and **re-locks** when the app spends more than 30 s in
  background (the grace period covers rotation, share sheets and quick app switches). The gate fails
  open **only** when the device has no secure lock at all (`KeyguardManager.isDeviceSecure`) — transient
  biometric states still show the prompt. Lock-state logic lives in `SecurityViewModel` (timestamps
  passed as parameters, so it is unit-tested without mocking the clock).
- **Crypto warmup at startup:** `FinFlowApplication` loads the native SQLCipher library and pre-creates
  the Keystore-backed passphrase on `Dispatchers.IO`, so the first DAO injection on the main thread
  finds everything ready (no startup jank). The database provider keeps an idempotent `loadLibrary`
  call as a correctness safety net against the warmup race.

### Changed
- `MainActivity` extends `FragmentActivity` (a `ComponentActivity` subclass, so no Compose behavior
  changes) because androidx.biometric hosts its prompt in a fragment and requires a `FragmentManager`.

### Fixed
- **Donut chart no longer leaves the ring open at 100%:** the 2° separation gap is only applied when
  there is more than one slice, and the last slice now takes the exact remainder to 360° so accumulated
  float rounding can never leave a visible notch.

### Security
- **Database encrypted at rest:** `finflow.db` is now a SQLCipher database (`sqlcipher-android` via
  Room's `openHelperFactory`). The passphrase is a random 256-bit key owned by the new `DatabaseKey`,
  stored in `EncryptedSharedPreferences` under an Android Keystore `MasterKey` — it never touches disk
  in plaintext and never leaves the device. The key is deliberately **not** derived from biometrics
  (re-enrolling a fingerprint cannot brick the data); the rollout recreates an empty encrypted DB with
  a one-time purge of the pre-release plaintext file (no user-facing release ever shipped unencrypted
  data). Writes the DB depends on (passphrase, purge flag) use synchronous `commit()` so a process
  death can never strand an encrypted database without its key.
- **Auto Backup exclusions:** `backup_rules.xml` (API ≤ 30) and `data_extraction_rules.xml` (API 31+,
  cloud backup **and** device transfer) now exclude the encrypted database (with its `-wal`/`-shm`
  files) and the encrypted prefs. The Keystore master key never leaves the device, so a restored copy
  would be undecryptable and crashed the app at startup.

## [0.5.0] - 2026-06-27

### Added
- **Charts feature:** a `charts` screen reachable from a new Home top-bar action, with two views over
  the existing transactions (no new tables): a **spending-by-category donut** for the current month and
  a **monthly income vs. expense bar chart** over a trailing 6-month window.
- **Charts domain:** `GetSpendingByCategoryUseCase` (current-month expenses grouped by category, summed,
  sorted descending) and `GetMonthlyTotalsUseCase` (income/expense per month with empty months as zero),
  with their `CategorySpending` / `MonthlyTotal` models, all unit-tested.
- **Custom Canvas rendering (no chart library):** the donut is drawn with `drawArc` as a stroked ring;
  the bars use plain layout (`Box` + `fillMaxHeight`). Both **animate on load** — the donut reveals
  clockwise via a shared `0→360°` budget and the bars grow from the baseline — and re-animate when data
  changes.
- **Categorical chart palette** added to the theme (`MaterialTheme.chartColors`, light/dark) for donut
  slices, documented in `DESIGN.md`; plus a hand-built bar-chart toolbar icon to avoid pulling in
  `material-icons-extended`.

## [0.4.0] - 2026-06-26

### Added
- **Categories feature:** a `categories` table with its own data layer (`CategoryEntity`/`CategoryDao`/
  mapper + `RoomCategoryRepository`) and domain (`CategoryRepository` + `GetCategories`, `AddCategory`,
  `RenameCategory`, `DeleteCategory` use cases, with a `CategorySaveResult` sealed outcome covering
  Success/Duplicate/InvalidName), all unit-tested.
- **Category management screen** (MVI): list, create, rename (dialog) and delete (with confirmation),
  reachable from an action in the Home top bar.
- **Edit a transaction:** tapping a Home row opens the Add screen reused in edit mode (prefilled, titled
  "Editar movimiento" / "Guardar cambios"). The `AddTransaction` nav key now carries an optional
  `transactionId`; the ViewModel branches between add and update on the row id. Backed by
  `UpdateTransactionUseCase`, `GetTransactionByIdUseCase` and a `getById` DAO JOIN query.
- **Delete a transaction:** swipe a Home row left to delete it (`DeleteTransactionUseCase`), with a
  snackbar that **undoes** the delete by re-inserting the row with its original id.

### Changed
- **Normalized the category relation (DB v3):** transactions now reference a category through a
  `categoryId` **foreign key** (`ON DELETE CASCADE`, indexed) instead of the denormalized `categoryName`
  snapshot; the name is resolved on read with a JOIN via the new `TransactionWithCategory` relation.
  Non-destructive `MIGRATION_2_3` re-seeds missing names, matches each snapshot to a category id and
  rebuilds the table (SQLite can't `ALTER` to add a FK). Schema v3 exported to `app/schemas/`.
- Database schema bumped **v1 → v2 → v3** with hand-written, non-destructive migrations
  (`MIGRATION_1_2` creates and seeds the `categories` table).

## [0.3.0] - 2026-06-25

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
  by `HomeViewModel` (a `StateFlow` that combines balance + list into a single `Content` state, plus
  `Loading`), unit-tested with Turbine. Reactive: a new transaction shows up instantly, and the
  all-time balance stays correct even when the current month has no movements.
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
