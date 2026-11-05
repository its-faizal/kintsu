import os
import math
import wave
import struct
import random

SAMPLE_RATE = 44100
DURATION_SEC = 16  # 16-second loopable chunk
TOTAL_SAMPLES = int(SAMPLE_RATE * DURATION_SEC)

OUTPUT_DIR = os.path.join(os.path.dirname(__file__), "..", "app", "src", "main", "res", "raw")
os.makedirs(OUTPUT_DIR, exist_ok=True)

def write_wav(filename, left_samples, right_samples):
    filepath = os.path.join(OUTPUT_DIR, filename)
    with wave.open(filepath, "w") as wav_file:
        wav_file.setnchannels(2)
        wav_file.setsampwidth(2)
        wav_file.setframerate(SAMPLE_RATE)
        
        # Apply crossfade loop to ensure zero clicks at loop boundary
        crossfade_len = int(SAMPLE_RATE * 1.0)
        n = len(left_samples)
        for i in range(crossfade_len):
            alpha = i / float(crossfade_len)
            # fade in start with end
            left_samples[i] = left_samples[i] * alpha + left_samples[n - crossfade_len + i] * (1.0 - alpha)
            right_samples[i] = right_samples[i] * alpha + right_samples[n - crossfade_len + i] * (1.0 - alpha)
        
        # Trim the wrapped tail
        samples_to_write = n - crossfade_len
        
        frames = bytearray()
        for i in range(samples_to_write):
            l = max(-32767, min(32767, int(left_samples[i] * 32767)))
            r = max(-32767, min(32767, int(right_samples[i] * 32767)))
            frames.extend(struct.pack("<hh", l, r))
            
        wav_file.writeframes(frames)
    print(f"Generated {filepath} ({samples_to_write / SAMPLE_RATE:.1f}s)")

# 1. Bamboo Forest Rain
def generate_rain():
    print("Synthesizing Bamboo Forest Rain...")
    left = [0.0] * TOTAL_SAMPLES
    right = [0.0] * TOTAL_SAMPLES
    
    # Pink noise approximation with IIR filters
    b0_l, b1_l, b2_l = 0.0, 0.0, 0.0
    b0_r, b1_r, b2_r = 0.0, 0.0, 0.0
    
    for i in range(TOTAL_SAMPLES):
        white_l = random.uniform(-1.0, 1.0)
        white_r = random.uniform(-1.0, 1.0)
        
        b0_l = 0.99765 * b0_l + white_l * 0.0990460
        b1_l = 0.96300 * b1_l + white_l * 0.2965164
        b2_l = 0.57000 * b2_l + white_l * 1.0526913
        pink_l = (b0_l + b1_l + b2_l + white_l * 0.1848) * 0.08
        
        b0_r = 0.99765 * b0_r + white_r * 0.0990460
        b1_r = 0.96300 * b1_r + white_r * 0.2965164
        b2_r = 0.57000 * b2_r + white_r * 1.0526913
        pink_r = (b0_r + b1_r + b2_r + white_r * 0.1848) * 0.08
        
        # Gentle swell
        mod = 0.85 + 0.15 * math.sin(2.0 * math.pi * 0.08 * (i / SAMPLE_RATE))
        left[i] = pink_l * mod
        right[i] = pink_r * mod

    # Add bamboo rain droplets
    num_drops = int(DURATION_SEC * 18)
    for _ in range(num_drops):
        start_idx = random.randint(0, TOTAL_SAMPLES - int(SAMPLE_RATE * 0.3))
        drop_freq = random.uniform(800.0, 2400.0)
        pan = random.uniform(0.1, 0.9)
        drop_dur = int(SAMPLE_RATE * random.uniform(0.04, 0.12))
        vol = random.uniform(0.05, 0.18)
        for d in range(drop_dur):
            t = d / float(SAMPLE_RATE)
            env = math.exp(-t * 45.0)
            val = math.sin(2.0 * math.pi * drop_freq * t) * env * vol
            left[start_idx + d] += val * (1.0 - pan)
            right[start_idx + d] += val * pan

    write_wav("zen_sound_rain.wav", left, right)

