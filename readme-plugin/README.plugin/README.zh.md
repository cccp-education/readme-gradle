<!-- translated from README.md rev 0.0.1 -->
# readme-gradle — 插件内部机制

> `readme-plugin` Gradle 插件的开发者与贡献者指南。

[![Maven Central](https://img.shields.io/maven-central/v/education.cccp/readme-plugin.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/education.cccp/readme-plugin)
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/education.cccp.readme.svg?label=Plugin%20Portal)](https://plugins.gradle.org/plugin/education.cccp.readme)
[![CI](https://img.shields.io/github/actions/workflow/status/cccp-education/readme-gradle/ci.yml?branch=main&label=CI)](https://github.com/cccp-education/readme-gradle/actions/workflows/ci.yml)
[![License](https://img.shields.io/github/license/cccp-education/readme-gradle?label=License)](../LICENCE)

- **版本**: `0.0.1` · **Group**: `education.cccp` · **Plugin ID**: `education.cccp.readme`
- **工具链**: Java 24 · Kotlin 2.3.20 · Gradle 9.5.1
- **构建**: `./gradlew -p readme-plugin build -x test` · **测试**: `./gradlew -p readme-plugin check` · **覆盖率**: 不设门禁

🌐 Languages: [English](README.md) | **中文** | [हिन्दी](README.hi.md) | [Español](README.es.md) | [Français](README.fr.md) | [العربية](README.ar.md) | [বাংলা](README.bn.md) | [Português](README.pt.md) | [Русский](README.ru.md) | [اردو](README.ur.md)

---

## 模块布局

```
readme-gradle/
├── build.gradle.kts                          # 消费者脚手架 — 应用 education.cccp.readme
├── settings.gradle.kts                        # pluginManagement: mavenLocal + portal + central
├── README_truth.adoc                          # EN 真相源 (本 README 的父引用)
├── README_truth_fr.adoc                       # FR 真相源
├── readme.yml                                 # gitignored — 从 CI secret 注入
└── readme-plugin/
    ├── build.gradle.kts                       # 插件模块 (signing, java-gradle-plugin, nmcp)
    ├── gradle/libs.versions.toml              # 版本目录
    └── src/
        ├── main/kotlin/readme/
        │   ├── ReadmePlugin.kt                # 插件入口点 — 注册 3 个任务
        │   ├── ReadmePlantUmlConfig.kt       # Jackson YAML 配置模型 (+ Source/Output/Git)
        │   ├── ScaffoldTask.kt               # generateReadme — 脚手架 readme.yml + workflow
        │   ├── ProcessReadmeTask.kt          # transformReadme — PNG 生成 + 块重写
        │   ├── CommitGeneratedReadmeTask.kt  # commitGeneratedReadme — JGit add/commit/push
        │   ├── AdocSourceFile.kt             # 真相源模型 + 语言检测
        │   ├── GitUtils.kt                   # 从 git 根解析 imgDir
        │   ├── GitRemoteValidator.kt         # 远程验证接口
        │   └── JGitRemoteValidator.kt        # JGit 实现
        ├── test/
        │   ├── kotlin/…                      # JUnit5 单元测试
        │   ├── scenarios/…                   # Cucumber 步骤定义
        │   └── features/                      # 5 个 .feature 文件 (56 个场景)
        │       ├── 1_minimal.feature
        │       ├── 2_scaffold.feature
        │       ├── 3_process.feature
        │       ├── 4_commit.feature
        │       └── 5_integration.feature
        └── functionalTest/kotlin/…           # GradleRunner 功能测试
```

## N0 契约 (通过 `education.cccp.codebase` 消费)

该插件应用 `education.cccp.codebase` 版本 `0.0.2` (来自
`libs.versions.toml`)。通过 codebase-gradle，它传递性地获得了
`workspace-bom` MEMPHIS 发布的共享 N0 契约访问权：

| 契约 | 制品 | 提供 |
|----------|----------|----------|
| `codebase-contracts`          | `education.cccp:codebase-contracts`          | ContextChannel, ChannelBudget, CompositeContext |
| `agent-contracts`             | `education.cccp:agent-contracts`             | Epic, UserStory, GradleTask, AgentState |
| `llm-pool-contracts`          | `education.cccp:llm-pool-contracts`          | LlmInstancePool, LlmInstance, QuotaConfig |
| `opencode-session-contracts`  | `education.cccp:opencode-session-contracts`  | SessionPrompt, SessionResponse, AgentContext |
| `i18n-contracts`              | `education.cccp:i18n-contracts`              | SupportedLanguage, LanguageCatalog, I18nConfig |

> 注：`readme-gradle` 以 *plugin* 形式应用 `education.cccp.codebase`
> (通过 Gradle 插件门户别名 `alias(libs.plugins.codebase)`)，而非作为
> `implementation` 依赖。直接的 N0 契约制品由 codebase-gradle 自身传递性消费。

## 关键依赖

| 库 | 版本 | 角色 |
|---------|---------|------|
| `net.sourceforge.plantuml:plantuml` | `1.2026.0` | 从 PlantUML 块生成 PNG |
| `org.eclipse.jgit:org.eclipse.jgit` | `7.5.0.202512021534-r` | 无需系统 git 二进制的 Git 操作 |
| `com.fasterxml.jackson.module:jackson-module-kotlin` | `2.21.1` | YAML → Kotlin data class 映射 |
| `com.fasterxml.jackson.dataformat:jackson-dataformat-yaml` | `2.21.1` | 解析 `readme.yml` |
| `io.arrow-kt:arrow-core` | `2.2.2` | 函数式类型 |
| `io.arrow-kt:arrow-fx-coroutines` | `2.2.2` | 基于协程的效应 |
| `com.github.node-gradle:gradle-node-plugin` | `7.1.0` | Node 工具 (未来 HTML 服务) |
| `dev.langchain4j:langchain4j` (+ providers) | `1.14.1` / `1.14.1-beta24` | LLM 提供者 (compileOnly — AI bundle) |
| `org.asciidoctor:asciidoctor-gradle-jvm` | `5.0.0-alpha.1` | Asciidoctor 工具 (compileOnly) |

LLM 提供者栈 (bundle `readme-ai`, `compileOnly`):
`langchain4j-ollama`, `langchain4j-open-ai`, `langchain4j-google-ai-gemini`,
`langchain4j-mistral-ai`, `langchain4j-pgvector`, `langchain4j-embeddings-all-minilm-l6-v2`.

`koog-agents` 在当前目录中**不是** readme-gradle 的直接依赖 —— AI
bundle 声明为 `compileOnly`，由消费构建 (例如 codebase-gradle 的运行时)
提供。

## 测试矩阵

| 任务 | 范围 | 备注 |
|------|-------|-------|
| `test`           | JUnit5 单元测试 | 排除 `readme.scenarios.**` 和 `readme.ReadmeGradlePluginFunctionalTests` |
| `cucumberTest`   | Cucumber BDD (5 个特性, 56 个场景) | 使用 `cucumber-junit-platform-engine`，排除 `junit-jupiter` 引擎 |
| `functionalTest`  | GradleRunner 功能测试 | 独立源集 `functionalTest`，用 `gradlePlugin.testSourceSets` 注册以生成 `plugin-under-test-metadata.properties` |
| `check`          | 聚合 `test` + `functionalTest` + `cucumberTest` | |

测试框架版本 (来自 `libs.versions.toml`):
- JUnit Platform `1.14.3` · Cucumber `7.34.3` · AssertJ `3.27.7`
- Mockito Kotlin `6.2.3` · Mockito Jupiter `5.23.0`
- Kotlinx Coroutines `1.10.2` · Testcontainers PostgreSQL `1.21.4`

> `logback-classic` 从 `testRuntimeClasspath`、
> `testImplementation` 和 `functionalTest.runtimeClasspath` 中**排除**，
> 以避免与 Gradle 自身日志的 SLF4J 绑定冲突。

## JVM 调优

插件构建在所有 `Test` 任务上使用 `-XX:+EnableDynamicAgentLoading`
(在 `tasks.withType<Test>` 中设置)。没有定制的 G1GC/SerialGC 分离配置
—— 如果你的 runner 需要更大的堆，请调整 `GRADLE_OPTS`：

```bash
export GRADLE_OPTS="-Xmx2g"
```

## 构建命令

```bash
./gradlew -p readme-plugin build                            # 完整构建 (编译 + 所有测试)
./gradlew -p readme-plugin build -x test                    # 仅编译
./gradlew -p readme-plugin test                             # JUnit5 单元测试
./gradlew -p readme-plugin cucumberTest                     # Cucumber BDD
./gradlew -p readme-plugin functionalTest                   # GradleRunner 功能测试
./gradlew -p readme-plugin check                            # test + functionalTest + cucumberTest
./gradlew -p readme-plugin publishToMavenLocal              # 本地发布
./gradlew -p readme-plugin publishAggregationToCentralPortal --no-daemon   # Maven Central (NMCP)
```

## CI 流水线

`.github/workflows/` 中有三个工作流：

1. **`readme_action.yml`** —— `从真相源生成 README`。在任何分支推送触及
   `README_truth*.adoc` 时触发。注入 `README_GRADLE_PLUGIN` secret，运行
   `./gradlew -p readme-plugin commitGeneratedReadme --no-daemon`
   (JGit 提交 + 推送)。JDK 23 (Temurin)。
2. **`ci.yml`** —— `CI`。在 `readme_action.yml` 完成后运行 (通过
   `workflow_run`)，以及推送/PR 时忽略 `README_truth*.adoc` 和 `*.md`。
   运行 `./gradlew -p readme-plugin clean check --no-daemon`，上传测试
   报告。JDK 23 (Temurin)。
3. **`integration_test.yml`** —— `集成测试 — commitGeneratedReadme`。
   仅手动触发。运行标记 `@integration` 的 Cucumber 场景：
   `./gradlew -p readme-plugin cucumberTest -Dcucumber.filter.tags="@integration"`。

## 发布 (NMCP)

- **Plugin Portal**: 通过 `com.gradle.plugin-publish` `2.1.0`
  (`alias(libs.plugins.publish)`) 发布。插件元数据: `id = education.cccp.readme`,
  `implementationClass = readme.ReadmePlugin`, displayName `"README helper Plugin"`,
  tags `asciidoc`, `plantuml`, `readme`, `documentation`, `github`, `diagram`.
- **Maven Central**: `publishing { repositories { mavenCentral() } }` 且每个
  `MavenPublication` 都有 POM。通过 `signing { useGpgCmd() }` 签名 —— 当
  `CI=true` 或版本以 `-SNAPSHOT` 结尾时跳过。Developer `cccp-education`,
  SCM 指向 `github.com/cccp-education/readme-gradle`.
- 项目属性 `relocationGroup`，若设置，则在 POM 中为 groupId 迁移发出
  `<distributionManagement><relocation>` 块。
- `java { withJavadocJar(); withSourcesJar() }` —— 两个 jar 均打包。
- 版本在目录中**硬编码**：`readme = "0.0.1"`。模块通过
  `libs.plugins.readme.get().version` 解析。

> 发布前确认：所有 `implementation` 依赖必须是已发布版本 (无
> `-SNAPSHOT`)，且任何 `education.cccp:*` 传递依赖必须已在 Central 上。

## 架构文档

插件遵循六边形布局 (驱动适配器 = Gradle 任务，应用核心 =
`ReadmePlugin` + 配置，领域 = `AdocSourceFile`，被驱动适配器 =
FileSystem / PlantUML 引擎 / JGit):

- [README_truth.adoc](../README_truth.adoc) —— AsciiDoc 参考，含 7 个内嵌
  PlantUML 图 (问题-解决方案、真相源、概览-序列、活动、组件、类图、
  六边形)。这是规范的架构描述。
- [README_truth_fr.adoc](../README_truth_fr.adoc) —— 参考的法语翻译。

`.github/workflows/readmes/images/{en,fr}/` 目录保存由插件自身流水线
提交的生成 PNG 制品 (7 个图 × 2 种语言)。

## 贡献

1. 构建编译通过: `./gradlew -p readme-plugin build -x test`
2. 单元测试通过: `./gradlew -p readme-plugin test`
3. Cucumber 通过: `./gradlew -p readme-plugin cucumberTest`
4. 功能测试通过: `./gradlew -p readme-plugin functionalTest`
5. `readme.yml` 中无密钥 —— 它被 gitignore 并从 `README_GRADLE_PLUGIN`
   CI secret 注入。切勿提交真实 PAT。
6. 遵循六边形约定 (任务作为驱动适配器，领域模型 `AdocSourceFile` 与
   基础设施隔离)。

## 许可证

Apache License 2.0 —— 见 [许可证](../LICENCE)。

---

_CCCP Education 生态的一部分 —— `groupId: education.cccp`._