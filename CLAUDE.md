# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Repository Overview

Monorepo of ZeroBias **standard** artifacts — formal documents with referenceable elements (requirements, articles, controls). Each `package/<type>/<vendor>/<suite>/<version>/` directory is one publishable standard package (e.g. `law/us/nara/v19` = The US Constitution).

The repo is on the **gradle + [zbb publish reusable workflow](https://github.com/zerobias-org/devops/blob/main/.github/workflows/zbb-publish-reusable.yml)** pipeline. Lerna and nx were removed in the migration. Sibling reference repos on the same pattern: `org/vendor`, `org/suite`, `org/product`, `org/framework`.

## Development Commands

```bash
# File-shape validation only:
./gradlew :<type>:<vendor>:<suite>:<version>:validateContent

# Full gate — validate → buildArtifacts → testIntegrationDataloader → writeGateStamp:
./gradlew :<type>:<vendor>:<suite>:<version>:gate

# Repo-wide cross-cut: fail if two yml files share an id UUID
./gradlew validateUniqueIds
```

`gate` writes `package/<path>/gate-stamp.json` (publish preflight requires it). `testIntegrationDataloader` runs against an ephemeral Neon branch; skipped locally without `NEON_*` env, re-run in CI.

## Package Structure & Naming

Standards are **depth 4** with a dropped category segment:

| Path | Sample | npm name | `zerobias.package` |
|---|---|---|---|
| `package/<type>/<vendor>/<suite>/<version>/` | `law/us/nara/v19` | `@zerobias-org/standard-<vendor>-<suite>-<version>` | `<vendor>.<suite>.<version>.standard` |

**The leading `<type>` (category, e.g. `law`) is part of the directory but DROPPED from the npm name and `zerobias.package`** (source: `scripts/createNewStandard.sh`). The `.standard` suffix on `zerobias.package` disambiguates artifact type. Dots in a version segment normalize to underscores for `zerobias.package`.

### Required files per package
- `index.yml` — standard metadata (id, name, code, externalId, version, elementTypes)
- `elements/<code>.yml` — one yaml per requirement/article (each with a unique `id`)
- `package.json` — `files` must include `index.yml` + `elements/**`
- `.npmrc`, `build.gradle.kts` (`plugins { id("zb.content") }`), `gate-stamp.json`

## Validator philosophy

The dataloader is the source of truth for schema rules. The gate validator (`build.gradle.kts`) only enforces what the dataloader can't see: (1) filesystem ↔ npm-name ↔ `zerobias.package` triangulation (with the category drop), and (2) repo-wide unique `id` UUIDs across `index.yml` + every `elements/*.yml`.

## Creating / migrating packages

- New standard: `sh scripts/createNewStandard.sh <type> <vendor> <suite> <version>`, then fill the remaining `{placeholders}` in `index.yml` + `package.json` and add `elements/` (the scaffold already writes `.npmrc` and the `zb.content` marker), `./gradlew :path:gate`.
- Every package is on the gradle pipeline. `/migrate-packages` (`.claude/skills/migrate-packages/SKILL.md`) is kept only as a reference for the migration.

## Community vs Proprietary

This is the **public** (`@zerobias-org`) standards repo — anyone can see it. Proprietary or licence-restricted standards live in `auditlogic/*` (password-gated) and **stay there**. Public-domain or openly licensed works (e.g. US Government publications, which are public domain under 17 U.S.C. §105) belong here, and may be relocated from auditlogic by keeping their identity: the same `zerobias.package` code and ids, a higher major version, and then removing the auditlogic source by directory removal only, never a deprecation publish (the deprecate path resolves by package code and would delete the live record). The four NIST standards under `technical/` and `guidance/` moved this way.

## Branches & commits

- `main` is canonical; `dev`/`qa`/`uat` kept in sync downstream by the publish workflow.
- [Conventional Commits](https://www.conventionalcommits.org/), validated by commitlint (no git hook is installed). Scope: `standard-<vendor>-<suite>-<version>`.

## CI/CD

`.github/workflows/publish.yml` wraps `zerobias-org/devops/.github/workflows/zbb-publish-reusable.yml@main` (detect → version → publish matrix → update-bundle → sync). Pre-release validation on a branch: `gh workflow run publish.yml --ref <branch>`.

## Related Documentation

- [Root CLAUDE.md](../../CLAUDE.md) — meta-repo guidance
- [org/framework/CLAUDE.md](../framework/CLAUDE.md) — sibling, frameworks reference standards
- [com/platform/dataloader/CLAUDE.md](../../com/platform/dataloader/CLAUDE.md) — StandardIndexFileHandler is the dataloader source of truth
