# RomM Companion (Android)

A companion Android client for [RomM](https://github.com/rommapp/romm) self-hosted
rom servers, built to work alongside [Cocoon](https://cocoon-shell.com/) rather than
replace it: this app browses your RomM library, lets you pick a download folder
*per system*, installs roms (with zip/7z auto-extraction and firmware/BIOS
download), and syncs cloud saves + save states. Cocoon stays the launcher —
point its per-platform library folders at the same folders you configure here
and it picks everything up.

## Status

This is a scaffold: project structure, data layer, RomM API client, and MVP
screens (pairing, library browse, per-platform folder settings, install
queue, paired-device management) are wired end to end. It has **not been
compiled** — see "Known limitation" below — so treat it as a strong starting
point to open in Android Studio, not a finished, verified build.

## How it talks to RomM

- **Pairing**: uses RomM's device-code pairing flow
  (`POST /api/auth/device/init`, `POST /api/auth/device/token` —
  `backend/endpoints/device_auth.py` in the RomM repo). The app shows a short
  user code; you approve it from the RomM web UI. No password ever touches
  this app.
- **Library**: `GET /api/platforms`, `GET /api/roms`, cached locally in Room
  so the library stays browsable offline.
- **Install**: `GET /api/roms/{id}/content/{fileName}` streamed into a
  per-platform folder you pick via Android's Storage Access Framework
  (`ACTION_OPEN_DOCUMENT_TREE`) — this is the same folder you'd point Cocoon's
  "Library & Data" setting at for that platform.
- **Cloud saves/states**: RomM's `/api/saves` and `/api/states` endpoints,
  which already track per-device sync state
  (`backend/endpoints/saves.py` / `states.py`) — the data layer and API
  client are in place; the sync UI/flow itself is the next milestone (see
  Roadmap below).
- **Firmware**: `GET /api/firmware` for BIOS files, downloaded into a
  separate per-platform folder.

DTOs in `data/remote/dto/` were modeled directly against the RomM backend's
response schemas (`backend/endpoints/responses/*.py`) rather than guessed, but
only include the fields this app currently uses — extend them as new screens
need more.

## Project layout

```
app/src/main/java/com/onideity/rommcompanion/
  data/
    remote/          RommApi (Retrofit), DTOs, auth interceptor, session
    local/db/         Room cache (platforms, roms, download state)
    local/prefs/       DataStore settings + encrypted token store
    repository/        AuthRepository, LibraryRepository, DeviceRepository
  download/            WorkManager install pipeline (download + extract)
  di/                  Hand-rolled AppContainer (no DI framework)
  ui/                  Compose screens + ViewModels, one package per screen
```

## Known limitation: unverified in this environment

This scaffold was written in a sandboxed session whose network egress policy
blocks Google's Maven repository (`dl.google.com`), which is required to
resolve the Android Gradle Plugin, Jetpack Compose, and most AndroidX
artifacts. That means **no build was run here** — Gradle syntax, dependency
versions, and API usage (Room, WorkManager, Compose Navigation, Moshi codegen,
commons-compress's `SevenZFile` builder API in particular) were written
carefully but not compiler-verified.

**First thing to do**: open this project in Android Studio (or run
`./gradlew assembleDebug` somewhere with normal internet access) and fix
whatever the compiler flags. The architecture and RomM integration logic
should hold up; expect minor build-config or API-signature fixes.

## Requirements

- Android Studio (Ladybug/Koala or newer) with JDK 17
- A running RomM server (4.x) reachable from the device
- minSdk 26, compileSdk/targetSdk 35

## Known follow-ups

- Cloud save/state **sync UI** isn't built yet — the API client and Room
  schema support it, but there's no screen driving upload/download/conflict
  resolution yet (Phase 3 from the original plan).
- Downloads screen shows rom IDs, not titles — needs a join against the rom
  cache.
- Collections/favorites, RetroAchievements display, update detection,
  storage-usage view, and settings export/import (Phases 2 & 4 of the
  original plan) aren't implemented yet.
- Emulator save/state file discovery (reading a save *out of* RetroArch's
  folder, for example) isn't implemented — needed before save sync is
  actually useful end to end.
- No automated tests yet.
