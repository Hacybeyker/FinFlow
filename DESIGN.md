# DESIGN.md

The design system for **FinFlow** — the visual contract that humans and AI agents follow when
creating screens and components in Jetpack Compose. It defines the *intent* behind every color,
type style, spacing and shape so the UI stays consistent across features.

> **Companion docs:** product vision → [README.md](README.md) · engineering standards →
> [AGENTS.md](AGENTS.md). This file owns **how the app looks**.

---

## ⚠️ Source of truth & the no-drift rule

This document describes **intent**. The **runtime source of truth** is the Kotlin theme under
`app/src/main/java/com/hacybeyker/finflow/core/ui/theme/`. They must always agree.

- **If you change a value here, change the matching token in `ui/theme/` in the same commit** (and
  vice versa). A divergence between this file and the code is a bug.
- In any `@Composable`, **never** hardcode `Color(0x…)`, a magic `.dp`, a raw `.sp`/`fontSize` or
  `FontFamily.Default`. Always consume a token: `MaterialTheme.colorScheme.*`,
  `MaterialTheme.financeColors.*`, `MaterialTheme.typography.*`, `MaterialTheme.spacing.*`,
  `MaterialTheme.shapes.*`.

| Concern | Document (intent) | Code (runtime SSOT) |
|---------|-------------------|---------------------|
| Colors | this file | `ui/theme/Color.kt`, `ui/theme/FinanceColors.kt`, `ui/theme/Theme.kt` |
| Typography | this file | `ui/theme/Type.kt` |
| Spacing | this file | `ui/theme/Spacing.kt` |
| Shapes | this file | `ui/theme/Shape.kt` |

---

## Design principles

1. **One hero per screen.** Each screen has exactly **one** loud element (the gradient balance card,
   the donut, the primary CTA); everything else stays quiet. Impact comes from that contrast — a
   screen where everything shouts, whispers.
2. **Money is semantic.** Income is always green, expense always coral. These meanings are fixed and
   never change with theme or wallpaper.
3. **Simple to use beats clever to look at.** Bold visuals never cost a tap: primary actions are
   labeled (extended FAB), touch targets are big, flows stay short.
4. **Consistency over cleverness.** Reuse a token or a shared component before inventing a new value.
5. **Legible first.** Comfortable sizes, ample line height and AA contrast in both themes.
6. **First-class dark mode.** Light and dark are designed in parallel, not retrofitted. Switching
   theme never snaps: `FinFlowTheme` cross-fades every color (scheme + finance + chart palettes,
   500 ms) so the whole screen transitions as one.

---

## Color system

An **electric violet** brand with a fresh **teal** accent over violet-tinted neutrals, paired with
dedicated **income/expense** semantics. The violet powers the hero surface and the primary CTA;
neutrals keep everything else calm. Every role below maps 1:1 to a token in code.

### Brand & neutral roles (Material 3)

| Role | Purpose | Light | Dark |
|------|---------|-------|------|
| `primary` | Main actions (FAB/CTA), active states | `#6C3DF4` | `#CFBDFF` |
| `onPrimary` | Content on `primary` | `#FFFFFF` | `#3813A0` |
| `primaryContainer` | **Hero surface** (balance card), selected chips, highlights | `#E9DDFF` | `#5224C4` |
| `secondary` | Supporting accents, less prominent controls | `#5F5A7D` | `#C9C2E8` |
| `tertiary` | Fresh accent (badges, subtle highlights) | `#00696B` | `#80D4D6` |
| `error` | Validation/error states (not "expense") | `#BA1A1A` | `#FFB4AB` |
| `background` / `surface` | App and component backgrounds | `#FBF8FF` | `#14121A` |
| `onSurface` | Primary text/icons | `#1C1B22` | `#E6E1EB` |
| `surfaceVariant` | Dividers fill, subtle containers | `#E6E0F2` | `#48454F` |
| `onSurfaceVariant` | Secondary text, icons | `#48454F` | `#CBC4D8` |
| `outline` | Borders, outlined fields | `#7A7684` | `#948F9E` |

**Tonal surface ladder** (violet-tinted elevation without shadows): list rows and quiet cards sit on
`surfaceContainerLow` (`#F5F1FC` / `#1C1A22`); sheets, menus and dialogs climb to
`surfaceContainerHigh` (`#E9E5F1` / `#2B2831`). The full ladder
(`surfaceDim/Bright`, `surfaceContainerLowest…Highest`, `inverse*`) is defined in
`ui/theme/Color.kt` and wired in `Theme.kt`.

> Full `on*` / container pairs are defined in `ui/theme/Color.kt`; the table lists the most-used
> roles. Use `error` only for failures (e.g. invalid form), **not** to color a spent amount — that's
> `financeColors.expense`.

