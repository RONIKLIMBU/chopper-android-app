# Chopper - Tony Tony Chopper Reminder App 🦌🌸💊

[![Download APK](https://img.shields.io/badge/Download-Chopper%20APK%20(v1.0.0)-FF69B4?style=for-the-badge&logo=android&logoColor=white)](https://github.com/RONIKLIMBU/chopper-android-app/releases/download/v1.0.0/Chopper-v1.0.0.apk)
[![Release](https://img.shields.io/badge/GitHub-Release%20v1.0.0-blue?style=for-the-badge&logo=github)](https://github.com/RONIKLIMBU/chopper-android-app/releases/tag/v1.0.0)

**Chopper** is an Android application built with Kotlin and Jetpack Compose, designed as a dedicated, fully functional **Tony Tony Chopper Reminder App** with authentic anime personality, custom GitHub voice streaming, dynamic emotional reactions, and two-stage schedule alerts.

---

## 📲 Direct App Download

You can download the pre-compiled Android APK directly to your phone:

- 🚀 **[Direct Download Chopper-v1.0.0.apk](https://github.com/RONIKLIMBU/chopper-android-app/releases/download/v1.0.0/Chopper-v1.0.0.apk)** *(22.5 MB)*
- 📦 **[View GitHub Release v1.0.0 Assets](https://github.com/RONIKLIMBU/chopper-android-app/releases/tag/v1.0.0)**

### How to Install on Android:
1. Tap the **[Direct Download](https://github.com/RONIKLIMBU/chopper-android-app/releases/download/v1.0.0/Chopper-v1.0.0.apk)** link above on your Android phone.
2. When prompted, select **Download anyway**.
3. Open your phone's notification or Downloads folder, tap `Chopper-v1.0.0.apk`, and select **Install** (allow *Install unknown apps* for your browser if prompted).
4. Launch Chopper and enjoy your personal AI doctor companion!

---

## ✨ Key Features

- **🎙️ GitHub Custom Voice Asset Integration**:
  - Connect your own GitHub repository containing Chopper's voice clips (e.g. `chuckle.mp3`, `angry.mp3`, `hiding.mp3`, `panic.mp3`, `candy.mp3`, `medicine.mp3`, `water.mp3`, `sleep.mp3`).
  - Seamlessly streams remote raw audio files using Android's asynchronous `MediaPlayer`.
  - Built-in `ChopperVoiceSettingsDialog` allows entering any repository URL with one-tap clip preview and testing.
  - Graceful fallback to high-pitched anime-tuned Android `TextToSpeech` when offline or before a custom repo is linked.

- **🦌 Authentic Chopper Reactions & Expressions**:
  - **🌸 Flustered Chuckling Dance**: Triggered when tasks are completed or when complimented ("*Kono yarō! Calling me a genius doctor won't make me happy, you jerk! Ehehehe~*").
  - **💢 Angry Doctor Scolding**: Triggered when reminders become overdue or when tapped ("*BAKA! Did you take your medicine?! Don't you dare collapse!*").
  - **🙈 Peek-a-boo Reverse Hiding**: Hides with his body exposed backwards from behind the screen!
  - **😱 Doctor Panic Mode**: Hilarious panic ("*DOCTOR! SOMEBODY CALL A DOCTOR!! ...Wait, I'M THE DOCTOR!!*").
  - **🍭 Cotton Candy Delight**: Pure bliss when sweet rewards or breaks are scheduled.

- **⚡ 1-Tap Doctor Chopper Presets**:
  - 💊 **Take Medicine** (+30 min)
  - 💧 **Hydration Water** (+45 min)
  - 🩺 **Doctor Rest Break** (+25 min)
  - 🍭 **Cotton Candy Snack** (+15 min)
  - 🌙 **Bedtime Rest** (+2 hours)

- **🔔 Fully Functional Two-Stage Reminder Engine**:
  - **Stage 1 (Day-Before Alert)**: 24-hour advance heads-up with gentle notification.
  - **Stage 2 (Day-Of Alert)**: Precise alarm when the event or medication time arrives.
  - **Snooze Support**: One-tap quick snooze (+15m, +1h).
  - **Reschedule & Custom Edit**: Instant time adjusters with Room database synchronization.
  - **Overdue Detection**: Highlights overdue tasks and prompts Chopper's scolding voice alert.
  - **Filter Tabs**: Toggle easily between Active, Completed, and All reminders.

- **🎙️ Voice Commands**:
  - Speak hands-free to add reminders: *"Remind me to drink water in 30 minutes"* or *"Schedule doctor checkup tomorrow at 2 PM"*.

- **💾 Room Database Persistence**:
  - Built on Room SQLite (`ReminderEntity` & `ReminderDao`) for persistent offline storage.

---

## ⚙️ Tech Stack & Architecture

- **Language**: Kotlin 2.0+
- **UI Toolkit**: Jetpack Compose with Material 3 Design
- **Architecture**: MVVM with unidirectional data flow (StateFlow)
- **Database**: Room Database with Coroutines Flow (`ChopperDatabase`)
- **Audio & Speech**: Android `MediaPlayer` (remote GitHub streaming) + `TextToSpeech` + `SpeechRecognizer`
- **Alarms & Background Alerts**: Android `AlarmManager` + `ChopperAlarmReceiver` + `NotificationManagerCompat`

