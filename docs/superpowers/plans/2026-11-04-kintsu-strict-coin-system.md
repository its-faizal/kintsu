# Strict Coin Economy, Fire Streaks (150-Day Curve), Negative Deduction System & Cryptographic Verification

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:subagent-driven-development` or `superpowers:executing-plans` to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement a strict, mathematically calibrated coin economy with base 1/2/3 values, a 150-day Fire Streak multiplier curve (doubling at 60 days up to 4x at 150 days), real-world negative deduction penalties for digital indiscipline (reels binge, bedtime breaches), expensive long-commitment shop sinks, and an offline-first hardware KeyStore HMAC-SHA256 tamper-proof ledger.

**Architecture:**
- **CoinSecurityGuard:** Hardware KeyStore HMAC-SHA256 append-only hash chainer, monotonic boot-time clock validator, and zero-sum ledger reconciler.
- **FireStreakEngine:** Tracks consecutive daily activity days, evaluates flame tiers (Spark -> Ember -> Golden Flame -> Sacred Blaze -> Phoenix -> Solar -> Centurion -> Divine -> Transcendent), computes multipliers (1.0x to 4.0x), and governs Kintsugi golden repair.
- **NegativeEconomyEngine:** Hooks into accessibility blocker and bedtime monitor to deduct coins upon conscious reel binging or bedtime curfew violations.
- **SanctuaryShop & Passbook:** High-commitment tiered catalog (150 to 7,500 coins) with live passbook UI showing signed positive/negative transaction statement.

**Tech Stack:** Kotlin 1.9+, Android SDK 34, Android KeyStore (TEE / StrongBox), Room SQLite with KSP, Jetpack DataStore Preferences, Jetpack Compose, Material 3, Monotonic Clock (SystemClock.elapsedRealtime()).

**Spec:** `C:\Users\rfviz\.gemini\antigravity\brain\76c5a5c2-26be-4ed6-b4ce-91d7025e4ede\kintsu_coin_system_plan.md`

## Global Constraints
- Strictly NO emojis anywhere in UI code; use clean text, typography, and vector icons (`KintsuIcons.kt`).
- Colors must strictly adhere to `KintsuTheme.colors`, never hardcoded AMOLED black.
- Offline-first: No remote network dependencies required for minting, verification, or balance computation.
- Never allow wallet balance to drop below 0 (floored at 0; negative balance creates temporary debt badge).
- Device target is only `000553438000171`. Never target Android TV. Never auto-launch app after installation.

## Review Focus
1. Monotonic clock tampering: Setting device date forward to harvest coins must be rejected by `SystemClock.elapsedRealtime()` delta check.
2. Ledger hash-chain corruption: Any raw SQLite row alteration must trigger automatic reconciliation and wipe fabricated coins.
3. Negative deduction underflow: Deductions must gracefully handle balance reaching 0 without integer overflow.
4. Idempotency: Duplicate completion of the same habit or task on the same day must not award multiple coins.
5. Streak continuity across midnight: Day boundaries must calculate correctly using `epochDay` in local time.

---

## Mathematical Economy Calibration Model

```
========================================================================================
BASE EARNINGS (The 1 / 2 / 3 Rule):
- Low Priority Task, Daily Intention, Reels Resisted:               +1 Coin
- Habit Check-off, Medium Task, Focus 15-25m, Evening Journal:      +2 Coins
- High Priority Task, Deep Focus 45m+, Flawless Day, Weekly Review: +3 Coins

NEGATIVE PENALTIES (Strict Attention Taxes):
- Reels Binge Overstay (ignoring intervention > 5 min):             -3 Coins
- Bedtime Curfew Violation (opening blocked app during bedtime):    -5 Coins
- Continuous App Marathon past warning threshold:                  -2 Coins
- Broken Habit Decay (uncompleted habit at midnight):               -1 Coin

FIRE STREAK MULTIPLIER CURVE (1 to 150 Days):
- Days 1 - 6:     Spark               -> 1.0x
- Days 7 - 13:    Ember               -> 1.1x (+10%)
- Days 14 - 29:   Golden Flame        -> 1.25x (+25%)
- Days 30 - 59:   Sacred Blaze        -> 1.5x (+50%)
- Days 60 - 89:   Eternal Phoenix     -> 2.0x (DOUBLE COINS - 60-Day Milestone!)
- Days 90 - 99:   Solar Flare         -> 2.5x (+150%)
- Days 100 - 119: Centurion Flame     -> 3.0x (TRIPLE COINS - 100-Day Milestone!)
- Days 120 - 149: Divine Sun          -> 3.5x (+250%)
- Days 150+:      Transcendent Lotus  -> 4.0x (QUADRUPLE COINS - 150-Day Milestone!)

