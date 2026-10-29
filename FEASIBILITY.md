# Feasibility Audit: Kintsu V3 Digital Wellbeing & Addiction Blocking Suite

**Target Platform:** Android 12 (API 31) through Android 15 (API 35+)  
**Date:** October 2026  
**Status:** Completed & Approved for Build  

---

## 1. Foreground App Detection: UsageStats vs AccessibilityService

| Metric | UsageStatsManager (`PACKAGE_USAGE_STATS`) | AccessibilityService (`AccessibilityEvent`) |
|---|---|---|
| **Latency** | 500ms - 1500ms (polling loop interval) | **< 50ms (instantaneous OS event push)** |
| **Battery Drain** | Moderate to High (CPU wakeups every 500ms) | **Extremely Low (event-driven, 0 CPU when idle)** |
| **Reliability** | Susceptible to polling jitter and OEM timer throttling | **High (fires synchronously on `TYPE_WINDOW_STATE_CHANGED`)** |
| **Intervention Speed**| App is briefly usable before block screen appears | **Intercepts before user can interact with the app** |
| **OS Restrictions**| Background activity launch (BAL) restrictions in Android 10+ block starting activities from background | **Accessibility service can launch activities or display `TYPE_ACCESSIBILITY_OVERLAY`** |
| **Grant Friction** | Special app access settings screen | Accessibility settings screen (may require "Allow restricted settings" on sideloaded APKs) |

### Selected Primary Architecture: Hybrid Engine
- **Primary Enforcer:** `AccessibilityService` (`KintsuAccessibilityBlockerService`) handles real-time app change detection (`TYPE_WINDOW_STATE_CHANGED`), instant mindful pause interventions, and immediate block overlays without battery-draining polling loops.
- **Secondary Metric Provider:** `UsageStatsManager` provides historical analytics, screen-time totals, pickup/unlock counting, and 7-day/30-day aggregations for Feature 8 (Screen-Time Insights).
- **Justification:** Users with addiction triggers experience compulsive quick-opens. A 1000ms polling delay allows the user to see dopamine-triggering feeds or notifications before the shield appears. Only `AccessibilityService` guarantees sub-50ms intervention without flattening the battery.

---

## 2. Presenting the Block Screen (Activity vs Overlay)

### Android OS Restrictions:
- Starting an activity from the background (`startActivity`) is blocked since Android 10 unless the app has an existing visible window, the user clicks a notification, or the app has `SYSTEM_ALERT_WINDOW`.
- On Android 14/15, Background Activity Launches (BAL) are even more strictly enforced.

### Solution:
1. **Accessibility Overlay Window (`TYPE_ACCESSIBILITY_OVERLAY` / `TYPE_APPLICATION_OVERLAY`)**:
   - The Accessibility Service displays a calm, full-screen Compose-rendered view directly above the blocked app.
2. **Graceful Redirect to Full-Screen Activity**:
   - The overlay simultaneously issues an intent to launch `BlockActivity` (or sends the user safely back to Home via `GLOBAL_ACTION_HOME`) so the user cannot interact with the underlying blocked application.
3. **Smooth Spring Animations**:
   - The intervention is styled with Apple-grade wabi-sabi minimalism: soft breathing animation, neutral tones, and no jarring transitions.

---

## 3. Service Survival & OEM Battery Management

1. **Foreground Service:**
   - Runs with `foregroundServiceType="specialUse"` / `"dataSync"` on Android 14+ with an honest ongoing notification ("Kintsu Wellbeing Protection Active").
2. **Boot Receiver (`BootAndAlarmReceiver`):**
   - Listens for `ACTION_BOOT_COMPLETED` and `ACTION_MY_PACKAGE_REPLACED` to reschedule alarms and verify protection state.
3. **Battery Optimization Exemption:**
   - Guided flow to `ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` prevents aggressive OEM task killers (e.g. Samsung OneUI, Xiaomi MIUI, Nothing OS) from pausing background monitors.
4. **Periodic Watchdog Worker:**
   - A lightweight `WorkManager` periodic worker runs every 15 minutes to verify the service status and self-repair if inadvertently stopped.
5. **Fail-Safe Mechanism:**
   - SharedPreferences-backed crash counter: if the blocking engine crashes 3 times within a 10-minute window, it automatically disarms itself, flags the state in `SettingsDataStore`, and presents a gentle notification explaining that protection was paused for safety.

---