### The hero surface

The signature FinFlow surface: a **solid `primaryContainer`** card clipped to `shapes.large` — soft
lavender in light, deep violet in dark, both with high-contrast `onPrimaryContainer` content. Rules:

- **At most one hero surface per screen** (today: the Home balance card).
- Content on it is always `onPrimaryContainer` (supporting text may soften to 85% alpha). Never
  place semantic green/coral text on it — it fails contrast; `AmountText` accepts a `color`
  override for exactly this case.
- **No gradients.** A solid brand surface reads calmer and keeps text contrast predictable in both
  themes (a violet→teal gradient was tried and reverted: it looked odd and hurt dark-mode
  legibility).
- The hero figure never wraps: `maxLines = 1` + `TextAutoSize` between `headlineSmall` and
  `displaySmall`, so a long balance shrinks instead of breaking onto a second line.

### Semantic finance roles (`MaterialTheme.financeColors`)

Brand-defined and **independent of dynamic color** — a positive amount is green everywhere (list,
detail, charts, widget), a negative one coral.

| Role | Purpose | Light | Dark |
|------|---------|-------|------|
| `income` | Positive amounts, income emphasis | `#2E6B4F` | `#98D8B1` |
| `incomeContainer` | Income chips/backgrounds | `#B4F1CC` | `#135137` |
| `expense` | Negative amounts, expense emphasis | `#B3403E` | `#FFB3AD` |
| `expenseContainer` | Expense chips/backgrounds | `#FFDAD6` | `#8C2A29` |

### Categorical chart palette (`MaterialTheme.chartColors`)

A qualitative scale Material 3 does not provide, for category breakdowns (donut slices) **and
category avatars** (initial over the accent at 16% tint). Slice 0 echoes the violet brand so charts
feel like FinFlow at a glance; the rest stay mutually distinguishable and carry **no** income/expense
meaning. Cycled by index via `chartColors.colorAt(index)` — donuts key by slice position, avatars by
category id (stable per category). Never hardcode a slice color. Defined in `ui/theme/Color.kt`
(`ChartSliceLight` / `ChartSliceDark`).

| # | Light | Dark | Hue |
|---|-------|------|-----|
| 0 | `#6D4FDB` | `#B7A6F8` | violet (brand echo) |
| 1 | `#3F8A8B` | `#7FC9CA` | teal |
| 2 | `#C0852E` | `#E5C07B` | amber |
| 3 | `#B85C8A` | `#E89BC0` | rose |
| 4 | `#5B8C4F` | `#A6CF9A` | green |
| 5 | `#4A78C8` | `#9DBCF0` | blue |

### Dynamic color decision

`FinFlowTheme(dynamicColor = false)` **by default**, so the brand palette is consistent on every
device. Dynamic color (Material You) is still supported and may be opted in from a user preference,
but the **finance semantics never follow the wallpaper** — they always use the values above.

### Launcher

The adaptive icon background is the brand violet `#6C3DF4` (solid, crisp at every size) —
`res/drawable/ic_launcher_background.xml` must match `primary` (light). XML resources hold **no**
other palette (`res/values/colors.xml` stays empty); colors live in Compose.

---

## Typography

Material 3 type scale with an **expressive twist**: display/headline/title roles are heavy
(ExtraBold/Bold) with tight tracking so hero numbers and titles carry the impact, while body/label
stay quiet and legible. Defined in full in `ui/theme/Type.kt`. Pick a **role**, never a raw size.

- **Brand font:** Inter (intended) for its excellent legibility and tabular figures, ideal for
  amounts. **Current default:** the platform sans-serif (`FinFlowFontFamily`) — zero APK weight and
  legible everywhere. Switching is a one-line change in `Type.kt`; all styles inherit it.
- **Hierarchy:** `display*` for hero numbers (e.g. monthly balance) · `headline*`/`title*` for
  section and screen titles · `body*` for content · `label*` for buttons, chips and captions.
- **Amounts:** list amounts use `titleMedium` (SemiBold); the hero balance uses `displaySmall`
  (ExtraBold, −0.5 tracking) so the number *is* the interface. Keep currency symbol and digits in
  the same style. **An amount never wraps** — display figures auto-shrink to one line
  (`TextAutoSize`, floor `headlineSmall`).