SHOP COMMITMENT PRICING (Expensively Balanced):
- Tier 1 (Parchment & Journal Accent):     150 - 250 Coins   (~2 weeks discipline)
- Tier 2 (Ambient Focus Soundscapes):       450 - 600 Coins   (~1 month discipline)
- Tier 3 (Signature Custom App Icons):      900 - 1,200 Coins (~1.5 - 2 months discipline)
- Tier 4 (Full Premium Sanctuary Themes):  2,000 - 3,500 Coins (~3 - 4 months discipline)
- Tier 5 (Legendary 150-Day Phoenix Skin): 5,000 - 7,500 Coins (~5 - 6 months discipline)
========================================================================================
```

---

## File Structure

```
app/src/main/java/com/kintsu/app/
├── coin/
│   ├── CoinSecurityGuard.kt          // Hardware KeyStore HMAC-SHA256, hash chain, anti-time-travel
│   ├── FireStreakEngine.kt           // 150-day tier progression, multipliers, streak evaluation
│   ├── NegativeEconomyEngine.kt      // Deductions: reels binge tax, bedtime breach, sloth decay
│   └── SanctuaryPassbookModels.kt    // Signed transaction models, audit logs, flame state
├── data/
│   ├── model/WellbeingEntities.kt    // Extended WalletTransactionEntity with hashes & signatures
│   ├── dao/WellbeingDao.kt           // Hash chain queries, sum aggregations, ledger insertion
│   ├── KintsuRepository.kt           // Integrated mint/deduct methods wired to activities
│   └── SettingsDataStore.kt          // Sealed balance hash, flame tier state, freeze shards
└── ui/
    ├── coin/
    │   ├── SanctuaryPassbookScreen.kt // Passbook statement, fire tier crest, integrity seal
    │   ├── FireStreakVisual.kt        // Animated canvas fire shader / flame particle visual
    │   └── KintsugiRepairDialog.kt    // Gold lacquer streak rekindle ceremony
    └── shop/
        └── ShopCatalog.kt             // Calibrated expensive pricing (150 to 7,500 coins)
