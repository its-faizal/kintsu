# RECOVERY.md: Emergency Recovery & Failsafe Guide

This guide provides failsafe instructions to safely disable or disarm Kintsu's blocking engine in the event of an unexpected lock-out or loop.

---

## 1. Automatic In-App Failsafe
Kintsu includes a built-in automated circuit breaker:
- **Crash Detection:** If the blocking engine encounters 3 unexpected crashes or errors within a 10-minute window, it automatically disables all active block rules and disarms the accessibility overlay.
- **Notification:** A gentle notice appears stating: *"Kintsu Wellbeing Protection was paused for your safety."*
- **Settings Toggle:** You can re-enable it manually in **Settings → Digital Wellbeing → Protection Status**.

---

## 2. In-App Emergency Exit (Always Available)
On every block shield:
1. Tap the **Emergency Exit** button at the bottom of the shield.
2. In **Gentle Mode**: The block is immediately dismissed with a single tap.
3. In **Balanced Mode**: Select a reason (e.g., "Work urgent", "Safety check") to unlock for 15 minutes.
4. In **Strict Mode**: A calm 60-second reflection countdown begins. Once the 60 seconds elapse, type the confirmation phrase *"I choose to continue"* to unlock the app for 15 minutes.

---

## 3. Emergency ADB Commands (Direct Computer Bypass)
If you have a computer connected via USB and need to instantly disable all blocking services from the command line:

### A. Disable Kintsu Accessibility Service
```bash
adb shell settings put secure enabled_accessibility_services ""
```
*Effect:* Instantly stops the accessibility event listener and removes all active overlay shields.

### B. Revoke Overlay Permission
```bash
adb shell pm revoke com.kintsu.app.debug android.permission.SYSTEM_ALERT_WINDOW
```

### C. Force Stop Kintsu App & Services
```bash
adb shell am force-stop com.kintsu.app.debug
```

### D. Clear Wellbeing Block Flags via App Data Reset (Preserving Core Database if needed)
```bash
adb shell am broadcast -a com.kintsu.app.ACTION_EMERGENCY_DISARM -p com.kintsu.app.debug
```

---

## 4. Protected Essentials (Guaranteed Never Blocked)
The following apps are hard-coded in the engine and **can never be blocked under any circumstances**:
- Phone / Dialer (`com.android.dialer`, `com.google.android.dialer`, `com.samsung.android.dialer`)
- Emergency Services
- SMS / Messages (`com.google.android.apps.messaging`)
- Device Settings (`com.android.settings`)
- System Launcher / Home
- Virtual Keyboard (Gboard, Samsung Keyboard, etc.)
- Clock / Alarm (`com.google.android.deskclock`, `com.sec.android.app.clockpackage`)
- Kintsu App (`com.kintsu.app`, `com.kintsu.app.debug`)
