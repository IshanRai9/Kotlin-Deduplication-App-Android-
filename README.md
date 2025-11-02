---

# Kotlin Deduplication App

## Overview

The Kotlin Deduplication App is an Android application built using Kotlin that detects and removes duplicate files from the user’s device storage. It provides a clean, fast, and efficient way to free up space and improve device performance.

The app scans selected directories, identifies duplicate files based on content hash matching, and lets users review and delete unwanted duplicates safely.

---

## Features

* Smart Scan – Quickly scans internal and external storage for duplicate files.
* Content-Based Detection – Uses hash comparison for accurate duplicate detection.
* Filter Support – Filter results by file type (images, videos, documents, etc.).
* Safe Deletion – Preview duplicates before deleting.
* Storage Summary – View total storage saved after cleanup.
* Simple UI – Minimal and intuitive user interface built using Material Design components.

---

## Tech Stack

* Language: Kotlin
* Framework: Android SDK
* Build Tool: Gradle
* Architecture: MVVM (Model-View-ViewModel)
* Libraries Used:

  * Jetpack Components (LiveData, ViewModel)
  * Coroutines for background scanning
  * RecyclerView for listing duplicates
  * Room / SQLite (optional) for caching scan results
  * ViewBinding / DataBinding for UI

---

## Installation

1. Clone the repository

   ```bash
   git clone https://github.com/yourusername/Kotlin-Deduplication-App.git
   cd Kotlin-Deduplication-App
   ```

2. Open in Android Studio

   * Launch Android Studio
   * Click on "Open an existing project"
   * Select the cloned folder

3. Build and Run

   * Connect your Android device or start an emulator
   * Click "Run" to build and launch the app

---

## Project Structure

```
app/
├── manifests/
│   └── AndroidManifest.xml
├── java/
│   └── com.example.deduplicationapp/
│       ├── ui/           # Activities, Fragments
│       ├── viewmodel/    # ViewModels for data handling
│       ├── data/         # Data layer, repository, file scanning logic
│       └── utils/        # Helper and utility classes
└── res/
    ├── layout/           # XML UI layouts
    ├── drawable/         # Icons, images
    └── values/           # Colors, strings, themes
```

---

## How It Works

1. The app requests storage permission from the user.
2. It scans files within selected directories and computes their hash values.
3. Files with identical hashes are grouped as duplicates.
4. The user can review duplicate groups and select which files to delete.

---

## Troubleshooting

If you encounter the error:

```
permission denied cannot scan files
```

Make sure you have granted READ_EXTERNAL_STORAGE and WRITE_EXTERNAL_STORAGE permissions (or MANAGE_EXTERNAL_STORAGE for Android 11+).
You can also enable them manually via:

```
Settings → Apps → Deduplication App → Permissions → Storage → Allow all files
```

---

## Future Enhancements

* Cloud duplicate detection (Google Drive, OneDrive integration)
* Automated cleanup scheduling
* File preview (image thumbnails, audio playback)
* In-app analytics dashboard

---

## Author

Ishan Rai
[ishanrai1109@gmail.com](mailto:ishanrai1109@gmail.com)
GitHub / LinkedIn: [Your Profile Link]

---

## License

This project is licensed under the MIT License.

---