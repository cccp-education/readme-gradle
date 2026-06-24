<!-- translated from README.md rev 1 -->
# readme-gradle — Internes du Plugin

> Guide développeur & contributeur pour le plugin Gradle `readme-plugin`.

[![Maven Central](https://img.shields.io/maven-central/v/education.cccp/readme-plugin.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/education.cccp/readme-plugin)
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/education.cccp.readme.svg?label=Plugin%20Portal)](https://plugins.gradle.org/plugin/education.cccp.readme)
[![CI](https://img.shields.io/github/actions/workflow/status/cccp-education/readme-gradle/ci.yml?branch=main&label=CI)](https://github.com/cccp-education/readme-gradle/actions/workflows/ci.yml)
[![Licence](https://img.shields.io/github/license/cccp-education/readme-gradle?label=Licence)](../LICENCE)

- **Version** : `0.0.1` · **Groupe** : `education.cccp` · **ID Plugin** : `education.cccp.readme`
- **Toolchain** : Java 24 · Kotlin 2.3.20 · Gradle 9.5.1
- **Build** : `./gradlew -p readme-plugin build -x test` · **Tests** : `./gradlew -p readme-plugin check` · **Couverture** : non bloquante

🌐 Langues : [English](README.md) | **Français**

---

## Arborescence du module

```
readme-gradle/
├── build.gradle.kts                          # Harnais consommateur — applique education.cccp.readme
├── settings.gradle.kts                        # pluginManagement : mavenLocal + portal + central
├── README_truth.adoc                          # Source de vérité EN (référence parente de ce README)
├── README_truth_fr.adoc                       # Source de vérité FR
├── readme.yml                                 # gitignoré — injecté depuis le secret CI
└── readme-plugin/
    ├── build.gradle.kts                       # Module plugin (signing, java-gradle-plugin, nmcp)
    ├── gradle/libs.versions.toml              # Catalogue de versions
    └── src/
        ├── main/kotlin/readme/
        │   ├── ReadmePlugin.kt                # Point d'entrée du plugin — enregistre 3 tâches
        │   ├── ReadmePlantUmlConfig.kt       # Modèle config Jackson YAML (+ Source/Output/Git)
        │   ├── ScaffoldTask.kt               # generateReadme — scaffold readme.yml + workflow
        │   ├── ProcessReadmeTask.kt          # transformReadme — génération PNG + rewrite
        │   ├── CommitGeneratedReadmeTask.kt  # commitGeneratedReadme — JGit add/commit/push
        │   ├── AdocSourceFile.kt             # Modèle source de vérité + détection langue
        │   ├── GitUtils.kt                   # Résolution imgDir depuis la racine git
        │   ├── GitRemoteValidator.kt         # Interface de validation remote
        │   └── JGitRemoteValidator.kt        # Implémentation JGit
        ├── test/
        │   ├── kotlin/…                      # Tests unitaires JUnit5
        │   ├── scenarios/…                   # Définitions de pas Cucumber
        │   └── features/                      # 5 fichiers .feature (56 scénarios)
        │       ├── 1_minimal.feature
        │       ├── 2_scaffold.feature
        │       ├── 3_process.feature
        │       ├── 4_commit.feature
        │       └── 5_integration.feature
        └── functionalTest/kotlin/…           # Tests fonctionnels GradleRunner
```

## Contrats N0 (consommés via `education.cccp.codebase`)

Le plugin applique `education.cccp.codebase` version `0.0.2` (depuis
`libs.versions.toml`). Via codebase-gradle il obtient un accès transitif aux
contrats N0 partagés publiés par `workspace-bom` MEMPHIS :

| Contrat | Artefact | Fournit |
|---------|----------|---------|
| `codebase-contracts`          | `education.cccp:codebase-contracts`          | ContextChannel, ChannelBudget, CompositeContext |
| `agent-contracts`             | `education.cccp:agent-contracts`             | Epic, UserStory, GradleTask, AgentState |
| `llm-pool-contracts`          | `education.cccp:llm-pool-contracts`          | LlmInstancePool, LlmInstance, QuotaConfig |
| `opencode-session-contracts`  | `education.cccp:opencode-session-contracts`  | SessionPrompt, SessionResponse, AgentContext |
| `i18n-contracts`              | `education.cccp:i18n-contracts`              | SupportedLanguage, LanguageCatalog, I18nConfig |

> Note : `readme-gradle` applique `education.cccp.codebase` comme *plugin* (via
> l'alias du portail de plugins Gradle `alias(libs.plugins.codebase)`), pas
> comme dépendance `implementation`. Les artefacts N0 directs sont consommés
> transitivement par codebase-gradle lui-même.

## Dépendances clés

| Bibliothèque | Version | Rôle |
|---------------|---------|------|
| `net.sourceforge.plantuml:plantuml` | `1.2026.0` | Génération PNG depuis les blocs PlantUML |
| `org.eclipse.jgit:org.eclipse.jgit` | `7.5.0.202512021534-r` | Opérations Git sans binaire git système |
| `com.fasterxml.jackson.module:jackson-module-kotlin` | `2.21.1` | Mapping YAML → classe de données Kotlin |
| `com.fasterxml.jackson.dataformat:jackson-dataformat-yaml` | `2.21.1` | Parsing de `readme.yml` |
| `io.arrow-kt:arrow-core` | `2.2.2` | Types fonctionnels |
| `io.arrow-kt:arrow-fx-coroutines` | `2.2.2` | Effets basés sur les coroutines |
| `com.github.node-gradle:gradle-node-plugin` | `7.1.0` | Tooling Node (futur serve HTML) |
| `dev.langchain4j:langchain4j` (+ providers) | `1.14.1` / `1.14.1-beta24` | Providers LLM (compileOnly — bundle AI) |
| `org.asciidoctor:asciidoctor-gradle-jvm` | `5.0.0-alpha.1` | Tooling Asciidoctor (compileOnly) |

Stack de providers LLM (bundle `readme-ai`, `compileOnly`) :
`langchain4j-ollama`, `langchain4j-open-ai`, `langchain4j-google-ai-gemini`,
`langchain4j-mistral-ai`, `langchain4j-pgvector`, `langchain4j-embeddings-all-minilm-l6-v2`.

`koog-agents` **n'est pas** une dépendance directe de readme-gradle dans le
catalogue actuel — le bundle AI est déclaré `compileOnly` et fourni par le build
consommateur (ex. runtime de codebase-gradle).

## Matrice de tests

| Tâche | Portée | Notes |
|-------|--------|-------|
| `test`           | Tests unitaires JUnit5 | Exclut `readme.scenarios.**` et `readme.ReadmeGradlePluginFunctionalTests` |
| `cucumberTest`   | Cucumber BDD (5 features, 56 scénarios) | Utilise `cucumber-junit-platform-engine`, exclut le moteur `junit-jupiter` |
| `functionalTest`  | Tests fonctionnels GradleRunner | Source set séparé `functionalTest` enregistré avec `gradlePlugin.testSourceSets` pour `plugin-under-test-metadata.properties` |
| `check`          | Agrège `test` + `functionalTest` + `cucumberTest` | |

Versions du framework de tests (depuis `libs.versions.toml`) :
- JUnit Platform `1.14.3` · Cucumber `7.34.3` · AssertJ `3.27.7`
- Mockito Kotlin `6.2.3` · Mockito Jupiter `5.23.0`
- Kotlinx Coroutines `1.10.2` · Testcontainers PostgreSQL `1.21.4`

> `logback-classic` est **exclu** de `testRuntimeClasspath`,
> `testImplementation` et `functionalTest.runtimeClasspath` pour éviter les
> conflits de liaison SLF4J avec le logging propre à Gradle.

## Réglages JVM

Le build du plugin utilise `-XX:+EnableDynamicAgentLoading` sur toutes les tâches
`Test` (défini dans `tasks.withType<Test>`). Il n'y a pas de profil dédié
G1GC/SerialGC — ajustez `GRADLE_OPTS` si votre runner nécessite plus de heap :

```bash
export GRADLE_OPTS="-Xmx2g"
```

## Commandes de build

```bash
./gradlew -p readme-plugin build                            # build complet (compile + tous tests)
./gradlew -p readme-plugin build -x test                    # compile uniquement
./gradlew -p readme-plugin test                             # tests unitaires JUnit5
./gradlew -p readme-plugin cucumberTest                     # Cucumber BDD
./gradlew -p readme-plugin functionalTest                   # fonctionnels GradleRunner
./gradlew -p readme-plugin check                            # test + functionalTest + cucumberTest
./gradlew -p readme-plugin publishToMavenLocal              # publication locale
./gradlew -p readme-plugin publishAggregationToCentralPortal --no-daemon   # Maven Central (NMCP)
```

## Pipeline CI

Trois workflows dans `.github/workflows/` :

1. **`readme_action.yml`** — `Generate README from truth sources`. Se déclenche
   sur push vers n'importe quelle branche touchant `README_truth*.adoc`. Injecte
   le secret `README_GRADLE_PLUGIN`, exécute `./gradlew -p readme-plugin
   commitGeneratedReadme --no-daemon` (JGit commit + push). JDK 23 (Temurin).
2. **`ci.yml`** — `CI`. S'exécute après la complétion de `readme_action.yml`
   (via `workflow_run`) et sur push/PR en ignorant `README_truth*.adoc` et
   `*.md`. Exécute `./gradlew -p readme-plugin clean check --no-daemon`, upload
   les rapports de tests. JDK 23 (Temurin).
3. **`integration_test.yml`** — `Integration Test — commitGeneratedReadme`.
   Manuel uniquement. Exécute les scénarios Cucumber taggés `@integration` :
   `./gradlew -p readme-plugin cucumberTest -Dcucumber.filter.tags="@integration"`.

## Publication (NMCP)

- **Plugin Portal** : publié via `com.gradle.plugin-publish` `2.1.0`
  (`alias(libs.plugins.publish)`). Métadonnées plugin : `id = education.cccp.readme`,
  `implementationClass = readme.ReadmePlugin`, displayName `"README helper Plugin"`,
  tags `asciidoc`, `plantuml`, `readme`, `documentation`, `github`, `diagram`.
- **Maven Central** : `publishing { repositories { mavenCentral() } }` avec POM
  sur chaque `MavenPublication`. Signature via `signing { useGpgCmd() }` — ignoré
  quand `CI=true` ou version finissant par `-SNAPSHOT`. Développeur
  `cccp-education`, SCM pointant vers `github.com/cccp-education/readme-gradle`.
- La propriété projet `relocationGroup`, si définie, émet un bloc
  `<distributionManagement><relocation>` dans le POM pour les migrations groupId.
- `java { withJavadocJar(); withSourcesJar() }` — les deux jars packagés.
- La version est **hardcodée** dans le catalogue : `readme = "0.0.1"`. Le module
  la résout via `libs.plugins.readme.get().version`.

> Confirmer avant publication : toutes les dépendances `implementation` doivent
> être des versions releasées (pas de `-SNAPSHOT`), et tout transitive
> `education.cccp:*` doit déjà être sur Central.

## Documentation d'architecture

Le plugin suit une disposition hexagonale (adaptateurs pilotes = tâches Gradle,
cœur applicatif = `ReadmePlugin` + config, domaine = `AdocSourceFile`,
adaptateurs pilotés = FileSystem / moteur PlantUML / JGit) :

- [README_truth.adoc](../README_truth.adoc) — Référence AsciiDoc avec 7 diagrammes
  PlantUML embarqués (problem-solution, source-of-truth, overview-sequence,
  activity, components, class-diagram, hexagonal). Ceci est la description
  architecturale canonique.
- [README_truth_fr.adoc](../README_truth_fr.adoc) — Traduction française de la
  référence.

Le répertoire `.github/workflows/readmes/images/{en,fr}/` contient les artefacts
PNG générés commités par le propre pipeline du plugin (7 diagrammes × 2 langues).

## Contribuer

1. Le build compile : `./gradlew -p readme-plugin build -x test`
2. Tests unitaires verts : `./gradlew -p readme-plugin test`
3. Cucumber vert : `./gradlew -p readme-plugin cucumberTest`
4. Tests fonctionnels verts : `./gradlew -p readme-plugin functionalTest`
5. Aucun secret dans `readme.yml` — il est gitignoré et injecté depuis le secret
   CI `README_GRADLE_PLUGIN`. Ne jamais commiter un vrai PAT.
6. Suivre les conventions hexagonales (tâches comme adaptateurs pilotes, modèle
   domaine `AdocSourceFile` isolé de l'infrastructure).

## Licence

Licence Apache 2.0 — voir [LICENCE](../LICENCE).

---

_Partie de l'écosystème CCCP Education — `groupId: education.cccp`._