# Kintsu (V1) — Architectural Decisions & Assumptions

This document records all implementation decisions and assumptions made during the development of Kintsu V1 in accordance with the project brief.

---

### 1. Navigation & App Shell
- **Four Bottom Tabs:** Habits (Home), Tasks, Goals, and Progress.
- **Top Bar Placement:** Every screen features an edge-to-edge frosted/translucent top bar displaying the screen title/greeting on the left and a dedicated Profile/Settings icon button on the right (never in the bottom navigation).
- **Settings Presentation:** Profile/Settings opens as a focused full-screen overlay/modal with inset grouped card sections and a top close button, ensuring no bottom bar pollution.

---

### 2. Kintsugi Philosophy & Habit Healing
- **Non-Punitive Progress:** A missed habit day is never displayed with harsh red markers or "failure" badges.
- **Gold Seam Repair:** Missed days can be redeemed/mended with gold seams (`#B8935A`). The habit list and progress calendar distinguish between standard completions and gold-mended days, reinforcing that broken pottery repaired with gold is more resilient and beautiful.
- **Streak Calculation:** Active streak counts days where rituals were maintained or mended with gold, ensuring continuous motivation without streak anxiety.

---

### 3. Task Management & Goal Linkage
- **Segmented Views:** Tasks are filtered into three calm tabs: **Today** (due today or unassigned), **Upcoming** (due in the future), and **Done** (completed tasks).
- **Goal Linkage:** Tasks optionally link to any active Horizon Goal. When a task is linked, it displays a subtle goal pill.
- **Priority System:** Three serene levels (Low, Medium, High) represented with subtle typography accents rather than aggressive caution flags.

---

### 4. Goals & Automatic Progress Calculation
- **Two Progress Modes:**
  1. `AUTO_FROM_ITEMS`: Calculates progress percentage dynamically as `(completed linked tasks / total linked tasks) * 100`.
  2. `MANUAL`: Allows the user to set a percentage slider (0–100%) for abstract or long-term horizons.
- **Linked Items Visibility:** Each goal card displays a preview of its associated tasks and their completion state.

---

### 5. Settings Store & Live Reactive Theming
- **Single Source of Truth:** AndroidX DataStore Preferences powers `KintsuSettings`.
- **Live Theme Switching:** Supports `LIGHT`, `DARK`, and `SYSTEM` modes, along with five curated accent color options (`Kintsugi Gold`, `Dusty Sage`, `Terracotta Clay`, `Muted Lavender`, `Warm Stone`). Any change in settings immediately updates colors and styles across all screens through `CompositionLocalProvider`.
- **First-Launch Onboarding:** Controlled via a DataStore flag `onboarding_completed`. If false, a 3-step serene introductory flow guides the user before entering the main workspace.

---

### 6. Data Export & Reset
- **Export Format:** Self-contained JSON format containing all user habits, logs, tasks, and goals. Displayed inside a bottom sheet with a one-tap "Copy to Clipboard" action, avoiding unnecessary storage permissions.
- **Reset Safety:** Resetting data triggers a confirmation alert dialog before wiping the Room database and DataStore preferences.

---

### 7. Accessibility & Touch Targets
- **Minimum 48dp Touch Targets:** All clickable icons, chips, buttons, and checkbox circles are enclosed in at least 48dp bounding boxes.
- **Semantics:** Explicit accessibility roles (`Role.Button`, `Role.Tab`, `Role.Checkbox`) and clear English `contentDescription` attributes are attached to all interactive components.
- **Text Contrast:** High-contrast text tokens (`#1E1B18` on light, `#F6F3ED` on dark) meet WCAG AA standards across all soft surface containers.

---

### 8. Build & Verification
- **Build Output:** Validated debug APK successfully produced via Gradle 8.9 / AGP 8.5.2 / Kotlin 2.0.0 (`assembleDebug`):
  - Location: `app/build/outputs/apk/debug/app-debug.apk` (Size: ~12.6 MB).
  - Package ID: `com.kintsu.app.debug`, minSdk 26, targetSdk 34.
  - Verified with AAPT2 badging: launchable `MainActivity`, bundled Inter fonts, vector drawables, edge-to-edge support.

---

### 9. Kintsu V1.5 Extensions & Assumptions

#### Gold Repair (Signature Kintsugi Feature)
- **Repair Window:** Configured in `GoldRepairConfig.REPAIR_WINDOW_HOURS = 48`. A missed scheduled day can be mended within 48 hours of day close.
- **Weekly Repair Limit:** Configured in `GoldRepairConfig.MAX_REPAIRS_PER_HABIT_PER_WEEK = 1`. Each habit can claim at most 1 gold repair per calendar week (Monday to Sunday).
- **Streak Preservation:** When Gold Repair is enabled, a gold-mended day bridges the streak seamlessly. When Gold Repair is disabled in Settings, missed days cause normal streak reset.
- **Visual Distinction:** In `ProgressScreen`, days are distinguished by shape and icon in addition to color (Fulfilled: solid circle; Gold Seam: squircle with 45° diagonal gold seam; Crack: broken border square with fracture line; Rest: quiet dot).
- **Animated Seam:** Completing a repair draws a 400ms gold seam along the card, immediately bypassed if system "reduce motion" is active.

#### Home-Screen Widgets (Jetpack Glance)
- **Today's Habits Widget:** Displays up to 4 scheduled habits for today with interactive checkbox circles that update Room DB and refresh all widgets instantly via `ActionCallback`.
- **Streak Counter Widget:** Displays the current streak in large typography with gold seam badge ("✦ X Mended"). Tapping launches the app to the Progress tab.
- **Styling:** Adheres to the central design tokens (`#F4EFE7` paper, `#EBE3D7` surface, `#B8935A` gold, `#2B2622` ink).

#### Smart Reminders & Exact Alarms
- **Multi-Time Support:** Habits support multiple reminder times stored as comma-separated `"HH:mm"` strings.
- **Only-If-Incomplete Mode:** `notifyOnlyIfNotDone` setting checks the Room log for today before displaying the notification, silencing alerts if already fulfilled.
- **Interactive Actions:** Direct notification action buttons for **Done** (marks habit completed immediately and updates widgets) and **Snooze** (10m / 30m).
- **Resilience:** `BootAndAlarmReceiver` reschedules all active alarms on `BOOT_COMPLETED`, `TIME_SET`, and `TIMEZONE_CHANGED`. Runtime `POST_NOTIFICATIONS` permission requested on Android 13+.

