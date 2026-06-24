<!-- master source — other languages are translations of this file -->
# readme-gradle — Plugin Internals

> Developer & contributor guide for the `readme-plugin` Gradle plugin.

[![Maven Central](https://img.shields.io/maven-central/v/education.cccp/readme-plugin.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/education.cccp/readme-plugin)
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/education.cccp.readme.svg?label=Plugin%20Portal)](https://plugins.gradle.org/plugin/education.cccp.readme)
[![CI](https://img.shields.io/github/actions/workflow/status/cccp-education/readme-gradle/ci.yml?branch=main&label=CI)](https://github.com/cccp-education/readme-gradle/actions/workflows/ci.yml)
[![License](https://img.shields.io/github/license/cccp-education/readme-gradle?label=License)](../LICENCE)

- **Version**: `0.0.1` · **Group**: `education.cccp` · **Plugin ID**: `education.cccp.readme`
- **Toolchain**: Java 24 · Kotlin 2.3.20 · Gradle 9.5.1
- **Build**: `./gradlew -p readme-plugin build -x test` · **Tests**: `./gradlew -p readme-plugin check` · **Coverage**: not gated

🌐 Languages: **EN** | [中文](README.plugin/README.zh.md) | [हिन्दी](README.plugin/README.hi.md) | [Español](README.plugin/README.es.md) | [Français](README.plugin/README.fr.md) | [العربية](README.plugin/README.ar.md) | [বাংলা](README.plugin/README.bn.md) | [Português](README.plugin/README.pt.md) | [Русский](README.plugin/README.ru.md) | [اردو](README.plugin/README.ur.md)

---

## Module layout

```
readme-gradle/
├── build.gradle.kts                          # Consumer harness — applies education.cccp.readme
├── settings.gradle.kts                        # pluginManagement: mavenLocal + portal + central
├── README_truth.adoc                          # EN source of truth (this README's parent reference)
├── README_truth_fr.adoc                       # FR source of truth
├── readme.yml                                 # gitignored — injected from CI secret
└── readme-plugin/
    ├── build.gradle.kts                       # Plugin module (signing, java-gradle-plugin, nmcp)
    ├── gradle/libs.versions.toml              # Version catalog
    └── src/
        ├── main/kotlin/readme/
        │   ├── ReadmePlugin.kt                # Plugin entry point — registers 3 tasks
        │   ├── ReadmePlantUmlConfig.kt       # Jackson YAML config model (+ Source/Output/Git)
        │   ├── ScaffoldTask.kt               # generateReadme — scaffolds readme.yml + workflow
        │   ├── ProcessReadmeTask.kt          # transformReadme — PNG gen + block rewrite
        │   ├── CommitGeneratedReadmeTask.kt  # commitGeneratedReadme — JGit add/commit/push
        │   ├── AdocSourceFile.kt             # Source-of-truth model + lang detection
        │   ├── GitUtils.kt                   # imgDir resolution from git root
        │   ├── GitRemoteValidator.kt         # Interface for remote validation
        │   └── JGitRemoteValidator.kt        # JGit implementation
        ├── test/
        │   ├── kotlin/…                      # JUnit5 unit tests
        │   ├── scenarios/…                   # Cucumber step definitions
        │   └── features/                      # 5 .feature files (56 scenarios)
        │       ├── 1_minimal.feature
        │       ├── 2_scaffold.feature
        │       ├── 3_process.feature
        │       ├── 4_commit.feature
        │       └── 5_integration.feature
        └── functionalTest/kotlin/…           # GradleRunner functional tests
```

## N0 contracts (consumed via `education.cccp.codebase`)

The plugin applies `education.cccp.codebase` version `0.0.2` (from
`libs.versions.toml`). Through codebase-gradle it gains transitive access to the
shared N0 contracts published by `workspace-bom` MEMPHIS:

| Contract | Artifact | Provides |
|----------|----------|----------|
| `codebase-contracts`          | `education.cccp:codebase-contracts`          | ContextChannel, ChannelBudget, CompositeContext |
| `agent-contracts`             | `education.cccp:agent-contracts`             | Epic, UserStory, GradleTask, AgentState |
| `llm-pool-contracts`          | `education.cccp:llm-pool-contracts`          | LlmInstancePool, LlmInstance, QuotaConfig |
| `opencode-session-contracts`  | `education.cccp:opencode-session-contracts`  | SessionPrompt, SessionResponse, AgentContext |
| `i18n-contracts`              | `education.cccp:i18n-contracts`              | SupportedLanguage, LanguageCatalog, I18nConfig |

> Note: `readme-gradle` applies `education.cccp.codebase` as a *plugin* (via the
> Gradle plugin portal alias `alias(libs.plugins.codebase)`), not as an
> `implementation` dependency. Direct N0 contract artifacts are consumed
> transitively by codebase-gradle itself.

## Key dependencies

| Library | Version | Role |
|---------|---------|------|
| `net.sourceforge.plantuml:plantuml` | `1.2026.0` | PNG generation from PlantUML blocks |
| `org.eclipse.jgit:org.eclipse.jgit` | `7.5.0.202512021534-r` | Git operations without a system git binary |
| `com.fasterxml.jackson.module:jackson-module-kotlin` | `2.21.1` | YAML → Kotlin data class mapping |
| `com.fasterxml.jackson.dataformat:jackson-dataformat-yaml` | `2.21.1` | Parsing of `readme.yml` |
| `io.arrow-kt:arrow-core` | `2.2.2` | Functional types |
| `io.arrow-kt:arrow-fx-coroutines` | `2.2.2` | Coroutine-based effects |
| `com.github.node-gradle:gradle-node-plugin` | `7.1.0` | Node tooling (future HTML serve) |
| `dev.langchain4j:langchain4j` (+ providers) | `1.14.1` / `1.14.1-beta24` | LLM providers (compileOnly — AI bundle) |
| `org.asciidoctor:asciidoctor-gradle-jvm` | `5.0.0-alpha.1` | Asciidoctor tooling (compileOnly) |

LLM provider stack (bundle `readme-ai`, `compileOnly`):
`langchain4j-ollama`, `langchain4j-open-ai`, `langchain4j-google-ai-gemini`,
`langchain4j-mistral-ai`, `langchain4j-pgvector`, `langchain4j-embeddings-all-minilm-l6-v2`.

`koog-agents` is **not** a direct dependency of readme-gradle in the current
catalog — the AI bundle is declared `compileOnly` and provided by the consuming
build (e.g. codebase-gradle's runtime).

## Test matrix

| Task | Scope | Notes |
|------|-------|-------|
| `test`           | JUnit5 unit tests | Excludes `readme.scenarios.**` and `readme.ReadmeGradlePluginFunctionalTests` |
| `cucumberTest`   | Cucumber BDD (5 features, 56 scenarios) | Uses `cucumber-junit-platform-engine`, excludes `junit-jupiter` engine |
| `functionalTest`  | GradleRunner functional tests | Separate source set `functionalTest` registered with `gradlePlugin.testSourceSets` for `plugin-under-test-metadata.properties` |
| `check`          | Aggregates `test` + `functionalTest` + `cucumberTest` | |

Test framework versions (from `libs.versions.toml`):
- JUnit Platform `1.14.3` · Cucumber `7.34.3` · AssertJ `3.27.7`
- Mockito Kotlin `6.2.3` · Mockito Jupiter `5.23.0`
- Kotlinx Coroutines `1.10.2` · Testcontainers PostgreSQL `1.21.4`

> `logback-classic` is **excluded** from `testRuntimeClasspath`,
> `testImplementation` and `functionalTest.runtimeClasspath` to avoid SLF4J
> binding conflicts with Gradle's own logging.

## JVM tuning

The plugin build uses `-XX:+EnableDynamicAgentLoading` on all `Test` tasks (set
in `tasks.withType<Test>`). There is no bespoke G1GC/SerialGC split profile —
adjust `GRADLE_OPTS` if your runner needs a larger heap:

```bash
export GRADLE_OPTS="-Xmx2g"
```

## Build commands

```bash
./gradlew -p readme-plugin build                            # full build (compile + all tests)
./gradlew -p readme-plugin build -x test                    # compile only
./gradlew -p readme-plugin test                             # JUnit5 unit tests
./gradlew -p readme-plugin cucumberTest                     # Cucumber BDD
./gradlew -p readme-plugin functionalTest                   # GradleRunner functional
./gradlew -p readme-plugin check                            # test + functionalTest + cucumberTest
./gradlew -p readme-plugin publishToMavenLocal              # local publish
./gradlew -p readme-plugin publishAggregationToCentralPortal --no-daemon   # Maven Central (NMCP)
```

## CI pipeline

Three workflows live in `.github/workflows/`:

1. **`readme_action.yml`** — `Generate README from truth sources`. Triggers on
   push to any branch touching `README_truth*.adoc`. Injects the
   `README_GRADLE_PLUGIN` secret, runs `./gradlew -p readme-plugin
   commitGeneratedReadme --no-daemon` (JGit commit + push). JDK 23 (Temurin).
2. **`ci.yml`** — `CI`. Runs after `readme_action.yml` completes (via
   `workflow_run`) and on push/PR ignoring `README_truth*.adoc` and `*.md`.
   Runs `./gradlew -p readme-plugin clean check --no-daemon`, uploads test
   reports. JDK 23 (Temurin).
3. **`integration_test.yml`** — `Integration Test — commitGeneratedReadme`.
   Manual dispatch only. Runs Cucumber scenarios tagged `@integration`:
   `./gradlew -p readme-plugin cucumberTest -Dcucumber.filter.tags="@integration"`.

## Publication (NMCP)

- **Plugin Portal**: published via `com.gradle.plugin-publish` `2.1.0`
  (`alias(libs.plugins.publish)`). Plugin metadata: `id = education.cccp.readme`,
  `implementationClass = readme.ReadmePlugin`, displayName `"README helper Plugin"`,
  tags `asciidoc`, `plantuml`, `readme`, `documentation`, `github`, `diagram`.
- **Maven Central**: `publishing { repositories { mavenCentral() } }` with POM
  on every `MavenPublication`. Signing via `signing { useGpgCmd() }` — skipped
  when `CI=true` or version ends with `-SNAPSHOT`. Developer `cccp-education`,
  SCM pointing to `github.com/cccp-education/readme-gradle`.
- The `relocationGroup` project property, if set, emits a
  `<distributionManagement><relocation>` block in the POM for groupId moves.
- `java { withJavadocJar(); withSourcesJar() }` — both jars packaged.
- Version is **hardcoded** in the catalog: `readme = "0.0.1"`. The module
  resolves it via `libs.plugins.readme.get().version`.

> Confirm before publishing: all `implementation` dependencies must be released
> versions (no `-SNAPSHOT`), and any `education.cccp:*` transitive must already
> be on Central.

## Architecture docs

The plugin follows a hexagonal layout (driving adapters = Gradle tasks,
application core = `ReadmePlugin` + config, domain = `AdocSourceFile`,
driven adapters = FileSystem / PlantUML engine / JGit):

- [README_truth.adoc](../README_truth.adoc) — AsciiDoc reference with 7 embedded
  PlantUML diagrams (problem-solution, source-of-truth, overview-sequence,
  activity, components, class-diagram, hexagonal). This is the canonical
  architectural description.
- [README_truth_fr.adoc](../README_truth_fr.adoc) — French translation of the
  reference.

The `.github/workflows/readmes/images/{en,fr}/` directory holds the generated
PNG artifacts committed by the plugin's own pipeline (7 diagrams × 2 langs).

## Contributing

1. Build compiles: `./gradlew -p readme-plugin build -x test`
2. Unit tests green: `./gradlew -p readme-plugin test`
3. Cucumber green: `./gradlew -p readme-plugin cucumberTest`
4. Functional tests green: `./gradlew -p readme-plugin functionalTest`
5. No secrets in `readme.yml` — it is gitignored and injected from the
   `README_GRADLE_PLUGIN` CI secret. Never commit a real PAT.
6. Follow the hexagonal conventions (tasks as driving adapters, domain model
   `AdocSourceFile` isolated from infrastructure).

## License

Apache License 2.0 — see [LICENCE](../LICENCE).

---

_Part of the CCCP Education ecosystem — `groupId: education.cccp`._