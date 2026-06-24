<!-- translated from README.md rev 0.0.1 -->
# readme-gradle — Interior del plugin

> Guía para desarrolladores y colaboradores del plugin de Gradle
> `readme-plugin`.

[![Maven Central](https://img.shields.io/maven-central/v/education.cccp/readme-plugin.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/education.cccp/readme-plugin)
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/education.cccp.readme.svg?label=Plugin%20Portal)](https://plugins.gradle.org/plugin/education.cccp.readme)
[![CI](https://img.shields.io/github/actions/workflow/status/cccp-education/readme-gradle/ci.yml?branch=main&label=CI)](https://github.com/cccp-education/readme-gradle/actions/workflows/ci.yml)
[![License](https://img.shields.io/github/license/cccp-education/readme-gradle?label=License)](../LICENCE)

- **Versión**: `0.0.1` · **Group**: `education.cccp` · **Plugin ID**: `education.cccp.readme`
- **Toolchain**: Java 24 · Kotlin 2.3.20 · Gradle 9.5.1
- **Build**: `./gradlew -p readme-plugin build -x test` · **Tests**: `./gradlew -p readme-plugin check` · **Cobertura**: sin umbral

🌐 Languages: [English](README.md) | [中文](README.zh.md) | [हिन्दी](README.hi.md) | **Español** | [Français](README.fr.md) | [العربية](README.ar.md) | [বাংলা](README.bn.md) | [Português](README.pt.md) | [Русский](README.ru.md) | [اردو](README.ur.md)

---

## Disposición de módulos

```
readme-gradle/
├── build.gradle.kts                          # Harness del consumidor — aplica education.cccp.readme
├── settings.gradle.kts                        # pluginManagement: mavenLocal + portal + central
├── README_truth.adoc                          # Fuente de verdad EN (referencia padre de este README)
├── README_truth_fr.adoc                       # Fuente de verdad FR
├── readme.yml                                 # gitignored — inyectado desde secret de CI
└── readme-plugin/
    ├── build.gradle.kts                       # Módulo del plugin (signing, java-gradle-plugin, nmcp)
    ├── gradle/libs.versions.toml              # Catálogo de versiones
    └── src/
        ├── main/kotlin/readme/
        │   ├── ReadmePlugin.kt                # Punto de entrada del plugin — registra 3 tareas
        │   ├── ReadmePlantUmlConfig.kt       # Modelo de config Jackson YAML (+ Source/Output/Git)
        │   ├── ScaffoldTask.kt               # generateReadme — scaffolds readme.yml + workflow
        │   ├── ProcessReadmeTask.kt          # transformReadme — gen PNG + reescritura de bloques
        │   ├── CommitGeneratedReadmeTask.kt  # commitGeneratedReadme — JGit add/commit/push
        │   ├── AdocSourceFile.kt             # Modelo fuente-de-verdad + detección de idioma
        │   ├── GitUtils.kt                   # Resolución de imgDir desde git root
        │   ├── GitRemoteValidator.kt         # Interfaz para validación remota
        │   └── JGitRemoteValidator.kt        # Implementación JGit
        ├── test/
        │   ├── kotlin/…                      # Tests unitarios JUnit5
        │   ├── scenarios/…                   # Definiciones de pasos Cucumber
        │   └── features/                      # 5 archivos .feature (56 escenarios)
        │       ├── 1_minimal.feature
        │       ├── 2_scaffold.feature
        │       ├── 3_process.feature
        │       ├── 4_commit.feature
        │       └── 5_integration.feature
        └── functionalTest/kotlin/…           # Tests funcionales GradleRunner
```

## Contratos N0 (consumidos vía `education.cccp.codebase`)

El plugin aplica `education.cccp.codebase` versión `0.0.2` (de
`libs.versions.toml`). A través de codebase-gradle obtiene acceso
transitivo a los contratos N0 compartidos publicados por `workspace-bom`
MEMPHIS:

| Contrato | Artefacto | Proporciona |
|----------|----------|----------|
| `codebase-contracts`          | `education.cccp:codebase-contracts`          | ContextChannel, ChannelBudget, CompositeContext |
| `agent-contracts`             | `education.cccp:agent-contracts`             | Epic, UserStory, GradleTask, AgentState |
| `llm-pool-contracts`          | `education.cccp:llm-pool-contracts`          | LlmInstancePool, LlmInstance, QuotaConfig |
| `opencode-session-contracts`  | `education.cccp:opencode-session-contracts`  | SessionPrompt, SessionResponse, AgentContext |
| `i18n-contracts`              | `education.cccp:i18n-contracts`              | SupportedLanguage, LanguageCatalog, I18nConfig |

> Nota: `readme-gradle` aplica `education.cccp.codebase` como *plugin* (vía el
> alias del portal de plugins de Gradle `alias(libs.plugins.codebase)`), no como
> dependencia `implementation`. Los artefactos de contrato N0 directos son
> consumidos transitivamente por el propio codebase-gradle.

## Dependencias clave

| Librería | Versión | Rol |
|---------|---------|------|
| `net.sourceforge.plantuml:plantuml` | `1.2026.0` | Generación de PNG desde bloques PlantUML |
| `org.eclipse.jgit:org.eclipse.jgit` | `7.5.0.202512021534-r` | Operaciones Git sin binario git del sistema |
| `com.fasterxml.jackson.module:jackson-module-kotlin` | `2.21.1` | Mapeo YAML → Kotlin data class |
| `com.fasterxml.jackson.dataformat:jackson-dataformat-yaml` | `2.21.1` | Parseo de `readme.yml` |
| `io.arrow-kt:arrow-core` | `2.2.2` | Tipos funcionales |
| `io.arrow-kt:arrow-fx-coroutines` | `2.2.2` | Efectos basados en corrutinas |
| `com.github.node-gradle:gradle-node-plugin` | `7.1.0` | Tooling de Node (futuro serve HTML) |
| `dev.langchain4j:langchain4j` (+ providers) | `1.14.1` / `1.14.1-beta24` | Proveedores LLM (compileOnly — AI bundle) |
| `org.asciidoctor:asciidoctor-gradle-jvm` | `5.0.0-alpha.1` | Tooling Asciidoctor (compileOnly) |

Stack de proveedores LLM (bundle `readme-ai`, `compileOnly`):
`langchain4j-ollama`, `langchain4j-open-ai`, `langchain4j-google-ai-gemini`,
`langchain4j-mistral-ai`, `langchain4j-pgvector`, `langchain4j-embeddings-all-minilm-l6-v2`.

`koog-agents` **no** es una dependencia directa de readme-gradle en el catálogo
actual — el bundle de IA se declara `compileOnly` y lo proporciona el build
consumidor (p. ej. el runtime de codebase-gradle).

## Matriz de tests

| Tarea | Alcance | Notas |
|------|-------|-------|
| `test`           | Tests unitarios JUnit5 | Excluye `readme.scenarios.**` y `readme.ReadmeGradlePluginFunctionalTests` |
| `cucumberTest`   | Cucumber BDD (5 features, 56 escenarios) | Usa `cucumber-junit-platform-engine`, excluye el motor `junit-jupiter` |
| `functionalTest`  | Tests funcionales GradleRunner | Source set separado `functionalTest` registrado con `gradlePlugin.testSourceSets` para `plugin-under-test-metadata.properties` |
| `check`          | Agrega `test` + `functionalTest` + `cucumberTest` | |

Versiones del framework de tests (de `libs.versions.toml`):
- JUnit Platform `1.14.3` · Cucumber `7.34.3` · AssertJ `3.27.7`
- Mockito Kotlin `6.2.3` · Mockito Jupiter `5.23.0`
- Kotlinx Coroutines `1.10.2` · Testcontainers PostgreSQL `1.21.4`

> `logback-classic` se **excluye** de `testRuntimeClasspath`,
> `testImplementation` y `functionalTest.runtimeClasspath` para evitar
> conflictos de binding SLF4J con el logging propio de Gradle.

## Ajuste JVM

El build del plugin usa `-XX:+EnableDynamicAgentLoading` en todas las tareas
`Test` (definido en `tasks.withType<Test>`). No hay un perfil a medida de
división G1GC/SerialGC — ajusta `GRADLE_OPTS` si tu runner necesita más heap:

```bash
export GRADLE_OPTS="-Xmx2g"
```

## Comandos de build

```bash
./gradlew -p readme-plugin build                            # build completo (compile + todos los tests)
./gradlew -p readme-plugin build -x test                    # solo compile
./gradlew -p readme-plugin test                             # tests unitarios JUnit5
./gradlew -p readme-plugin cucumberTest                     # Cucumber BDD
./gradlew -p readme-plugin functionalTest                   # funcional GradleRunner
./gradlew -p readme-plugin check                            # test + functionalTest + cucumberTest
./gradlew -p readme-plugin publishToMavenLocal              # publicación local
./gradlew -p readme-plugin publishAggregationToCentralPortal --no-daemon   # Maven Central (NMCP)
```

## Pipeline CI

Tres workflows viven en `.github/workflows/`:

1. **`readme_action.yml`** — `Generar README desde fuentes de verdad`. Se
   dispara al hacer push a cualquier rama que toque `README_truth*.adoc`.
   Inyecta el secret `README_GRADLE_PLUGIN`, ejecuta `./gradlew -p readme-plugin
   commitGeneratedReadme --no-daemon` (JGit commit + push). JDK 23 (Temurin).
2. **`ci.yml`** — `CI`. Se ejecuta tras completarse `readme_action.yml` (vía
   `workflow_run`) y en push/PR ignorando `README_truth*.adoc` y `*.md`.
   Ejecuta `./gradlew -p readme-plugin clean check --no-daemon`, sube los
   reportes de test. JDK 23 (Temurin).
3. **`integration_test.yml`** — `Integration Test — commitGeneratedReadme`.
   Solo dispatch manual. Ejecuta los escenarios Cucumber etiquetados
   `@integration`: `./gradlew -p readme-plugin cucumberTest -Dcucumber.filter.tags="@integration"`.

## Publicación (NMCP)

- **Plugin Portal**: publicado vía `com.gradle.plugin-publish` `2.1.0`
  (`alias(libs.plugins.publish)`). Metadatos del plugin:
  `id = education.cccp.readme`, `implementationClass = readme.ReadmePlugin`,
  displayName `"README helper Plugin"`, tags `asciidoc`, `plantuml`,
  `readme`, `documentation`, `github`, `diagram`.
- **Maven Central**: `publishing { repositories { mavenCentral() } }` con POM
  en cada `MavenPublication`. Firma vía `signing { useGpgCmd() }` — se omite
  cuando `CI=true` o la versión termina en `-SNAPSHOT`. Developer
  `cccp-education`, SCM apuntando a `github.com/cccp-education/readme-gradle`.
- La propiedad de proyecto `relocationGroup`, si se define, emite un bloque
  `<distributionManagement><relocation>` en el POM para migraciones de groupId.
- `java { withJavadocJar(); withSourcesJar() }` — ambos jars empaquetados.
- La versión está **hardcodeada** en el catálogo: `readme = "0.0.1"`. El
  módulo la resuelve vía `libs.plugins.readme.get().version`.

> Confirma antes de publicar: todas las dependencias `implementation` deben
> ser versiones released (sin `-SNAPSHOT`), y cualquier `education.cccp:*`
> transitivo debe estar ya en Central.

## Documentos de arquitectura

El plugin sigue un layout hexagonal (adaptadores de conducción = tareas
Gradle, núcleo de aplicación = `ReadmePlugin` + config, dominio =
`AdocSourceFile`, adaptadores impulsados = FileSystem / motor PlantUML /
JGit):

- [README_truth.adoc](../README_truth.adoc) — Referencia AsciiDoc con 7
  diagramas PlantUML embebidos (problema-solución, fuente-de-verdad,
  visión-secuencia, actividad, componentes, diagrama-de-clases, hexagonal).
  Esta es la descripción arquitectónica canónica.
- [README_truth_fr.adoc](../README_truth_fr.adoc) — Traducción al francés de
  la referencia.

El directorio `.github/workflows/readmes/images/{en,fr}/` contiene los
artefactos PNG generados y confirmados por el propio pipeline del plugin
(7 diagramas × 2 idiomas).

## Colaborar

1. El build compila: `./gradlew -p readme-plugin build -x test`
2. Tests unitarios en verde: `./gradlew -p readme-plugin test`
3. Cucumber en verde: `./gradlew -p readme-plugin cucumberTest`
4. Tests funcionales en verde: `./gradlew -p readme-plugin functionalTest`
5. Sin secretos en `readme.yml` — está gitignored y se inyecta desde el
   secret de CI `README_GRADLE_PLUGIN`. Nunca confirmes un PAT real.
6. Sigue las convenciones hexagonales (tareas como adaptadores de
   conducción, modelo de dominio `AdocSourceFile` aislado de la
   infraestructura).

## Licencia

Apache License 2.0 — ver [Licencia](../LICENCE).

---

_Parte del ecosistema CCCP Education — `groupId: education.cccp`._