#### Backup & Restore (Storage Access Framework)
- **SAF Export:** User picks destination file to write versioned JSON backup (`schemaVersion: 2`).
- **SAF Import:** Validates JSON schema, checks `"app": "Kintsu"`, and prompts the user with an explicit modal choice: **Replace** (clean slate restore) or **Merge** (intelligently deduplicates by name and updates logs).

#### Tier 2 Capabilities
- **Completion Note & Mood Tag:** Prompts an optional one-line note and one of 5 serene mood tags (`🌿 Calm`, `⛰ Grounded`, `☀️ Energized`, `🪷 Peaceful`, `🤍 Grateful`) upon completing a habit.
- **Flexible Frequency:** Extends `FrequencyType` with `TIMES_PER_WEEK` and `TIMES_PER_DAY` with adjustable target counts.
- **Habit Stacking:** Routine groups (`Morning`, `Evening`, or custom) selectable on Home via filter chips.

#### Database Migrations & Safety
- **No Destructive Migrations:** Room database migrated from Version 2 to Version 3 via `MIGRATION_2_3`. Destructive migration fallback was intentionally removed.
- **Automated Tests:** 10 automated unit tests cover Gold Repair domain logic, streak calculation edge cases, migration SQL execution, and backup JSON validation.

---

### 10. Phase 0 Stability Fixes

#### Issue 1: Device Layout Adaptation & Insets
- **Full Edge-to-Edge & Insets:** Configured `enableEdgeToEdge()` and applied proper `WindowInsets` handling across status bars, navigation bars, display cutouts, and the software keyboard (IME).
- **IME & System Bar Overlap Prevention:** `KintsuBottomSheet` incorporates `imePadding()` and `navigationBarsPadding()` alongside a scrollable container (`verticalScroll`), preventing keyboards or system bars from occluding input fields or confirmation actions.
- **Adaptive Screen Constraint:** Root tab content in `KintsuApp`, `SettingsScreen`, and `OnboardingScreen` is capped to `640dp` maximum width and horizontally centered (`widthIn(max = 640.dp)`), ensuring balanced layouts across small phones (360dp), large phones, foldables, and tablets.

#### Issue 2: Font & Text Scaling (Up to 200%)
- **Flexible Container Hierarchy:** Eliminated rigid fixed element heights that caused text truncation or clipping under enlarged system accessibility font scales (up to 200%).
- **Accessible Touch & Chip Scaling:** `KintsuChip` and interactive components maintain a minimum 48dp touch target with multi-line text wrapping.
- **Scroll Containment:** Modal sheets and dialogs allow complete vertical scrolling so all options and buttons remain reachable regardless of font magnification.

#### Issue 3: Back Stack & Predictive Back Navigation
- **Predictive Back Support:** Enabled `android:enableOnBackInvokedCallback="true"` in `AndroidManifest.xml`.
- **Hierarchical Back Order:**
  1. Dismiss open dialogs, mood sheets, or the Settings modal.
  2. For add/edit sheets (Habits, Tasks, Goals), prompt with an unsaved changes confirmation dialog before discarding modified data.
  3. Non-Home tabs (Tasks, Goals, Progress) backtrack through visited tabs to Home (`Habits`).
  4. From Home (`Habits`), back exits the application.
- **Dedicated Back Stack Engine:** Implemented `NavigationBackStack` to preserve tab state without duplicate stack pollution. Validated with automated unit tests (`NavigationBackStackTest`).

#### Issue 4: Mindful Permission Request Architecture
- **Eliminated Startup Flashes:** Completely removed automatic permission requests on `MainActivity` launch and composition.
- **Dedicated Onboarding Step:** Notification permission is introduced in a calm, explicit step ("Mindful Reminders") with an explanatory description and dedicated action button.
- **Recomposition & Re-prompting Resilience:** Launcher request state is preserved via `rememberSaveable`. If denied or dismissed, the app remains fully functional without repeatedly nagging on subsequent launches, allowing the user to quietly enable reminders in Settings whenever ready.

---

### 11. Kintsu V2 Architecture & Feature Implementation

#### 1. Mindful Focus Timer
- **Timestamp-Derived Timing:** Session remaining time is strictly derived from `(targetEndTimeMillis - nowMillis) / 1000`, guaranteeing that timer accuracy is immune to OS thread sleep, CPU throttling, screen turn-off, or app suspension.
- **Foreground Service Integration:** Managed by `FocusForegroundService` with `specialUse` foreground service type (targetSdk 34 compliant) and a low-priority ongoing notification featuring Pause/Resume and Stop actions.
- **Habit Linking & Gold Repair Harmony:** An active focus session can link to any scheduled ritual. Upon reaching completion, if the habit has a repairable crack within the 48h repair window, it mends the crack with gold seam (`isRepairedGold = true`), preserving streak continuity without friction.
- **Process Death & Reboot Clean-up:** On device boot (`BootAndAlarmReceiver`), active timer states are cleanly cleaned to avoid stale notification loops.

#### 2. Daily Intention
- **Core Daily Focus:** Positioned at the pinnacle of Home as a calm, unobtrusive single-line text field ("What matters most today?").
- **Minimalist Completion:** A quiet circular checkmark marks the intention fulfilled with a subtle gold strike. Full historical timeline is archived under Progress.

#### 3. Quiet Journal
- **End-of-Day Reflections:** Free-text journaling accompanied by one of 5 serene atmosphere chips (`🌿 Calm`, `⛰ Grounded`, `☀️ Energized`, `🪷 Peaceful`, `🤍 Grateful`).
- **100% Private & Local:** Stored solely in local SQLite Room database with zero network footprint. Complete history with non-destructive deletion and SnackBar undo.

#### 4. Weekly Review & Perspective
- **Derived Exclusively from Real Data:** Aggregates real completion %, longest streak, crack and gold repair counts, total focus minutes, most consistent habit, and goal progress summary. Zero fabricated or AI-hallucinated stats.
- **Fixed Reflection Prompts:** Stores 3 guided reflection prompts for weekly intentionality.
- **Calendar Alignment:** Automatically surfaces on Home on the final day of the week (respecting user-configured `weekStartMonday`).

#### 5. Room Migration 3 -> 4 & Backup Schema 3
- **Zero Destructive Fallback:** Database migrated seamlessly using `MIGRATION_3_4` creating `focus_sessions`, `daily_intentions`, `journal_entries`, and `weekly_reviews` tables.
- **Backward-Compatible SAF Backup:** Backup version bumped to `schemaVersion: 3`. Full backward compatibility maintained for Schema 1 (V1) and Schema 2 (V1.5) backups during both Replace and Merge operations.

