# Almanac

A private self-portrait time capsule for Android. One intentional portrait most days,
kept on your own phone, so that in ten years you can sit down and look at how you
changed.

There is no account, no cloud, no subscription, no feed, and no company between you and
your own face.

## License

Copyright © 2026 Allen. Licensed under the [GNU General Public License v3.0 or later](LICENSE)
(SPDX: `GPL-3.0-or-later`). The full license text is in [`LICENSE`](LICENSE) at the root
of this repository.

## What it is

- **At least one portrait a day, and as many more as you actually take.** Today can
  hold one portrait or several; nothing is replaced or discarded to make room for
  another. Every one is kept, individually.
- **Taken in the app.** There is no photo picker and no gallery permission. A portrait
  can be taken, never chosen.
- **Stored on the phone.** App-private storage is the source of truth, not a cache of
  something else.
- **Browsable.** Calendar (the primary view, with a small count badge on days that
  hold more than one portrait), timeline, a two-date comparison, a by-year
  progression, and a detail view for any single day or single portrait.
- **Exportable.** A plain ZIP of your original JPEGs plus a readable `index.json`,
  written to a folder you choose through the system file picker.
- **Light or dark**, following the system setting automatically. No separate toggle
  to find and no third palette to maintain — one set of tokens, two values each.
- **An optional torch** on the capture screen, shown only when the active camera
  actually reports a flash unit, and always off again the moment you switch cameras.
- **A page of optional, unenforced tips** for a portrait that compares well across
  years — same spot, same light, same rough time of day. Reachable from Archive,
  never shown unprompted, and nothing about it is checked or scored.

## What it is not, and will not become

No ads. No analytics. No tracking SDK of any kind. No crash reporting. No Google Play
Services, no Google Maps, no Firebase. No login. No social features. No streaks, no
counters, no gamification. No face detection, face recognition, or "AI analysis" of any
kind. No beauty filters, no smoothing, no auto-enhance. No subscription or paywall.

The app does not nag. A missed day is not marked, counted, or mentioned, and there is no
red square on the calendar, because the app keeps no record of what you meant to do —
only of what you did. There **is** one optional daily reminder now (see below), and the
line it does not cross is the important part: it fires at most once a day, only if today
has no portrait yet, says nothing about yesterday or any other day, keeps no streak, and
is on by default but a single switch away from off, permanently, in Archive.

## The daily reminder

Scheduled with `WorkManager`, using an ordinary `PeriodicWorkRequest` with no
constraints — no network, no charging, no foreground service. There is no
`SCHEDULE_EXACT_ALARM` permission anywhere in this app, so the fire time is
best-effort: WorkManager's own periodic-work window, which can drift under Doze. For a
reminder whose own copy says "whenever suits you", that is the right trade, not a
shortcut.

Each run checks quietly and does nothing at all — no notification, no side effect — if
reminders are off, if today already has a portrait, or if the permission was never
granted. There is no catch-up for a missed day; a reminder that fired for yesterday
would be exactly the nagging this app has always refused to do.

The `POST_NOTIFICATIONS` prompt is asked automatically exactly once, right after the
first-launch introduction finishes, never mid-onboarding and never repeated
automatically. Turning the switch back on later in Archive asks again, as a direct
response to that tap — never on its own.

## The data boundary

Everything the app owns lives in app-private storage under the app's own data
directory, which on a modern Android device is protected by file-based encryption tied
to your device lock:

```
files/originals/     the JPEG the camera produced, byte for byte, one file per portrait
files/thumbnails/    small derived JPEGs, disposable and regenerable
databases/almanac.db Room database: one row per portrait, day no longer the key
datastore/           preferences: guide, lock, number label, onboarding flags,
                      reminder on/off and time of day
```

Nothing is written outside that directory except the ZIP you explicitly export, to the
single location you pick in the system file picker.

Cloud backup and device-to-device transfer are switched **off** in the manifest
(`allowBackup="false"` plus explicit exclusion rules), so your portraits do not ride
along in a Google account backup without you asking.

## The photograph is not touched

The bytes CameraX produces are the bytes written to disk. The app does not decode,
re-encode, crop, rotate, mirror, or grade the original. The framing guide is drawn by
Compose on top of the preview surface and never reaches the file. No CameraX Extensions
are used anywhere, so no HDR, bokeh, or "face retouch" pipeline can be engaged.

The one derived artefact is the thumbnail, which is downsampled and does have EXIF
rotation applied so it displays correctly in a grid. A thumbnail is never the record; if
one is missing it is silently rebuilt from the original on next launch.

**A deliberate deviation from the design prototype:** the Classical `.plate` class
applies a warm archival grade (`sepia(.22) saturate(.82) contrast(1.05)`) to
photographs. That grade is not reproduced here. This archive's entire promise is an
unaltered record of a face across years, and a warm filter on display would quietly
change what you see when you compare 2026 with 2036. The plate's mat and hairline
outline carry the archival feeling; the photograph is left alone. Say the word and it
becomes a toggle.

## Permissions

Verified against the packaged APK with `aapt dump permissions`, not against the source
manifest:

