<!-- translated from README.md rev 0.0.1 -->
# readme-gradle — Interior do plugin

> Guia para desenvolvedores e colaboradores do plugin Gradle
> `readme-plugin`.

[![Maven Central](https://img.shields.io/maven-central/v/education.cccp/readme-plugin.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/education.cccp/readme-plugin)
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/education.cccp.readme.svg?label=Plugin%20Portal)](https://plugins.gradle.org/plugin/education.cccp.readme)
[![CI](https://img.shields.io/github/actions/workflow/status/cccp-education/readme-gradle/ci.yml?branch=main&label=CI)](https://github.com/cccp-education/readme-gradle/actions/workflows/ci.yml)
[![License](https://img.shields.io/github/license/cccp-education/readme-gradle?label=License)](../LICENCE)

- **Versão**: `0.0.1` · **Group**: `education.cccp` · **Plugin ID**: `education.cccp.readme`
- **Toolchain**: Java 24 · Kotlin 2.3.20 · Gradle 9.5.1
- **Build**: `./gradlew -p readme-plugin build -x test` · **Testes**: `./gradlew -p readme-plugin check` · **Cobertura**: sem portão

🌐 Languages: [English](README.md) | [中文](README.zh.md) | [हिन्दी](README.hi.md) | [Español](README.es.md) | [Français](README.fr.md) | [العربية](README.ar.md) | [বাংলা](README.bn.md) | **Português** | [Русский](README.ru.md) | [اردو](README.ur.md)

---

## Disposição de módulos

```
readme-gradle/
├── build.gradle.kts                          # Harness do consumidor — aplica education.cccp.readme
├── settings.gradle.kts                        # pluginManagement: mavenLocal + portal + central
├── README_truth.adoc                          # Fonte de verdade EN (referência pai deste README)
├── README_truth_fr.adoc                       # Fonte de verdade FR
├── readme.yml                                 # gitignored — injetado do secret de CI
└── readme-plugin/
    ├── build.gradle.kts                       # Módulo do plugin (signing, java-gradle-plugin, nmcp)
    ├── gradle/libs.versions.toml              # Catálogo de versões
    └── src/
        ├── main/kotlin/readme/
        │   ├── ReadmePlugin.kt                # Ponto de entrada do plugin — registra 3 tarefas
        │   ├── ReadmePlantUmlConfig.kt       # Modelo de config Jackson YAML (+ Source/Output/Git)
        │   ├── ScaffoldTask.kt               # generateReadme — scaffolds readme.yml + workflow
        │   ├── ProcessReadmeTask.kt          # transformReadme — geração PNG + reescrita de bloco
        │   ├── CommitGeneratedReadmeTask.kt  # commitGeneratedReadme — JGit add/commit/push
        │   ├── AdocSourceFile.kt             # Modelo fonte-da-verdade + detecção de idioma
        │   ├── GitUtils.kt                   # Resolução de imgDir a partir da raiz git
        │   ├── GitRemoteValidator.kt         # Interface para validação remota
        │   └── JGitRemoteValidator.kt        # Implementação JGit
        ├── test/
        │   ├── kotlin/…                      # Testes unitários JUnit5
        │   ├── scenarios/…                   # Definições de passos Cucumber
        │   └── features/                      # 5 arquivos .feature (56 cenários)
        │       ├── 1_minimal.feature
        │       ├── 2_scaffold.feature
        │       ├── 3_process.feature
        │       ├── 4_commit.feature
        │       └── 5_integration.feature
        └── functionalTest/kotlin/…           # Testes funcionais GradleRunner
```

## Contratos N0 (consumidos via `education.cccp.codebase`)

O plugin aplica `education.cccp.codebase` versão `0.0.2` (de
`libs.versions.toml`). Através do codebase-gradle ganha acesso transitivo
aos contratos N0 compartilhados publicados pelo `workspace-bom` MEMPHIS:

| Contrato | Artefato | Fornece |
|----------|----------|----------|
| `codebase-contracts`          | `education.cccp:codebase-contracts`          | ContextChannel, ChannelBudget, CompositeContext |
| `agent-contracts`             | `education.cccp:agent-contracts`             | Epic, UserStory, GradleTask, AgentState |
| `llm-pool-contracts`          | `education.cccp:llm-pool-contracts`          | LlmInstancePool, LlmInstance, QuotaConfig |
| `opencode-session-contracts`  | `education.cccp:opencode-session-contracts`  | SessionPrompt, SessionResponse, AgentContext |
| `i18n-contracts`              | `education.cccp:i18n-contracts`              | SupportedLanguage, LanguageCatalog, I18nConfig |

> Nota: o `readme-gradle` aplica `education.cccp.codebase` como *plugin*
> (via o alias do portal de plugins do Gradle `alias(libs.plugins.codebase)`),
> não como dependência `implementation`. Artefatos de contrato N0 diretos
> são consumidos transitivamente pelo próprio codebase-gradle.

## Dependências principais

| Biblioteca | Versão | Papel |
|---------|---------|------|
| `net.sourceforge.plantuml:plantuml` | `1.2026.0` | Geração de PNG a partir de blocos PlantUML |
| `org.eclipse.jgit:org.eclipse.jgit` | `7.5.0.202512021534-r` | Operações Git sem binário git do sistema |
| `com.fasterxml.jackson.module:jackson-module-kotlin` | `2.21.1` | Mapeamento YAML → Kotlin data class |
| `com.fasterxml.jackson.dataformat:jackson-dataformat-yaml` | `2.21.1` | Parse de `readme.yml` |
| `io.arrow-kt:arrow-core` | `2.2.2` | Tipos funcionais |
| `io.arrow-kt:arrow-fx-coroutines` | `2.2.2` | Efeitos baseados em corrotinas |
| `com.github.node-gradle:gradle-node-plugin` | `7.1.0` | Tooling de Node (futuro serve HTML) |
| `dev.langchain4j:langchain4j` (+ providers) | `1.14.1` / `1.14.1-beta24` | Provedores LLM (compileOnly — AI bundle) |
| `org.asciidoctor:asciidoctor-gradle-jvm` | `5.0.0-alpha.1` | Tooling Asciidoctor (compileOnly) |

Stack de provedores LLM (bundle `readme-ai`, `compileOnly`):
`langchain4j-ollama`, `langchain4j-open-ai`, `langchain4j-google-ai-gemini`,
`langchain4j-mistral-ai`, `langchain4j-pgvector`, `langchain4j-embeddings-all-minilm-l6-v2`.

`koog-agents` **não** é uma dependência direta do readme-gradle no catálogo
atual — o bundle de IA é declarado `compileOnly` e fornecido pelo build
consumidor (ex.: o runtime do codebase-gradle).

## Matriz de testes

| Tarefa | Escopo | Notas |
|------|-------|-------|
| `test`           | Testes unitários JUnit5 | Exclui `readme.scenarios.**` e `readme.ReadmeGradlePluginFunctionalTests` |
| `cucumberTest`   | Cucumber BDD (5 features, 56 cenários) | Usa `cucumber-junit-platform-engine`, exclui o motor `junit-jupiter` |
| `functionalTest`  | Testes funcionais GradleRunner | Source set separado `functionalTest` registrado com `gradlePlugin.testSourceSets` para `plugin-under-test-metadata.properties` |
| `check`          | Agrega `test` + `functionalTest` + `cucumberTest` | |

Versões do framework de testes (de `libs.versions.toml`):
- JUnit Platform `1.14.3` · Cucumber `7.34.3` · AssertJ `3.27.7`
- Mockito Kotlin `6.2.3` · Mockito Jupiter `5.23.0`
- Kotlinx Coroutines `1.10.2` · Testcontainers PostgreSQL `1.21.4`

> `logback-classic` é **excluído** de `testRuntimeClasspath`,
> `testImplementation` e `functionalTest.runtimeClasspath` para evitar
> conflitos de binding SLF4J com o logging próprio do Gradle.

## Ajuste JVM

O build do plugin usa `-XX:+EnableDynamicAgentLoading` em todas as tarefas
`Test` (definido em `tasks.withType<Test>`). Não há um perfil sob medida de
divisão G1GC/SerialGC — ajuste `GRADLE_OPTS` se seu runner precisar de mais
heap:

```bash
export GRADLE_OPTS="-Xmx2g"
```

## Comandos de build

```bash
./gradlew -p readme-plugin build                            # build completo (compile + todos os testes)
./gradlew -p readme-plugin build -x test                    # apenas compile
./gradlew -p readme-plugin test                             # testes unitários JUnit5
./gradlew -p readme-plugin cucumberTest                     # Cucumber BDD
./gradlew -p readme-plugin functionalTest                   # funcional GradleRunner
./gradlew -p readme-plugin check                            # test + functionalTest + cucumberTest
./gradlew -p readme-plugin publishToMavenLocal              # publicação local
./gradlew -p readme-plugin publishAggregationToCentralPortal --no-daemon   # Maven Central (NMCP)
```

## Pipeline CI

Três workflows vivem em `.github/workflows/`:

1. **`readme_action.yml`** — `Gerar README a partir de fontes de verdade`.
   Dispara ao fazer push em qualquer branch que toque `README_truth*.adoc`.
   Injeta o secret `README_GRADLE_PLUGIN`, executa `./gradlew -p readme-plugin
   commitGeneratedReadme --no-daemon` (JGit commit + push). JDK 23 (Temurin).
2. **`ci.yml`** — `CI`. Executa após `readme_action.yml` completar (via
   `workflow_run`) e em push/PR ignorando `README_truth*.adoc` e `*.md`.
   Executa `./gradlew -p readme-plugin clean check --no-daemon`, sobe os
   relatórios de teste. JDK 23 (Temurin).
3. **`integration_test.yml`** — `Integration Test — commitGeneratedReadme`.
   Apenas dispatch manual. Executa cenários Cucumber marcados `@integration`:
   `./gradlew -p readme-plugin cucumberTest -Dcucumber.filter.tags="@integration"`.

## Publicação (NMCP)

- **Plugin Portal**: publicado via `com.gradle.plugin-publish` `2.1.0`
  (`alias(libs.plugins.publish)`). Metadados do plugin:
  `id = education.cccp.readme`, `implementationClass = readme.ReadmePlugin`,
  displayName `"README helper Plugin"`, tags `asciidoc`, `plantuml`,
  `readme`, `documentation`, `github`, `diagram`.
- **Maven Central**: `publishing { repositories { mavenCentral() } }` com POM
  em cada `MavenPublication`. Assinatura via `signing { useGpgCmd() }` —
  ignorada quando `CI=true` ou a versão termina em `-SNAPSHOT`. Developer
  `cccp-education`, SCM apontando para `github.com/cccp-education/readme-gradle`.
- A propriedade de projeto `relocationGroup`, se definida, emite um bloco
  `<distributionManagement><relocation>` no POM para migrações de groupId.
- `java { withJavadocJar(); withSourcesJar() }` — ambos jars empacotados.
- A versão está **hardcoded** no catálogo: `readme = "0.0.1"`. O módulo a
  resolve via `libs.plugins.readme.get().version`.

> Confirme antes de publicar: todas as dependências `implementation` devem
> ser versões released (sem `-SNAPSHOT`), e qualquer `education.cccp:*`
> transitivo já deve estar no Central.

## Documentos de arquitetura

O plugin segue um layout hexagonal (adaptadores de condução = tarefas
Gradle, núcleo de aplicação = `ReadmePlugin` + config, domínio =
`AdocSourceFile`, adaptadores conduzidos = FileSystem / motor PlantUML /
JGit):

- [README_truth.adoc](../README_truth.adoc) — Referência AsciiDoc com 7
  diagramas PlantUML embutidos (problema-solução, fonte-da-verdade,
  visão-sequência, atividade, componentes, diagrama-de-classes, hexagonal).
  Esta é a descrição arquitetônica canônica.
- [README_truth_fr.adoc](../README_truth_fr.adoc) — Tradução em francês da
  referência.

O diretório `.github/workflows/readmes/images/{en,fr}/` contém os artefatos
PNG gerados e confirmados pelo próprio pipeline do plugin (7 diagramas ×
2 idiomas).

## Contribuindo

1. O build compila: `./gradlew -p readme-plugin build -x test`
2. Testes unitários verdes: `./gradlew -p readme-plugin test`
3. Cucumber verde: `./gradlew -p readme-plugin cucumberTest`
4. Testes funcionais verdes: `./gradlew -p readme-plugin functionalTest`
5. Sem secrets em `readme.yml` — ele está gitignored e é injetado do
   secret de CI `README_GRADLE_PLUGIN`. Nunca confirme um PAT real.
6. Siga as convenções hexagonais (tarefas como adaptadores de condução,
   modelo de domínio `AdocSourceFile` isolado da infraestrutura).

## Licença

Apache License 2.0 — ver [Licença](../LICENCE)。

---

_Parte do ecossistema CCCP Education — `groupId: education.cccp`._