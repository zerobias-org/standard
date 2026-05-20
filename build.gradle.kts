import com.zerobias.buildtools.content.SchemaPrimitives

plugins {
    id("zb.workspace")
}

group = "com.zerobias.content"

// ════════════════════════════════════════════════════════════
// Standard content validator — owned by this repo.
//
// Philosophy (per Chris/Kevin): the dataloader is the source of truth
// for schema rules (UUID format, status enum, semver, elementType
// lookup, etc.). Re-validating those here just creates drift risk —
// when the dataloader tightens a rule, the gate gets stale.
//
// Standard's payload shape:
//   package/<type>/<vendor>/<suite>/<version>/
//     index.yml                          — standard metadata + element types
//     elements/<elementCode>.yml         — one yaml per requirement
//   The leading <type> segment (e.g. "law") is a CATEGORY — it is part
//   of the on-disk layout but is DROPPED from the npm name and
//   zerobias.package. Source: scripts/createNewStandard.sh, which builds
//   FOLDER_PATH=package/<type>/<vendor>/<suite>/<version> but CODE and
//   package identity from <vendor>_<suite>_<version> only.
//
// This validator only enforces things the dataloader CANNOT or DOES NOT
// check:
//
//   1. Filesystem ↔ npm ↔ zerobias-block triangulation. Dataloader
//      reads zerobias.package but never the npm `name` field, and has
//      no view of the on-disk directory layout.
//
//      Standard formula (depth 4, category dropped):
//        dir              = package/<type>/<vendor>/<suite>/<version>/
//        npm name         = @zerobias-org/standard-<vendor>-<suite>-<version>
//        zerobias.package = <vendor>.<suite>.<version>.standard
//      (Trailing `.standard` suffix disambiguates artifact type, same
//      pattern as tag's `.tag` / framework's `.framework`. Dots in a
//      version segment are normalized to underscores for the
//      zerobias.package, matching how framework handles dotted versions.)
//
//   2. Repo-wide unique `id` UUIDs across BOTH index.yml AND every
//      elements/*.yml file (separate :validateUniqueIds task).
//
// Everything else (UUID format, semver parse, elementType lookup,
// status enum, etc.) is delegated to the dataloader running in
// testIntegrationDataloader during gate.
// ════════════════════════════════════════════════════════════
extra["contentValidator"] = { proj: org.gradle.api.Project ->
    val projectDir = proj.projectDir
    val tag = "[standard-validator] ${proj.path}"

    require(projectDir.resolve("index.yml").isFile)    { "$tag index.yml missing in ${projectDir.path}" }
    require(projectDir.resolve("package.json").isFile) { "$tag package.json missing in ${projectDir.path}" }
    require(projectDir.resolve(".npmrc").isFile)       { "$tag .npmrc missing in ${projectDir.path}" }

    // ── 1. Filesystem ↔ npm ↔ zerobias-block triangulation ──
    val version = projectDir.name
    val suite = projectDir.parentFile.name
    val vendor = projectDir.parentFile.parentFile.name
    val category = projectDir.parentFile.parentFile.parentFile.name   // dropped from identity
    val packageVersion = version.replace(".", "_")

    val pkgDoc = SchemaPrimitives.parseJson(projectDir.resolve("package.json"))
    SchemaPrimitives.requirePackageIdentity(
        pkgDoc,
        expectedNpmName = "@zerobias-org/standard-$vendor-$suite-$version",
        expectedZerobiasPackage = "$vendor.$suite.$packageVersion.standard",
        field = "$tag package.json",
    )
    require(SchemaPrimitives.getPath(pkgDoc, "zerobias.import-artifact") == "standard" ||
            SchemaPrimitives.getPath(pkgDoc, "auditmation.import-artifact") == "standard") {
        "$tag zerobias.import-artifact must be 'standard'"
    }

    proj.logger.lifecycle("$tag: category=$category vendor=$vendor suite=$suite version=$version")
}

// ════════════════════════════════════════════════════════════
// :validateUniqueIds — repo-wide cross-cut.
// Walks every *.yml under package/ (index.yml + elements/*.yml),
// extracts `id`, fails on collision. Dataloader sees one artifact at a
// time; collisions only surface when the second tries to overwrite the
// first DB row.
// ════════════════════════════════════════════════════════════
val validateUniqueIds by tasks.registering {
    group = "verification"
    description = "Fail if two standard / element YAMLs share the same id UUID"

    val packageDir = layout.projectDirectory.dir("package").asFile
    inputs.files(
        fileTree(packageDir) {
            include("**/*.yml")
            exclude("**/node_modules/**")
        }
    )

    doLast {
        val byId = mutableMapOf<String, MutableList<String>>()
        packageDir.walkTopDown()
            .onEnter { it.name != "node_modules" }
            .filter { it.isFile && it.name.endsWith(".yml") }
            .forEach { f ->
                val doc = try {
                    SchemaPrimitives.parseYaml(f)
                } catch (e: Exception) {
                    logger.warn("[validateUniqueIds] skipping unparseable ${f.relativeTo(rootDir)}: ${e.message}")
                    return@forEach
                }
                val id = (doc["id"] as? String)?.lowercase() ?: return@forEach
                byId.getOrPut(id) { mutableListOf() }.add(f.relativeTo(rootDir).path)
            }

        val collisions = byId.filterValues { it.size > 1 }
        if (collisions.isNotEmpty()) {
            val report = collisions.entries.joinToString("\n") { (id, paths) ->
                "  $id\n    " + paths.joinToString("\n    ")
            }
            throw GradleException("[validateUniqueIds] duplicate standard/element ids across the repo:\n$report")
        }
        logger.lifecycle("[validateUniqueIds] ${byId.size} unique ids across ${byId.values.sumOf { it.size }} yaml files")
    }
}

subprojects {
    tasks.matching { it.name == "validateContent" }.configureEach {
        dependsOn(rootProject.tasks.named("validateUniqueIds"))
    }
}

val projectPaths by tasks.registering {
    group = "info"
    description = "Output project-to-directory mappings for tooling (used by zbb CLI)"
    doLast {
        subprojects.filter { it.buildFile.exists() }.forEach { p ->
            println("${p.path}=${p.projectDir.relativeTo(rootDir)}")
        }
    }
}

val changedModules by tasks.registering {
    group = "info"
    description = "List standard packages changed since last version tag"
    doLast {
        val lastTag = try {
            providers.exec { commandLine("git", "describe", "--tags", "--abbrev=0") }
                .standardOutput.asText.get().trim()
        } catch (e: Exception) {
            logger.warn("No version tags found -- listing all standard packages as changed")
            null
        }

        val diffArgs = if (lastTag != null) listOf("git", "diff", "--name-only", lastTag, "HEAD")
                       else listOf("git", "ls-files")

        val result = providers.exec { commandLine(diffArgs) }.standardOutput.asText.get()

        // Standard packages live four directories deep under package/ —
        // package/<type>/<vendor>/<suite>/<version>/. Walk up each changed
        // file to its nearest build.gradle.kts to find the owning package.
        val packageDir = rootDir.resolve("package")
        val changed = mutableSetOf<String>()
        result.lines()
            .filter { it.startsWith("package/") }
            .forEach { line ->
                var dir = rootDir.resolve(line).parentFile
                while (dir != null && dir != packageDir && dir.startsWith(packageDir)) {
                    if (dir.resolve("build.gradle.kts").isFile) {
                        changed.add(dir.relativeTo(packageDir).path)
                        break
                    }
                    dir = dir.parentFile
                }
            }
        changed.forEach { println(it) }
    }
}