| Permission | Why |
| --- | --- |
| `android.permission.CAMERA` | To take the portrait. The one thing the app does. |
| `android.permission.USE_BIOMETRIC` | Merged in by `androidx.biometric`, used only for the optional app lock. It reads nothing; it asks the OS to confirm it is you. |
| `android.permission.POST_NOTIFICATIONS` | For the optional daily reminder, on by default. Asked once; refuse it and the reminder is silently never shown again until switched on in Archive, which asks again. |
| `…DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION` | A signature-level permission the app defines for its own broadcast receivers, added by `androidx.core`. Granted to nothing else. |

There is no `INTERNET` permission. The app is not capable of network access — not
restricted from it by policy, incapable of it by manifest. There is no location
permission, no external storage permission, and no package-visibility query.

`USE_FINGERPRINT` is explicitly removed in the manifest: `androidx.biometric` merges it
for API 23–28, and this app's minimum is 29, so it would be a permission declared and
never used. `WorkManager` (used to schedule the reminder) likewise merges in
`WAKE_LOCK`, `RECEIVE_BOOT_COMPLETED`, `FOREGROUND_SERVICE`, and
`ACCESS_NETWORK_STATE` — all four are explicitly removed, because this app's minimum
API (29) always uses `JobScheduler`, which persists scheduled work across reboots on
its own and needs none of them. The reminder sets no constraints, so the network-state
tracker is never touched either.

## Dependencies

Every third-party artefact in the shipped APK, and why it is there:

| Dependency | Purpose |
| --- | --- |
| `androidx.core:core-ktx` | Android platform extensions. |
| `androidx.activity:activity-compose` | Compose host activity, permission and file-picker contracts. |
| `androidx.fragment` (via biometric) | `FragmentActivity`, required by `BiometricPrompt`. |
| `androidx.lifecycle:*` | ViewModel, lifecycle-aware state collection. |
| `androidx.compose:*` (BOM 2024.09.03) | UI toolkit: foundation, ui, material3. |
| `androidx.room:*` | The metadata database, with a generated schema under `app/schemas`. |
| `androidx.camera:*` (CameraX 1.3.4) | Camera preview and still capture. |
| `androidx.datastore:datastore-preferences` | Preferences: guide, lock, number label, onboarding flags, reminder settings. |
| `androidx.biometric:biometric` | The optional app lock. |
| `androidx.work:work-runtime-ktx` | Schedules the optional daily reminder. No network, charging, or foreground-service constraints are ever set. |
| `io.coil-kt:coil-compose` | Image loading and request cancellation while scrolling the archive. |
| Cormorant Garamond, Lora (OFL) | Bundled in `res/font`; nothing is fetched at runtime. Licences in `licenses/`. |
| Lucide icon paths (ISC) | Transcribed as Compose vectors in `ui/components/Icons.kt`. |

No dependency in this list performs network I/O on the app's behalf, and the app could
not reach the network if one tried.

All AndroidX, Jetpack Compose, CameraX, Room, WorkManager, and Coil artefacts above are
Apache License 2.0. Every dependency is pulled only from `google()` or `mavenCentral()` (see
`settings.gradle.kts`) — no other Maven repository, and no proprietary or Google Play
Services / Firebase / analytics / crash-reporting artefact is referenced anywhere in
this project.

## Build and verify

```bash
./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
aapt dump permissions app/build/outputs/apk/debug/app-debug.apk
```

## What was added after your review

Four things you asked for, built to match what you'd already specified elsewhere in
this brief rather than as new, separate rules:

- **A one-time welcome screen**, shown on the true first launch only, never again.
  Persisted in DataStore (`hasSeenWelcome`). Says what the app is and who made it, once,
  then gets out of the way — no banner on every open.
- **A neutral line on the capture screen** ("Whenever you're ready.") instead of a
  compliment about appearance, consistent with "not a beauty app... a truthful record."
- **A restrained animated indicator on the bottom nav** — a small accent underline that
  grows in under the active tab, plus a colour crossfade, both on the existing 180ms
  budget. No bounce, no parallax, matches the system's own animation rule.
- **A redesigned launcher icon** — a page with a folded corner (the almanac) holding a
  portrait mark and a caption rule, in accent-gold stroke, echoing the in-app
  placeholder glyph. Stroke only, no fill, matching the design system.

**Declined, and why:** exporting photos to a third-party AI service was not built.
Sending years of face photos to an external API is a fundamentally different privacy
posture than the rest of this app, is not reversible once sent, and directly
contradicts "do not silently upload photos... no AI face analysis" from the original
brief. Local export to a folder (already built) remains the only way data leaves the
phone, and it requires you to explicitly choose the destination every time.

**A real bug found and fixed while wiring the welcome screen:** the biometric lock
never actually triggered on cold start. `requireLockIfEnabled()` was called
synchronously in `onCreate`, before the DataStore-backed settings had loaded — so it
always read the default (`biometricLock = false`), regardless of what was actually
saved. Cold-start routing (Lock → Welcome-if-unseen → Today) is now decided reactively,
once, after the first real settings emission.

