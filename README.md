# Flota — AI-Powered Autonomous Website Development Assistant

Flota is an Android mobile application built with **Kotlin**, **Jetpack Compose**, and **Material 3**. It allows users to describe website ideas through text or voice conversation, review detailed architectural blueprints, and generate full-stack websites with live preview and source code export.

---

## 📱 Features

- **Conversational Builder**: Natural language chat and voice input in English and Urdu.
- **Project Blueprint & Planning**: Structured plans covering executive summaries, information architecture, features, tech stack, and pre-flight quality checks.
- **Full-Stack Generation**: Generates modern responsive web apps with HTML/CSS/JavaScript and Node.js backends.
- **Live Preview & Testing**: In-app WebView preview with viewport switching (Mobile/Tablet/Desktop).
- **Source Code Export**: Download full ZIP bundles with clean source files and README docs.
- **Durable Local Storage**: Room database for offline project persistence.

---

## 🛠️ How to Build and Run the App (Make APK)

### Option 1: Direct APK Download in Google AI Studio
1. In the top-right toolbar of **Google AI Studio**, click the **Settings / Export** menu (or the three dots icon `⋮`).
2. Select **Generate APK / Download APK**.
3. Download the generated `app-debug.apk` directly to your Android device or computer.
4. On your Android phone, open the APK file and tap **Install** (allow installation from unknown sources if prompted).

### Option 2: Open in Android Studio
1. Export or download this project as a **ZIP** from AI Studio.
2. Extract the ZIP file to your computer.
3. Open **Android Studio** (Ladybug or newer).
4. Select **File > Open...** and choose the extracted folder.
5. Wait for Gradle sync to complete.
6. Connect your Android phone with USB Debugging enabled, or start an Android Emulator.
7. Click the green **Run ▶** button to install and run the app.
8. To build a standalone APK, click **Build > Build Bundle(s) / APK(s) > Build APK(s)**.

---

## 📦 Project Structure

```
├── app/
│   ├── src/main/java/com/example/
│   │   ├── MainActivity.kt
│   │   ├── data/           # Room Database, DAOs, Repositories
│   │   ├── generator/      # Website Generation Engine
│   │   ├── model/          # Data Models & Plans
│   │   └── ui/             # Jetpack Compose Screens (Home, Blueprint, Preview, Chat)
│   └── src/main/res/       # Vector assets, themes, and string resources
├── build.gradle.kts        # Root Gradle build script
└── settings.gradle.kts     # Gradle project configuration
```

---

## 📄 License

Created with Google AI Studio.
