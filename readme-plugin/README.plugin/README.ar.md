<!-- translated from README.md rev 0.0.1 -->
# readme-gradle — باطن الإضافة

> دليل المطوّرين والمساهمين لإضافة `readme-plugin` الخاصة بـ Gradle.

[![Maven Central](https://img.shields.io/maven-central/v/education.cccp/readme-plugin.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/education.cccp/readme-plugin)
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/education.cccp.readme.svg?label=Plugin%20Portal)](https://plugins.gradle.org/plugin/education.cccp.readme)
[![CI](https://img.shields.io/github/actions/workflow/status/cccp-education/readme-gradle/ci.yml?branch=main&label=CI)](https://github.com/cccp-education/readme-gradle/actions/workflows/ci.yml)
[![License](https://img.shields.io/github/license/cccp-education/readme-gradle?label=License)](../LICENCE)

- **الإصدار**: `0.0.1` · **Group**: `education.cccp` · **Plugin ID**: `education.cccp.readme`
- **سلسلة الأدوات**: Java 24 · Kotlin 2.3.20 · Gradle 9.5.1
- **البناء**: `./gradlew -p readme-plugin build -x test` · **الاختبارات**: `./gradlew -p readme-plugin check` · **التغطية**: غير مقيّدة

🌐 Languages: [English](README.md) | [中文](README.zh.md) | [हिन्दी](README.hi.md) | [Español](README.es.md) | [Français](README.fr.md) | **العربية** | [বাংলা](README.bn.md) | [Português](README.pt.md) | [Русский](README.ru.md) | [اردو](README.ur.md)

---

## تخطيط الوحدات

```
readme-gradle/
├── build.gradle.kts                          # هارنس المستهلك — يطبّق education.cccp.readme
├── settings.gradle.kts                        # pluginManagement: mavenLocal + portal + central
├── README_truth.adoc                          # مصدر الحقيقة EN (مرجع الأصل لهذا README)
├── README_truth_fr.adoc                       # مصدر الحقيقة FR
├── readme.yml                                 # gitignored — يُحقن من سر CI
└── readme-plugin/
    ├── build.gradle.kts                       # وحدة الإضافة (signing, java-gradle-plugin, nmcp)
    ├── gradle/libs.versions.toml              # فهرس الإصدارات
    └── src/
        ├── main/kotlin/readme/
        │   ├── ReadmePlugin.kt                # نقطة دخول الإضافة — تسجّل 3 مهام
        │   ├── ReadmePlantUmlConfig.kt       # نموذج تهيئة Jackson YAML (+ Source/Output/Git)
        │   ├── ScaffoldTask.kt               # generateReadme — سقالة readme.yml + workflow
        │   ├── ProcessReadmeTask.kt          # transformReadme — توليد PNG + إعادة كتابة البلوك
        │   ├── CommitGeneratedReadmeTask.kt  # commitGeneratedReadme — JGit add/commit/push
        │   ├── AdocSourceFile.kt             # نموذج مصدر-الحقيقة + كشف اللغة
        │   ├── GitUtils.kt                   # حل imgDir من جذر git
        │   ├── GitRemoteValidator.kt         # واجهة للتحقق عن بُعد
        │   └── JGitRemoteValidator.kt        # تطبيق JGit
        ├── test/
        │   ├── kotlin/…                      # اختبارات وحدة JUnit5
        │   ├── scenarios/…                   # تعريفات خطوات Cucumber
        │   └── features/                      # 5 ملفات .feature (56 سيناريو)
        │       ├── 1_minimal.feature
        │       ├── 2_scaffold.feature
        │       ├── 3_process.feature
        │       ├── 4_commit.feature
        │       └── 5_integration.feature
        └── functionalTest/kotlin/…           # اختبارات وظيفية GradleRunner
```

## عقود N0 (تُستهلك عبر `education.cccp.codebase`)

تطبّق الإضافة `education.cccp.codebase` إصدار `0.0.2` (من
`libs.versions.toml`). عبر codebase-gradle تكتسب وصولاً عابرًا إلى عقود
N0 المشتركة المنشورة بواسطة `workspace-bom` MEMPHIS:

| العقد | الأثر | يوفّر |
|----------|----------|----------|
| `codebase-contracts`          | `education.cccp:codebase-contracts`          | ContextChannel, ChannelBudget, CompositeContext |
| `agent-contracts`             | `education.cccp:agent-contracts`             | Epic, UserStory, GradleTask, AgentState |
| `llm-pool-contracts`          | `education.cccp:llm-pool-contracts`          | LlmInstancePool, LlmInstance, QuotaConfig |
| `opencode-session-contracts`  | `education.cccp:opencode-session-contracts`  | SessionPrompt, SessionResponse, AgentContext |
| `i18n-contracts`              | `education.cccp:i18n-contracts`              | SupportedLanguage, LanguageCatalog, I18nConfig |

> ملاحظة: يطبّق `readme-gradle` الإضافة `education.cccp.codebase` كـ *plugin*
> (عبر الاسم المستعار لبوابة إضافات Gradle `alias(libs.plugins.codebase)`),
> وليس كاعتماد `implementation`. أثر عقود N0 المباشرة تُستهلك عابرًا بواسطة
> codebase-gradle نفسه.

## الاعتمادات الرئيسية

| المكتبة | الإصدار | الدور |
|---------|---------|------|
| `net.sourceforge.plantuml:plantuml` | `1.2026.0` | توليد PNG من كتل PlantUML |
| `org.eclipse.jgit:org.eclipse.jgit` | `7.5.0.202512021534-r` | عمليات Git دون بائنary git للنظام |
| `com.fasterxml.jackson.module:jackson-module-kotlin` | `2.21.1` | تعيين YAML → Kotlin data class |
| `com.fasterxml.jackson.dataformat:jackson-dataformat-yaml` | `2.21.1` | تحليل `readme.yml` |
| `io.arrow-kt:arrow-core` | `2.2.2` | أنواع دالّية |
| `io.arrow-kt:arrow-fx-coroutines` | `2.2.2` | تأثيرات قائمة على coroutine |
| `com.github.node-gradle:gradle-node-plugin` | `7.1.0` | أدوات Node (خدمة HTML مستقبلية) |
| `dev.langchain4j:langchain4j` (+ providers) | `1.14.1` / `1.14.1-beta24` | مزوّدو LLM (compileOnly — AI bundle) |
| `org.asciidoctor:asciidoctor-gradle-jvm` | `5.0.0-alpha.1` | أدوات Asciidoctor (compileOnly) |

مكدس مزوّدي LLM (bundle `readme-ai`, `compileOnly`):
`langchain4j-ollama`, `langchain4j-open-ai`, `langchain4j-google-ai-gemini`,
`langchain4j-mistral-ai`, `langchain4j-pgvector`, `langchain4j-embeddings-all-minilm-l6-v2`.

`koog-agents` **ليس** اعتمادًا مباشرًا على readme-gradle في الفهرس الحالي
— حزمة AI مُعلنة `compileOnly` ويوفّرها البناء المستهلِك (مثل runtime
codebase-gradle).

## مصفوفة الاختبار

| المهمة | النطاق | ملاحظات |
|------|-------|-------|
| `test`           | اختبارات وحدة JUnit5 | يستثني `readme.scenarios.**` و`readme.ReadmeGradlePluginFunctionalTests` |
| `cucumberTest`   | Cucumber BDD (5 ميزات, 56 سيناريو) | يستخدم `cucumber-junit-platform-engine`, يستثني محرّك `junit-jupiter` |
| `functionalTest`  | اختبارات وظيفية GradleRunner | طقم مصدر منفصل `functionalTest` مسجّل مع `gradlePlugin.testSourceSets` لـ `plugin-under-test-metadata.properties` |
| `check`          | يجمع `test` + `functionalTest` + `cucumberTest` | |

إصدارات إطار الاختبار (من `libs.versions.toml`):
- JUnit Platform `1.14.3` · Cucumber `7.34.3` · AssertJ `3.27.7`
- Mockito Kotlin `6.2.3` · Mockito Jupiter `5.23.0`
- Kotlinx Coroutines `1.10.2` · Testcontainers PostgreSQL `1.21.4`

> `logback-classic` **مستثنى** من `testRuntimeClasspath`,
> `testImplementation` و`functionalTest.runtimeClasspath` لتجنّب تعارضات
> ربط SLF4J مع تسجيل Gradle نفسه.

## ضبط JVM

يستخدم بناء الإضافة `-XX:+EnableDynamicAgentLoading` على جميع مهام `Test`
(مضبوط في `tasks.withType<Test>`). لا يوجد ملف تعريف مخصّص لتقسيم
G1GC/SerialGC — اضبط `GRADLE_OPTS` إذا احتاج رانر خاصتك ذاكرة أكبر:

```bash
export GRADLE_OPTS="-Xmx2g"
```

## أوامر البناء

```bash
./gradlew -p readme-plugin build                            # بناء كامل (compile + جميع الاختبارات)
./gradlew -p readme-plugin build -x test                    # compile فقط
./gradlew -p readme-plugin test                             # اختبارات وحدة JUnit5
./gradlew -p readme-plugin cucumberTest                     # Cucumber BDD
./gradlew -p readme-plugin functionalTest                   # وظيفي GradleRunner
./gradlew -p readme-plugin check                            # test + functionalTest + cucumberTest
./gradlew -p readme-plugin publishToMavenLocal              # نشر محلي
./gradlew -p readme-plugin publishAggregationToCentralPortal --no-daemon   # Maven Central (NMCP)
```

## خط أنابيب CI

ثلاثة workflows في `.github/workflows/`:

1. **`readme_action.yml`** — `توليد README من مصادر الحقيقة`. يعمل عند
   الدفع إلى أي فرع يمسّ `README_truth*.adoc`. يحقن سر
   `README_GRADLE_PLUGIN`, يشغّل `./gradlew -p readme-plugin
   commitGeneratedReadme --no-daemon` (JGit commit + push). JDK 23 (Temurin).
2. **`ci.yml`** — `CI`. يعمل بعد اكتمال `readme_action.yml` (عبر
   `workflow_run`) وعند push/PR متجاهلاً `README_truth*.adoc` و`*.md`.
   يشغّل `./gradlew -p readme-plugin clean check --no-daemon`, يرفع تقارير
   الاختبار. JDK 23 (Temurin).
3. **`integration_test.yml`** — `اختبار تكامل — commitGeneratedReadme`.
   إرسال يدوي فقط. يشغّل سيناريوهات Cucumber موسومة `@integration`:
   `./gradlew -p readme-plugin cucumberTest -Dcucumber.filter.tags="@integration"`.

## النشر (NMCP)

- **Plugin Portal**: يُنشَر عبر `com.gradle.plugin-publish` `2.1.0`
  (`alias(libs.plugins.publish)`). بيانات الإضافة الوصفية:
  `id = education.cccp.readme`, `implementationClass = readme.ReadmePlugin`,
  displayName `"README helper Plugin"`, وسوم `asciidoc`, `plantuml`,
  `readme`, `documentation`, `github`, `diagram`.
- **Maven Central**: `publishing { repositories { mavenCentral() } }` مع POM
  على كل `MavenPublication`. توقيع عبر `signing { useGpgCmd() }` — يُتخطّى
  عند `CI=true` أو انتهاء الإصدار بـ `-SNAPSHOT`. المطوّر `cccp-education`,
  SCM يشير إلى `github.com/cccp-education/readme-gradle`.
- خاصية المشروع `relocationGroup`, إن ضُبطت, تُطلق كتلة
  `<distributionManagement><relocation>` في POM لترحيلات groupId.
- `java { withJavadocJar(); withSourcesJar() }` — كلا jar مُحزَّم.
- الإصدار **مُرمد** في الفهرس: `readme = "0.0.1"`. تحلّه الوحدة عبر
  `libs.plugins.readme.get().version`.

> أكّد قبل النشر: جميع اعتمادات `implementation` يجب أن تكون إصدارات
> منشورة (لا `-SNAPSHOT`), وأي `education.cccp:*` عابر يجب أن يكون على
> Central مسبقًا.

## وثائق البنية

تتبع الإضافة تخطيطًا سداسيًا (المحوّكات القائدة = مهام Gradle, نواة
التطبيق = `ReadmePlugin` + التهيئة, النطاق = `AdocSourceFile`, المحوّكات
المُحرَّكة = FileSystem / محرّك PlantUML / JGit):

- [README_truth.adoc](../README_truth.adoc) — مرجع AsciiDoc مع 7 مخططات
  PlantUML مضمّنة (مشكلة-حل, مصدر-الحقيقة, نظرة-تسلسل, نشاط, مكوّنات,
  مخطط-فئات, سداسي). هذا هو الوصف المعماري القانوني.
- [README_truth_fr.adoc](../README_truth_fr.adoc) — ترجمة فرنسية للمرجع.

دليل `.github/workflows/readmes/images/{en,fr}/` يحوي أثر PNG المولّدة
الملتزَمة بواسطة خط أنابيب الإضافة نفسه (7 مخططات × 2 لغات).

## المساهمة

1. البناء يُترجم: `./gradlew -p readme-plugin build -x test`
2. اختبارات الوحدة خضراء: `./gradlew -p readme-plugin test`
3. Cucumber خضراء: `./gradlew -p readme-plugin cucumberTest`
4. الاختبارات الوظيفية خضراء: `./gradlew -p readme-plugin functionalTest`
5. لا أسرار في `readme.yml` — هو gitignored ويُحقن من سر CI
   `README_GRADLE_PLUGIN`. لا تلتزم أبدًا بـ PAT حقيقي.
6. اتبع اصطلاحات السداسي (المهام كمحوّكات قائدة, نموذج النطاق
   `AdocSourceFile` معزول عن البنية التحتية).

## الترخيص

Apache License 2.0 — راجع [الترخيص](../LICENCE)。

---

_جزء من منظومة CCCP Education — `groupId: education.cccp`._