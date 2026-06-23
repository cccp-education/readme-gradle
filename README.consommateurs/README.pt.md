<!-- translated from README.md rev 0.0.1 -->
# readme-gradle — Guia do consumidor

> Plugin do Gradle que gera arquivos `README.adoc` compatíveis com GitHub
> a partir de arquivos-fonte `README_truth.adoc`, renderizando blocos
> nativos `[plantuml]` em imagens PNG confirmadas via GitHub Actions e
> JGit.

[![Maven Central](https://img.shields.io/maven-central/v/education.cccp/readme-plugin.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/education.cccp/readme-plugin)
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/education.cccp.readme.svg?label=Plugin%20Portal)](https://plugins.gradle.org/plugin/education.cccp.readme)
[![CI](https://img.shields.io/github/actions/workflow/status/cccp-education/readme-gradle/ci.yml?branch=main&label=CI)](https://github.com/cccp-education/readme-gradle/actions/workflows/ci.yml)
[![License](https://img.shields.io/github/license/cccp-education/readme-gradle?label=License)](../LICENCE)

- **Versão**: `0.0.1` · **Group**: `education.cccp` · **Plugin ID**: `education.cccp.readme`
- **Toolchain**: Java 24 · Kotlin 2.3.20 · Gradle 9.5.1
- **Build**: `./gradlew build` · **Testes**: `./gradlew check` (JUnit5 + 56 cenários Cucumber em 5 funcionalidades)
- **Cobertura**: sem portão (nenhuma regra Kover conectada ao `check`)

🌐 Languages: [English](README.md) | [中文](README.zh.md) | [हिन्दी](README.hi.md) | [Español](README.es.md) | [Français](README.fr.md) | [العربية](README.ar.md) | [বাংলা](README.bn.md) | **Português** | [Русский](README.ru.md) | [اردو](README.ur.md)

---

## O que faz

O GitHub não renderiza nativamente a sintaxe PlantUML embutida em
arquivos AsciiDoc. O `readme-gradle` automatiza a solução alternativa: você
edita uma única fonte de verdade (`README_truth.adoc`), o plugin extrai
cada bloco `[plantuml, name, png]`, gera imagens PNG, reescreve o bloco
como referência `image::`, e confirma tanto o `README.adoc` reescrito quanto
as imagens de volta ao seu repositório via JGit — não é necessário binário
`git` do sistema no runner de CI.

```
README_truth.adoc → generateReadme → transformReadme → commitGeneratedReadme → README.adoc + images/
                    (scaffolds yml)  (PNG + rewrite)    (JGit add + commit + push)
```

## Início rápido

### 1. Aplique o plugin

```gradle
plugins {
    id("education.cccp.readme") version "0.0.1"
}
```

### 2. Crie sua fonte de verdade

Crie `README_truth.adoc` na raiz do projeto. Use blocos PlantUML nativos:

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

Para uma variante em francês, nomeie o arquivo `README_plantuml_fr.adoc`
— o sufixo `_fr` é detectado e produz `README_fr.adoc`.

### 3. Configure via `readme.yml`

O plugin lê `readme.yml` da raiz do projeto. **Nunca confirme um token
real** — armazene o arquivo completo (token incluído) no secret do GitHub
`README_GRADLE_PLUGIN` e injete-o no CI:

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

Passo de CI:

```yaml
- name: Inject plugin config
  run: echo "${{ secrets.README_GRADLE_PLUGIN }}" > readme.yml

- name: Generate README and commit via JGit
  run: ./gradlew -p readme-plugin commitGeneratedReadme --no-daemon
```

### 4. Execute o pipeline

```bash
./gradlew commitGeneratedReadme          # pipeline completo (CI)
./gradlew transformReadme               # apenas geração PNG + reescrita
./gradlew generateReadme                 # scaffold readme.yml + workflow se ausentes
```

## Tarefas disponíveis

| Tarefa | Grupo | Descrição |
|------|-------|-------------|
| `generateReadme`         | generate  | Cria `readme.yml` e `.github/workflows/readme_action.yml` se ausentes |
| `transformReadme`        | transform | Gera PNGs e reescreve `README*.adoc` a partir de `README_truth*.adoc` (depende de `generateReadme`) |
| `commitGeneratedReadme`  | deploy    | Confirma e envia os `README*.adoc` + imagens gerados via JGit (apenas CI, depende de `transformReadme`) |

## DSL de extensão

O plugin **não tem bloco de extensão do Gradle**. Toda configuração é lida
do arquivo `readme.yml` na raiz do projeto (parseado por Jackson YAML para
`ReadmePlantUmlConfig`). Se `readme.yml` estiver ausente ou vazio, padrões
razoáveis são usados silenciosamente.

| Seção config | Campo | Padrão |
|----------------|-------|---------|
| `source`  | `dir`         | `"."` |
| `source`  | `defaultLang` | `"en"` |
| `output`  | `imgDir`      | `".github/workflows/readmes/images"` |
| `git`     | `userName`        | `"github-actions[bot]"` |
| `git`     | `userEmail`       | `"github-actions[bot]@users.noreply.github.com"` |
| `git`     | `commitMessage`   | `"chore: generate readme [skip ci]"` |
| `git`     | `token`           | `""` (deve ser definido — erro em tempo de execução se vazio/placeholder) |
| `git`     | `watchedBranches`  | `["main", "master"]` |

## Pré-requisitos

- **Java** 24+ (toolchain Kotlin 2.3.20)
- **Gradle** 9.5.1+
- **GitHub** repositório com acesso de escrita (para JGit push)
- **GitHub secret** `README_GRADLE_PLUGIN` contendo o `readme.yml` completo com um PAT válido

## Build e teste

```bash
./gradlew -p readme-plugin build              # build completo (compile + unit + cucumber + functional)
./gradlew -p readme-plugin test               # apenas testes unitários JUnit5
./gradlew -p readme-plugin cucumberTest       # Cucumber BDD (5 arquivos feature, 56 cenários)
./gradlew -p readme-plugin functionalTest     # testes funcionais GradleRunner
./gradlew -p readme-plugin check              # test + functionalTest + cucumberTest
./gradlew -p readme-plugin publishToMavenLocal # publicação local
```

## Solução de problemas

| Sintoma | Correção |
|---------|-----|
| `GitHub token is empty or still a placeholder in readme.yml` | Defina o secret `README_GRADLE_PLUGIN`; garanta que o CI o injete antes da tarefa rodar |
| Nenhum PNG gerado, README inalterado | Confirme que `README_truth*.adoc` existe em `source.dir`; verifique a sintaxe do bloco `[plantuml, name, png]` |
| JGit push rejeitado | O branch deve estar em `watchedBranches` (`main`/`master` por padrão); verifique se o PAT tem `contents: write` |
| `Java heap space` | `export GRADLE_OPTS="-Xmx2g"` |
| Conflito SLF4J com Logback | O build exclui `logback-classic` dos classpaths de teste automaticamente — não o adicione novamente |

Consulte [README_truth.adoc](../README_truth.adoc) para a referência AsciiDoc completa.

## Licença

Apache License 2.0 — consulte [Licença](../LICENCE).

---

_Parte do ecossistema CCCP Education — `groupId: education.cccp`._