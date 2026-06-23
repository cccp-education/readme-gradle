<!-- translated from README.md rev 0.0.1 -->
# readme-gradle — Guía del consumidor

> Plugin de Gradle que genera archivos `README.adoc` compatibles con GitHub
> a partir de archivos fuente `README_truth.adoc`, renderizando los bloques
> nativos `[plantuml]` en imágenes PNG confirmadas mediante GitHub Actions
> y JGit.

[![Maven Central](https://img.shields.io/maven-central/v/education.cccp/readme-plugin.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/education.cccp/readme-plugin)
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/education.cccp.readme.svg?label=Plugin%20Portal)](https://plugins.gradle.org/plugin/education.cccp.readme)
[![CI](https://img.shields.io/github/actions/workflow/status/cccp-education/readme-gradle/ci.yml?branch=main&label=CI)](https://github.com/cccp-education/readme-gradle/actions/workflows/ci.yml)
[![License](https://img.shields.io/github/license/cccp-education/readme-gradle?label=License)](../LICENCE)

- **Versión**: `0.0.1` · **Group**: `education.cccp` · **Plugin ID**: `education.cccp.readme`
- **Toolchain**: Java 24 · Kotlin 2.3.20 · Gradle 9.5.1
- **Build**: `./gradlew build` · **Tests**: `./gradlew check` (JUnit5 + 56 escenarios Cucumber en 5 características)
- **Cobertura**: sin umbral (ninguna regla Kover conectada a `check`)

🌐 Languages: [English](README.md) | [中文](README.zh.md) | [हिन्दी](README.hi.md) | **Español** | [Français](README.fr.md) | [العربية](README.ar.md) | [বাংলা](README.bn.md) | [Português](README.pt.md) | [Русский](README.ru.md) | [اردو](README.ur.md)

---

## Qué hace

GitHub no renderiza nativamente la sintaxis PlantUML embebida en archivos
AsciiDoc. `readme-gradle` automatiza la solución alternativa: editas una
única fuente de verdad (`README_truth.adoc`), el plugin extrae cada bloque
`[plantuml, name, png]`, genera imágenes PNG, reescribe el bloque como
referencia `image::`, y confirma tanto el `README.adoc` reescrito como las
imágenes de vuelta a tu repositorio mediante JGit — no se requiere binario
`git` del sistema en el runner de CI.

```
README_truth.adoc → generateReadme → transformReadme → commitGeneratedReadme → README.adoc + images/
                    (scaffolds yml)  (PNG + rewrite)    (JGit add + commit + push)
```

## Inicio rápido

### 1. Aplica el plugin

```gradle
plugins {
    id("education.cccp.readme") version "0.0.1"
}
```

### 2. Crea tu fuente de verdad

Crea `README_truth.adoc` en la raíz del proyecto. Usa bloques PlantUML
nativos:

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

Para una variante en francés, nombra el archivo `README_plantuml_fr.adoc`
— el sufijo `_fr` se detecta y produce `README_fr.adoc`.

### 3. Configura mediante `readme.yml`

El plugin lee `readme.yml` desde la raíz del proyecto. **Nunca confirmes un
token real** — almacena el archivo completo (token incluido) en el secret
de GitHub `README_GRADLE_PLUGIN` e inyéctalo en CI:

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

Paso de CI:

```yaml
- name: Inject plugin config
  run: echo "${{ secrets.README_GRADLE_PLUGIN }}" > readme.yml

- name: Generate README and commit via JGit
  run: ./gradlew -p readme-plugin commitGeneratedReadme --no-daemon
```

### 4. Ejecuta el pipeline

```bash
./gradlew commitGeneratedReadme          # pipeline completo (CI)
./gradlew transformReadme               # solo generación PNG + reescritura
./gradlew generateReadme                 # scaffold readme.yml + workflow si faltan
```

## Tareas disponibles

| Tarea | Grupo | Descripción |
|------|-------|-------------|
| `generateReadme`         | generate  | Crea `readme.yml` y `.github/workflows/readme_action.yml` si no existen |
| `transformReadme`        | transform | Genera PNG y reescribe `README*.adoc` desde `README_truth*.adoc` (depende de `generateReadme`) |
| `commitGeneratedReadme`  | deploy    | Confirma y empuja los `README*.adoc` + imágenes generados vía JGit (solo CI, depende de `transformReadme`) |

## DSL de extensión

El plugin **no tiene bloque de extensión de Gradle**. Toda la configuración
se lee del archivo `readme.yml` en la raíz del proyecto (parseado por
Jackson YAML a `ReadmePlantUmlConfig`). Si `readme.yml` falta o está vacío,
se usan valores por defecto razonables de forma silenciosa.

| Sección config | Campo | Por defecto |
|----------------|-------|---------|
| `source`  | `dir`         | `"."` |
| `source`  | `defaultLang` | `"en"` |
| `output`  | `imgDir`      | `".github/workflows/readmes/images"` |
| `git`     | `userName`        | `"github-actions[bot]"` |
| `git`     | `userEmail`       | `"github-actions[bot]@users.noreply.github.com"` |
| `git`     | `commitMessage`   | `"chore: generate readme [skip ci]"` |
| `git`     | `token`           | `""` (debe establecerse — error en tiempo de ejecución si está vacío/placeholder) |
| `git`     | `watchedBranches`  | `["main", "master"]` |

## Requisitos previos

- **Java** 24+ (toolchain Kotlin 2.3.20)
- **Gradle** 9.5.1+
- **GitHub** repositorio con acceso de escritura (para JGit push)
- **GitHub secret** `README_GRADLE_PLUGIN` que contenga el `readme.yml` completo con un PAT válido

## Build y test

```bash
./gradlew -p readme-plugin build              # build completo (compile + unit + cucumber + functional)
./gradlew -p readme-plugin test               # solo tests unitarios JUnit5
./gradlew -p readme-plugin cucumberTest       # Cucumber BDD (5 archivos feature, 56 escenarios)
./gradlew -p readme-plugin functionalTest     # tests funcionales GradleRunner
./gradlew -p readme-plugin check              # test + functionalTest + cucumberTest
./gradlew -p readme-plugin publishToMavenLocal # publicación local
```

## Solución de problemas

| Síntoma | Solución |
|---------|-----|
| `GitHub token is empty or still a placeholder in readme.yml` | Establece el secret `README_GRADLE_PLUGIN`; asegúrate de que CI lo inyecta antes de que la tarea se ejecute |
| No se genera PNG, README sin cambios | Confirma que `README_truth*.adoc` existe en `source.dir`; verifica la sintaxis del bloque `[plantuml, name, png]` |
| JGit push rechazado | La rama debe estar en `watchedBranches` (`main`/`master` por defecto); verifica que el PAT tenga `contents: write` |
| `Java heap space` | `export GRADLE_OPTS="-Xmx2g"` |
| Conflicto SLF4J con Logback | El build excluye `logback-classic` de los classpaths de test automáticamente — no lo vuelvas a añadir |

Consulta [README_truth.adoc](../README_truth.adoc) para la referencia AsciiDoc completa.

## Licencia

Apache License 2.0 — ver [Licencia](../LICENCE).

---

_Parte del ecosistema CCCP Education — `groupId: education.cccp`._