## 4. Permissions Breakdown (Plain Language)

1. **Accessibility Service (`BIND_ACCESSIBILITY_SERVICE`)**
   - *Purpose:* Detects when a distracting app is opened and immediately displays the mindful pause or block shield.
2. **Usage Access (`PACKAGE_USAGE_STATS`)**
   - *Purpose:* Calculates daily screen time, phone unlocks, and app usage breakdown.
3. **Display Over Other Apps (`SYSTEM_ALERT_WINDOW`)**
   - *Purpose:* Allows the calm block screen and breathing pause to appear smoothly above restricted apps.
4. **Notification Policy Access (`ACCESS_NOTIFICATION_POLICY`)**
   - *Purpose:* Enables Do Not Disturb during Digital Detox, Bedtime, and Focus sessions so the phone stays silent.
5. **Post Notifications (`POST_NOTIFICATIONS`)**
   - *Purpose:* Shows the ongoing protection status and mindful reminders.
6. **Ignore Battery Optimizations (`REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`)**
   - *Purpose:* Prevents the OS from putting Kintsu's scheduled blocks and timers to sleep.
7. **Device Administrator (`BIND_DEVICE_ADMIN`) [Optional]**
   - *Purpose:* Adds friction against impulsive uninstalls during strict commitment lock periods.

---

## 5. Feasibility Limits & Cut Features (Honest Boundary Report)

### 1. True Impossible-to-Uninstall Enforcement
- **Verdict:** Cut / Skipped.
- **Reason:** On unrooted consumer Android, an app cannot completely prevent its own uninstallation without Device Owner (MDM enterprise enrollment). Device Admin can add a deactivation step with a cooling-off warning, but cannot guarantee 100% uninstall lockout. We implement Device Admin friction with transparent user copy.

### 2. Deep Browser URL Content Sniffing via Accessibility
- **Verdict:** Cut from Phase 1.
- **Reason:** Inspecting URL text nodes across dozens of third-party browsers (Chrome, Firefox, Brave, Opera, Edge) is fragile, prone to breaking across browser UI updates, and introduces performance and privacy overhead. Phase 1 focuses on rock-solid app-level blocking. System-wide domain blocking is deferred to Phase 2 via local loopback VPN.

### 3. Blocking Protected Essentials
- **Verdict:** Hard-Coded Forbidden.
- **Protected Apps:** Phone/Dialer, Emergency Calling, SMS/Messages, System Settings, Default Launcher/Home, Virtual Keyboard (IME), System Clock/Alarms, and Kintsu itself.
- **Reason:** Safety-first principle. The user must never be prevented from making emergency calls or accessing device settings.

---

## 6. Recommended Build Scope

- **Step 0:** Feasibility audit and architecture design (**Completed**).
- **Step 1:** Clean start initialization (Zero dummy tasks/rituals on fresh launch; clean empty states).
- **Step 2:** Home UI Header Revamp (Profile avatar on top-left opening Profile & Settings; Kintsugi Gold Coins balance & Sanctuary Shop icon near Profile).
- **Step 3:** Wellbeing Setup Wizard & Protection Status card.
- **Step 4:** Room Database V3 Migration & Entities (App groups, rules, schedules, limits, detox sessions, quit-habits, urge logs, commitment locks, shop inventory).
- **Step 5:** Blocking Engine & Accessibility Service (`KintsuAccessibilityBlockerService` + `BlockActivity` + Fail-safe).
- **Step 6:** App Groups & Category Suggestion Engine.
- **Step 7:** Block Modes (Daily limits, multi-window schedules, focus integration, mindful breathing pause intervention).
- **Step 8:** Digital Detox Mode (1h, 3h, 8h, 24h, custom, allowlist, DND, gold repair integration).
- **Step 9:** Strictness Levels & 24h Commitment Lock.
- **Step 10:** Quit-Habit Support ("Break a habit", clean streak, urge log with triggers, SOS urge-surfing flow, trusted contact, Gold Repair crack repair).
- **Step 11:** Screen-Time Insights & Analytics (pickups, uninterrupted sessions, daily goal, Progress & Weekly review).
- **Step 12:** Sanctuary Shop (spend earned Kintsugi gold coins on themes, soundscapes, repair gold, achievement badges).
- **Step 13:** Backup / Restore V3 Schema updates & Verification.
- **Step 14:** Comprehensive Unit Tests, Verification, and Signed Debug APK Build.
