# Kintsu — Technical & Design Specification
Version 1.0

## 1. Vision & Philosophy
**Visual Reference:** [Stitch Project 14271009755712295354](https://stitch.withgoogle.com/projects/14271009755712295354)

Kintsu is a native Android phone app rooted in the philosophy of *kintsugi* (金継ぎ)—mending broken pottery with lacquer dusted with powdered gold. 
- **Non-punitive progress:** The streak count never resets to zero. Missed days produce dark cracks on the ceramic vessel.
- **Redemption & Gold Seams:** Completing a mend action (a 10-minute focus session or double ritual) turns dark cracks into gleaming matte gold seams. Over time, the vessel becomes more beautiful and characterful through its repairs.
- **Calm, Premium Aesthetic:** Generous whitespace, serif typography, tactile ceramic/paper tones, and zero shame language.

---

## 2. Design System

### 2.1 Color Palette
Strictly non-glare, organic, tactile tones. No pure black (`#000000`), no pure white (`#FFFFFF`), no saturated primary blues/purples, no neon, no heavy drop shadows.

| Role | Light Mode (Default) | Dark / Night Mode | Description & Usage |
| :--- | :--- | :--- | :--- |
| **Background** | `#F4EFE7` (Warm Paper) | `#1E1B18` (Warm Charcoal) | Screen root background |
| **Surface** | `#EADFCF` / `#D8C3AB` (Soft Clay) | `#2A2622` (Charcoal Surface) | Vessel surface, card containers |
| **Surface Subdued** | `#EFE8DD` | `#24201D` | Input fields, calendar inactive cells |
| **Ink / Text Primary**| `#2B2622` (Deep Ink) | `#EADFCF` (Muted Bone) | Headings, primary text, dark cracks |
| **Ink / Text Secondary**| `#72685E` (Warm Umber) | `#9E9388` (Warm Stone) | Subtitles, labels, preset buttons |
| **Gold (Accent)** | `#B8935A` (Matte Gold) | `#B8935A` (Matte Gold) | **Sole accent:** Mended seams & key milestone moments |
| **Dark Crack** | `#453E38` | `#141210` | Unmended hairline cracks on the vessel |

### 2.2 Typography
Only two bundled typefaces in `res/font`:
1. **Newsreader** (Serif): Used for the app title, primary screen headers, big numbers (streak counter, timer digits), and milestone cards.
2. **DM Sans** (Geometric Humanist Sans): Used for ritual titles, set/rep details, buttons, presets, task descriptions, and navigation.

#### Type Scale
- **Display Large:** Newsreader Regular/Medium, 52sp, line-height 58sp (Focus Timer, Streak Hero)
- **Headline Medium:** Newsreader Medium, 28sp, line-height 34sp (Screen Headers, Dates)
- **Title Medium:** DM Sans Medium, 18sp, line-height 24sp (Ritual Titles, Goal Headers)
- **Body Large:** DM Sans Regular, 16sp, line-height 22sp (Default text, Descriptions)
- **Body Medium:** DM Sans Regular, 14sp, line-height 20sp (Secondary details, Subtext)
- **Label Medium:** DM Sans Medium, 12sp, line-height 16sp, letter-spacing 0.5sp (Presets, Badges, Tabs)

### 2.3 Spacing & Layout Principles
- **Generous Margins:** 24dp screen padding on left/right; 32dp+ vertical spacing between sections.
- **One Hero Element Per Screen:**
  - Home: The Ceramic Vessel.
  - Focus: The Serif Countdown Timer.
  - Journey: The Kintsugi Calendar & Seams History.
- **Minimal Borders & Elevation:** Borderless cards; depth achieved purely via subtle `#D8C3AB` vs `#F4EFE7` tone contrasts. Corner radii fixed to 16dp for surfaces, 24dp for interactive chips.
- **Iconography:** Handcrafted thin stroke (1.5dp) line icons matching Ink color; no emoji or stock solid icons.

### 2.4 Motion & Animation
- **Timing:** 300ms to 600ms ease-out transitions (`CubicBezier(0.2, 0.0, 0.2, 1.0)`).
- **Signature Moment (The Gold Mend):** When a mend completes, a golden liquid effect travels along the target dark crack path over 600ms via `Animatable` path trimming and stroke expansion, accompanied by a subtle 400ms alpha glow before settling into solid matte gold (`#B8935A`).

---

## 3. Screens (v1 Architecture)

### 3.1 Tab 1: Home Screen
- **Header:** Quiet formatted date (e.g. *"Tuesday, 27 October"*) and total journey days (`"Day 42"`, Newsreader).
- **Hero Vessel:** Procedurally drawn ceramic vase/bowl rendered via Compose `Canvas` & `Path`.
  - Reflects accumulated history: dark cracks for unmended misses, gold seams for mended ones.
  - Interactive tap: Shows a gentle tooltip with vessel history (e.g., *"14 seams mended with gold"*).
- **Mend Banner (Conditional):** If unmended cracks exist, an unobtrusive clay-surface card appears: *"Mend a crack — 10 min focus or a double ritual"*.
- **Today's Rituals List:**
  - **Habit:** Single-tap circular tick toggle with a soft checkmark transition.
  - **Workout:** Expandable drawer for logging sets, reps, and optional weight (e.g., 3 sets × 10 reps).
  - Quick inline addition of rituals.

### 3.2 Tab 2: Focus Screen
- **Hero Element:** Large quiet serif countdown timer (Display Large Newsreader, e.g., `25:00`).
- **Task Line:** Clean editable text field (`"Focusing on: Deep Reading"`).
- **Duration Presets:** 3 quiet pill buttons: `25 min`, `50 min`, `90 min` (plus a dedicated `10 min Mend` option when a crack is pending).
- **Controls:** Minimal Play / Pause / Reset glyphs in ink.
- **Completion Event:** Gentle chime sound / soft haptic feedback. Prompts vessel nourishment and applies gold repair if mending a crack.

### 3.3 Tab 3: Journey Screen
- **Calendar View:** Monthly grid indicating:
  - Days with completed rituals (solid soft clay dot).
  - Days with unaddressed cracks (dark mark).
  - Days with mended cracks (gold dot with tiny gold aura).
- **Seams Gallery / Stats:** Lifetime streak count, total focus hours, and total gold seams earned.
- **Horizons / Goals:** Clean tiered goal tracking (Weekly, Monthly, Quarterly, Yearly) with minimal horizontal progress rules.

---

## 4. Crack and Mend Logic

1. **Daily Evaluation:**
   - Evaluated at local end-of-day or first launch on a new day.
   - If at least one scheduled ritual was missed on day $D$:
     - Streak counter **does not reset**.
     - A new `Crack` entity is spawned with status `UNMENDED` and mapped to a procedural seed/coordinate on the vessel vector path.
2. **Mend Eligibility:**
   - Any unmended crack in the database can be redeemed.
   - Eligible actions:
     - **Focus Session:** Completing $\ge 10$ minutes in Focus mode.
     - **Double Ritual:** Completing an extra repetition or secondary session of a daily ritual.
3. **Mending Transition:**
   - Upon completion of the mend action, the oldest unmended crack transitions:
     - `status: UNMENDED -> MENDED`
     - `dateMended = System.currentTimeMillis()`
   - Home screen triggers the gold seam stroke animation along the crack path.
4. **Shame-Free Voice & Copy:**
   - Never say: *"Streak broken"*, *"You failed"*, *"Missed day"*.
   - Always say: *"The vessel awaits mending"*, *"Seam restored with gold"*, *"Day 48 of your journey"*.

---

## 5. Data Model (Room Architecture)

```kotlin
// 1. Daily journey log
@Entity(tableName = "day_records")
data class DayRecord(
    @PrimaryKey val epochDay: Long, // LocalDate.toEpochDay()
    val ritualsScheduledCount: Int,
    val ritualsCompletedCount: Int,
    val isCracked: Boolean = false,
    val note: String? = null
)

// 2. Ritual definitions
enum class RitualType { HABIT, WORKOUT }

@Entity(tableName = "rituals")
data class Ritual(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val type: RitualType,
    val targetDaysMask: Int = 127, // Bitmask for Mon-Sun (default all 7 days)
    val isArchived: Boolean = false,
    val displayOrder: Int = 0
)

// 3. Daily ritual completions
@Entity(
    tableName = "ritual_entries",
    foreignKeys = [
        ForeignKey(
            entity = Ritual::class,
            parentColumns = ["id"],
            childColumns = ["ritualId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("ritualId"), Index(value = ["ritualId", "epochDay"], unique = true)]
)
data class RitualEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ritualId: Long,
    val epochDay: Long,
    val isCompleted: Boolean = false,
    val setsRepsJson: String? = null // Serialized List<WorkoutSet> for workout rituals
)

data class WorkoutSet(
    val setNumber: Int,
    val reps: Int,
    val weightKg: Float? = null
)

// 4. Focus sessions
@Entity(tableName = "focus_sessions")
data class FocusSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startTime: Long,
    val durationMinutes: Int,
    val taskName: String,
    val isCompleted: Boolean,
    val mendedCrackId: Long? = null
)

// 5. Vessel cracks and golden seams
enum class CrackStatus { UNMENDED, MENDED }

@Entity(tableName = "vessel_cracks")
data class VesselCrack(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val epochDayCreated: Long,
    val status: CrackStatus = CrackStatus.UNMENDED,
    val epochDayMended: Long? = null,
    val pathSeed: Long = 0, // Deterministic seed for generating crack path coordinates
    val mendType: String? = null // "FOCUS_10_MIN", "DOUBLE_RITUAL"
)

// 6. Horizon goals
enum class GoalPeriod { WEEKLY, MONTHLY, QUARTERLY, YEARLY }

@Entity(tableName = "goals")
data class Goal(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val period: GoalPeriod,
    val targetCount: Int,
    val currentCount: Int = 0,
    val epochDayDeadline: Long? = null
)
```

---

## 6. Implementation Notes for Later Steps
- **Vector Vessel Rendering:** Implemented as a custom Compose `@Composable` using `Path` geometry representing a classic Japanese ceramic cup/vase profile with subtle paper/clay texturing and overlaid crack paths parameterized by `pathSeed`.
- **Packaging:** Target SDK 34/35, Kotlin 2.x, Compose BOM, Room 2.6.x, Material 3, Navigation Compose.
- **Build Target:** Strictly Debug APKs.