| Role | Size / Line | Weight / Tracking | Typical use |
|------|-------------|-------------------|-------------|
| `displaySmall` | 36 / 44 | ExtraBold · −0.5 | Hero balance figure |
| `headlineSmall` | 24 / 32 | Bold · 0 | Screen title |
| `titleLarge` | 22 / 28 | Bold · 0 | Card / section title, top bar |
| `titleMedium` | 16 / 24 | SemiBold · 0.1 | List item amount/title |
| `bodyMedium` | 14 / 20 | Normal · 0.25 | Default body text |
| `labelLarge` | 14 / 20 | SemiBold · 0.1 | Buttons, hero label |
| `bodySmall` / `labelSmall` | 12–11 | Normal/Medium | Captions, metadata |

> `displayLarge/Medium` are ExtraBold · −0.5, `headlineLarge` Bold · −0.25, `headlineMedium` Bold,
> `titleSmall` SemiBold — see `Type.kt` for the full scale.

---

## Spacing

4dp-based scale in `ui/theme/Spacing.kt`, consumed via `MaterialTheme.spacing`. Never write a magic
`.dp` for layout spacing.

| Token | Value | Typical use |
|-------|-------|-------------|
| `xxs` | 2dp | Hairline gaps |
| `xs` | 4dp | Icon ↔ text |
| `sm` | 8dp | Compact gaps, chip padding, list row gap |
| `md` | 16dp | Default padding, list item, screen gutter (`screen`) |
| `lg` | 24dp | Section separation, hero padding |
| `xl` | 32dp | Large blocks |
| `xxl` | 48dp | Empty-state / hero spacing, avatar size |

---

## Shape

Generous, confident rounding is part of the brand signature (`ui/theme/Shape.kt`), via
`MaterialTheme.shapes`. Nothing in FinFlow reads sharp.

| Token | Radius | Use |
|-------|--------|-----|
| `extraSmall` | 8dp | Tags, small indicators |
| `small` | 12dp | Inputs, small buttons |
| `medium` | 20dp | **List rows / cards** (transactions, chart cards) |
| `large` | 28dp | **Hero balance**, dialogs, bottom sheets |
| `extraLarge` | 36dp | Full-bleed modal surfaces |

---

## Components & UI states

Shared, "dumb" components live in `ui/components/` and are reused by every feature instead of being
re-created per screen. A component renders state and emits events; it holds no business logic
(see [AGENTS.md](AGENTS.md)).

- **Build a shared component** when a visual pattern appears in 2+ places (amount text, section
  header, transaction row, primary button, chips).
- **Encapsulate semantic rules** in components — e.g. `AmountText` decides green vs. coral from
  `financeColors` (with a `color` override only for colored surfaces like the hero), so screens
  never branch on color.
- **Standardize the four MVI states** with shared `Loading`, `Empty`, `Error` and `Content`
  scaffolds so every screen looks the same in each state. Empty states are an invitation, not a
  dead end: title + one-line hint pointing at the primary action.

### Signature elements (the FinFlow look)

| Element | Recipe |
|---------|--------|
| **Hero balance** | solid `primaryContainer` · `shapes.large` · `onPrimaryContainer` text · `displaySmall` figure, one line + auto-shrink |
| **Category avatar** | `CategoryAvatar`: initial on a 48dp circle, `chartColors` accent at 16% tint (decorative — name always adjacent) |
| **List row** | `surfaceContainerLow` · `shapes.medium` · avatar + title/date + `AmountText` |
| **Primary CTA** | Extended FAB, `primary`/`onPrimary`, always labeled (icon-only FABs don't teach the app) |

---

## Accessibility

- **Contrast:** text/icons meet **WCAG AA** (≥ 4.5:1 body, ≥ 3:1 large) in both themes. New color
  pairings must be checked before adoption (the hero pairs `onPrimaryContainer` on
  `primaryContainer`: ≈ 12:1 in light, ≈ 6:1 in dark). Checking the math is necessary but not
  sufficient — verify perceptually in dark mode too.
- **Don't rely on color alone:** pair income/expense color with a sign (`+`/`−`) or icon; category
  avatars are decorative and always accompanied by the category name.
- **Touch targets:** interactive elements ≥ **48×48dp**.
- **Dynamic type:** sizes in `sp`; layouts tolerate larger system font scales without clipping.

---

## Checklist before merging UI

- [ ] No hardcoded colors / sizes / fonts — only theme tokens.
- [ ] Verified in **light and dark**.
- [ ] Income/expense use `financeColors` (+ sign/icon), not `error`/raw green.
- [ ] At most **one hero surface** per screen; everything else on the quiet surface ladder.
- [ ] Reused or added a shared `ui/components/` element; explicit loading/empty/error/content.
- [ ] AA contrast and ≥ 48dp touch targets.
- [ ] `@Preview` (light + dark); Roborazzi baseline updated if the look changed.
- [ ] This file and `ui/theme/` still agree.