---

### 12. Phase 2 Research: Distraction Shield Feasibility Assessment

**Objective:** Assess viability of blocking distracting third-party apps during an active focus session on Android 12–14 (API 31–34).

**Key Findings:**
1. **Background Activity Start Restrictions (Android 10+ / 12+ / 14):**
   Android strictly prohibits background services or broadcast receivers from starting activities (`startActivity`) while the app is in the background. An app cannot abruptly snatch foreground focus from another app unless explicitly permitted via high-risk special permissions.
2. **`SYSTEM_ALERT_WINDOW` (`TYPE_APPLICATION_OVERLAY`):**
   Drawing full-screen overlays over third-party applications requires the user to manually grant "Display over other apps". In Android 13 and 14, system overlays are subject to severe touch-filtering (`FLAG_NOT_TOUCHABLE`) and aggressive security scrutiny to prevent tapjacking.
3. **Usage Access Polling (`PACKAGE_USAGE_STATS`):**
   Detecting foreground app switching requires continuous polling of `UsageStatsManager.queryUsageEvents()` in a loop (every 500ms–1000ms). This causes noticeable battery drain and triggers aggressive OS process termination by OEM battery killers (Samsung OneUI, Xiaomi HyperOS, OnePlus OxygenOS).
4. **Accessibility Service (`AccessibilityService`) Exploits:**
   While an accessibility service can detect `TYPE_WINDOW_STATE_CHANGED`, using accessibility services for app blocking is explicitly prohibited under Google Play Developer Content Policies unless designed specifically for users with disabilities. Furthermore, Android 13+ marks sideloaded/debug apps using Accessibility Services under "Restricted Settings", blocking normal users from activating them without multi-step developer settings workarounds.

**Conclusion & Decision:**
In accordance with the specification requirement (*"If not viable or too fragile, stop and report instead of hacking around OS limits"*), **implementing a third-party app-blocking distraction shield on modern Android is fundamentally fragile and contrary to Kintsu's calm, stable architectural principles**. The app prioritizes pure, uncompromised stability and mindful focus without resorting to fragile OS bypasses.




---

### 13. Kintsu V2.1 Home Hub Architecture & Assumptions

#### 1. Navigation Topology & Centered Start Destination
- **5-Tab Navigation Order:** `[Habits, Tasks, Home, Goals, Progress]`. `HOME` sits in the visual centre of the bottom navigation bar and acts as the application's root start destination (`initialTab = KintsuNavTab.HOME`).
- **Quiet Emphasis on Home Icon:** Home uses a slightly larger icon (23dp vs 19dp) with a subtle gold accent container (`colors.accentSoft.copy(alpha = 0.5f)`), adhering to Apple-grade minimalism without mimicking a loud floating action button or adding visual clutter.
- **Root & Scroll-To-Top Contract:** Tapping any active tab returns to its root. Tapping `HOME` while on Home smoothly scrolls to the top of the feed (`homeScrollToTopEvents` SharedFlow collected by `LazyListState.animateScrollToItem(0)`).
- **Hierarchical Back Stack:** `sheet / dialog` -> `detail screen` -> `Home` -> `app exit`. Pressing Back from any non-Home tab (`Habits`, `Tasks`, `Goals`, `Progress`) immediately returns to `Home` before exiting the application.

