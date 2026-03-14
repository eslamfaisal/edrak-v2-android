# 🎨 Design System

Edrak uses a dark-first design system with the **Space Grotesk** typeface and a neon cyan accent color.

## Color Palette

| Token | Hex | Usage |
|-------|-----|-------|
| `primary` | `#45F0DF` | Accents, active states, CTAs, glow effects |
| `background-dark` | `#102220` | Main dark-mode background |
| `background-light` | `#F6F8F8` | Main light-mode background |
| `card-dark` | `#111D38` | Glass-card backgrounds |
| `nav-dark` | `#060A18` | Bottom navigation background |
| `slate-100` | `#F1F5F9` | Primary text on dark |
| `slate-400` | `#94A3B8` | Secondary text / labels |
| `slate-500` | `#64748B` | Tertiary / timestamps |
| `slate-800` | `#1E293B` | Inactive chip backgrounds |
| `blue-400` | `#60A5FA` | Info accent (tasks, recent note) |
| `orange-400` | `#FB923C` | Warning accent (reminders, priority) |
| `chat-user` | `#1E3058` | User chat bubble background |

## Typography

All text uses **Space Grotesk** exclusively.

| Style | Weight | Size | Usage |
|-------|--------|------|-------|
| Display | 700 | 24sp | Section titles ("Active Listening") |
| Heading | 700 | 18sp | Section headers ("Today's Pulse") |
| Body | 400 | 16sp | Card content text |
| Label | 700 | 10sp | Category badges, nav labels |
| Caption | 500 | 12sp | Timestamps, secondary info |
| Stat | 700 | 24sp | Pulse card numbers |

### Platform Implementation

=== "Android"

    5 font weights bundled in `res/font/`:
    `space_grotesk_light.ttf`, `_regular.ttf`, `_medium.ttf`, `_semibold.ttf`, `_bold.ttf`

    ```kotlin
    val SpaceGrotesk = FontFamily(
        Font(R.font.space_grotesk_light, FontWeight.Light),
        Font(R.font.space_grotesk_regular, FontWeight.Normal),
        Font(R.font.space_grotesk_medium, FontWeight.Medium),
        Font(R.font.space_grotesk_semibold, FontWeight.SemiBold),
        Font(R.font.space_grotesk_bold, FontWeight.Bold),
    )
    ```

=== "iOS"

    System font fallback (Phase A). Future: bundled custom font via SPM.

## Shared Components

### Glass Card

Dark translucent card with primary border accent.

| Property | Value |
|----------|-------|
| Background | `card-dark` at 60% opacity |
| Border | 1px `primary/10` |
| Border radius | 24dp |

### Category Badge

Uppercase pill label for classification display.

| Property | Value |
|----------|-------|
| Background | `primary/10` |
| Border | 1px `primary/20` |
| Text | 10sp bold uppercase, `primary` color |

### Filter Chip

Capsule-shaped filter for vault/list screens.

| State | Background | Text | Border |
|-------|-----------|------|--------|
| Active | `primary/20` | `primary` | `primary/30` |
| Inactive | `slate-800/50` | `slate-400` | transparent |

### Stat Card

Metric display card for digest stats.

| Property | Value |
|----------|-------|
| Background | `primary/5` |
| Border | 1px `primary/10` |
| Border radius | 16dp |

### Glow Button

CTA button with shadow glow effect.

| Property | Value |
|----------|-------|
| Background | `primary` solid |
| Text | `background-dark`, bold |
| Shadow | 20px `primary/30` |

## Bottom Navigation

4 tabs with dark background and primary accent.

| Property | Value |
|----------|-------|
| Background | `#060A18` (nav-dark) |
| Active color | `#45F0DF` (primary) |
| Inactive color | `#64748B` (slate-500) |
| Labels | 10sp bold uppercase, 1sp letter spacing |
| Tabs | Home, Vault, Chat, Digest |
| Active icon | Filled variant |
| Inactive icon | Outlined variant |

## Screens

| Screen | Key Components |
|--------|---------------|
| **Home Dashboard** | Hero Mic Hub (192px, pulsing glow), Today's Pulse cards, Quick Insights cards |
| **Memory Vault** | Sticky header + search, filter tabs, memory cards (left border + badge + checkbox) |
| **Ask Edrak Chat** | Glass header, user/AI bubbles, typing indicator, rounded pill input bar |
| **Daily Digest** | AI Summary card, pending task checkboxes, stats grid, archive CTA |