## The feature round before publishing

Seven things asked for before this app goes to F-Droid, weighed against the same rules
the rest of it already follows rather than treated as a free pass around them:

- **More than one portrait a day, for real.** Not a "retake" that discards the old
  file — a schema change (`PortraitEntryEntity`'s primary key moved from the day to a
  generated UUID, with the day kept as an indexed, non-unique column) so every
  portrait taken is kept. A day with more than one shows a count badge on Calendar and
  a plain "pick which one" list; a day with exactly one behaves exactly as before.
- **Dark mode**, following the system setting, not a separate in-app toggle. `Ink`'s
  theme-reactive tokens (`bg`, `surface`, `text`, `accent`, `accent600`, `accent700`,
  `scrim`) became Compose-observable state instead of `val`s, swapped once per
  composition from `AlmanacTheme` via `isSystemInDarkTheme()`, so the ~20 files that
  already read `Ink.text` etc. needed no changes at all. The capture screen's
  always-dark ground (`darkBg`, `darkText`, `guideGold`) stays constant either way —
  that was never about system theme, and still is not.
- **An optional torch**, shown only when `Camera.cameraInfo.hasFlashUnit()` says the
  active camera actually has one — true for most rear cameras, essentially never for
  front. Reset to off on every camera switch, since a fresh CameraX binding always
  starts torch-off regardless of what the UI last showed.
- **A one-time feature walkthrough** ("Five places, always the same"), shown once
  after Welcome, describing where things live rather than what the app is for — the
  Welcome screen already covers that.
- **A page of tips for a more comparable portrait**, opened from Archive, closed with
  Back, never surfaced unprompted, and worded to describe *why* each tip helps rather
  than issue instructions.
- **A daily reminder**, on by default, detailed above under "The daily reminder".
- **A GitHub Sponsors link** in Archive, and a `.github/FUNDING.yml`. Opens the
  system browser via `ACTION_VIEW`; the app still cannot reach the network itself, and
  nothing about the request is silent — the row states plainly what it does before you
  tap it.

## Known limitations

Found during the emulator QA pass and left open on purpose, rather than quietly fixed
or quietly ignored:

- ~~A full-resolution portrait takes a moment to decode on Today~~ — fixed. The plate
  now shows the derived thumbnail immediately and crossfades to the full original once
  it decodes (`PortraitImage`'s `thumbnailFile` param, used on Today, Entry, and
  Compare's two-dates view).
- **Verified at realistic scale.** Imported a 2,058-entry / 6-year archive (matching
  the design prototype's own ~2,000-day synthetic dataset). Cold start to first frame:
  ~590ms. Timeline under a fast fling: 98.4% of frames within budget. 24 rapid Calendar
  month-navigations: ~93ms average, correct entry counts throughout. Compare's by-year
  view groups and sorts all 2,058 entries synchronously on the main thread — cheap
  enough at this scale (a few ms) that it wasn't worth moving off it, but flagged here
  in case the archive one day reaches an order of magnitude larger.
- **The lock is a cold-start gate only.** It does not re-arm when the app is resumed.
- **No recycle bin.** A confirmed delete is final.
- **`File.usableSpace` is the free-space guard** (2 lint warnings). Android suggests
  `StorageManager.getAllocatableBytes()`, which accounts for reclaimable cache. The
  current check is conservative, so it errs toward refusing a save rather than
  half-writing one.
- ~~Framework `android.media.ExifInterface`~~ — swapped to `androidx.exifinterface`
  (1.4.2), which is more robust across odd or imported files. Fixed.
- **Most `GradleDependency` lint warnings are left unaddressed on purpose.** CameraX
  (1.3→1.6), Room (2.6→2.8), the Compose BOM (two years forward), and Lifecycle
  (2.8→2.11) all have newer versions available, but each is a minor-or-larger jump with
  real migration surface, and there is no device matrix here to re-verify against. For
  an app meant to still open correctly in 2036, a working pinned version beats a fresher
  one that hasn't been proven. Revisit if a specific CVE or bug fix in a newer release
  becomes relevant.
- There is still no instrumented (on-device) test suite. The domain logic is covered
  by 37 JVM unit tests; the storage, camera, notification, and import/export paths
  were verified by scripted emulator walkthroughs, most recently after the multi-entry
  migration, dark mode, torch, and reminder work.
- **The performance figures above predate multi-entry.** They were measured against a
  2,058-*day* archive when one day meant one row. The schema change that lets a day
  hold several portraits did not change any indexed access pattern (`day_id` is still
  indexed, just no longer unique), so the same numbers should hold for a
  2,058-*portrait* archive, but this has not been re-measured against a synthetic
  dataset with real multi-entry days at that scale.

## Status

**Phase 1 (offline local-first MVP), and the feature round above, are built.** Phase 3
(self-hosted backup) is not, and remains a separate approval gate. The Archive screen's
backup row says so plainly rather than showing a screen that pretends.

See `docs/architecture.md` and `docs/threat-model.md`. For F-Droid submission status,
see [`docs/fdroid-readiness.md`](docs/fdroid-readiness.md).
