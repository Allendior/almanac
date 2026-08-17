# Architecture

Single Gradle module, Kotlin, Jetpack Compose, Room, coroutines and Flow. Manual
dependency wiring in `AlmanacApp.Container` — the graph is six objects deep and lives
entirely on the device, so a DI framework would add build cost and indirection for
nothing.

```
ui/screens/*        Compose screens. No Room, no File, no Context beyond what Compose gives.
ui/AlmanacViewModel One StateFlow<AlmanacUiState>. All navigation and intent.
data/PortraitRepository   The only boundary. Screens never touch storage directly.
data/PhotoStore     Files: originals, thumbnails, hashes, free-space guard.
data/db/*           Room: one row per local calendar day.
domain/*            Pure Kotlin, no Android types. Where the unit tests live.
```

`domain/` holds the logic worth testing without a device: the day-id rules, the gap-line
tiers, the "years and months apart" figure, and import validation. 37 unit tests cover
it.

## Storage

```
files/originals/<yyyy-MM-dd>_<sha256[0..12]>.jpg
files/thumbnails/<entryId>.jpg
databases/almanac.db
datastore/almanac_settings.preferences_pb
```

**Originals are still named from the day and a prefix of the content hash, never from
note text.** A note is private prose; it must not become a name that shows up in a file
listing, a backup index, or over someone's shoulder. **Thumbnails are named from the
entry's id instead of the day** — since a day migration, day alone stopped being a safe
filename basis for the thumbnail once more than one entry could share a day.

**Originals versus thumbnails.** The original is the record: written once, never
re-encoded. The thumbnail is a derived convenience; if it is lost or corrupt the archive
is intact, and it is rebuilt from the original at next launch. The code treats the two
asymmetrically on purpose, and the UI never falls back to a thumbnail as if it were the
record.

**Hashes.** SHA-256 of the file contents, computed at save time and stored on the row.
Its purpose is *integrity and duplicate detection* — it lets an import say "this file is
not what the index claims it is", and lets a re-import of the same archive be a no-op.
It is **not** a security control: it does not authenticate anything and it does not
protect against someone who can write to app-private storage, because such a person can
rewrite the hash too.

## Identity: entry id versus day id

Each entry has two identifiers now, and they answer different questions:

- **`id`** (a UUID v4, generated once at save time) is the Room primary key and the
  entry's real identity — the thing files are named from, the thing import dedupes by,
  the thing every screen keys its state on.
- **`day_id`** (the ISO local date at the instant of capture) is an indexed, non-unique
  column. It answers "what day was this," not "which row is this."

That split is the whole schema change behind letting a day hold more than one
portrait: `day_id` used to be the primary key (`Migration 1→2` moved it off), and one
row per day was a property of the schema itself rather than something the UI had to
remember to enforce. It no longer is — `PortraitDao.insert()` always inserts, never
upserts, and nothing on the save path looks for an existing row to replace.

Two things about `day_id` did not change:

- Travelling across timezones can never move, merge, or rewrite an existing entry,
  because nothing recomputes the id it was captured under. The UTC offset at capture is
  stored alongside, so a portrait taken at 07:54 in Kolkata still reads 07:54 when you
  open it years later from anywhere else.
- Every screen that groups by day (Calendar, Timeline, Compare) still groups by
  `day_id` — it just now expects `groupBy`, not `associateBy`, since the result can be
  a list of more than one.

`today` is re-read in `onResume`, so midnight passing while the app sits open is picked
up rather than cached.

## Photo lifecycle

```
shutter
  → CameraX ImageCapture.takePicture(OnImageCapturedCallback)
  → JPEG bytes copied out of the ImageProxy plane, nothing decoded
  → held in memory as CaptureDraft. Nothing has touched disk yet.
  → Review screen: mood / number / note are optional and edit the draft only
  → Save:
      free-space check (refuse under 40 MB rather than half-write)
      generate a new entry id (UUID v4)
      write originals/<name>.part, fsync via close, rename to final   [atomic commit]
      derive thumbnail, named from the new entry id
      insert the Room row — always an insert, never an upsert; nothing for
      the same day is looked up, touched, or replaced
  → Discard: the bytes are dropped. Nothing was ever written.
```

The write order matters: a crash mid-save can leave an orphan file, which is harmless
and reclaimable, but can never leave a database row pointing at a file that does not
exist.

Deleting an entry removes the row and the file together, and is always confirmed by a
dialog that states plainly that no other copy exists unless you exported one.

## Export and import

**Export** (`ACTION_CREATE_DOCUMENT`) writes a ZIP the app never sees again:

```
index.json          format, version, export time, entry count, one object per entry
originals/*.jpg     the untouched files
```

The format is deliberately boring. Anyone with a laptop and no copy of this app —
including you in 2036 — can unzip it, read the JSON in a text editor, and look at the
photographs. Reversibility is the point.

**Import** (`ACTION_OPEN_DOCUMENT`) is additive and idempotent:

- every row is validated before it is trusted (see `domain/EntryValidation.kt`);
- file names are checked to be a plain name in the shape this app writes — no
  traversal, no separators, no dotfiles — because a name from an archive is used to open
  a file inside app-private storage;
- each photograph's SHA-256 must match what the index claims, or the row is counted
  unreadable and skipped;
- the entry id is the stable identity now, not the day, so **an existing entry (by id)
  is never overwritten** — and since a day can legitimately hold more than one entry,
  import no longer treats "this day already has a row" as a reason to skip anything.
  The archive on this phone always wins for any id it already holds, and importing the
  same file twice adds nothing the second time;
- the result is a visible report: added / already in archive / unreadable / rejected,
  with nothing silently dropped.

The app holds no storage permission. It is handed exactly one URI per operation, uses
it, and forgets it.

## Theming: light and dark from one token set

`Ink` (`ui/theme/Tokens.kt`) is a singleton object, not a `CompositionLocal` — that
was the pre-existing shape, and every screen already read `Ink.text`, `Ink.bg`, and so
on directly. Retrofitting dark mode without touching every one of those call sites
meant making the theme-reactive tokens (`bg`, `surface`, `text`, `accent`, `accent600`,
`accent700`, `scrim`) Compose-observable state instead of plain `val`s: still read the
same way, but now swappable underneath.

`AlmanacTheme` reads `isSystemInDarkTheme()` and calls `Ink.applyDarkMode(dark)`
directly in its composable body, before `content()` composes — not from a
`LaunchedEffect` or `SideEffect`, both of which run *after* the current composition
pass, which would let the first frame render with the wrong palette when the app
opens straight into dark mode. Setting the values synchronously, before the rest of
the tree reads them, avoids that flash entirely.

Three tokens are deliberately excluded: `darkBg`, `darkText`, and `guideGold` are the
capture screen's always-inverted ground, a design decision that predates dark mode and
is independent of the system setting — they stay plain constants. Everything else
(`divider`, `textMuted`, `textFaint`, `textGhost`) is a `get()` computed from `text` at
whatever alpha the handoff specified, so it never needs its own dark-mode branch and
can never drift out of sync with a theme switch.

## Reminder scheduling

`ReminderScheduler` (`notifications/`) wraps a `PeriodicWorkRequest` with a 1-day
interval and no constraints — no network, no charging, no foreground service. The
initial delay is computed to the next occurrence of the saved time-of-day; changing
the time or toggling the switch re-enqueues with `ExistingPeriodicWorkPolicy.UPDATE`,
while the one-time bootstrap on cold start uses `KEEP`, so opening the app never
disturbs an already-scheduled fire time.

`ReminderWorker` is a `CoroutineWorker` built by a custom `ReminderWorkerFactory`,
because it needs the same `PortraitRepository` and `SettingsStore` as everything else
rather than a bare no-arg constructor — `AlmanacApp` implements
`Configuration.Provider` to supply that factory, and the manifest removes
WorkManager's default `androidx.startup` initializer accordingly (see
`AndroidManifest.xml`'s `WorkManagerInitializer` `tools:node="remove"`). Each run
checks settings, checks whether today already has an entry, checks the notification
permission, and does nothing at all unless every one of those says to proceed — there
is no catch-up for a day the worker didn't get to run on.

## State and navigation

One `StateFlow<AlmanacUiState>`. Five bottom-nav destinations (Today, Calendar,
Timeline, Compare, Archive) and several full-surface overlays (Capture, Review, Entry,
DayEntries, Lock, Welcome, Introduction, Tips) that hide the navigation because each is
a single task with a single way out.

Changing destination clears open dialogs, note editors and date pickers. System back is
handled per overlay and returns to the list you came from.

Process death: the draft lives in memory only and is intentionally not persisted — a
portrait that was never saved should not reappear as a ghost days later. Everything
saved is in Room and reloads through the Flow.

## The backup boundary (Phase 3, not built)

Nothing in this codebase can reach a network, and adding backup would mean adding the
`INTERNET` permission — a visible change on install, which is the correct amount of
friction for that decision.

The design constraints, recorded now so the boundary is explicit before any of it
exists:

- the phone remains authoritative; the server is a second copy, never a dependency;
- foreground-only, user-triggered "Back up now" and "Restore". No background sync;
- TLS over a private transport (local Wi-Fi with a trusted certificate, or a
  Tailscale/WireGuard-style private network). Never a public unauthenticated endpoint;
- pairing via a one-time code shown by the server, exchanged for a revocable
  per-device credential held in the Android Keystore. No basic auth, no shared
  household password, no long-lived secret in the APK or the repository;
- append-only immutable portrait records; metadata edits versioned with a stated
  last-edited policy;
- the app never deletes a local original because a copy exists on the server;
- revocation is server-side and the app handles it visibly.

Until that design is approved, the Archive screen's backup row opens a dialog that says
it is not built. It does not show a screen that pretends.
