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
tiers, the "years and months apart" figure, and import validation. 29 unit tests cover
it.

## Storage

```
files/originals/<yyyy-MM-dd>_<sha256[0..12]>.jpg
files/thumbnails/<yyyy-MM-dd>.jpg
databases/almanac.db
datastore/almanac_settings.preferences_pb
```

**Filenames are built from the day and a prefix of the content hash, never from note
text.** A note is private prose; it must not become a name that shows up in a file
listing, a backup index, or over someone's shoulder.

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

## The day boundary

The entry's identity is the ISO local date at the instant of capture, decided once on
the device, and stored as the Room primary key. Two consequences, both deliberate:

- "One portrait per day" is a property of the schema, not a rule the UI remembers to
  enforce.
- Travelling across timezones can never move, merge, or rewrite an existing entry,
  because nothing recomputes the id. The UTC offset at capture is stored alongside, so a
  portrait taken at 07:54 in Kolkata still reads 07:54 when you open it years later from
  anywhere else.

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
      write originals/<name>.part, fsync via close, rename to final   [atomic commit]
      derive thumbnail
      upsert the Room row
      only then delete a superseded file for the same day
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
- the day id is the stable identity, so **an existing day is never overwritten**. The
  archive on this phone always wins, and importing the same file twice adds nothing the
  second time;
- the result is a visible report: added / already in archive / unreadable / rejected,
  with nothing silently dropped.

The app holds no storage permission. It is handed exactly one URI per operation, uses
it, and forgets it.

## State and navigation

One `StateFlow<AlmanacUiState>`. Five bottom-nav destinations (Today, Calendar,
Timeline, Compare, Archive) and four full-surface overlays (Capture, Review, Entry,
Lock) that hide the navigation because each is a single task with a single way out.

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