# 2. Sacred Singing Bowl (432 Hz)
def generate_singing_bowl():
    print("Synthesizing Sacred Singing Bowl...")
    left = [0.0] * TOTAL_SAMPLES
    right = [0.0] * TOTAL_SAMPLES
    
    # 432 Hz fundamental with overtone modes
    modes = [
        (432.0, 0.35, 0.4),    # fundamental
        (433.0, 0.25, 0.4),    # beating / shimmer
        (1209.0, 0.15, 0.7),   # harmonic 2
        (2376.0, 0.08, 1.2),   # harmonic 3
        (3888.0, 0.03, 1.8),   # shimmering top
    ]
    
    # Warm continuous hum with recurring gentle strikes
    strikes = [0.5, 8.0]
    for s_time in strikes:
        s_idx = int(s_time * SAMPLE_RATE)
        for i in range(s_idx, min(TOTAL_SAMPLES, s_idx + int(SAMPLE_RATE * 9.0))):
            t = (i - s_idx) / float(SAMPLE_RATE)
            decay = math.exp(-t * 0.35)
            sample_val = 0.0
            for freq, amp, d_mult in modes:
                harmonic_decay = math.exp(-t * (0.28 * d_mult))
                sample_val += math.sin(2.0 * math.pi * freq * t) * amp * harmonic_decay
            left[i] += sample_val * 0.55
            right[i] += sample_val * 0.55 * (0.95 + 0.05 * math.sin(2.0 * math.pi * 0.2 * t))

    # Add soft background aura
    for i in range(TOTAL_SAMPLES):
        t = i / float(SAMPLE_RATE)
        bg = (math.sin(2.0 * math.pi * 432.0 * t) + 0.4 * math.sin(2.0 * math.pi * 1209.0 * t)) * 0.04
        left[i] += bg
        right[i] += bg

    write_wav("zen_sound_bowl.wav", left, right)

# 3. River Stream & Koto
def generate_river_koto():
    print("Synthesizing River Stream & Koto...")
    left = [0.0] * TOTAL_SAMPLES
    right = [0.0] * TOTAL_SAMPLES
    
    # Low murmur water stream
    lp_l, lp_r = 0.0, 0.0
    for i in range(TOTAL_SAMPLES):
        w_l = random.uniform(-1.0, 1.0)
        w_r = random.uniform(-1.0, 1.0)
        lp_l += (w_l - lp_l) * 0.08
        lp_r += (w_r - lp_r) * 0.08
        flow = 0.7 + 0.3 * math.sin(2.0 * math.pi * 0.15 * (i / SAMPLE_RATE))
        left[i] = lp_l * 0.18 * flow
        right[i] = lp_r * 0.18 * flow

    # Water gurgles
    for _ in range(int(DURATION_SEC * 12)):
        s = random.randint(0, TOTAL_SAMPLES - int(SAMPLE_RATE * 0.2))
        f = random.uniform(350.0, 900.0)
        for d in range(int(SAMPLE_RATE * 0.15)):
            t = d / float(SAMPLE_RATE)
            val = math.sin(2.0 * math.pi * f * t) * math.exp(-t * 22.0) * 0.05
            left[s + d] += val
            right[s + d] += val

    # Pentatonic Koto plucks (D Insen scale: D4=293.66, Eb4=311.13, G4=392.00, A4=440.00, C5=523.25)
    koto_notes = [(1.5, 293.66), (4.5, 392.00), (7.0, 440.00), (10.0, 523.25), (12.5, 311.13)]
    for n_time, freq in koto_notes:
        s_idx = int(n_time * SAMPLE_RATE)
        pan = random.uniform(0.3, 0.7)
        for d in range(int(SAMPLE_RATE * 3.0)):
            if s_idx + d >= TOTAL_SAMPLES:
                break
            t = d / float(SAMPLE_RATE)
            # Koto string harmonics
            pluck = (math.sin(2.0 * math.pi * freq * t) +
                     0.5 * math.sin(2.0 * math.pi * freq * 2.0 * t) +
                     0.25 * math.sin(2.0 * math.pi * freq * 3.0 * t)) * math.exp(-t * 2.2) * 0.28
            left[s_idx + d] += pluck * (1.0 - pan)
            right[s_idx + d] += pluck * pan

    write_wav("zen_sound_stream.wav", left, right)

