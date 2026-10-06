# Placement Companion

Android placement preparation app submitted for the Mobile Application Development Laboratory, NIT Tiruchirappalli.

**Team:** Anuj Tanwar (205124017) and Kratish Mewada (205124045).

## Features

- Study planner with persistent daily task checkboxes.
- Preparation progress calculated from completed tasks and distinct solved PYQs.
- Offline company question search across 659 companies and 17,826 company-question entries.
- Separate To solve and Completed question lists, bookmarks and notes.
- Company preparation checklists and interview preparation resources.
- Groq-powered study planning, doubt assistance and interview feedback.

## Run from source

1. Clone this repository and open its root folder in Android Studio.
2. Install Android SDK 35 and let Gradle sync. The Gradle daemon configuration requests JDK 25; allow Gradle to provision it, or configure an installed JDK 25.
3. For AI features, add `GROQ_API_KEY=your_key_here` to your local `local.properties`, or set the `GROQ_API_KEY` environment variable before building. Do not commit credentials.
4. Run on an emulator or Android phone running Android 8.0 (API 26) or newer.

The PYQ dataset is bundled, so question search works offline. AI features require internet access and a valid locally configured API key. Android packages embed build-time configuration; keep builds containing your personal key private.

Build an APK with `gradlew.bat :app:assembleDebug` on Windows, or `./gradlew :app:assembleDebug` on Linux/macOS. The output is `app/build/outputs/apk/debug/app-debug.apk`.

## Project materials

- [Presentation and lab demo materials](presentation/README.md)
- [Preparation progress calculation](data/PREPARATION_PROGRESS.md)
- [Offline question dataset provenance](data/leetcode-companywise/README.md)

The question dataset comes from [leetcode-companywise-interview-questions](https://github.com/snehasishroy/leetcode-companywise-interview-questions). Source attribution and the pinned snapshot are included with the data.
