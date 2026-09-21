# Chopper - Personal AI Companion & Care Assistant 🦌🌸

[![Download APK](https://img.shields.io/badge/Download-Chopper%20APK%20(v1.0.0)-FF69B4?style=for-the-badge&logo=android&logoColor=white)](https://github.com/RONIKLIMBU/chopper-android-app/releases/download/v1.0.0/Chopper-v1.0.0.apk)
[![Release](https://img.shields.io/badge/GitHub-Release%20v1.0.0-blue?style=for-the-badge&logo=github)](https://github.com/RONIKLIMBU/chopper-android-app/releases/tag/v1.0.0)

**Chopper** is an Android application built with Kotlin and Jetpack Compose, designed to act as your caring personal doctor, schedule assistant, and daily companion.

---

## 📲 Direct App Download

You can download the pre-compiled Android APK directly to your phone:

- 🚀 **[Direct Download Chopper-v1.0.0.apk](https://github.com/RONIKLIMBU/chopper-android-app/releases/download/v1.0.0/Chopper-v1.0.0.apk)** *(22.5 MB)*
- 📦 **[View GitHub Release v1.0.0 Assets](https://github.com/RONIKLIMBU/chopper-android-app/releases/tag/v1.0.0)**

### How to Install on Android:
1. Tap the **[Direct Download](https://github.com/RONIKLIMBU/chopper-android-app/releases/download/v1.0.0/Chopper-v1.0.0.apk)** link above on your Android phone.
2. When prompted, select **Download anyway**.
3. Open your phone's notification or Downloads folder, tap `Chopper-v1.0.0.apk`, and select **Install** (allow *Install unknown apps* for your browser if prompted).
4. Launch Chopper and enjoy your personal AI care companion!

---

## ✨ Key Features

- **🌸 Chopper Voice Profile**:
  - Integrated with Android TextToSpeech engine and custom anime voice configuration (`voiceId = "e5e3a1d83d6f491db3c26b3929052b34"`).
  - High-pitched, cheerful cadence (1.35x pitch, 1.05x speech rate) addressing you as "Boss".
  - One-tap instant voice test chip and understanding confirmation dialogue.

- **⏰ Permanent Daily Check-Ins & Health Routines**:
  - **☀️ 7:00 AM Morning Briefing**: Wake-up greeting, schedule preview, and glass of water reminder.
  - **🍱 2:00 PM Lunch Reminder**: Stepping away from work to eat well.
  - **🌙 11:00 PM Goodnight Greetings**: Bedtime wrap-up and rest prescription.
  - **💧 Drinking Water Check-ins**: Scheduled hydration notifications (10:30 AM & 3:30 PM).
  - **🩺 Doctor Chopper "Take Care" Alerts**: Routine posture and stretch reminders (4:45 PM).

- **🔔 Two-Stage Smart Reminders**:
  - Stage 1: Alerts 24 hours prior to deadline or event.
  - Stage 2: Timely alerts on the day of the event.

- **📥 Triage Inbox & Assistant**:
  - Categorizes incoming messages into Urgents, Inquiries, Follow-ups, and Receipts.
  - High-priority notifications and quick action drafts.

- **🎙️ Hands-Free & Wake Word**:
  - Speech recognizer listening for "Chopper", "Hey Chopper", or "Doctor Chopper".

- **💾 Offline-First Persistence**:
  - Powered by Room Database (`ChopperDatabase`) for instant offline availability.

---

## 🚀 How to Push this App to GitHub

You can export and push this repository to GitHub through two simple methods:

### Method 1: Using AI Studio Direct Export (Recommended)
1. In the Google AI Studio top-right toolbar or project settings menu, click **Export** (or **GitHub**).
2. Choose **Push to GitHub** to link your repository and push the entire codebase directly.
3. Alternatively, click **Export as ZIP** to download the complete Android Studio project to your local computer.

### Method 2: Push via Git Command Line

If you have downloaded or cloned the project files locally:

```bash
# Initialize git (if not already initialized)
git init
git checkout -b main

# Add all files and commit
git add .
git commit -m "Initial commit: Chopper Personal AI Companion Android App"

# Create a new repository on https://github.com/new
# Link your remote repository (replace with your repo URL):
git remote add origin https://github.com/<your-username>/chopper-companion-app.git

# Push to GitHub
git push -u origin main
```

---

## 🛠️ Building & Installing the APK

### Build with Gradle
From the project root:

```bash
# Build the Debug APK
./gradlew assembleDebug
```

The compiled APK will be available at:
```
app/build/outputs/apk/debug/app-debug.apk
```

### Install onto your Device or Emulator:
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## ⚙️ Tech Stack & Architecture

- **Language**: Kotlin 2.0+
- **UI Toolkit**: Jetpack Compose with Material 3 Design
- **Architecture**: MVVM with unidirectional data flow (StateFlow)
- **Database**: Room Database with Coroutines Flow
- **Audio & Speech**: Android TextToSpeech (`ChopperVoiceManager`) & Android SpeechRecognizer
- **Alarms & Background Alerts**: AlarmManager with BroadcastReceiver (`ChopperAlarmReceiver`) & NotificationManagerCompat