```

---

## Implementation Tasks

### Task 1: Cryptographic Ledger & Hardware KeyStore Security Guard
**Files:**
- Create: `app/src/main/java/com/kintsu/app/coin/CoinSecurityGuard.kt`
- Modify: `app/src/main/java/com/kintsu/app/data/model/WellbeingEntities.kt:304-315`
- Modify: `app/src/main/java/com/kintsu/app/data/dao/WellbeingDao.kt`

- [ ] Step 1: Extend `WalletTransactionEntity` with `previousHash: String`, `transactionHash: String`, `signature: String`, `multiplier: Float`, `isDeduction: Boolean`.
- [ ] Step 2: Implement `CoinSecurityGuard` with Android KeyStore HMAC-SHA256 key generation (`kintsu_ledger_key` in AndroidKeyStore provider).
- [ ] Step 3: Implement `computeHash(prevHash, id, amount, type, epochDay, timestamp, nonce)` and `signPayload()`.
- [ ] Step 4: Implement monotonic clock anti-time-travel check (`SystemClock.elapsedRealtime()` comparison against stored epoch timestamp).
- [ ] Step 5: Implement zero-sum ledger audit function `reconcileBalance(transactions)` returning verified balance and tampering flag.

### Task 2: Fire Streak Engine (150-Day Multiplier Curve)
**Files:**
- Create: `app/src/main/java/com/kintsu/app/coin/FireStreakEngine.kt`
- Modify: `app/src/main/java/com/kintsu/app/data/KintsuRepository.kt`
- Modify: `app/src/main/java/com/kintsu/app/data/SettingsDataStore.kt`

- [ ] Step 1: Define `FireStreakTier` enum: `SPARK(1..6, 1.0f)`, `EMBER(7..13, 1.1f)`, `GOLDEN_FLAME(14..29, 1.25f)`, `SACRED_BLAZE(30..59, 1.5f)`, `PHOENIX(60..89, 2.0f)`, `SOLAR(90..99, 2.5f)`, `CENTURION(100..119, 3.0f)`, `DIVINE(120..149, 3.5f)`, `TRANSCENDENT(150..Int.MAX_VALUE, 4.0f)`.
- [ ] Step 2: Implement streak calculation reading `HabitLogEntity` consecutive completed dates.
- [ ] Step 3: Implement multiplier resolver applying active fire tier multiplier to base reward (1/2/3).
- [ ] Step 4: Implement Kintsugi Golden Streak Rekindle logic (deducts 25/50/100 coins based on tier to repair broken streak).

### Task 3: Negative Economy & Deduction Engine
**Files:**
- Create: `app/src/main/java/com/kintsu/app/coin/NegativeEconomyEngine.kt`
- Modify: `app/src/main/java/com/kintsu/app/wellbeing/service/KintsuAccessibilityBlockerService.kt`
- Modify: `app/src/main/java/com/kintsu/app/wellbeing/domain/DailyLimitTracker.kt`

- [ ] Step 1: Implement `deductCoins(amount, reason, sourceActivity, nonce)` in `CoinSecurityGuard` and `KintsuRepository`.
- [ ] Step 2: Wire reel overstay deduction (-3 coins per 5m binge) when user ignores `ReelInterventionOverlayActivity`.
- [ ] Step 3: Wire bedtime curfew violation deduction (-5 coins) when distracting apps are opened during active bedtime lock.
- [ ] Step 4: Ensure balance is never negative (minimum 0), recording deduction transaction in the signed ledger.

### Task 4: Deep Integration into Habits, Tasks, Focus & Mindful Sanctuary
**Files:**
- Modify: `app/src/main/java/com/kintsu/app/data/KintsuRepository.kt`
- Modify: `app/src/main/java/com/kintsu/app/ui/screens/HomeScreen.kt`
- Modify: `app/src/main/java/com/kintsu/app/ui/KintsuViewModel.kt`

- [ ] Step 1: Update `toggleHabitCompletion` to use base 2 coins * Fire Tier multiplier, with +2 morning routine bonus before 09:00 AM.
- [ ] Step 2: Update `toggleTaskCompletion` to use priority-weighted base (High: 3, Med: 2, Low: 1) * Fire Tier multiplier.
- [ ] Step 3: Add Perfect Day bonus (+3 coins * multiplier) when 100% of habits for the epoch day are completed.
- [ ] Step 4: Add Triad Batch bonus (+2 coins) on the 3rd task completed on the same day.
- [ ] Step 5: Wire Reel Intercept Conscious Exit (+1 coin * multiplier) when user chooses "Close Reels" or "1-min Breath".

### Task 5: Expensive Sanctuary Shop Calibration
**Files:**
- Modify: `app/src/main/java/com/kintsu/app/data/ShopCatalog.kt`
- Modify: `app/src/main/java/com/kintsu/app/ui/shop/ShopHomeScreen.kt`

- [ ] Step 1: Update prices in `ShopCatalog.kt` to match long-term commitment tiers (Accents: 150-250, Soundscapes: 450-600, App Icons: 900-1,200, Themes: 2,000-3,500, Legendary Phoenix: 5,000-7,500).
- [ ] Step 2: Add progress indicator showing days of discipline required for each catalog item based on user's average earning rate.

### Task 6: Sanctuary Passbook UI & Flame Visual
**Files:**
- Create: `app/src/main/java/com/kintsu/app/ui/coin/SanctuaryPassbookScreen.kt`
- Create: `app/src/main/java/com/kintsu/app/ui/coin/FireStreakVisual.kt`
- Modify: `app/src/main/java/com/kintsu/app/ui/components/KintsuTopBar.kt`

- [ ] Step 1: Build `FireStreakVisual.kt` featuring dynamic vector flame canvas that evolves color and particle count across Spark -> Phoenix -> Transcendent.
- [ ] Step 2: Build `SanctuaryPassbookScreen.kt` displaying:
  - Hero card: Reconciled gold coin balance + fire multiplier badge (e.g. Fire 2.0x Phoenix • Day 64).
  - Cryptographic badge: `[Cryptographically Verified • 0 Anomalies]`.
  - Filterable list of positive (green gold) and negative (rust crimson) signed transactions with exact reasons.
- [ ] Step 3: Connect top bar coin pill click on Home and Shop to open `SanctuaryPassbookScreen`.

### Task 7: Compilation, Verification & Device Deployment
- [ ] Step 1: Run `:app:compileDebugKotlin` and verify 0 errors.
- [ ] Step 2: Run `:app:assembleDebug` and generate APK.
- [ ] Step 3: Install APK to device `000553438000171` via `adb install -r`.
- [ ] Step 4: Verify positive awards on habit/task completion, multiplier calculation, negative deductions, and passbook rendering.
