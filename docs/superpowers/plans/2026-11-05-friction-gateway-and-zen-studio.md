# Mindful Friction Gateway & Zen Studio with 5 High-Res Soundscapes Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a pre-app Mindful Friction Gateway that intercepts distracting apps with a 5-second breath guide and micro-session timer, plus an immersive Zen Studio with a split-flap desk flip clock and 5 high-resolution ambient soundscapes.

**Architecture:** 
1. The pre-app gateway uses `KintsuAccessibilityBlockerService` window state detection to trigger `MindfulFrictionOverlayActivity`, integrating with `KintsuFloatingPillOverlay` for live countdown.
2. Zen Studio provides a fullscreen landscape/portrait Jetpack Compose screen featuring a custom mechanical split-flap desk clock.
3. The audio engine uses `ZenAudioPlayer` and `ZenAudioService` to play 5 high-fidelity, seamlessly looping ambient audio tracks with background playback and notification controls.

**Tech Stack:** Kotlin, Jetpack Compose, Android Accessibility Service, MediaPlayer/AudioTrack, Coroutines/StateFlow, Room Database.

**Spec:** `docs/superpowers/specs/2026-11-05-friction-gateway-and-zen-studio-design.md`

## Global Constraints
- Strictly do NOT compile or assemble the APK via Gradle (`DONT COMPLIE THE APK .`).
- Device target: USB device `000553438000171`.
- UI Design: Luxury obsidian (`#141210`), warm gold (`#D4AF37`), ceramic ivory (`#F5F2EB`).
- String Rules: Zero emojis in any UI labels or strings.

## Review Focus
1. Rapid app switching: Re-opening the same app within 10 seconds of an approved micro-pass should not trigger repeated friction overlays.
2. Background audio focus: Incoming phone calls or other media playback must pause or duck ambient audio smoothly without crashes.
3. Clean loop transitions: The 5 ambient soundscapes must loop seamlessly without audible clicks or gaps.
4. Back navigation: Tapping the Android back gesture on the friction screen must safely dismiss back to the launcher rather than letting the user fall through into the restricted app.
5. Landscape flip clock: Fullscreen desk clock must dynamically scale split-flap tiles cleanly between portrait and landscape modes without clipping.

---

### Task 1: Generate 5 High-Resolution Ambient Soundscapes

**Files:**
- Create: `app/src/main/res/raw/zen_sound_rain.wav`
- Create: `app/src/main/res/raw/zen_sound_bowl.wav`
- Create: `app/src/main/res/raw/zen_sound_stream.wav`
- Create: `app/src/main/res/raw/zen_sound_fire.wav`
- Create: `app/src/main/res/raw/zen_sound_bell.wav`
- Create: `scripts/generate_zen_audio.py`

**Interfaces:**
- Produces: 5 stereo 44.1kHz 16-bit PCM WAV audio resources in `res/raw/` with zero-crossing loop points for gapless ambient playback:
  1. `R.raw.zen_sound_rain` (Bamboo Forest Rain)
  2. `R.raw.zen_sound_bowl` (Sacred Singing Bowl 432Hz)
  3. `R.raw.zen_sound_stream` (River Stream & Koto)
  4. `R.raw.zen_sound_fire` (Fireside Embers)
  5. `R.raw.zen_sound_bell` (Night Mist & Temple Bell)

- [ ] **Step 1: Write audio synthesis script `scripts/generate_zen_audio.py`**
  - Implement algorithmic procedural audio generators for:
    - Rain (filtered pink noise with variable droplet impulses)
    - Singing Bowl (432 Hz fundamental with natural inharmonic bronze overtones and exponential decay)
    - Stream (brown/pink noise modulating resonant low-pass filter with gentle pentatonic koto plucks)
    - Fireside (low thermal rumble with discrete high-frequency crackle/pop transients)
    - Temple Bell (deep 144 Hz bronze gong tone with gentle wind harmonic breeze)
  - Ensure beginning and end samples fade smoothly to zero for gapless looping.

- [ ] **Step 2: Generate all 5 audio files in `app/src/main/res/raw/`**
  - Run `python scripts/generate_zen_audio.py`
  - Verify all 5 files exist and have non-zero size.

- [ ] **Step 3: Commit**
  - `git add app/src/main/res/raw/ scripts/generate_zen_audio.py`
  - `git commit -m "feat(audio): add 5 high-resolution ambient soundscapes in res/raw"`