# 4. Fireside Embers
def generate_fireside():
    print("Synthesizing Fireside Embers...")
    left = [0.0] * TOTAL_SAMPLES
    right = [0.0] * TOTAL_SAMPLES
    
    # Warm low frequency fire roar/rumble
    rumble_l, rumble_r = 0.0, 0.0
    for i in range(TOTAL_SAMPLES):
        w_l = random.uniform(-1.0, 1.0)
        w_r = random.uniform(-1.0, 1.0)
        rumble_l += (w_l - rumble_l) * 0.03
        rumble_r += (w_r - rumble_r) * 0.03
        left[i] = rumble_l * 0.22
        right[i] = rumble_r * 0.22

    # Crackles and snaps
    num_crackles = int(DURATION_SEC * 24)
    for _ in range(num_crackles):
        s = random.randint(0, TOTAL_SAMPLES - int(SAMPLE_RATE * 0.08))
        snap_vol = random.uniform(0.08, 0.42)
        pan = random.uniform(0.1, 0.9)
        f_snap = random.uniform(1200.0, 4800.0)
        dur = int(SAMPLE_RATE * random.uniform(0.005, 0.035))
        for d in range(dur):
            t = d / float(SAMPLE_RATE)
            val = math.sin(2.0 * math.pi * f_snap * t) * math.exp(-t * 180.0) * snap_vol
            left[s + d] += val * (1.0 - pan)
            right[s + d] += val * pan

    write_wav("zen_sound_fire.wav", left, right)

# 5. Night Mist & Temple Bell
def generate_temple_bell():
    print("Synthesizing Night Mist & Temple Bell...")
    left = [0.0] * TOTAL_SAMPLES
    right = [0.0] * TOTAL_SAMPLES
    
    # Atmospheric wind / mist
    wind_l, wind_r = 0.0, 0.0
    for i in range(TOTAL_SAMPLES):
        t = i / float(SAMPLE_RATE)
        w_l = random.uniform(-1.0, 1.0)
        w_r = random.uniform(-1.0, 1.0)
        wind_l += (w_l - wind_l) * 0.015
        wind_r += (w_r - wind_r) * 0.015
        swell = 0.6 + 0.4 * math.sin(2.0 * math.pi * 0.06 * t)
        left[i] = wind_l * 0.12 * swell
        right[i] = wind_r * 0.12 * swell

    # Deep bronze gong tone strikes (144 Hz fundamental, 288 Hz, 432 Hz, 576 Hz)
    bell_strikes = [1.0, 8.5]
    for b_time in bell_strikes:
        s_idx = int(b_time * SAMPLE_RATE)
        for d in range(int(SAMPLE_RATE * 7.0)):
            if s_idx + d >= TOTAL_SAMPLES:
                break
            t = d / float(SAMPLE_RATE)
            decay = math.exp(-t * 0.4)
            strike = (0.5 * math.sin(2.0 * math.pi * 144.0 * t) +
                      0.35 * math.sin(2.0 * math.pi * 288.0 * t) +
                      0.20 * math.sin(2.0 * math.pi * 432.0 * t) +
                      0.10 * math.sin(2.0 * math.pi * 864.0 * t)) * decay * 0.45
            left[s_idx + d] += strike
            right[s_idx + d] += strike

    write_wav("zen_sound_bell.wav", left, right)

if __name__ == "__main__":
    generate_rain()
    generate_singing_bowl()
    generate_river_koto()
    generate_fireside()
    generate_temple_bell()
    print("All 5 high-resolution ambient soundscapes successfully generated!")
