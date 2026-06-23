<!-- translated from README.md rev 0.0.1 -->
# readme-gradle — Руководство потребителя

> Плагин Gradle, генерирующий совместимые с GitHub файлы `README.adoc` из
> исходных файлов `README_truth.adoc`, с рендерингом нативных блоков
> `[plantuml]` в зафиксированные PNG-изображения через GitHub Actions и
> JGit.

[![Maven Central](https://img.shields.io/maven-central/v/education.cccp/readme-plugin.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/education.cccp/readme-plugin)
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/education.cccp.readme.svg?label=Plugin%20Portal)](https://plugins.gradle.org/plugin/education.cccp.readme)
[![CI](https://img.shields.io/github/actions/workflow/status/cccp-education/readme-gradle/ci.yml?branch=main&label=CI)](https://github.com/cccp-education/readme-gradle/actions/workflows/ci.yml)
[![License](https://img.shields.io/github/license/cccp-education/readme-gradle?label=License)](../LICENCE)

- **Версия**: `0.0.1` · **Group**: `education.cccp` · **Plugin ID**: `education.cccp.readme`
- **Инструментарий**: Java 24 · Kotlin 2.3.20 · Gradle 9.5.1
- **Сборка**: `./gradlew build` · **Тесты**: `./gradlew check` (JUnit5 + 56 сценариев Cucumber в 5 функциях)
- **Покрытие**: без порога (правило Kover не подключено к `check`)

🌐 Languages: [English](README.md) | [中文](README.zh.md) | [हिन्दी](README.hi.md) | [Español](README.es.md) | [Français](README.fr.md) | [العربية](README.ar.md) | [বাংলা](README.bn.md) | [Português](README.pt.md) | **Русский** | [اردو](README.ur.md)

---

## Что он делает

GitHub нативно не рендерит синтаксис PlantUML, встроенный в файлы
AsciiDoc. `readme-gradle` автоматизирует обходной путь: вы редактируете
единый источник истины (`README_truth.adoc`), плагин извлекает каждый
блок `[plantuml, name, png]`, генерирует PNG-изображения, переписывает
блок как ссылку `image::`, и фиксирует как переписанный `README.adoc`,
так и изображения обратно в ваш репозиторий через JGit — системный
двоичный файл `git` на CI-раннере не требуется.

```
README_truth.adoc → generateReadme → transformReadme → commitGeneratedReadme → README.adoc + images/
                    (scaffolds yml)  (PNG + rewrite)    (JGit add + commit + push)
```

## Быстрый старт

### 1. Примените плагин

```gradle
plugins {
    id("education.cccp.readme") version "0.0.1"
}
```

### 2. Создайте источник истины

Создайте `README_truth.adoc` в корне проекта. Используйте нативные блоки
PlantUML:

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

Для французского варианта назовите файл `README_plantuml_fr.adoc` —
суффикс `_fr` распознаётся и создаёт `README_fr.adoc`.

### 3. Настройка через `readme.yml`

Плагин читает `readme.yml` из корня проекта. **Никогда не фиксируйте
реальный токен** — храните полный файл (включая токен) в секрете GitHub
`README_GRADLE_PLUGIN` и внедряйте его в CI:

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

Шаг CI:

```yaml
- name: Inject plugin config
  run: echo "${{ secrets.README_GRADLE_PLUGIN }}" > readme.yml

- name: Generate README and commit via JGit
  run: ./gradlew -p readme-plugin commitGeneratedReadme --no-daemon
```

### 4. Запустите конвейер

```bash
./gradlew commitGeneratedReadme          # полный конвейер (CI)
./gradlew transformReadme               # только генерация PNG + перезапись
./gradlew generateReadme                 # каркас readme.yml + workflow если отсутствуют
```

## Доступные задачи

| Задача | Группа | Описание |
|------|-------|-------------|
| `generateReadme`         | generate  | Создаёт `readme.yml` и `.github/workflows/readme_action.yml` при отсутствии |
| `transformReadme`        | transform | Генерирует PNG и переписывает `README*.adoc` из `README_truth*.adoc` (зависит от `generateReadme`) |
| `commitGeneratedReadme`  | deploy    | Фиксирует и отправляет сгенерированные `README*.adoc` + изображения через JGit (только CI, зависит от `transformReadme`) |

## DSL расширения

Плагин **не имеет блока расширения Gradle**. Вся конфигурация читается из
файла `readme.yml` в корне проекта (разбирается Jackson YAML в
`ReadmePlantUmlConfig`). Если `readme.yml` отсутствует или пуст,
молчаливо используются разумные значения по умолчанию.

| Секция конфиг | Поле | По умолчанию |
|----------------|-------|---------|
| `source`  | `dir`         | `"."` |
| `source`  | `defaultLang` | `"en"` |
| `output`  | `imgDir`      | `".github/workflows/readmes/images"` |
| `git`     | `userName`        | `"github-actions[bot]"` |
| `git`     | `userEmail`       | `"github-actions[bot]@users.noreply.github.com"` |
| `git`     | `commitMessage`   | `"chore: generate readme [skip ci]"` |
| `git`     | `token`           | `""` (должен быть задан — ошибка во время выполнения если пуст/placeholder) |
| `git`     | `watchedBranches`  | `["main", "master"]` |

## Предварительные требования

- **Java** 24+ (инструментарий Kotlin 2.3.20)
- **Gradle** 9.5.1+
- **GitHub** репозиторий с доступом на запись (для JGit push)
- **секрет GitHub** `README_GRADLE_PLUGIN` содержащий полный `readme.yml` с валидным PAT

## Сборка и тестирование

```bash
./gradlew -p readme-plugin build              # полная сборка (compile + unit + cucumber + functional)
./gradlew -p readme-plugin test               # только модульные тесты JUnit5
./gradlew -p readme-plugin cucumberTest       # Cucumber BDD (5 файлов feature, 56 сценариев)
./gradlew -p readme-plugin functionalTest     # функциональные тесты GradleRunner
./gradlew -p readme-plugin check              # test + functionalTest + cucumberTest
./gradlew -p readme-plugin publishToMavenLocal # локальная публикация
```

## Устранение неполадок

| Симптом | Исправление |
|---------|-----|
| `GitHub token is empty or still a placeholder in readme.yml` | Установите секрет `README_GRADLE_PLUGIN`; убедитесь что CI внедряет его до запуска задачи |
| PNG не сгенерирован, README не изменился | Подтвердите наличие `README_truth*.adoc` в `source.dir`; проверьте синтаксис блока `[plantuml, name, png]` |
| JGit push отклонён | Ветка должна быть в `watchedBranches` (`main`/`master` по умолчанию); проверьте что PAT имеет `contents: write` |
| `Java heap space` | `export GRADLE_OPTS="-Xmx2g"` |
| Конфликт SLF4J с Logback | Сборка автоматически исключает `logback-classic` из тестовых classpath — не добавляйте повторно |

См. [README_truth.adoc](../README_truth.adoc) для полного AsciiDoc-справочника.

## Лицензия

Apache License 2.0 — см. [Лицензия](../LICENCE).

---

_Часть экосистемы CCCP Education — `groupId: education.cccp`._