---

### Task 2: Zen Audio Engine & Playback Controller

**Files:**
- Create: `app/src/main/java/com/kintsu/app/audio/ZenSoundTrack.kt`
- Create: `app/src/main/java/com/kintsu/app/audio/ZenAudioPlayer.kt`
- Create: `app/src/main/java/com/kintsu/app/audio/ZenAudioService.kt`

**Interfaces:**
- Consumes: `R.raw.zen_sound_*` resources
- Produces: 
  - `ZenSoundTrack` enum: `RAIN`, `SINGING_BOWL`, `RIVER_STREAM`, `FIRESIDE_EMBERS`, `TEMPLE_BELL` with titles and descriptions.
  - `ZenAudioPlayer`:
    - `fun playTrack(track: ZenSoundTrack, volume: Float = 0.8f)`
    - `fun stopTrack(fadeOutMs: Long = 800)`
    - `fun setVolume(volume: Float)`
    - `val currentTrack: StateFlow<ZenSoundTrack?>`
    - `val isPlaying: StateFlow<Boolean>`
  - `ZenAudioService`: Foreground Android service managing audio focus and notification controls.

- [ ] **Step 1: Create `ZenSoundTrack.kt`**
  - Define enum with title, subtitle, duration, and `rawResId`.

- [ ] **Step 2: Implement `ZenAudioPlayer.kt`**
  - Wrap Android `MediaPlayer` with `isLooping = true`, smooth volume ramping coroutines for 800ms fade-in and fade-out, and audio focus management via `AudioManager`.

- [ ] **Step 3: Implement `ZenAudioService.kt`**
  - Android service handling media notification with Play/Pause action and sticky foreground lifecycle.
  - Register `ZenAudioService` in `AndroidManifest.xml`.

- [ ] **Step 4: Commit**
  - `git add app/src/main/java/com/kintsu/app/audio/ AndroidManifest.xml`
  - `git commit -m "feat(audio): implement ZenAudioPlayer and ZenAudioService"`

---

### Task 3: Luxury Split-Flap Desk Flip Clock Component

**Files:**
- Create: `app/src/main/java/com/kintsu/app/ui/focus/ZenFlipClock.kt`

**Interfaces:**
- Consumes: Time values (hours, minutes, seconds).
- Produces: 
  - `@Composable fun ZenFlipClock(minutes: Int, seconds: Int, modifier: Modifier)`
  - `@Composable fun SplitFlapDigit(digit: Char, modifier: Modifier)`

- [ ] **Step 1: Implement `SplitFlapDigit` with 3D Flip Rotation**
  - Top flap: stationary upper half of digit on obsidian card (`#1C1A17`) with gold top rim.
  - Bottom flap: lower half of digit on obsidian card with gold bottom rim.
  - Flipping flap: uses `graphicsLayer { rotationX = animatedAngle; cameraDistance = 16f }` to create an authentic mechanical split-flap fold.
  - Horizontal gold hairline seam divider (`#D4AF37`) across the center.

- [ ] **Step 2: Implement `ZenFlipClock`**
  - Pairs 2 digits for minutes, separator colon, and 2 digits for seconds.
  - Responsive sizing that scales cleanly in both portrait and landscape tablet/desk orientations.

- [ ] **Step 3: Commit**
  - `git add app/src/main/java/com/kintsu/app/ui/focus/ZenFlipClock.kt`
  - `git commit -m "feat(ui): add luxury split-flap ZenFlipClock component"`

---

### Task 4: Zen Studio Fullscreen Mode & Ambient Selector

**Files:**
- Create: `app/src/main/java/com/kintsu/app/ui/focus/ZenStudioScreen.kt`
- Create: `app/src/main/java/com/kintsu/app/ui/focus/ZenStudioActivity.kt`
- Modify: `app/src/main/AndroidManifest.xml`

**Interfaces:**
- Consumes: `ZenAudioPlayer`, `ZenFlipClock`, `KintsuRepository`
- Produces:
  - Fullscreen immersive activity supporting sensor-based landscape and portrait orientation.
  - Soundscape selector row allowing one-tap switching between the 5 ambient tracks.
  - Session timer modes: 15m, 25m (Pomodoro), 45m, 60m, or continuous stopwatch.
  - Coin completion modal awarding +25 Kintsu Coins upon finishing a focused session.

