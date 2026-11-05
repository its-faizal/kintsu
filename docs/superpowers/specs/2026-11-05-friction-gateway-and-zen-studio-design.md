# Design Document: Mindful Friction Gateway & Zen Studio with 5 High-Resolution Soundscapes

**Date:** 2026-11-05  
**Topic:** Pre-App Mindful Interception & Deep Focus Studio with Ambient Soundscapes and Desk Flip Clock  
**Target Repository:** Kintsu Digital Wellbeing (Android / Jetpack Compose)

---

## 1. Intent & Purpose

Kintsu's digital wellbeing architecture currently excels at hard-enforcing app limits, blocking doom-scrolling reels, and rewarding users with coins for streaks. However, true digital autonomy requires two additional pillars:
1. **Pre-App Friction (The Mindful Gateway):** Breaking the unconscious dopamine habit loop *before* a distracting app opens, nudging users to declare their intention and setting an automatic micro-timer.
2. **Deep Work Immersion (Zen Studio & Flip Clock):** Providing an offline, tranquil sanctuary for focused work and study with 5 high-resolution ambient soundscapes and a luxury split-flap desk clock.

---

## 2. Goals, Non-Goals & Anti-Goals

### Goals
- **Friction Gateway:**
  - Intercept configured distracting apps upon initial launch before the user can interact with them.
  - Display a 5-second harmonic breath circle ("Inhale... Exhale... Open with intention").
  - Offer deliberate session options: "5 Minutes", "15 Minutes", or "Step Back".
  - If "Step Back" is tapped, immediately navigate home and award +5 Kintsu Coins for practicing conscious restraint.
  - If a timed session is selected, launch the app with Kintsu's draggable **Floating Gold Pill Countdown** active. When the countdown hits 00:00, smoothly block or exit the app.
- **Zen Studio & Desk Flip Clock:**
  - Create a full-screen immersive studio mode with luxury obsidian/gold mechanical split-flap clock typography.
  - Support both portrait and landscape desk-stand orientations.
  - Implement 5 high-resolution, seamlessly looping audio soundscapes:
    1. *Bamboo Forest Rain* (Soft rainfall on bamboo grove)
    2. *Sacred Singing Bowl* (Resonant 432Hz Tibetan bronze bowl harmonics)
    3. *River Stream & Koto* (Tranquil mountain water with ambient acoustic strings)
    4. *Fireside Embers* (Warm crackling cedar hearth fire)
    5. *Night Mist & Temple Bell* (Calming night breeze with distant bronze wind bell)
  - Background audio playback with system media controls, audio focus handling, and sleep timer.
  - Reward coins upon completing focused sessions (e.g., 25m Pomodoro).

### Non-Goals
- Real-time online streaming of audio tracks (all 5 soundscapes must work 100% offline).
- Complex multi-track mixing console (keep the audio controls zen, uncluttered, and minimalist).

### Anti-Goals
- Intrusive or annoying friction that interrupts productivity apps (e.g. phone, calculator, messages).
- Audio distortion, harsh clicks at loop boundaries, or battery drain.
- Any emojis in the UI or strings (maintaining the strict Kintsu luxury aesthetic).

---

## 3. Constraints

- **STRICT COMPILATION CONSTRAINT:** Do not compile or assemble the APK via Gradle (`DONT COMPLIE THE APK .`). All code, assets, audio resources, and manifests must be syntactically valid and git-committed.
- **Target Device:** USB device `000553438000171` (do not run Wi-Fi adb or unauthorized background daemons).
- **Aesthetic:** Obsidian `#141210`, warm gold `#D4AF37`, ceramic ivory `#F5F2EB`, and typography adhering strictly to `KintsuTheme`.

---

## 4. Approaches Considered

