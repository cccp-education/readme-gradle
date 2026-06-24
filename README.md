<!-- master source — other languages are translations of this file -->
# readme-gradle — Consumer Guide

> Gradle plugin that generates GitHub-compatible `README.adoc` files from
> `README_truth.adoc` source files, rendering native `[plantuml]` blocks into
> committed PNG images via GitHub Actions and JGit.

[![Maven Central](https://img.shields.io/maven-central/v/education.cccp/readme-plugin.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/education.cccp/readme-plugin)
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/education.cccp.readme.svg?label=Plugin%20Portal)](https://plugins.gradle.org/plugin/education.cccp.readme)
[![CI](https://img.shields.io/github/actions/workflow/status/cccp-education/readme-gradle/ci.yml?branch=main&label=CI)](https://github.com/cccp-education/readme-gradle/actions/workflows/ci.yml)
[![License](https://img.shields.io/github/license/cccp-education/readme-gradle?label=License)](../LICENCE)

- **Version**: `0.0.1` · **Group**: `education.cccp` · **Plugin ID**: `education.cccp.readme`
- **Toolchain**: Java 24 · Kotlin 2.3.20 · Gradle 9.5.1
- **Build**: `./gradlew build` · **Tests**: `./gradlew check` (JUnit5 + 56 Cucumber scenarios across 5 features)
- **Coverage**: not gated (no Kover rule wired into `check`)

🌐 Languages: **EN** | [中文](README.consommateurs/README.zh.md) | [हिन्दी](README.consommateurs/README.hi.md) | [Español](README.consommateurs/README.es.md) | [Français](README.consommateurs/README.fr.md) | [العربية](README.consommateurs/README.ar.md) | [বাংলা](README.consommateurs/README.bn.md) | [Português](README.consommateurs/README.pt.md) | [Русский](README.consommateurs/README.ru.md) | [اردو](README.consommateurs/README.ur.md)

---

## What it does

GitHub does not natively render PlantUML syntax embedded in AsciiDoc files.
`readme-gradle` automates the workaround: you edit a single source of truth
(`README_truth.adoc`), the plugin extracts every `[plantuml, name, png]` block,
generates PNG images, rewrites the block as an `image::` reference, and commits
both the rewritten `README.adoc` and the images back to your repository through
JGit — no system `git` binary required on the CI runner.

```
README_truth.adoc → generateReadme → transformReadme → commitGeneratedReadme → README.adoc + images/
                   (scaffolds yml)  (PNG + rewrite)    (JGit add + commit + push)
```

## Quick Start

### 1. Apply the plugin

```gradle
plugins {
    id("education.cccp.readme") version "0.0.1"
}
```

### 2. Author your source of truth

Create `README_truth.adoc` at the project root. Use native PlantUML blocks:

```asciidoc
[plantuml, architecture, png]
----
@startuml
package "plugin" {
  [ReadmePlugin]
}
@enduml
----
```

For a French variant, name the file `README_plantuml_fr.adoc` — the `_fr`
suffix is detected and produces `README_fr.adoc`.

### 3. Configure via `readme.yml`

The plugin reads `readme.yml` from the project root. **Never commit a real
token** — store the full file (token included) in the GitHub secret
`README_GRADLE_PLUGIN` and inject it in CI:

```yaml
source:
  dir: "."
  defaultLang: "en"

output:
  imgDir: ".github/workflows/readmes/images"

git:
  userName: "github-actions[bot]"
  userEmail: "github-actions[bot]@users.noreply.github.com"
  commitMessage: "chore: generate readme [skip ci]"
  token: "<YOUR_GITHUB_PAT>"
  watchedBranches:
    - "main"
    - "master"
```

CI step:

```yaml
- name: Inject plugin config
  run: echo "${{ secrets.README_GRADLE_PLUGIN }}" > readme.yml

- name: Generate README and commit via JGit
  run: ./gradlew -p readme-plugin commitGeneratedReadme --no-daemon
```

### 4. Run the pipeline

```bash
./gradlew commitGeneratedReadme          # full pipeline (CI)
./gradlew transformReadme               # PNG generation + rewrite only
./gradlew generateReadme                 # scaffold readme.yml + workflow if missing
```

## Available tasks

| Task | Group | Description |
|------|-------|-------------|
| `generateReadme`         | generate  | Creates `readme.yml` and `.github/workflows/readme_action.yml` if absent |
| `transformReadme`        | transform | Generates PNGs and rewrites `README*.adoc` from `README_truth*.adoc` (depends on `generateReadme`) |
| `commitGeneratedReadme`  | deploy    | Commits and pushes generated `README*.adoc` + images via JGit (CI only, depends on `transformReadme`) |

## Extension DSL

The plugin has **no Gradle extension block**. All configuration is read from
the `readme.yml` file at the project root (parsed by Jackson YAML into
`ReadmePlantUmlConfig`). If `readme.yml` is absent or empty, sensible defaults
are used silently.

| Config section | Field | Default |
|----------------|-------|---------|
| `source`  | `dir`         | `"."` |
| `source`  | `defaultLang` | `"en"` |
| `output`  | `imgDir`      | `".github/workflows/readmes/images"` |
| `git`     | `userName`        | `"github-actions[bot]"` |
| `git`     | `userEmail`       | `"github-actions[bot]@users.noreply.github.com"` |
| `git`     | `commitMessage`   | `"chore: generate readme [skip ci]"` |
| `git`     | `token`           | `""` (must be set — error at runtime if empty/placeholder) |
| `git`     | `watchedBranches`  | `["main", "master"]` |

## Prerequisites

- **Java** 24+ (Kotlin 2.3.20 toolchain)
- **Gradle** 9.5.1+
- **GitHub** repository with write access (for JGit push)
- **GitHub secret** `README_GRADLE_PLUGIN` containing the full `readme.yml` with a valid PAT

## Build & test

```bash
./gradlew -p readme-plugin build              # full build (compile + unit + cucumber + functional)
./gradlew -p readme-plugin test               # JUnit5 unit tests only
./gradlew -p readme-plugin cucumberTest       # Cucumber BDD (5 feature files, 56 scenarios)
./gradlew -p readme-plugin functionalTest     # GradleRunner functional tests
./gradlew -p readme-plugin check              # test + functionalTest + cucumberTest
./gradlew -p readme-plugin publishToMavenLocal # local publish
```

## Troubleshooting

| Symptom | Fix |
|---------|-----|
| `GitHub token is empty or still a placeholder in readme.yml` | Set `README_GRADLE_PLUGIN` secret; ensure CI injects it before the task runs |
| No PNG generated, README unchanged | Confirm `README_truth*.adoc` exists in `source.dir`; check block syntax `[plantuml, name, png]` |
| JGit push rejected | Branch must be in `watchedBranches` (`main`/`master` by default); verify PAT has `contents: write` |
| `Java heap space` | `export GRADLE_OPTS="-Xmx2g"` |
| Logback SLF4J conflict | The build excludes `logback-classic` from test classpaths automatically — do not re-add it |

See [README_truth.adoc](../README_truth.adoc) for the full AsciiDoc reference.

## License

Apache License 2.0 — see [LICENCE](../LICENCE).

---

_Part of the CCCP Education ecosystem — `groupId: education.cccp`._