- [ ] **Step 1: Implement `ZenStudioScreen.kt`**
  - Immersive obsidian surface (`#100E0D`).
  - Top bar with back navigation, orientation lock, and session mode selector.
  - Center stage: `ZenFlipClock` with glowing gold reflection.
  - Bottom bar: Horizontal scroll of 5 sound cards with active gold halo, volume slider, and session start/pause/complete buttons.

- [ ] **Step 2: Implement `ZenStudioActivity.kt`**
  - Fullscreen flag handling (`WindowInsetsControllerCompat.hide(WindowInsetsCompat.Type.systemBars())`).
  - Keep screen on flag during active focus sessions (`FLAG_KEEP_SCREEN_ON`).

- [ ] **Step 3: Add `ZenStudioActivity` to `AndroidManifest.xml`**
  - Configured with `android:configChanges="orientation|screenSize"`.

- [ ] **Step 4: Commit**
  - `git add app/src/main/java/com/kintsu/app/ui/focus/ app/src/main/AndroidManifest.xml`
  - `git commit -m "feat(focus): add ZenStudio fullscreen screen and activity"`

---

### Task 5: Mindful Friction Gateway Pre-App Interception

**Files:**
- Create: `app/src/main/java/com/kintsu/app/wellbeing/ui/MindfulFrictionOverlayActivity.kt`
- Modify: `app/src/main/AndroidManifest.xml`
- Modify: `app/src/main/java/com/kintsu/app/service/KintsuAccessibilityBlockerService.kt`

**Interfaces:**
- Consumes: `KintsuFloatingPillOverlay`, `KintsuRepository`
- Produces:
  - Pre-app intention gate triggered on opening monitored apps.
  - 5-second harmonic breath pacing circle.
  - 3 action options: "5 Minutes", "15 Minutes", "Reflect & Step Back".
  - If "Step Back", navigates home and awards +5 Coins.
  - If 5m/15m selected, pins the floating gold pill timer and permits app usage until the timer runs out.

- [ ] **Step 1: Implement `MindfulFrictionOverlayActivity.kt`**
  - Translucent obsidian background.
  - Pulse-breathing gold circle using Jetpack Compose `Animatable` (4-second inhale, 4-second exhale).
  - Target app icon and name centered.
  - Intention buttons:
    - "5 Minutes Micro-Pass"
    - "15 Minutes Focus Pass"
    - "Reflect & Step Back" (+5 Coins)
  - Back press handling: Safely returns home.

- [ ] **Step 2: Integrate Launch Detection into `KintsuAccessibilityBlockerService.kt`**
  - Track `lastApprovedPackage` and `approvedUntilMillis`.
  - When a monitored app window appears and is not in an active pass:
    - Launch `MindfulFrictionOverlayActivity`.
  - When approved with micro-pass:
    - Arm `KintsuFloatingPillOverlay` with the approved duration.
    - When the pass expires, trigger standard Kintsu block overlay.

- [ ] **Step 3: Add `MindfulFrictionOverlayActivity` to `AndroidManifest.xml`**
  - Set theme to `@style/Theme.Kintsu.TranslucentOverlay` and `android:launchMode="singleTop"`.

- [ ] **Step 4: Commit**
  - `git add app/src/main/java/com/kintsu/app/wellbeing/ui/ app/src/main/java/com/kintsu/app/service/ app/src/main/AndroidManifest.xml`
  - `git commit -m "feat(wellbeing): implement Mindful Friction Gateway pre-app interception"`

---

### Task 6: Add Zen Studio Entry Point to Navigation

**Files:**
- Modify: `app/src/main/java/com/kintsu/app/ui/focus/FocusScreen.kt` or `app/src/main/java/com/kintsu/app/MainActivity.kt`

**Interfaces:**
- Consumes: `ZenStudioActivity`
- Produces:
  - Luxury "Enter Zen Studio" button with gold shimmer border in the Focus / Wellbeing tab.
  - Tapping launches `ZenStudioActivity`.

- [ ] **Step 1: Add Zen Studio Card to Focus UI**
  - Elegant card with split-flap preview icon, "Zen Studio & Desk Clock" header, and "5 Ambient Soundscapes" tag.

- [ ] **Step 2: Commit & Final Verification**
  - Verify working tree status with `git status`
  - Ensure zero gradle compilation commands were executed.
  - `git commit -am "feat(navigation): add Zen Studio entry point in Focus tab"`
  - `git push origin main`