#### 2. Home Hub Sections (Essentials Only)
- **Header:** Warm greeting conditioned on time of day ("Good morning / afternoon / evening, $name"), formatted date, and a profile/settings icon button top-right. When the focus timer is active, displays a compact, sleek mini-bar with time remaining (`"In Focus • MM:SS remaining"`), tapping which immediately returns to the active timer.
- **Today's Rituals Summary:** Hides when no habits are scheduled for today. Features an interactive circular progress ring (`KintsuProgressRing`), active Gold Repair badges (showing remaining repair window e.g. `"Mend with gold • 14h left"`), and today's scheduled rituals with completion checkmarks. Habits tab retains complete habit management (routines, archiving, creation, editing).
- **Daily Intention:** Minimalist single-line card with quiet checkbox. Hides when disabled in Settings.
- **Focus Quick-Start:** Single preset card (defaulting to the user's preferred duration e.g. 25 min) with a 1-tap start button. Hides when Focus is disabled in Settings.
- **Key Numbers:** 3 serene pill cards displaying Streak count, Tasks due today, and Focus minutes today. Tapping routes directly to the relevant workspace.
- **Quick Actions:** Single row of up to 5 rounded icon buttons (`ADD_HABIT`, `ADD_TASK`, `START_FOCUS`, `JOURNAL`, `NEW_GOAL`, or `WEEKLY_REVIEW`).
- **Clutter Elimination:** Strictly no activity feeds, news, promos, tips, or unrequested secondary metrics.

#### 3. Customisation (Settings -> Home Hub)
- **Visible Elements & Reordering:** Users can reorder (`↑` / `↓`) and toggle (`Switch`) the reorderable sections (`DAILY_INTENTION`, `FOCUS`, `KEY_NUMBERS`, `QUICK_ACTIONS`), while Header and Today rituals remain fixed at the top.
- **Quick Action Selector:** Configurable selection of up to 5 quick action buttons with one-tap toggle chips.
- **Layout Reset:** "Reset Home Layout" action restores default section ordering and quick actions in DataStore.

#### 4. App Shortcuts (Android Launcher)
- Configured in `res/xml/shortcuts.xml` and registered in `AndroidManifest.xml`:
  - `shortcut_add_task`: Opens directly to Tasks with Add Task modal.
  - `shortcut_start_focus`: Launches the Mindful Focus timer screen.
  - `shortcut_journal`: Opens the Evening Journal reflection sheet.
- Dispatched via `MainActivity.onNewIntent` to `KintsuApp`.

#### 5. Domain Derivation & High Performance
- **Zero Redundant Tables:** All home metrics (`HomeKeyNumbers`) are computed on demand in memory from existing Room entities (`habits`, `logs`, `tasks`, `focus_sessions`) via `HomeStatsCalculator`.
- **Performance Benchmark:** Stress-tested with 1,500 habit logs, 400 tasks, and 300 focus sessions (`HomePerformanceTest`), completing in **3ms** (well below the 50ms jank-free threshold).

#### 6. Backup & Restore Schema 4
- **Schema Bump:** Bumped to `schemaVersion: 4`, encapsulating `home_preferences` (`section_order`, `hidden_sections`, `quick_actions_order`).
- **100% Backward Compatibility:** Verified by automated unit tests (`BackupRestoreTest`) confirming seamless imports of Schema 1, 2, and 3 backups in both Replace and Merge modes.

---

### 14. Kintsu Core Feature Completion Architecture & Assumptions

#### 1. Habit-to-Goal Linkage & Database Migration (Room Version 5)
- **Schema Evolution:** Extended `HabitEntity` with `linkedGoalId: Long? = null` to enable direct connection between daily rituals and Horizon Goals.
- **`MIGRATION_4_5`:** Safe, non-destructive migration executing `ALTER TABLE habits ADD COLUMN linkedGoalId INTEGER DEFAULT NULL`. Registered in `KintsuDatabase.MIGRATION_4_5` and verified via `MigrationTest.kt`.
- **Habit Scheduling UI:** In `HabitEditSheet`, selecting `FrequencyType.SPECIFIC_DAYS` renders interactive day-of-week chips (Mon–Sun) mapping directly to `weekdaysMask` bit flags (1..64). A scrollable "Link to Goal (Optional)" chip row allows assigning or clearing goal associations.
- **Visual Goal Badges:** Habits with an active linked goal display a subtle `IconGoal` pill within `HabitRowItem`.

#### 2. Fully Unified Goal Progress (`AUTO_FROM_ITEMS`)
- **Dual-Source Auto Progress:** Horizon goals configured with `GoalProgressType.AUTO_FROM_ITEMS` now compute progress from **both linked tasks and linked daily rituals**:
  $$\text{Progress} = \frac{\text{Completed Tasks} + \text{Completed Rituals Today}}{\text{Total Linked Tasks} + \text{Total Linked Rituals}}$$
- **Rich Goal Previews:** In `GoalsScreen` and `ProgressScreen`, goal cards show detailed completion summaries (e.g., `"2 of 3 linked items complete • 1/1 rituals today"`), and the expandable drawer lists all associated daily rituals with checkmark states alongside linked tasks.
- **Preserved Horizon Target Date:** In `GoalEditSheet`, existing target dates are preserved during edits unless the user deliberately selects a new horizon timeline offset.

#### 3. Task Management Refinements
- **Uncapped Goal Linking:** Replaced the previous 3-item limit on goal chips in `TaskEditSheet` with a horizontally scrollable list (`Modifier.horizontalScroll(rememberScrollState())`), enabling users to connect tasks to any active goal regardless of count.

#### 4. Real-Time Reactive Stats Engine
- **Eliminated Manual Stats Polling:** In `KintsuViewModel`, `stats` is computed via `combine(activeHabits, allLogs, allTasks, allFocusSessions, settings)`. Any mutation to Room (from in-app interactions, home-screen Glance widgets, background alarms, or notification action receivers) automatically and instantaneously updates Progress stats and Home key numbers without requiring manual refresh triggers.

#### 5. Direct 1-Tap Quick Action Dispatches
- **State-Driven External Triggers:** Connected `externalOpenAddSheet` parameters across `HabitsScreen`, `TasksScreen`, and `GoalsScreen`. Triggering "+ Habit", "+ Task", or "+ Goal" from Home Quick Actions or Android app shortcuts routes to the appropriate tab and immediately displays the creation bottom sheet.

#### 6. Comprehensive Test Verification & Build Artifacts
- **Test Suite:** 29 automated unit tests passing across domain, performance, migration, and backup suites (`GoalProgressTest`, `BackupRestoreTest`, `MigrationTest`, `HomePerformanceTest`, `HomeStatsCalculatorTest`, `GoldRepairDomainTest`, `FocusTimerManagerTest`, `WeeklyReviewGeneratorTest`, `NavigationBackStackTest`).
- **Validated Artifact:** Clean debug APK produced at `app/build/outputs/apk/debug/app-debug.apk` (13.2 MB) with zero regressions to the Apple-grade minimalist aesthetic.

---

### 15. Kintsu V2.2 — Five Premium Themes Architecture & Assumptions

#### 1. Five Curated Atmospheric Themes
- **`SAGE_IVORY` (Default):** Warm ivory (`#F7F5EE`), soft sage (`#DFE8D9`), muted green (`#829B83`), antique gold (`#B49A68`). Calm, natural, and balanced.
- **`LAVENDER_MIST`:** Pearl white (`#F8F6FA`), dusty lavender (`#EAE4F1`), muted violet (`#9B8BAD`), soft lilac (`#B5A0C5`). Soft, airy, and sophisticated.
- **`TERRACOTTA_LINEN`:** Cream (`#FAF5ED`), pale clay (`#F0DDD0`), terracotta (`#BF826C`), warm brown (`#8E685B`). Earthy, welcoming, and grounded.
- **`COASTAL_MIST`:** Cool porcelain (`#F4F8F9`), mist blue (`#DCE9EC`), muted teal (`#7F9FAA`), slate (`#607D8B`). Fresh, clear, and focused.
- **`MIDNIGHT_CHAMPAGNE`:** Warm charcoal (`#202224`), graphite (`#303236`), champagne gold (`#C4AC7B`), warm pearl (`#ECE5D8`). Elegant, restrained, and immersive.

#### 2. Dark/Light Dynamic Handling & Midnight Champagne Identity
- Coherent light and dark variants defined for all 5 themes.
- **Midnight Champagne Default:** When `themeMode == SYSTEM` or `DARK`, Midnight Champagne retains its signature dark visual identity (`#202224` charcoal background, `#ECE5D8` warm pearl text, `#C4AC7B` champagne gold). When `themeMode == LIGHT` is explicitly chosen, it transitions to an accessible light counterpart (`#F5F2ED` warm pearl cream, `#202224` charcoal ink).
- For all other themes (`SAGE_IVORY`, `LAVENDER_MIST`, `TERRACOTTA_LINEN`, `COASTAL_MIST`), `ThemeMode.SYSTEM` adheres to the system's dark/light state.

#### 3. Theme & Accent Relationship
- Selecting an `AppTheme` preset automatically supplies the default harmonious accent for that preset (`SAGE` for Sage & Ivory, `LAVENDER` for Lavender Mist, `CLAY` for Terracotta Linen, `TEAL` for Coastal Mist, `GOLD` for Midnight Champagne).
- The user can still tap any accent circle in the Accent Color picker to explicitly customize the interactive accent while preserving the active theme's background, surface, and text colors.

#### 4. Kintsugi Gold Treatment Integrity
- The signature Kintsugi gold repair seam (`goldSeam` and `goldSeamSoft`) remains visually distinct and recognizable across all five themes, anchoring the philosophical identity of the app.

#### 5. Settings Appearance UI
- **Settings → Appearance → App Theme:** Renders five selectable cards. Each preview displays:
  - Theme display name and descriptive subtitle.
  - "Default" chip badge on Sage & Ivory.
  - Four circular swatches displaying the theme's core colors.
  - Accessible radio-style checkmark selection indicator.
- Instant reactive update across the entire app via Compose `CompositionLocalProvider(LocalKintsuColors provides activeColors)`.

#### 6. Persistence & Backup Schema
- Stored in DataStore under `Keys.APP_THEME`.
- Exported and imported seamlessly in `appearance_preferences` JSON with fallback defaults.

#### 7. Verification & Build Artifacts
- **39 unit tests passing** (`ThemePaletteTest`, `BackupRestoreTest`, `GoalProgressTest`, `MigrationTest`, `HomePerformanceTest`, `HomeStatsCalculatorTest`, `GoldRepairDomainTest`, `FocusTimerManagerTest`, `WeeklyReviewGeneratorTest`).
- Verified WCAG AA contrast compliance across all 10 light/dark palette pairs (all primary text contrasts $\ge 4.5:1$).
- Debug APK successfully compiled and deployed to device (`192.168.29.14:5555`).

---

### 16. Kintsu V2.3 — Smart Planning & Task Management Architecture & Assumptions

#### 1. Recurrence Architecture & Infinite Record Prevention
- Instead of generating unlimited future database records (which bloats SQLite, causes index bloat, and breaks sync/rescheduling), recurring tasks use a **virtual schedule + completion ledger model**:
  - The task entity stores `recurrenceType`, `recurrenceWeekdaysMask`, and `recurrenceActive`.
  - Its `dueDateEpochDay` represents the date of the **currently pending instance**.
  - When the user checks off a recurring task, the occurrence is logged into `task_completions` (storing `taskId`, `epochDay`, `completedAt`), and the task's `dueDateEpochDay` is atomically advanced to its next calculated recurrence using `TaskRecurrenceCalculator`. The task remains uncompleted (`isCompleted = false`), ready for the next scheduled occurrence.
  - Pausing recurrence preserves the schedule without advancing or deleting history; stopping recurrence sets `recurrenceType = NEVER`.
  - Non-recurring tasks follow the standard completion toggle (`isCompleted = true/false`).

#### 2. Recurrence Calculations, Month-Ends & DST
- **Daily:** Increments `dueDateEpochDay` by 1.
- **Weekdays:** Evaluates a 7-bit bitmask (bits 0..6 for Mon..Sun), finding the next active day. Skipping weekends (Mon-Fri) is the default.
- **Weekly:** Increments `dueDateEpochDay` by 7.
- **Monthly:** Uses `LocalDate.ofEpochDay(d).plusMonths(1).toEpochDay()`, natively handling month-end bounds (Jan 31 $\to$ Feb 28/29, Mar 31 $\to$ Apr 30) and leap years.
- Because all schedules are computed using calendar `LocalDate` epoch days, daylight-saving transitions cause zero drift or missed hours.

#### 3. Subtasks & Zero Double-Counting Guarantee
- `subtasks` table links each subtask to `tasks(id)` with `ON DELETE CASCADE`.
- Subtasks have `title`, `isCompleted`, `orderIndex`, and `createdAt`.
- **Goal Progress Isolation:** Goal progress is calculated strictly over parent items (linked tasks and habits). Subtasks are internal child milestones of their parent task and are not added to goal calculations, strictly guaranteeing zero double-counting.
- **Completion Independence:** Completing a parent task leaves subtasks as they are without forcing them to change. Completing all subtasks emits a gentle, non-blocking UI prompt (`"All subtasks completed for '[Task]'. Complete parent task?"`) allowing one-tap completion without forcing it.

#### 4. Daily Planner & Time Blocks ("Today / Plan")
- Located directly inside the **Tasks** screen as the first segmented tab (`Today / Plan`). No 6th bottom navigation item is added, preserving the 5-tab Apple-grade navigation hierarchy.
- **Overdue Section:** Highlights overdue tasks in a soft terracotta alert card with a one-tap "Reschedule to Today" action.
- **Time Blocks:** Four calm periods (`Morning`, `Afternoon`, `Evening`, `Anytime`). Tasks planned for today are grouped under these blocks with drag/reassign capability.
- **Integrated Habit Rhythm:** Today's active habits are quietly displayed in horizontal chips alongside the time blocks matching their reminder times, providing a unified daily rhythm at a glance.
- **Unscheduled / Backlog Section:** Lists open pending tasks with a fast "+ Plan" button to schedule them into today's timeline.

#### 5. "Plan My Day" Flow
- Accessible via a prominent but calm "Plan Day" button at the top of the Tasks screen.
- Opens an interactive sheet presenting open tasks with checkboxes and time block chips.
- Persists user selections immediately to Room (`plannedDateEpochDay`, `plannedTimeBlock`, `planOrder`) so the plan survives app restarts.

#### 6. Database Migration (Version 5 $\to$ 6)
- `MIGRATION_5_6` adds `recurrenceType`, `recurrenceWeekdaysMask`, `recurrenceActive`, `plannedDateEpochDay`, `plannedTimeBlock`, and `planOrder` to `tasks`.
- Creates `subtasks` table with foreign key to `tasks(id) ON DELETE CASCADE` and index on `taskId`.
- Creates `task_completions` table with index on `taskId`.
- Backward-compatible and non-destructive.

#### 7. Backup & Restore Schema V5
- Schema version bumped to `5` in `BackupRestoreManager`.
- Exports subtasks and task completions alongside recurring task attributes.
- Fully backwards-compatible with backup schemas 1..4.

---

### 17. Kintsu V2.4 — Advanced Insights + Tasks UI Repair Architecture & Assumptions

#### 1. Tasks Screen Layout Hierarchy & Responsive Separation
- **Separated Navigation from Action Area:**
  - Row 1: Segmented View Navigation (`Today / Plan | All Tasks | Completed`) in a horizontally scrollable container with touch targets and text padding. No buttons share this row, eliminating clipping or overlap on narrow screens (360dp) or at 200% system font size.
  - Row 2: Action Area (`Plan Day` + `+ New Task`), cleanly aligned and distinct from navigation filters.
  - Row 3: Dedicated Filter & Sort Controls with scrollable chip rows for immediate readability and zero crowding.
- **Empty Planning Blocks:**
  - Bulky placeholder cards in `Morning`, `Afternoon`, `Evening`, `Anytime` replaced with a sleek, compact single-line slot (`"No tasks planned for $block" · "+ Add"`).
  - Tapping opens the task sheet with that time block pre-selected.
- **Interactive Habit Rhythm:**
  - Habit chips under time blocks are directly interactive: tapping immediately toggles completion (`onToggleHabit(habit.id)`), synchronizing in real time with the Home hub and Progress metrics.
  - Strikethrough is only applied when completed; uncompleted habits display clean typography and border contrast.
- **Theme Contrast & WCAG AA:**
  - `KintsuFilterChip` enhanced with a subtle border in both light and dark modes to guarantee outline definition.
  - Dark mode tertiary text colors across all five theme presets (especially Midnight Champagne `#8E8679`, Coastal Mist `#8198A4`, Lavender Mist `#8E869C`, Terracotta Linen `#948278`, Sage & Ivory `#869585`) adjusted to achieve $\ge 4.5:1$ contrast against their respective backgrounds.

#### 2. Advanced Progress & Consistency Insights
- **Scheduled-Frequency Aware Consistency (B1):**
  - Habits are evaluated strictly over **eligible scheduled days** calculated via `GoldRepairManager.isHabitScheduledOn(habit, day)` rather than blind calendar days.
  - A habit scheduled for Mon, Wed, Fri has 3 eligible days per week. Completing all 3 results in a 100% completion rate without penalizing rest days (Tue, Thu, Sat, Sun).
  - Calculated over 7, 30, and 90-day timeframes with toggleable breakdown for individual habits.
- **Interactive Weekly Trends (B2):**
  - Renders 4 weekly trend bars reflecting scheduled completion rates.
  - Tapping any bar reveals a detailed tooltip showing the week label, percentage, rituals completed vs eligible, and completed tasks.
- **Task Productivity Insights (B3):**
  - Aggregates tasks completed this week and this month, overdue task count, and task completion distribution across `HIGH`, `MEDIUM`, and `LOW` priorities.
  - Recurring tasks count actual completed occurrences from `task_completions` records rather than template records.
- **Horizon Goal Progress Insights (B4):**
  - Tracks active vs achieved goals, average progress across all goals, and deadlines within 14 days.
  - Preserves both automatic (`AUTO_FROM_ITEMS`) and manual progress calculations without double-counting.
- **Mindful Focus Insights (B5):**
  - Aggregates total recorded focus minutes, completed sessions, average completed session duration, and this week's focus investment from actual `FocusSessionEntity` records. Unfinished sessions are excluded.
- **Personal Highlights (B6):**
  - Highlights (Most Consistent Ritual, Peak Momentum Day, Deep Focus Practice, Task Velocity) are generated strictly and factually when real data thresholds are met.
- **Weekly Review Integration (B7):**
  - Directly displays recent weekly review reflections and metrics alongside the progress view.

## 18. Kintsu V2.5 Focus, Productivity & Tasks Interface Refinement

### Priority 1: Tasks & Plan Interface Refinement
- **A1. Excessive Spacing Eliminated:**
  - Standardized main LazyColumn horizontal padding from `spacing.xl` (24dp) to `spacing.l` (16dp).
  - Reduced vertical arrangement from `Arrangement.spacedBy(spacing.m)` (16dp) to `Arrangement.spacedBy(spacing.s)` (8-12dp).
  - Maintained comfortable 48dp touch targets for interactive elements (checkboxes, action buttons, chips).
- **A2. Reorganised Top Controls:**
  - Row 1: High-level navigation filter chips (`Today / Plan | All Tasks | Completed`).
  - Row 2: Clean action row with primary `New Task` (featuring single `IconAdd`) and secondary `Plan Day` button.
  - Row 3: Compact filter and sort controls with minimal margins.
- **A3. Simplified Empty Planning Periods:**
  - Replaced repetitive bulky placeholder cards for Morning, Afternoon, Evening, and Anytime with clean, single-line slots: `[ Period • No tasks planned | + Add ]`.
  - Tapping `+ Add` directly pre-selects the corresponding period and marks it planned for today in `TaskEditSheet`.
- **A4. Task Scheduling Consistency:**
  - Resolved date ambiguity for tasks scheduled for today that have future deadlines: explicitly displays `• Planned for Today (Due Tomorrow)` or `• Planned for Today (Due [Date])` instead of misleadingly labeling them due today or past due.
- **A5. Habits vs Tasks Visual Separation:**
  - Grouped habits in time blocks under dedicated subheadings (`Today's Rituals` / `[Period] Rituals`).
  - Subtle completion styling for habits (faint line and gold checkmark) replacing heavy strikethrough.
- **A6 & A7. Typography, Contrast & High Screen Density:**
  - Contrast compliance across all five themes (especially Midnight Champagne and Coastal Mist).
  - Tightened vertical layout so actionable tasks appear immediately above the fold without extensive scrolling.
- **A8. Interaction Integrity Preserved:**
  - Maintained full support for recurring tasks, subtasks, goal attachments, priority filtering, and daily time-block adjustments.
- **A9. Duplicate Icon Elimination:**
  - Fixed `+ + New Task` button text to `New Task` with `IconAdd` leading icon, rendering clean and professional `+ New Task`.

### Priority 2: Focus & Productivity Enhancements
- **B1. Task-Linked Focus Sessions:**
  - Focus sessions can be launched directly from any task row ("Focus" action).
  - Timer interface displays `Focusing on: [Task Title]` banner.
  - Added `linkedTaskId` column with SQLite index to `focus_sessions` table.
  - Completing a focus session does not prematurely auto-complete the task, allowing multi-session deep work.
- **B2. Refined Focus Presets:**
  - Short Focus: 15 min.
  - Standard Focus: 25 min (default).
  - Deep Focus: 50 min.
  - Custom duration with persistent slider (5 to 120 min).
- **B3. Session Lifecycle Precision:**
  - Foreground service with notification channel `kintsu_focus_channel`.
  - Timestamp-based elapsed calculation resilient to app backgrounding, screen off, and pause/resume cycles.
- **B4. Recent Focus History:**
  - Added clean history view in Focus screen showing past sessions with duration, completion status, and linked task/habit titles.
- **B5. Room Migration 6 to 7 & Backup/Restore:**
  - Room migration `MIGRATION_6_7` safely adds `linkedTaskId` column and index.
  - Backup & restore manager updated to serialize and restore `linkedTaskId` without loss.


## 19. Kintsu V2.6 — Premium Experience & Personalisation

### Global UI Consistency & Visual Refinement
- **Consistent Tokens Across All Screens:** Standardized spacing (`KintsuSpacing`), typography scales, corner radii (`KintsuShapeTokens`), and button heights across Home, Habits, Tasks & Plan, Goals, Progress, Focus, Journal, and Settings.
- **Button Icon Cleanliness:** Resolved button icon duplication across creation buttons (e.g. `+ New Task` instead of `+ + New Task`).
- **Interaction & Motion Refinement:** Smooth, restrained 200–300ms transitions, haptic feedback integration, consistent bottom-sheet dismissal, and strict respect for Android's reduced-motion preferences.
- **Settings Screen Switch Alignment:** Replaced oversized custom toggles with elegant Material3 switches adhering strictly to the active color palette.

## 20. Kintsu V3.0 — Cloud Sync & Multi-Device Experience

### Architectural Overview & Provider Selection
- **Architecture:** PostgREST / Supabase REST client with Bearer token authentication, fully decoupled from proprietary Google Play Services binary blobs.
- **Offline-First Room Source of Truth:** The Room database remains the absolute source of truth for the UI at all times. Network latency, disconnects, or sync delays never block or stutter app interactions.
- **Guest Mode (Zero Forced Registration):** Unauthenticated users operate transparently in Local Mode with zero restrictions. No telemetry, forced prompts, or degraded offline experiences.

### Database Schema (Version 8) & Migration (MIGRATION_7_8)
- **`sync_registry` Table:** Maps local device auto-increment integer IDs (`localRecordId: Long`) to universally unique UUIDs (`syncUuid: String`), record table name (`localTableName: String`), mutation timestamp (`updatedAt: Long`), and soft-deletion flag (`isDeleted: Boolean`). This prevents primary key collisions across multiple devices and ensures foreign key references (e.g. `task.linkedGoalId`, `subtask.taskId`) resolve reliably.
- **`sync_queue` Table:** Persistent offline mutation queue recording operations (`UPSERT`, `DELETE`), serialized JSON payloads, and timestamps, guaranteeing that changes survive app termination and device reboots.
- **`MIGRATION_7_8`:** Safe SQLite DDL migration adding `sync_registry` and `sync_queue` tables with indexes on `syncUuid`, `localTableName`, and `localRecordId`.

### Conflict Resolution & Multi-Device Consistency
- **Deterministic Last-Write-Wins (LWW):** For structured entities (habits, habit completions, tasks, subtasks, task completions, goals, focus sessions, daily intentions, weekly reviews, theme settings), conflicts resolve deterministically using millisecond-precision timestamps (`updatedAt`).
- **Non-Destructive Journal Branching:** Because personal journal reflections and review notes are irreplaceable, concurrent conflicting edits on separate devices branch non-destructively: the incoming remote reflection is appended as a clearly labeled branch (`[Synchronised revision from another device]: ...`) instead of discarding user words.
- **Tombstone Propagation:** Entity deletions record tombstones (`isDeleted = true`) in `sync_registry` and queue `DELETE` mutations, preventing resurrecting deleted records during subsequent pull cycles.

### Background Synchronization & Lifecycle
- **WorkManager Integration (`KintsuSyncWorker`):** Uses Android WorkManager with `NetworkType.CONNECTED` constraints to execute periodic background synchronization (every 6 hours) and immediate one-time sync upon network reconnection or manual trigger.
- **Reactive State Flow:** Sync state (`IDLE`, `SYNCING`, `SUCCESS`, `ERROR`) and error messages are broadcast via `StateFlow` from `KintsuRepository` and `KintsuViewModel` into `SettingsScreen` and the broader UI.
- **Initial Data Claim & Migration:** Upon first sign-in with an account, existing local guest data (all habits, tasks, goals, focus sessions, journal entries, weekly reviews) is claimed, registered with stable UUIDs, and pushed directly to the user's cloud account without data loss.

### Settings & Account Interface
- **Account & Cloud Sync Section:** Embedded directly within Settings below Profile, featuring:
  - Account identity card (showing email/display name or "Guest Mode (Local Only)").
  - "Sign In" modal bottom sheet (`AuthSheet`) supporting Email/Password sign-in, account creation, and Google sign-in with a reassuring data-preservation banner.
  - "Sync Enabled" toggle switch controlling background syncing.
  - Live sync status indicator pill (`Synced just now`, `Syncing...`, `Error`).
  - Manual "Sync Now" button.
  - Sign-Out dialog with explicit confirmation that local data remains intact.
  - "Delete Cloud Data" dialog for GDPR/privacy compliance.


## 21. Kintsu System Permissions & Runtime Access Management

### Architectural Strategy
- **Privacy-First Mindful Prompting:** Permissions are requested with clear context explaining why each permission is essential to running the app reliably (habit reminders, exact timing, background timer persistence) without aggressive system interruptions.
- **Three Core System Capabilities:**
  1. **Mindful Notifications (`android.permission.POST_NOTIFICATIONS` on API 33+):** Needed for gentle ritual reminders, morning/evening intention prompts, task due notices, and focus session completion chimes.
  2. **Exact Alarms & Timers (`android.permission.SCHEDULE_EXACT_ALARM` on API 31+):** Needed for pinpoint accuracy of scheduled habit reminders and daily reviews without Android Doze delay.
  3. **Unrestricted Background Execution (`android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`):** Needed to prevent aggressive OEM battery managers from terminating active focus countdown timers and WorkManager sync workers when the phone screen is off.
- **Normal Permissions Automatically Granted:** `RECEIVE_BOOT_COMPLETED` (for restoring alarms on reboot), `VIBRATE` (for tactile feedback), and `FOREGROUND_SERVICE` / `FOREGROUND_SERVICE_SPECIAL_USE` (for the active focus countdown).

### UI Integration & User Control
- **`PermissionHelper`:** Central utility in `com.kintsu.app.util` that queries permission states via `ContextCompat`, `AlarmManager.canScheduleExactAlarms()`, and `PowerManager.isIgnoringBatteryOptimizations()`, while handling system intent navigation fallback paths.
- **First-Run / Update Prompt (`PermissionRequestSheet`):** If notifications or exact alarms are unconfigured, Kintsu surfaces a graceful bottom sheet when entering the sanctuary so the user can review and grant access.
- **Settings Dashboard (`SYSTEM PERMISSIONS`):** Embedded directly in `SettingsScreen` between Account & Cloud Sync and Appearance, displaying real-time status pills (`Allowed`, `Configure`, `Unrestricted`) with one-tap configuration buttons and a `Review All Permissions` sheet launcher.
- **Live Lifecycle Synchronization:** Utilizes `DisposableEffect(lifecycleOwner)` with `ON_RESUME` to reactively re-evaluate permission statuses when returning from system Settings menus.


## 22. Production-Grade Secure Backend & True Google Authentication Gateway

### Architectural Foundations
- **Runtime Stack:** Node.js v24 native environment running in `C:\Users\rfviz\.gemini\antigravity\scratch\kintsu-backend`. Uses Node's built-in `node:sqlite` (`DatabaseSync`) in WAL mode for zero external database dependencies, high concurrency, and ACID guarantees.
- **Security Engineering:**
  - **Helmet Middleware:** Configures HTTP security headers (HSTS, XSS filtering, frameguard, anti-sniffing).
  - **CORS Support:** Secure cross-origin resource sharing tailored for mobile clients and the accompanying web portal.
  - **Rate Limiting:** Protects `/api/auth/*` against brute-force attacks (200 requests / 15 minutes) and `/api/sync/*` against burst overload.
  - **Password Security:** Salted password hashing with 12 bcrypt rounds.
  - **Cryptographic Tokens:** Signed JWT access tokens (1 hour lifetime) and high-entropy 40-byte hex refresh tokens (30 days lifetime) stored with expiration timestamps in SQLite.

### True Google Authentication Protocol
- **Public Token Verification (`/api/auth/google` & `/auth/v1/token?grant_type=id_token`):** Validates Google ID tokens directly against Google OAuth2 tokeninfo endpoint (`https://oauth2.googleapis.com/tokeninfo?id_token=...`), verifying audience, issuer, expiration, and user email authenticity.
- **Identity Auto-Linking:** Automatically matches Google accounts to existing email registrations or creates a new user profile with Google `sub`, email, display name, and avatar URL.
- **Development & Sandbox Gateway:** Transparently accepts development tokens (`dev_google_*` / `mock_google_*`) allowing complete offline and local network testing without requiring live Google Cloud Console OAuth consent screen deployments.

### Synchronization Engine & Dual Compatibility
- **Modern REST Endpoints:**
  - `POST /api/auth/register`, `POST /api/auth/login`, `POST /api/auth/google`, `POST /api/auth/refresh`, `POST /api/auth/logout`, `GET /api/auth/me`.
  - `POST /api/sync/push`: Transactional batch upsert with Last-Write-Wins millisecond resolution.
  - `GET /api/sync/pull?since={timestamp}`: Incremental synchronization query using SQLite composite index `(user_id, updated_at)`.
  - `GET /api/sync/stats`: Real-time breakdown of user cloud records across rituals, tasks, focus sessions, and journal reflections.
  - `DELETE /api/sync/data`: GDPR-compliant cloud record erasure.
- **Supabase / PostgREST Compatibility Alias:** Provides 1:1 drop-in route compatibility for `/auth/v1/signup`, `/auth/v1/token`, `/auth/v1/logout`, and `/rest/v1/sync_records`, allowing existing mobile clients to switch between local self-hosted and cloud providers with zero client code rewrite.

### Accompanying Web Sanctuary Portal
- **Dashboard Interface (`http://localhost:8080`):** Zen-styled aesthetic web interface (Sage & Ivory / Midnight Champagne palette) displaying live server health, active user counts, real-time sync metrics, an interactive Google Sign-In tester, and a JSON payload inspector.
- **Dual Connectivity Modes:**
  - **USB Reverse (`http://127.0.0.1:8080`):** Tunneled via `adb reverse tcp:8080 tcp:8080` for zero-latency, firewall-exempt USB tethering.
  - **Wi-Fi LAN (`http://192.168.29.155:8080`):** Direct local area network connection accessible from any mobile device on the local network.


## 23. Kintsu V3 Digital Wellbeing, Addiction Blocking, and Economy

### Clean First Launch Architecture
- By default, Kintsu launches completely brand new with **zero** pre-populated habits, tasks, or horizon goals.
- The user is greeted by serene, peaceful empty states guiding them to mindfully add their first ritual or task at their own pace.

### Header & Navigation Restructuring
- **Top-Left Placement:**
  - **Profile Avatar:** Located at the top-left of the Home header. Tapping opens the unified Profile & Settings sheet/screen.
  - **Kintsugi Coins & Sanctuary Shop:** Positioned next to the Profile Avatar. Shows current balance of Kintsugi Gold Coins.
- **Sanctuary Shop Economy:**
  - Users earn Gold Coins through mindful actions: completing daily rituals (+5 coins), finishing tasks (+3 coins), completing deep focus sessions (+10 coins), maintaining clean streaks (+20 coins/milestone), and finishing Digital Detoxes (+15 coins).
  - Coins can be spent in the **Sanctuary Shop** on:
    * Kintsugi Repair Gold Vials (used for Gold Repair of cracked habits).
    * Ambient Soundscapes & Meditative Singing Bowls for focus sessions.
    * Wabi-sabi Custom Theme Palettes (e.g. Slate Zen, Forest Moss, Twilight Indigo).
    * Mindful Achievement Trophy Badges.
- **Settings Consolidation:**
  - Redundant settings entry points are consolidated into the top-left Profile icon and the dedicated Settings page.

### Digital Wellbeing & Addiction Blocking Architecture
- **Detection:** AccessibilityService (`KintsuAccessibilityBlockerService`) event-driven detection (`TYPE_WINDOW_STATE_CHANGED`) backed by `UsageStatsManager` for analytics.
- **Tamper Resistance:** Monotonic time (`SystemClock.elapsedRealtime()`) prevents bypassing blocks via system clock manipulation.
- **Safety Fail-Safe:** Built-in crash breaker disarms blocking if 3 crashes occur in 10 minutes.
- **Protected Essentials:** Hard-coded list (Phone, Emergency Dialer, SMS, Settings, Launcher, Keyboard, Clock/Alarm, Kintsu) can never be blocked.
- **Commitment Lock:** Weakening block rules requires a 24-hour cooling-off period to prevent craving-driven relapses.
- **Quit-Habit Support:** Tracks days clean, urge logs with triggers, SOS urge-surfing flow, and integrates with Gold Repair.
