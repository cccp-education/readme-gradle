<!-- translated from README.md rev 0.0.1 -->
# readme-gradle — Внутреннее устройство плагина

> Руководство для разработчиков и контрибьюторов плагина Gradle
> `readme-plugin`.

[![Maven Central](https://img.shields.io/maven-central/v/education.cccp/readme-plugin.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/education.cccp/readme-plugin)
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/education.cccp.readme.svg?label=Plugin%20Portal)](https://plugins.gradle.org/plugin/education.cccp.readme)
[![CI](https://img.shields.io/github/actions/workflow/status/cccp-education/readme-gradle/ci.yml?branch=main&label=CI)](https://github.com/cccp-education/readme-gradle/actions/workflows/ci.yml)
[![License](https://img.shields.io/github/license/cccp-education/readme-gradle?label=License)](../LICENCE)

- **Версия**: `0.0.1` · **Group**: `education.cccp` · **Plugin ID**: `education.cccp.readme`
- **Инструментарий**: Java 24 · Kotlin 2.3.20 · Gradle 9.5.1
- **Сборка**: `./gradlew -p readme-plugin build -x test` · **Тесты**: `./gradlew -p readme-plugin check` · **Покрытие**: без порога

🌐 Languages: [English](README.md) | [中文](README.zh.md) | [हिन्दी](README.hi.md) | [Español](README.es.md) | [Français](README.fr.md) | [العربية](README.ar.md) | [বাংলা](README.bn.md) | [Português](README.pt.md) | **Русский** | [اردو](README.ur.md)

---

## Расположение модулей

```
readme-gradle/
├── build.gradle.kts                          # Харнес потребителя — применяет education.cccp.readme
├── settings.gradle.kts                        # pluginManagement: mavenLocal + portal + central
├── README_truth.adoc                          # EN источник истины (родительская ссылка этого README)
├── README_truth_fr.adoc                       # FR источник истины
├── readme.yml                                 # gitignored — внедряется из секрета CI
└── readme-plugin/
    ├── build.gradle.kts                       # Модуль плагина (signing, java-gradle-plugin, nmcp)
    ├── gradle/libs.versions.toml              # Каталог версий
    └── src/
        ├── main/kotlin/readme/
        │   ├── ReadmePlugin.kt                # Точка входа плагина — регистрирует 3 задачи
        │   ├── ReadmePlantUmlConfig.kt       # Модель конфига Jackson YAML (+ Source/Output/Git)
        │   ├── ScaffoldTask.kt               # generateReadme — каркас readme.yml + workflow
        │   ├── ProcessReadmeTask.kt          # transformReadme — ген PNG + перезапись блока
        │   ├── CommitGeneratedReadmeTask.kt  # commitGeneratedReadme — JGit add/commit/push
        │   ├── AdocSourceFile.kt             # Модель источника-истины + определение языка
        │   ├── GitUtils.kt                   # Разрешение imgDir из git-корня
        │   ├── GitRemoteValidator.kt         # Интерфейс удалённой валидации
        │   └── JGitRemoteValidator.kt        # Реализация JGit
        ├── test/
        │   ├── kotlin/…                      # Модульные тесты JUnit5
        │   ├── scenarios/…                   # Определения шагов Cucumber
        │   └── features/                      # 5 файлов .feature (56 сценариев)
        │       ├── 1_minimal.feature
        │       ├── 2_scaffold.feature
        │       ├── 3_process.feature
        │       ├── 4_commit.feature
        │       └── 5_integration.feature
        └── functionalTest/kotlin/…           # Функциональные тесты GradleRunner
```

## Контракты N0 (потребляются через `education.cccp.codebase`)

Плагин применяет `education.cccp.codebase` версии `0.0.2` (из
`libs.versions.toml`). Через codebase-gradle он получает транзитный доступ
к общим контрактам N0, опубликованным `workspace-bom` MEMPHIS:

| Контракт | Артефакт | Предоставляет |
|----------|----------|----------|
| `codebase-contracts`          | `education.cccp:codebase-contracts`          | ContextChannel, ChannelBudget, CompositeContext |
| `agent-contracts`             | `education.cccp:agent-contracts`             | Epic, UserStory, GradleTask, AgentState |
| `llm-pool-contracts`          | `education.cccp:llm-pool-contracts`          | LlmInstancePool, LlmInstance, QuotaConfig |
| `opencode-session-contracts`  | `education.cccp:opencode-session-contracts`  | SessionPrompt, SessionResponse, AgentContext |
| `i18n-contracts`              | `education.cccp:i18n-contracts`              | SupportedLanguage, LanguageCatalog, I18nConfig |

> Примечание: `readme-gradle` применяет `education.cccp.codebase` как
> *plugin* (через алиас портала плагинов Gradle `alias(libs.plugins.codebase)`),
> не как зависимость `implementation`. Прямые артефакты контрактов N0
> потребляются транзитно самим codebase-gradle.

## Ключевые зависимости

| Библиотека | Версия | Роль |
|---------|---------|------|
| `net.sourceforge.plantuml:plantuml` | `1.2026.0` | Генерация PNG из блоков PlantUML |
| `org.eclipse.jgit:org.eclipse.jgit` | `7.5.0.202512021534-r` | Git-операции без системного двоичного git |
| `com.fasterxml.jackson.module:jackson-module-kotlin` | `2.21.1` | Маппинг YAML → Kotlin data class |
| `com.fasterxml.jackson.dataformat:jackson-dataformat-yaml` | `2.21.1` | Разбор `readme.yml` |
| `io.arrow-kt:arrow-core` | `2.2.2` | Функциональные типы |
| `io.arrow-kt:arrow-fx-coroutines` | `2.2.2` | Эффекты на корутинах |
| `com.github.node-gradle:gradle-node-plugin` | `7.1.0` | Node-инструменты (будущий HTML serve) |
| `dev.langchain4j:langchain4j` (+ providers) | `1.14.1` / `1.14.1-beta24` | Провайдеры LLM (compileOnly — AI bundle) |
| `org.asciidoctor:asciidoctor-gradle-jvm` | `5.0.0-alpha.1` | Инструменты Asciidoctor (compileOnly) |

Стек провайдеров LLM (bundle `readme-ai`, `compileOnly`):
`langchain4j-ollama`, `langchain4j-open-ai`, `langchain4j-google-ai-gemini`,
`langchain4j-mistral-ai`, `langchain4j-pgvector`, `langchain4j-embeddings-all-minilm-l6-v2`.

`koog-agents` **не** является прямой зависимостью readme-gradle в текущем
каталоге — AI-bundle объявлен `compileOnly` и предоставляется потребляющим
сборкой (например, рантаймом codebase-gradle).

## Матрица тестов

| Задача | Область | Примечания |
|------|-------|-------|
| `test`           | Модульные тесты JUnit5 | Исключает `readme.scenarios.**` и `readme.ReadmeGradlePluginFunctionalTests` |
| `cucumberTest`   | Cucumber BDD (5 фич, 56 сценариев) | Использует `cucumber-junit-platform-engine`, исключает движок `junit-jupiter` |
| `functionalTest`  | Функциональные тесты GradleRunner | Отдельный source set `functionalTest`, зарегистрированный в `gradlePlugin.testSourceSets` для `plugin-under-test-metadata.properties` |
| `check`          | Агрегирует `test` + `functionalTest` + `cucumberTest` | |

Версии тестового фреймворка (из `libs.versions.toml`):
- JUnit Platform `1.14.3` · Cucumber `7.34.3` · AssertJ `3.27.7`
- Mockito Kotlin `6.2.3` · Mockito Jupiter `5.23.0`
- Kotlinx Coroutines `1.10.2` · Testcontainers PostgreSQL `1.21.4`

> `logback-classic` **исключён** из `testRuntimeClasspath`,
> `testImplementation` и `functionalTest.runtimeClasspath`, чтобы избежать
> конфликтов привязки SLF4J с собственным логированием Gradle.

## Настройка JVM

Сборка плагина использует `-XX:+EnableDynamicAgentLoading` на всех задачах
`Test` (задано в `tasks.withType<Test>`). Специального профиля разделения
G1GC/SerialGC нет — настройте `GRADLE_OPTS`, если вашему runner-у нужен
больший heap:

```bash
export GRADLE_OPTS="-Xmx2g"
```

## Команды сборки

```bash
./gradlew -p readme-plugin build                            # полная сборка (compile + все тесты)
./gradlew -p readme-plugin build -x test                    # только compile
./gradlew -p readme-plugin test                             # модульные тесты JUnit5
./gradlew -p readme-plugin cucumberTest                     # Cucumber BDD
./gradlew -p readme-plugin functionalTest                   # функциональные GradleRunner
./gradlew -p readme-plugin check                            # test + functionalTest + cucumberTest
./gradlew -p readme-plugin publishToMavenLocal              # локальная публикация
./gradlew -p readme-plugin publishAggregationToCentralPortal --no-daemon   # Maven Central (NMCP)
```

## CI-конвейер

Три workflow находятся в `.github/workflows/`:

1. **`readme_action.yml`** — `Сгенерировать README из источников истины`.
   Срабатывает при пуше в любую ветку, затрагивающую `README_truth*.adoc`.
   Внедряет секрет `README_GRADLE_PLUGIN`, запускает `./gradlew -p readme-plugin
   commitGeneratedReadme --no-daemon` (JGit commit + push). JDK 23 (Temurin).
2. **`ci.yml`** — `CI`. Запускается после завершения `readme_action.yml`
   (через `workflow_run`) и при пуше/PR, игнорируя `README_truth*.adoc` и
   `*.md`. Запускает `./gradlew -p readme-plugin clean check --no-daemon`,
   загружает отчёты тестов. JDK 23 (Temurin).
3. **`integration_test.yml`** — `Интеграционный тест — commitGeneratedReadme`.
   Только ручной запуск. Запускает сценарии Cucumber с тегом `@integration`:
   `./gradlew -p readme-plugin cucumberTest -Dcucumber.filter.tags="@integration"`.

## Публикация (NMCP)

- **Plugin Portal**: публикуется через `com.gradle.plugin-publish` `2.1.0`
  (`alias(libs.plugins.publish)`). Метаданные плагина:
  `id = education.cccp.readme`, `implementationClass = readme.ReadmePlugin`,
  displayName `"README helper Plugin"`, теги `asciidoc`, `plantuml`,
  `readme`, `documentation`, `github`, `diagram`.
- **Maven Central**: `publishing { repositories { mavenCentral() } }` с POM
  для каждого `MavenPublication`. Подпись через `signing { useGpgCmd() }` —
  пропускается, когда `CI=true` или версия оканчивается на `-SNAPSHOT`.
  Developer `cccp-education`, SCM указывает на
  `github.com/cccp-education/readme-gradle`.
- Свойство проекта `relocationGroup`, если задано, порождает блок
  `<distributionManagement><relocation>` в POM для миграций groupId.
- `java { withJavadocJar(); withSourcesJar() }` — оба jar упакованы.
- Версия **хардкодится** в каталоге: `readme = "0.0.1"`. Модуль разрешает её
  через `libs.plugins.readme.get().version`.

> Подтвердите перед публикацией: все зависимости `implementation` должны быть
> released-версиями (без `-SNAPSHOT`), а любая транзитная `education.cccp:*`
> должна уже быть на Central.

## Архитектурные документы

Плагин следует гексагональной раскладке (управляющие адаптеры = задачи
Gradle, ядро приложения = `ReadmePlugin` + конфиг, домен =
`AdocSourceFile`, управляемые адаптеры = FileSystem / движок PlantUML /
JGit):

- [README_truth.adoc](../README_truth.adoc) — AsciiDoc-справочник с 7
  встроенными PlantUML-диаграммами (проблема-решение, источник-истины,
  обзор-последовательность, деятельность, компоненты, диаграмма-классов,
  гексагон). Это каноническое архитектурное описание.
- [README_truth_fr.adoc](../README_truth_fr.adoc) — Французский перевод
  справочника.

Каталог `.github/workflows/readmes/images/{en,fr}/` содержит сгенерированные
PNG-артефакты, зафиксированные собственным конвейером плагина (7 диаграмм ×
2 языка).

## Контрибьютинг

1. Сборка компилируется: `./gradlew -p readme-plugin build -x test`
2. Модульные тесты зелёные: `./gradlew -p readme-plugin test`
3. Cucumber зелёные: `./gradlew -p readme-plugin cucumberTest`
4. Функциональные тесты зелёные: `./gradlew -p readme-plugin functionalTest`
5. Без секретов в `readme.yml` — он gitignored и внедряется из CI-секрета
   `README_GRADLE_PLUGIN`. Никогда не фиксируйте реальный PAT.
6. Следуйте гексагональным соглашениям (задачи как управляющие адаптеры,
   доменная модель `AdocSourceFile` изолирована от инфраструктуры).

## Лицензия

Apache License 2.0 — см. [Лицензия](../LICENCE)。

---

_Часть экосистемы CCCP Education — `groupId: education.cccp`._