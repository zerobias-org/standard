---
name: migrate-packages
description: Migrate the next batch of standard packages onto the gradle pipeline. Drops per-package build.gradle.kts marker, ensures .npmrc, runs full ./gradlew :<path>:gate (writes gate-stamp.json), fixes drift, major-bumps the version when applicable, commits per-package.
argument-hint: "[<type>/<vendor>/<suite>/<version>...] [--batch=N] [--dry-run]"
---

# Migrate Standard Packages

Per-repo companion to `/migrate-content-to-zbb` (which bootstrapped this repo onto gradle). Use this skill to migrate standard packages **one at a time** within `org/standard`.

**Depth 4 with a dropped category segment:**

| Path | Sample | npm name | `zerobias.package` |
|---|---|---|---|
| `package/<type>/<vendor>/<suite>/<version>/` | `law/us/nara/v19` | `@zerobias-org/standard-<vendor>-<suite>-<version>` | `<vendor>.<suite>.<version>.standard` |

**The leading `<type>` segment (e.g. `law`) is a category — it's part of the directory layout but DROPPED from the npm name and `zerobias.package`.** Source: `scripts/createNewStandard.sh` (FOLDER_PATH uses `<type>/<vendor>/<suite>/<version>`, CODE uses `<vendor>_<suite>_<version>`). The validator (`build.gradle.kts`) enforces this. Dots in a version segment are normalized to underscores for `zerobias.package` (same as framework).

## Trigger

```
/migrate-packages [<type>/<vendor>/<suite>/<version>...] [--batch=N] [--dry-run]
```

## Pre-flight

1. `git status` — must be on a feature branch, not `main`.
2. Confirm gradle bootstrap: root `build.gradle.kts`, `settings.gradle.kts`, `gradle.properties`, `gradle-ci.properties`, `.github/workflows/publish.yml` (uses `zbb-publish-reusable.yml@main`).
3. Identify candidates — directories WITHOUT `build.gradle.kts`: `find package -name index.yml | xargs dirname | sort -u`.

## Per-package loop

For each package: 1) drop `package/<path>/build.gradle.kts` = `plugins { id("zb.content") }`; 2) ensure `.npmrc`; 3) run **full** `./gradlew :<type>:<vendor>:<suite>:<version>:gate` (writes the mandatory `gate-stamp.json`); 4) major-bump (`1.x → 2.0.0`, `0.x → 1.0.0`, `2.x → no-op`); 5) commit per package.

Common drift the validator surfaces:
- **`package.json name` mismatch** — must be `@zerobias-org/standard-<vendor>-<suite>-<version>` (NOT including the `<type>` category).
- **`zerobias.package` mismatch** — must be `<vendor>.<suite>.<version>.standard`.
- **`zerobias.import-artifact` must be `standard`.**
- **Duplicate `id` UUID** — `:validateUniqueIds` collision across `index.yml` AND every `elements/*.yml`. Regenerate the duplicate via `uuidgen` (keep the original holder; renumber the copy).
- **`files` array** — should include `index.yml` + `elements/**` (standards have no logo). A stale `files` listing `logo.svg` with no `elements/**` means published packages drop their elements — fix it.

Commit format: `feat(standard-<vendor>-<suite>-<version>)!: migrate to gradle pipeline (<oldVer> → 2.0.0)`. Stage marker + `gate-stamp.json` + `package.json` + any drift fixes.

## What NOT to do

- Do NOT change `id` UUIDs except to resolve a genuine duplicate.
- Do NOT include the `<type>` category in the npm name / `zerobias.package` — it's dropped by design.
- Do NOT rename directories to match metadata. Metadata follows the directory.
- Do NOT skip the major bump for 1.x packages.

## See also

- Root `build.gradle.kts` — validator with the category-drop formula.
- `com/platform/dataloader/src/processors/standard/StandardIndexFileHandler.ts` — dataloader source of truth.
- `/migrate-content-to-zbb` — meta-repo skill that bootstrapped this repo.
