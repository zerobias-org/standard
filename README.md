# Standard monorepo

ZeroBias standard artifacts — formal documents with referenceable elements (requirements, articles, controls). Each `package/<type>/<vendor>/<suite>/<version>/` directory is one publishable standard package (e.g. `law/us/nara/v19`).

## Authentication

Set `ZB_TOKEN` in your environment to authenticate with the npm registry. Get one from [ZeroBias](https://app.zerobias.com).

## Build & validate

This repo is on the gradle + [zbb](https://github.com/zerobias-org/devops) publish pipeline.

```bash
# Validate one package (file-shape checks only):
./gradlew :<type>:<vendor>:<suite>:<version>:validateContent

# Full gate (validate → buildArtifacts → testIntegrationDataloader → writeGateStamp):
./gradlew :<type>:<vendor>:<suite>:<version>:gate
```

`gate` writes `gate-stamp.json` — the publish workflow rejects any package without a committed stamp.

## Naming

Standards are depth 4, and the leading `<type>` category segment is **dropped** from the package identity:

- dir: `package/<type>/<vendor>/<suite>/<version>/` → `law/us/nara/v19`
- npm: `@zerobias-org/standard-<vendor>-<suite>-<version>` → `standard-us-nara-v19`
- `zerobias.package`: `<vendor>.<suite>.<version>.standard` → `us.nara.v19.standard`

## Creating a new standard

```bash
sh scripts/createNewStandard.sh <type> <vendor> <suite> <version>
```

Then fill `index.yml`, add `elements/*.yml`, drop the gradle marker (`echo 'plugins { id("zb.content") }' > package/<type>/<vendor>/<suite>/<version>/build.gradle.kts`), and run `./gradlew :<type>:<vendor>:<suite>:<version>:gate`.

## Publishing

`.github/workflows/publish.yml` invokes `zerobias-org/devops/.github/workflows/zbb-publish-reusable.yml@main` on push to `main`/`qa`/`dev`/`uat`. Pre-release validation on a feature branch: `gh workflow run publish.yml --ref <branch>`.

## Commit format

[Conventional Commits](https://www.conventionalcommits.org/), enforced by `commitlint` (husky `commit-msg` hook).

```
feat(standard-<vendor>-<suite>-<version>): short subject
```
