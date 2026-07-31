# SelfTrack

**Track Every Step. Own Your Data.**

A 100% offline-first, no-login, privacy-first fitness tracker for Android. No Firebase, no Supabase, no backend, no analytics — everything stays on the device.

This build implements the **core loop end-to-end**: Dashboard → Live GPS Recording → Activity Summary, backed by Room and a foreground location service. Other v1.0 features from the spec (Calendar, full Statistics, Personal Records, Heatmap, Routes, GPX Viewer, Import/Export, Backup, Search, Widgets) are scaffolded as future modules and not yet implemented — see `Future Work` below.

## Architecture

- **Language:** Kotlin
- **UI:** Jetpack Compose, Material 3 (dynamic color / Material You, AMOLED-friendly dark theme)
- **Pattern:** MVVM + a small repository layer over Room
- **DI:** Hilt
- **Persistence:** Room (SQLite)
- **Location:** FusedLocationProviderClient in a foreground `Service`, shared with the UI via a `RecordingSessionManager` singleton `StateFlow`
- **Maps:** Google Maps SDK via `maps-compose`
- **Navigation:** Navigation Compose

```
app/src/main/java/com/selftrack/app/
├── data/database/       # Room entities, DAO, database
├── data/repository/      # ActivityRepository (single source of truth)
├── di/                    # Hilt modules (database, location)
├── domain/model/          # ActivityType
├── location/              # LocationTracker, RecordingSessionManager
├── service/               # RecordingService (foreground GPS tracking)
├── ui/dashboard/          # Dashboard screen + ViewModel
├── ui/recording/          # Live recording screen + ViewModel
├── ui/summary/            # Post-activity summary screen + ViewModel
├── ui/components/         # Shared Compose components (StatCard, ActivityTypeChip)
├── ui/theme/              # Color, typography, Material You theme
├── ui/navigation/         # NavGraph, Destinations
└── util/                  # Formatters (distance, pace, duration)
```

## Google Maps API key

The key is **never committed**. It's read from `local.properties`, which is git-ignored:

1. Copy `local.properties.example` to `local.properties`.
2. Get a key at the [Google Cloud Console](https://console.cloud.google.com/google/maps-apis) and restrict it to the Maps SDK for Android with your app's package name (`com.selftrack.app`) and SHA-1.
3. Set `MAPS_API_KEY=your_real_key` in `local.properties`.

If no key is configured, the project still builds (using a placeholder), and the app shows a friendly **"Maps API key missing"** message on the recording and summary screens instead of crashing.

## Building locally

Open the project in Android Studio (Koala or newer) and run it, or from the command line:

```bash
./gradlew assembleDebug
```

(You'll need to generate the Gradle wrapper once with `gradle wrapper` if you're not using Android Studio, since the wrapper jar isn't committed to this repo.)

## Building an APK via GitHub Actions

A workflow at `.github/workflows/build.yml` builds a debug APK on every push to `main` and uploads it as a build artifact.

1. Push this project to a GitHub repository.
2. (Optional but recommended) Add your Maps key as a repo secret: **Settings → Secrets and variables → Actions → New repository secret**, name `MAPS_API_KEY`.
3. Push to `main`, or trigger the workflow manually from the **Actions** tab.
4. Download the APK from the finished run's **Artifacts** section.

Without the `MAPS_API_KEY` secret, CI still builds successfully — the app just falls back to the "Maps API key missing" screen at runtime.

## Future work (v1.0 spec, not yet built)

Calendar, full Statistics/Personal Records/Heatmap, Routes, GPX Viewer, Import (GPX/FIT/TCX/CSV/JSON/Strava ZIP), Export, encrypted Backup/Restore, Search, Settings, Home Screen widgets, auto-pause, battery-aware adaptive GPS interval tuning, and database migrations (currently uses destructive fallback — replace before shipping real user data).
