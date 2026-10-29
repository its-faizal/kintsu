# BLOCKED.md: Cut, Skipped & Deferred Features

This document logs features that were audited and deliberately cut, skipped, or modified to protect stability, user safety, and adherence to Android OS guidelines.

---

### 1. In-App Browser URL Sniffing via Accessibility
- **Status:** CUT from Phase 1.
- **Reason:** Parsing accessibility node hierarchies of third-party browsers (Chrome, Firefox, Brave, Samsung Internet) to block specific websites is fragile. Class names and view IDs change with browser updates, leading to false positives and high CPU drain.
- **Alternative:** App-level blocking in Phase 1; DNS/VPN-based domain filtering deferred to Phase 2.

### 2. True Impossible Uninstallation (MDM Device Owner)
- **Status:** SKIPPED / MODIFIED to Friction Mode.
- **Reason:** Consumer Android does not allow an app to block its own removal without enterprise Device Owner provisioning (via NFC or factory reset QR). 
- **Alternative:** Implemented optional Device Administration with a 24-hour commitment cooling-off warning and deactivation friction.

### 3. Blocking System-Critical Applications
- **Status:** HARD-CODED FORBIDDEN.
- **Target Apps:** Phone (`com.android.dialer`, `com.google.android.dialer`), Emergency Contacts, SMS/Messages, System Settings (`com.android.settings`), Default Launcher, System Keyboards (IME), Clock/Alarm (`com.google.android.deskclock`), and Kintsu (`com.kintsu.app`).
- **Reason:** Non-negotiable safety requirement. A user must never be locked out of emergency communication or device configuration.

### 4. Background Activity Launching without Overlay/Accessibility Privileges
- **Status:** CUT.
- **Reason:** Calling `startActivity` directly from a background service is blocked on Android 10+ (BAL restrictions).
- **Alternative:** Accessibility overlay (`TYPE_ACCESSIBILITY_OVERLAY`) + immediate home redirect (`GLOBAL_ACTION_HOME`) + user-driven full-screen launch.
