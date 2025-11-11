# Attribute Swap Timer - The PvP Attribute Swap Timer

Tired of guessing how good your breach swaps are? Want to master the 1-tick timing for your Mace-to-Wind-Charge combos?

**Attribute Swap Timer** is a simple, client-side HUD mod that gives you instant, on-screen feedback for your post-attack attribute swaps. Stop guessing and start perfecting your PvP timing!

Built for Fabric 1.21.10, this mod is lightweight and works on any server.

## Features

- **Instant Swap Feedback**: As soon as you attack and swap items, a timer appears right above your crosshair.
- **Precise Timing**: Shows you exactly how many ticks, milliseconds, and seconds your swap took.
- **Client-Side**: Works on any server that allows Fabric clients.
- **Clear Ratings**: Get an instant rating for your swap:
  1. **PERFECT! (1 tick)**
  2. **Good (+1 tick)**
  3. **Late (+2 ticks)**
  4. **Too Late (3+ ticks)**


## How It Works

The mod's robust detection logic identifies a true attack (based on attack cooldown and hand swing) and then starts a timer. The moment you swap to a new item in your main hand, the timer stops and displays the results.

The results stay on-screen for 0.5 seconds—just long enough for you to read them before your next engagement.

**What You'll See:**

When you land a perfect 1-tick swap, your HUD will display:


```
Ticks: 1
Time: 1ms (0.001s)
PERFECT!
```

(The "PERFECT!" text will be bright green, with other ratings in yellow, orange, or red.)

## Requirements
- **Minecraft**: 1.21.10
- **Mod Loader**: Fabric (0.18.0+)
- **API**: Fabric API
