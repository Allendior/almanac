# Threat model

Who this protects, from what, and — more usefully — what it does not protect against.

The asset is not secret in the usual sense. It is a decade of photographs of one
person's face, plus a few private lines of text. The realistic harms are **loss** and
**casual exposure**, not a targeted attacker. The design is weighted accordingly.

## What is in scope

### 1. Someone picks up your unlocked phone

**Real, and the most likely exposure by far.**

Mitigated: an optional biometric/device-credential lock, asked at cold start.
`allowBackup="false"` and explicit backup-exclusion rules keep the archive out of
Google account backups and device-to-device transfer.

Not mitigated: the lock is a cold-start gate, not a per-resume one. Someone holding
your unlocked phone with the app already open sees the archive. The photographs are not
encrypted with a separate app key, so anything that can read app-private storage can
read them.

If you want the stronger version — per-file encryption under a Keystore key, or a lock
that re-arms on every resume — say so. It is a real cost: a Keystore key is destroyed by
a factory reset, so encrypted originals become unrecoverable without an exported
archive. I would want the export path to be well-worn first.

### 2. Device loss, theft, or death

**The likeliest way you actually lose ten years.** Not a breach — a dropped phone.

Mitigated: export produces a complete, portable, self-describing ZIP; import restores
it without duplicating anything.

Not mitigated: nothing exports automatically. If you never export, a lost phone is a
lost archive. This is the single largest residual risk in Phase 1 and it is a habit
problem, not a code problem. Phase 3 exists to reduce it.

### 3. A malicious or simply wrong home-server endpoint

Out of scope today, because the app cannot reach a network: no `INTERNET` permission,
verified on the packaged APK. A wrong hostname cannot receive anything because nothing
can be sent.

When Phase 3 lands: TLS over a private transport, server identity pinned and shown in
the UI with its fingerprint, pairing via a one-time code rather than a typed password,
and a per-device credential in the Keystore. The app must show which server it is
talking to and let you revoke it.

### 4. A leaked backup token

Out of scope today — there is no token, and no credential of any kind is embedded in the
APK or the repository.

When Phase 3 lands: the credential is per-device, scoped, revocable server-side, and
stored in the Android Keystore rather than in preferences or source. Revocation must be
something you can do from the server alone, and the app must handle being revoked
clearly rather than silently retrying.

### 5. Accidental deletion

Mitigated: deletion is always confirmed, and the confirmation states plainly that the
file will be deleted from this phone and that no copy exists unless you exported one.
Replacing today's portrait is likewise deliberate.

Not mitigated: there is no trash and no undo. A confirmed delete is final. If you would
rather have a 30-day recycle bin, that is a small change and worth asking for.

### 6. Corrupted or missing media

Mitigated: originals are written to a `.part` file and renamed, so a crash mid-write
cannot produce a truncated file at the final name. Every entry stores a SHA-256, and
import verifies it. A row whose file has vanished is *reported* — on the Archive screen
and in the entry itself — rather than shown as a blank frame. Missing thumbnails are
rebuilt automatically.

Not mitigated: there is no periodic full-archive integrity scan. Silent bit-rot in
app-private storage would be found at export or import time, not before.

### 7. An out-of-date app

Mitigated: Room is built **without** `fallbackToDestructiveMigration`. A schema surprise
fails loudly rather than silently discarding ten years of metadata. The archive format
carries a version number, and an archive written by a newer version is refused with an
explanation rather than half-read.

Not mitigated: an OS update that breaks CameraX would stop new captures. The existing
archive stays readable and exportable, because it is ordinary JPEGs and a SQLite file.

### 8. A hostile import file

**The one place data the app did not create becomes a record.**

Mitigated: every field is validated rather than trusted. File names must match the exact
shape this app writes — no `..`, no separators, no dotfiles — because the name is used
to open a file inside app-private storage. Hashes must match. Impossible UTC offsets,
non-finite numbers, malformed dates, and overlong notes are rejected with a stated
reason. Existing days are never overwritten. Twelve unit tests cover this specifically,
including path traversal.

Not mitigated: a malformed JPEG that is well-formed enough to pass the hash check is
written to disk and handed to the platform decoder. The decoder is the OS's, not the
app's.

## Explicitly out of scope

- A forensic adversary with physical possession and an unlocked bootloader.
- Malware already running with root on the device.
- The camera hardware or OS lying about what it captured.
- Anyone who can read the screen while you are using the app.

## Decisions that need your approval before implementation

1. **Per-file encryption at rest** (threat 1) — stronger, with a real recovery cost.
2. **A recycle bin instead of a hard delete** (threat 5).
3. **Re-arming the lock on resume**, not only at cold start (threat 1).
4. **The whole of Phase 3** (threats 3 and 4), design first, code after.
5. **Whether the warm archival grade should return as an optional display toggle** —
   currently omitted so the record stays truthful. See `README.md`.
