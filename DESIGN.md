# DESIGN.md

The design system for **FinFlow** — the visual contract that humans and AI agents follow when
creating screens and components in Jetpack Compose. It defines the *intent* behind every color,
type style, spacing and shape so the UI stays consistent across features.

> **Companion docs:** product vision → [README.md](README.md) · engineering standards →
> [AGENTS.md](AGENTS.md). This file owns **how the app looks**.

---

## ⚠️ Source of truth & the no-drift rule

This document describes **intent**. The **runtime source of truth** is the Kotlin theme under
`app/src/main/java/com/hacybeyker/finflow/ui/theme/`. They must always agree.

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

1. **Calm, not loud.** Visual impact comes from hierarchy, generous spacing and confident
   typography — not from saturated colors. The palette is muted and easy on the eyes.
2. **Money is semantic.** Income is always green, expense always coral. These meanings are fixed and
   never change with theme or wallpaper.
3. **Consistency over cleverness.** Reuse a token or a shared component before inventing a new value.
4. **Legible first.** Comfortable sizes, ample line height and AA contrast in both themes.
5. **First-class dark mode.** Light and dark are designed in parallel, not retrofitted.

---

## Color system

A calm, muted **indigo** brand with soft neutrals, paired with dedicated **income/expense**
semantics. Every role below maps 1:1 to a token in code.

### Brand & neutral roles (Material 3)

| Role | Purpose | Light | Dark |
|------|---------|-------|------|
| `primary` | Main actions, active states, key emphasis | `#4A5BC4` | `#BBC3FF` |
| `onPrimary` | Content on `primary` | `#FFFFFF` | `#152178` |
| `primaryContainer` | Tinted emphasis surfaces (selected chips, highlights) | `#DFE0FF` | `#323D90` |
| `secondary` | Supporting accents, less prominent controls | `#5A5D72` | `#C3C5DD` |
| `tertiary` | Soft contrast accent (badges, subtle highlights) | `#3F6375` | `#A7CCDF` |
| `error` | Validation/error states (not "expense") | `#BA1A1A` | `#FFB4AB` |
| `background` / `surface` | App and component backgrounds | `#FBF8FD` | `#131318` |
| `onSurface` | Primary text/icons | `#1B1B21` | `#E4E1E9` |
| `surfaceVariant` | Dividers fill, subtle containers | `#E3E1EC` | `#46464F` |
| `onSurfaceVariant` | Secondary text, icons | `#46464F` | `#C7C5D0` |
| `outline` | Borders, outlined fields | `#777680` | `#918F9A` |

> Full `on*` / container pairs are defined in `ui/theme/Color.kt`; the table lists the most-used
> roles. Use `error` only for failures (e.g. invalid form), **not** to color a spent amount — that's
> `financeColors.expense`.

### Semantic finance roles (`MaterialTheme.financeColors`)

Brand-defined and **independent of dynamic color** — a positive amount is green everywhere (list,
detail, charts, widget), a negative one coral.

| Role | Purpose | Light | Dark |
|------|---------|-------|------|
| `income` | Positive amounts, income emphasis | `#2E6B4F` | `#98D8B1` |
| `incomeContainer` | Income chips/backgrounds | `#B4F1CC` | `#135137` |
| `expense` | Negative amounts, expense emphasis | `#B3403E` | `#FFB3AD` |
| `expenseContainer` | Expense chips/backgrounds | `#FFDAD6` | `#8C2A29` |

### Dynamic color decision

`FinFlowTheme(dynamicColor = false)` **by default**, so the brand palette is consistent on every
device. Dynamic color (Material You) is still supported and may be opted in from a user preference,
but the **finance semantics never follow the wallpaper** — they always use the values above.

---

## Typography

Material 3 type scale, defined in full in `ui/theme/Type.kt`. Pick a **role**, never a raw size.

