<!-- translated from README.md rev 0.0.1 -->
# readme-gradle — 消费者指南

> Gradle 插件，从 `README_truth.adoc` 源文件生成 GitHub 兼容的
> `README.adoc` 文件，通过 GitHub Actions 和 JGit 将原生
> `[plantuml]` 块渲染为已提交的 PNG 图片。

[![Maven Central](https://img.shields.io/maven-central/v/education.cccp/readme-plugin.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/education.cccp/readme-plugin)
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/education.cccp.readme.svg?label=Plugin%20Portal)](https://plugins.gradle.org/plugin/education.cccp.readme)
[![CI](https://img.shields.io/github/actions/workflow/status/cccp-education/readme-gradle/ci.yml?branch=main&label=CI)](https://github.com/cccp-education/readme-gradle/actions/workflows/ci.yml)
[![License](https://img.shields.io/github/license/cccp-education/readme-gradle?label=License)](../LICENCE)

- **版本**: `0.0.1` · **Group**: `education.cccp` · **Plugin ID**: `education.cccp.readme`
- **工具链**: Java 24 · Kotlin 2.3.20 · Gradle 9.5.1
- **构建**: `./gradlew build` · **测试**: `./gradlew check` (JUnit5 + 56 个 Cucumber 场景，覆盖 5 个特性)
- **覆盖率**: 不设门禁 (未在 `check` 中接入 Kover 规则)

🌐 Languages: [English](README.md) | **中文** | [हिन्दी](README.hi.md) | [Español](README.es.md) | [Français](README.fr.md) | [العربية](README.ar.md) | [বাংলা](README.bn.md) | [Português](README.pt.md) | [Русский](README.ru.md) | [اردو](README.ur.md)

---

## 它做什么

GitHub 原生不支持渲染嵌入在 AsciiDoc 文件中的 PlantUML 语法。
`readme-gradle` 自动完成这一变通方案：你只需编辑唯一的真相源
(`README_truth.adoc`)，插件会提取每个 `[plantuml, name, png]` 块，
生成 PNG 图片，将该块重写为 `image::` 引用，并通过 JGit 将重写后的
`README.adoc` 与图片一起提交回你的仓库 —— CI 运行器上无需系统
`git` 二进制文件。

```
README_truth.adoc → generateReadme → transformReadme → commitGeneratedReadme → README.adoc + images/
                    (scaffolds yml)  (PNG + rewrite)    (JGit add + commit + push)
```

## 快速开始

### 1. 应用插件

```gradle
plugins {
    id("education.cccp.readme") version "0.0.1"
}
```

### 2. 编写你的真相源

在项目根目录创建 `README_truth.adoc`。使用原生 PlantUML 块：

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

法语变体请将文件命名为 `README_plantuml_fr.adoc` —— `_fr`
后缀会被识别并生成 `README_fr.adoc`。

### 3. 通过 `readme.yml` 配置

插件从项目根目录读取 `readme.yml`。**切勿提交真实 token** —— 将完整文件
(含 token) 存储在 GitHub secret `README_GRADLE_PLUGIN` 中，并在 CI 中注入：

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

CI 步骤：

```yaml
- name: Inject plugin config
  run: echo "${{ secrets.README_GRADLE_PLUGIN }}" > readme.yml

- name: Generate README and commit via JGit
  run: ./gradlew -p readme-plugin commitGeneratedReadme --no-daemon
```

### 4. 运行流水线

```bash
./gradlew commitGeneratedReadme          # 完整流水线 (CI)
./gradlew transformReadme               # 仅生成 PNG + 重写
./gradlew generateReadme                 # 缺失时脚手架 readme.yml + workflow
```

## 可用任务

| 任务 | 组 | 描述 |
|------|-------|-------------|
| `generateReadme`         | generate  | 若不存在则创建 `readme.yml` 和 `.github/workflows/readme_action.yml` |
| `transformReadme`        | transform | 从 `README_truth*.adoc` 生成 PNG 并重写 `README*.adoc` (依赖 `generateReadme`) |
| `commitGeneratedReadme`  | deploy    | 通过 JGit 提交并推送生成的 `README*.adoc` + 图片 (仅 CI，依赖 `transformReadme`) |

## 扩展 DSL

该插件**没有 Gradle 扩展块**。所有配置均从项目根目录的
`readme.yml` 文件读取 (由 Jackson YAML 解析为
`ReadmePlantUmlConfig`)。若 `readme.yml` 缺失或为空，将静默使用合理的默认值。

| 配置区段 | 字段 | 默认值 |
|----------------|-------|---------|
| `source`  | `dir`         | `"."` |
| `source`  | `defaultLang` | `"en"` |
| `output`  | `imgDir`      | `".github/workflows/readmes/images"` |
| `git`     | `userName`        | `"github-actions[bot]"` |
| `git`     | `userEmail`       | `"github-actions[bot]@users.noreply.github.com"` |
| `git`     | `commitMessage`   | `"chore: generate readme [skip ci]"` |
| `git`     | `token`           | `""` (必须设置 —— 为空或占位符时运行时报错) |
| `git`     | `watchedBranches`  | `["main", "master"]` |

## 前置条件

- **Java** 24+ (Kotlin 2.3.20 工具链)
- **Gradle** 9.5.1+
- **GitHub** 仓库且有写入权限 (用于 JGit push)
- **GitHub secret** `README_GRADLE_PLUGIN`，包含带有效 PAT 的完整 `readme.yml`

## 构建与测试

```bash
./gradlew -p readme-plugin build              # 完整构建 (编译 + 单元 + cucumber + 功能)
./gradlew -p readme-plugin test               # 仅 JUnit5 单元测试
./gradlew -p readme-plugin cucumberTest       # Cucumber BDD (5 个特性文件, 56 个场景)
./gradlew -p readme-plugin functionalTest     # GradleRunner 功能测试
./gradlew -p readme-plugin check              # test + functionalTest + cucumberTest
./gradlew -p readme-plugin publishToMavenLocal # 本地发布
```

## 故障排查

| 症状 | 修复 |
|---------|-----|
| `GitHub token is empty or still a placeholder in readme.yml` | 设置 `README_GRADLE_PLUGIN` secret；确保 CI 在任务运行前注入 |
| 未生成 PNG，README 未变 | 确认 `README_truth*.adoc` 存在于 `source.dir`；检查块语法 `[plantuml, name, png]` |
| JGit push 被拒 | 分支必须在 `watchedBranches` 中 (默认 `main`/`master`)；验证 PAT 具有 `contents: write` |
| `Java heap space` | `export GRADLE_OPTS="-Xmx2g"` |
| Logback SLF4J 冲突 | 构建已自动从测试类路径排除 `logback-classic` —— 请勿重新添加 |

完整 AsciiDoc 参考请见 [README_truth.adoc](../README_truth.adoc)。

## 许可证

Apache License 2.0 —— 见 [许可证](../LICENCE)。

---

_CCCP Education 生态的一部分 —— `groupId: education.cccp`._