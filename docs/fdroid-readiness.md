# F-Droid readiness

An honest status check against F-Droid's published Inclusion Policy, verified against
this specific codebase rather than asserted from general knowledge. Last checked:
2026-08-15.

## Done

| Requirement | Status |
| --- | --- |
| FLOSS license | **GPL-3.0-or-later**, full text in [`../LICENSE`](../LICENSE). |
| No proprietary/tracking dependencies | Verified by grep across the whole tree: no Google Play Services, Firebase, Crashlytics, or analytics SDK anywhere. |
| Dependencies from trusted repositories | Every artefact resolves from `google()` or `mavenCentral()` only (`settings.gradle.kts`); no other repository is declared. |
| No embedded API keys or secrets | None exist — the app makes no network calls at all. |
| No auto-downloaded executable binaries | Not present; there is no update mechanism, no plugin loading. |
| Application ID does not collide with another domain | Renamed from `com.almanac.portrait` (which reverse-mapped to the real, in-use `almanac.com`) to `io.github.allendior.almanac`, following F-Droid's own recommended convention for developers publishing from GitHub without a personal domain. |
| Public version control | `git init` done, initial commit made locally. **Not yet pushed anywhere** — see "Not done" below. |
| Reproducible build via Gradle wrapper | `./gradlew` committed (wrapper jar, properties, both scripts), with `distributionSha256Sum` pinned against Gradle's own published checksum for tamper-evidence. Verified: `./gradlew :app:assembleDebug :app:assembleRelease` both succeed from a clean invocation. |
| Release build actually builds | Previously only ever verified with a manually-located Gradle binary. Now confirmed via the committed wrapper: R8 minification, resource shrinking, and packaging all succeed; output is `app-release-unsigned.apk`. |
| Packaged permissions match the privacy claims | Checked with `aapt dump permissions` on **both** debug and release APKs: `CAMERA`, `USE_BIOMETRIC`, and the app's own broadcast-receiver permission. No `INTERNET`, no location, no storage. |
| Fastlane listing metadata | `fastlane/metadata/android/en-US/{title,short_description,full_description}.txt` and `changelogs/1.txt` written, matching the app's actual feature set — not marketing copy. |
| Draft F-Droid build recipe | [`../fdroid/io.github.allendior.almanac.yml`](../fdroid/io.github.allendior.almanac.yml) — see the comment block at the top of that file for what to do with it. |

## Not done — needs you, specifically

These are steps that require either a decision only you can make, an account only you
control, or an interaction with a third party's infrastructure that cannot happen from
this environment:

1. **Push this repository to a public git host** (GitHub, GitLab, Codeberg — anywhere
   reachable over plain `git clone`). F-Droid's build server clones from a public URL;
   nothing here is publicly reachable yet. The draft metadata assumes
   `github.com/Allendior/almanac` based on the GitHub identity your local git config is
   already using, but **that repository does not exist yet and I have not verified the
   username is correct** — confirm or correct it before using the draft file.
2. **Pick an actual license year/name form if you want it different** — the LICENSE
   file and README currently say "Copyright © 2026 Allen." Change this if you'd rather
   use a different name, a pseudonym, or add co-authors.
3. **Fill in the real commit or tag** in `fdroid/io.github.allendior.almanac.yml` once
   pushed — a local, unpushed commit hash means nothing to F-Droid's build server.
4. **Choose a path**: submit to the official main F-Droid repo (a merge request to
   `fdroiddata`, reviewed by volunteers, can take days to weeks) or self-host your own
   repo with `fdroidserver` (immediate, but no main-repo discoverability, and you sign
   with your own key). The draft file's header comment explains both.
5. **App icon / feature graphic sizing for the store listing** — the launcher icon
   exists as an adaptive-icon vector; F-Droid's own client renders that fine, but if
   you want a polished listing you may want a flat 512×512 PNG for `fastlane/metadata/android/en-US/images/icon.png`
   and phone screenshots under `.../images/phoneScreenshots/`. Not required — F-Droid
   will generate a listing without them — but it looks better with them.
6. **Ongoing maintenance commitment** — the policy expects actively maintained apps.
   That's a statement about your intent, not something I can satisfy on your behalf.

## Explicitly not attempted

I did not create an account on GitHub/GitLab/Codeberg, push any code, or open a merge
request against `fdroiddata`. Those are all actions on third-party platforms under your
identity — publishing code publicly and interacting with other people's infrastructure
under your name is exactly the kind of action that needs your explicit, in-the-moment
go-ahead, not something to do quietly on your behalf.