### Approach A: Integrated Overlay with Foreground Audio Service (Chosen)
- **Friction Gateway:** Implemented as a lightweight translucent activity (`MindfulFrictionOverlayActivity`) triggered by `KintsuAccessibilityBlockerService` on `TYPE_WINDOW_STATE_CHANGED`. Integrates directly with `KintsuFloatingPillOverlay` to carry over the active micro-pass countdown.
- **Zen Studio:** A dedicated Jetpack Compose screen and fullscreen activity (`ZenStudioActivity`) featuring interactive split-flap clock canvas rendering.
- **Audio Engine:** `ZenAudioService` using Android `MediaPlayer` / `ExoPlayer` with smooth fade-in/fade-out, looping, and high-fidelity local sound generation.
- **Why Chosen:** Reliable across all Android API versions, fully isolated, works completely offline, and reuses our existing floating pill and accessibility service infrastructure seamlessly.

### Approach B: In-Process Dialog Injection inside Monitored Apps
- Injecting view overlays directly via WindowManager without an activity.
- **Why Rejected:** Fragile across different OEM skins and keyboard interactions, and lacks clean back-button and home-gesture handling compared to a dedicated translucent activity.

---

## 5. System Architecture & Components

```
                    ┌────────────────────────────────────────┐
                    │     KintsuAccessibilityBlockerService   │
                    └───────────────────┬────────────────────┘
                                        │
             App Opened                 │ Approaching Limit / Emergency Pass
                 ▼                      ▼
  ┌──────────────────────────────┐  ┌──────────────────────────────┐
  │ MindfulFrictionOverlayActivity│  │  KintsuFloatingPillOverlay   │
  │  - 5s Harmonic Breathing     │  │   - Real-time Live Seconds   │
  │  - "5m" / "15m" / "Step Back"│  │   - Draggable Luxury Seam    │
  └──────────────┬───────────────┘  └──────────────────────────────┘
                 │
   User chooses "Step Back" (+5 Coins) OR Micro-Pass (arms Pill)
                 │
                 ▼
  ┌────────────────────────────────────────────────────────────┐
  │                    Zen Studio & Audio Engine                │
  │  ┌────────────────────────────┐  ┌────────────────────────┐ │
  │  │   ZenFlipClock Component   │  │    ZenAudioService     │ │
  │  │ - Split-flap luxury digits │  │ - 5 High-Res Loops     │ │
  │  │ - Landscape/Portrait desk  │  │ - Gapless Fade In/Out  │ │
  │  └────────────────────────────┘  └────────────────────────┘ │
  └────────────────────────────────────────────────────────────┘
```

### Component Details
1. **`MindfulFrictionOverlayActivity.kt`** (`com.kintsu.app.wellbeing.ui`):
   - Translucent obsidian theme (`@style/Theme.Kintsu.TranslucentOverlay`).
   - Animated breath guide circle scaling smoothly with `Animatable`.
   - Action buttons: "5 Minutes", "15 Minutes", "Reflect & Step Back".
2. **`ZenAudioPlayer.kt` & `ZenAudioService.kt`** (`com.kintsu.app.audio`):
   - Manages playback of the 5 ambient soundscapes.
   - Built-in volume attenuation, gentle 800ms fade-in/out transitions, and seamless loop resets.
   - Media notification for background listening.
3. **5 High-Resolution Audio Assets** (`app/src/main/res/raw/`):
   - `zen_sound_rain.ogg` (Bamboo Forest Rain)
   - `zen_sound_bowl.ogg` (Sacred Singing Bowl)
   - `zen_sound_stream.ogg` (River Stream & Koto)
   - `zen_sound_fire.ogg` (Fireside Embers)
   - `zen_sound_bell.ogg` (Night Mist & Temple Bell)
4. **`ZenStudioScreen.kt` & `ZenFlipClock.kt`** (`com.kintsu.app.ui.focus`):
   - Split-flap desk clock rendering with animated top/bottom flap rotation.
   - Ambient sound selector row with gentle gold glow on active track.
   - Session timer (15m, 25m, 45m, 60m or infinite stopwatch).
   - Coin reward attribution on completion via `KintsuRepository`.