- **Brand font:** Inter (intended) for its excellent legibility and tabular figures, ideal for
  amounts. **Current default:** the platform sans-serif (`FinFlowFontFamily`) — zero APK weight and
  legible everywhere. Switching is a one-line change in `Type.kt`; all styles inherit it.
- **Hierarchy:** `display*` for hero numbers (e.g. monthly balance) · `headline*`/`title*` for
  section and screen titles · `body*` for content · `label*` for buttons, chips and captions.
- **Amounts:** use a `title`/`*Medium` weight so figures read with authority; keep currency symbol
  and digits in the same style.

| Role | Size / Line | Weight | Typical use |
|------|-------------|--------|-------------|
| `displaySmall` | 36 / 44 | Normal | Hero balance figure |
| `headlineSmall` | 24 / 32 | SemiBold | Screen title |
| `titleLarge` | 22 / 28 | SemiBold | Card / section title |
| `titleMedium` | 16 / 24 | Medium | List item amount/title |
| `bodyMedium` | 14 / 20 | Normal | Default body text |
| `labelLarge` | 14 / 20 | Medium | Buttons |
| `bodySmall` / `labelSmall` | 12–11 | Normal/Medium | Captions, metadata |

---

## Spacing

4dp-based scale in `ui/theme/Spacing.kt`, consumed via `MaterialTheme.spacing`. Never write a magic
`.dp` for layout spacing.

| Token | Value | Typical use |
|-------|-------|-------------|
| `xxs` | 2dp | Hairline gaps |
| `xs` | 4dp | Icon ↔ text |
| `sm` | 8dp | Compact gaps, chip padding |
| `md` | 16dp | Default padding, list item, screen gutter (`screen`) |
| `lg` | 24dp | Section separation |
| `xl` | 32dp | Large blocks |
| `xxl` | 48dp | Empty-state / hero spacing |

---

## Shape

Soft, generous rounding (`ui/theme/Shape.kt`), via `MaterialTheme.shapes`.

| Token | Radius | Use |
|-------|--------|-----|
| `extraSmall` | 4dp | Tags, small indicators |
| `small` | 8dp | Inputs, small buttons |
| `medium` | 16dp | **Cards** (balance, transactions) |
| `large` | 24dp | Dialogs, bottom sheets |
| `extraLarge` | 32dp | Hero / modal surfaces |

---

## Components & UI states

Shared, "dumb" components live in `ui/components/` and are reused by every feature instead of being
re-created per screen. A component renders state and emits events; it holds no business logic
(see [AGENTS.md](AGENTS.md)).

- **Build a shared component** when a visual pattern appears in 2+ places (amount text, section
  header, transaction row, primary button, chips).
- **Encapsulate semantic rules** in components — e.g. an `AmountText` decides green vs. coral from
  `financeColors`, so screens never branch on color.
- **Standardize the four MVI states** with shared `Loading`, `Empty`, `Error` and `Content`
  scaffolds so every screen looks the same in each state.

---

## Accessibility

- **Contrast:** text/icons meet **WCAG AA** (≥ 4.5:1 body, ≥ 3:1 large) in both themes. New color
  pairings must be checked before adoption.
- **Don't rely on color alone:** pair income/expense color with a sign (`+`/`−`) or icon.
- **Touch targets:** interactive elements ≥ **48×48dp**.
- **Dynamic type:** sizes in `sp`; layouts tolerate larger system font scales without clipping.

---

## Checklist before merging UI

- [ ] No hardcoded colors / sizes / fonts — only theme tokens.
- [ ] Verified in **light and dark**.
- [ ] Income/expense use `financeColors` (+ sign/icon), not `error`/raw green.
- [ ] Reused or added a shared `ui/components/` element; explicit loading/empty/error/content.
- [ ] AA contrast and ≥ 48dp touch targets.
- [ ] `@Preview` (light + dark); Roborazzi baseline updated if the look changed.
- [ ] This file and `ui/theme/` still agree.
