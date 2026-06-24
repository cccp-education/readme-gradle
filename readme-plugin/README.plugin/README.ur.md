<!-- translated from README.md rev 0.0.1 -->
# readme-gradle — پلگ ان کا باطن

> `readme-plugin` Gradle پلگ ان کے لیے ڈویلپرز اور شراکت داروں کا رہنما۔

[![Maven Central](https://img.shields.io/maven-central/v/education.cccp/readme-plugin.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/education.cccp/readme-plugin)
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/education.cccp.readme.svg?label=Plugin%20Portal)](https://plugins.gradle.org/plugin/education.cccp.readme)
[![CI](https://img.shields.io/github/actions/workflow/status/cccp-education/readme-gradle/ci.yml?branch=main&label=CI)](https://github.com/cccp-education/readme-gradle/actions/workflows/ci.yml)
[![License](https://img.shields.io/github/license/cccp-education/readme-gradle?label=License)](../LICENCE)

- **نسخہ**: `0.0.1` · **Group**: `education.cccp` · **Plugin ID**: `education.cccp.readme`
- **ٹول چین**: Java 24 · Kotlin 2.3.20 · Gradle 9.5.1
- **بلڈ**: `./gradlew -p readme-plugin build -x test` · **ٹیسٹ**: `./gradlew -p readme-plugin check` · **کوریج**: گیٹڈ نہیں

🌐 Languages: [English](README.md) | [中文](README.zh.md) | [हिन्दी](README.hi.md) | [Español](README.es.md) | [Français](README.fr.md) | [العربية](README.ar.md) | [বাংলা](README.bn.md) | [Português](README.pt.md) | [Русский](README.ru.md) | **اردو**

---

## ماڈیول ترتیب

```
readme-gradle/
├── build.gradle.kts                          # صارف ہارنیس — education.cccp.readme لگاتا ہے
├── settings.gradle.kts                        # pluginManagement: mavenLocal + portal + central
├── README_truth.adoc                          # EN صداقت کا مصدر (اس README کا والد حوالہ)
├── README_truth_fr.adoc                       # FR صداقت کا مصدر
├── readme.yml                                 # gitignored — CI secret سے داخل
└── readme-plugin/
    ├── build.gradle.kts                       # پلگ ان ماڈیول (signing, java-gradle-plugin, nmcp)
    ├── gradle/libs.versions.toml              # ورژن کیٹلاگ
    └── src/
        ├── main/kotlin/readme/
        │   ├── ReadmePlugin.kt                # پلگ ان داخلہ نقطہ — 3 کام رجسٹر کرتا ہے
        │   ├── ReadmePlantUmlConfig.kt       # Jackson YAML ترتیب ماڈل (+ Source/Output/Git)
        │   ├── ScaffoldTask.kt               # generateReadme — readme.yml + workflow سکفولڈ
        │   ├── ProcessReadmeTask.kt          # transformReadme — PNG پیدا + بلاک پونرلیکھن
        │   ├── CommitGeneratedReadmeTask.kt  # commitGeneratedReadme — JGit add/commit/push
        │   ├── AdocSourceFile.kt             # صداقت-مصدر ماڈل + زبان کی شناخت
        │   ├── GitUtils.kt                   # git روٹ سے imgDir حل
        │   ├── GitRemoteValidator.kt         # ریموٹ توثیق کا انٹرفیس
        │   └── JGitRemoteValidator.kt        # JGit عمل درآمد
        ├── test/
        │   ├── kotlin/…                      # JUnit5 یونٹ ٹیسٹ
        │   ├── scenarios/…                   # Cucumber سٹیپ تعریفیں
        │   └── features/                      # 5 .feature فائلیں (56 منظرنامے)
        │       ├── 1_minimal.feature
        │       ├── 2_scaffold.feature
        │       ├── 3_process.feature
        │       ├── 4_commit.feature
        │       └── 5_integration.feature
        └── functionalTest/kotlin/…           # GradleRunner فعال ٹیسٹ
```

## N0 معاہدے (`education.cccp.codebase` کے ذریعے استعمال)

پلگ ان `education.cccp.codebase` نسخہ `0.0.2` لگاتا ہے
(`libs.versions.toml` سے)۔ codebase-gradle کے ذریعے اسے
`workspace-bom` MEMPHIS کے شائع کردہ مشترکہ N0 معاہدوں تک عبوری رسائی
حاصل ہوتی ہے:

| معاہدہ | کلاکارتی | فراہم کرتا ہے |
|----------|----------|----------|
| `codebase-contracts`          | `education.cccp:codebase-contracts`          | ContextChannel, ChannelBudget, CompositeContext |
| `agent-contracts`             | `education.cccp:agent-contracts`             | Epic, UserStory, GradleTask, AgentState |
| `llm-pool-contracts`          | `education.cccp:llm-pool-contracts`          | LlmInstancePool, LlmInstance, QuotaConfig |
| `opencode-session-contracts`  | `education.cccp:opencode-session-contracts`  | SessionPrompt, SessionResponse, AgentContext |
| `i18n-contracts`              | `education.cccp:i18n-contracts`              | SupportedLanguage, LanguageCatalog, I18nConfig |

> نوٹ: `readme-gradle` `education.cccp.codebase` کو *plugin* کے طور پر
> لگاتا ہے (Gradle پلگ ان پورٹل عرفی نام `alias(libs.plugins.codebase)` کے
> ذریعے)، `implementation` انحصار کے طور پر نہیں۔ براہ راست N0 معاہدہ
> کلاکارتیاں codebase-gradle خود عبوری طور پر استعمال کرتا ہے۔

## اہم انحصارات

| لائبریری | نسخہ | کردار |
|---------|---------|------|
| `net.sourceforge.plantuml:plantuml` | `1.2026.0` | PlantUML بلاکس سے PNG پیدا |
| `org.eclipse.jgit:org.eclipse.jgit` | `7.5.0.202512021534-r` | سسٹم git بائنری کے بغیر Git کارروائیاں |
| `com.fasterxml.jackson.module:jackson-module-kotlin` | `2.21.1` | YAML → Kotlin data class میپنگ |
| `com.fasterxml.jackson.dataformat:jackson-dataformat-yaml` | `2.21.1` | `readme.yml` تجزیہ |
| `io.arrow-kt:arrow-core` | `2.2.2` | فنکشنل اقسام |
| `io.arrow-kt:arrow-fx-coroutines` | `2.2.2` | کوروٹین پر مبنی اثرات |
| `com.github.node-gradle:gradle-node-plugin` | `7.1.0` | Node ٹولنگ (مستقبل HTML سرو) |
| `dev.langchain4j:langchain4j` (+ providers) | `1.14.1` / `1.14.1-beta24` | LLM فراہم کنندگان (compileOnly — AI bundle) |
| `org.asciidoctor:asciidoctor-gradle-jvm` | `5.0.0-alpha.1` | Asciidoctor ٹولنگ (compileOnly) |

LLM فراہم کنندگان اسٹیک (bundle `readme-ai`, `compileOnly`):
`langchain4j-ollama`, `langchain4j-open-ai`, `langchain4j-google-ai-gemini`,
`langchain4j-mistral-ai`, `langchain4j-pgvector`, `langchain4j-embeddings-all-minilm-l6-v2`.

`koog-agents` موجودہ کیٹلاگ میں readme-gradle کی **براہ راست** انحصار
**نہیں** ہے — AI bundle `compileOnly` کے طور پر اعلان کیا گیا ہے اور
استعمال کنندہ بلڈ (جیسے codebase-gradle کا runtime) فراہم کرتا ہے۔

## ٹیسٹ میٹرکس

| کام | دائرہ | نوٹس |
|------|-------|-------|
| `test`           | JUnit5 یونٹ ٹیسٹ | `readme.scenarios.**` اور `readme.ReadmeGradlePluginFunctionalTests` کو خارج کرتا ہے |
| `cucumberTest`   | Cucumber BDD (5 فیچرز, 56 منظرنامے) | `cucumber-junit-platform-engine` استعمال کرتا ہے، `junit-jupiter` انجن خارج |
| `functionalTest`  | GradleRunner فعال ٹیسٹ | الگ سورس سیٹ `functionalTest` جو `plugin-under-test-metadata.properties` کے لیے `gradlePlugin.testSourceSets` میں رجسٹرڈ |
| `check`          | `test` + `functionalTest` + `cucumberTest` مجموعہ | |

ٹیسٹ فریم ورک نسخے (`libs.versions.toml` سے):
- JUnit Platform `1.14.3` · Cucumber `7.34.3` · AssertJ `3.27.7`
- Mockito Kotlin `6.2.3` · Mockito Jupiter `5.23.0`
- Kotlinx Coroutines `1.10.2` · Testcontainers PostgreSQL `1.21.4`

> `logback-classic` کو `testRuntimeClasspath`,
> `testImplementation` اور `functionalTest.runtimeClasspath` سے **خارج**
> کیا گیا ہے تاکہ Gradle کے اپنے لاگنگ سے SLF4J بائنڈنگ تصادم نہ ہو۔

## JVM ٹیوننگ

پلگ ان بلڈ تمام `Test` کاموں پر `-XX:+EnableDynamicAgentLoading` استعمال
کرتا ہے (`tasks.withType<Test>` میں سیٹ)۔ کوئی مخصوص G1GC/SerialGC تقسیم
پروفائل نہیں ہے — اگر آپ کے رنر کو بڑا ہیپ درکار ہو تو `GRADLE_OPTS`
نمبر کریں:

```bash
export GRADLE_OPTS="-Xmx2g"
```

## بلڈ کمانڈز

```bash
./gradlew -p readme-plugin build                            # مکمل بلڈ (compile + تمام ٹیسٹ)
./gradlew -p readme-plugin build -x test                    # صرف compile
./gradlew -p readme-plugin test                             # JUnit5 یونٹ ٹیسٹ
./gradlew -p readme-plugin cucumberTest                     # Cucumber BDD
./gradlew -p readme-plugin functionalTest                   # GradleRunner فعال
./gradlew -p readme-plugin check                            # test + functionalTest + cucumberTest
./gradlew -p readme-plugin publishToMavenLocal              # مقامی اشاعت
./gradlew -p readme-plugin publishAggregationToCentralPortal --no-daemon   # Maven Central (NMCP)
```

## CI پائپ لائن

`.github/workflows/` میں تین workflows موجود ہیں:

1. **`readme_action.yml`** — `صداقت کے مصادر سے README پیدا کریں`۔ کسی بھی
   شاخ پر `README_truth*.adoc` کو چھونے والے پش پر ٹرگر۔ `README_GRADLE_PLUGIN`
   secret داخل کرتا ہے، `./gradlew -p readme-plugin commitGeneratedReadme
   --no-daemon` چلاتا ہے (JGit commit + push)۔ JDK 23 (Temurin)۔
2. **`ci.yml`** — `CI`۔ `readme_action.yml` مکمل ہونے کے بعد (`workflow_run`
   کے ذریعے) اور `README_truth*.adoc` اور `*.md` کو نظر انداز کرتے ہوئے
   push/PR پر چلتا ہے۔ `./gradlew -p readme-plugin clean check --no-daemon`
   چلاتا ہے، ٹیسٹ رپورٹس اپ لوڈ کرتا ہے۔ JDK 23 (Temurin)۔
3. **`integration_test.yml`** — `اینتگریشن ٹیسٹ — commitGeneratedReadme`۔
   صرف مینوئل ڈسپیچ۔ `@integration` ٹیگ کردہ Cucumber منظرنامے چلاتا ہے:
   `./gradlew -p readme-plugin cucumberTest -Dcucumber.filter.tags="@integration"`۔

## اشاعت (NMCP)

- **Plugin Portal**: `com.gradle.plugin-publish` `2.1.0`
  (`alias(libs.plugins.publish)`) کے ذریعے شائع۔ پلگ ان میٹا ڈیٹا:
  `id = education.cccp.readme`, `implementationClass = readme.ReadmePlugin`,
  displayName `"README helper Plugin"`, tags `asciidoc`, `plantuml`,
  `readme`, `documentation`, `github`, `diagram`.
- **Maven Central**: `publishing { repositories { mavenCentral() } }` ہر
  `MavenPublication` پر POM کے ساتھ۔ `signing { useGpgCmd() }` کے ذریعے
  دستخط — جب `CI=true` یا نسخہ `-SNAPSHOT` پر ختم ہو تو چھوڑا جاتا ہے۔
  Developer `cccp-education`, SCM `github.com/cccp-education/readme-gradle`
  کی طرف اشارہ۔
- پروجیکٹ پراپرٹی `relocationGroup`, اگر سیٹ ہو, groupId منتقلی کے لیے POM
  میں `<distributionManagement><relocation>` بلاک خارج کرتی ہے۔
- `java { withJavadocJar(); withSourcesJar() }` — دونوں jars پیکجز۔
- نسخہ کیٹلاگ میں **ہارڈکوڈڈ** ہے: `readme = "0.0.1"`۔ ماڈیول اسے
  `libs.plugins.readme.get().version` سے حل کرتا ہے۔

> اشاعت سے پہلے تصدیق کریں: تمام `implementation` انحصارات ریلیزڈ نسخے
> ہونے چاہئیں (کوئی `-SNAPSHOT` نہیں), اور کوئی بھی `education.cccp:*` عبوری
> پہلے سے Central پر ہو۔

## فن تعمیر دستاویزات

پلگ ان مسدسی ترتیب کی پیروی کرتا ہے (ڈرائیونگ ایڈاپٹر = Gradle کام,
ایپلی کیشن کور = `ReadmePlugin` + ترتیب, ڈومین = `AdocSourceFile`,
ڈرائیون ایڈاپٹر = FileSystem / PlantUML انجن / JGit):

- [README_truth.adoc](../README_truth.adoc) — AsciiDoc حوالہ جس میں 7
  شامل PlantUML خاکے (مسئلہ-حل, صداقت-مصدر, جائزہ-تسلسل, سرگرمی,
  اجزاء, طبقاتی-خاکہ, مسدسی)۔ یہ کینونیکل فن تعمیر بیان ہے۔
- [README_truth_fr.adoc](../README_truth_fr.adoc) — حوالے کا فرانسیسی
  ترجمہ۔

`.github/workflows/readmes/images/{en,fr}/` ڈائریکٹری پلگ ان کے اپنے
پائپ لائن کے ذریعے مرتب کردہ پیدا کردہ PNG کلاکارتی (7 خاکے × 2 زبانیں)
رکھتی ہے۔

## شراکت

1. بلڈ کمپائل ہو: `./gradlew -p readme-plugin build -x test`
2. یونٹ ٹیسٹ سبز: `./gradlew -p readme-plugin test`
3. Cucumber سبز: `./gradlew -p readme-plugin cucumberTest`
4. فعال ٹیسٹ سبز: `./gradlew -p readme-plugin functionalTest`
5. `readme.yml` میں کوئی secret نہیں — یہ gitignored ہے اور
   `README_GRADLE_PLUGIN` CI secret سے داخل ہوتا ہے۔ کبھی حقیقی PAT مرتب
   نہ کریں۔
6. مسدسی روایات کی پیروی کریں (کام ڈرائیونگ ایڈاپٹر کے طور پر, ڈومین ماڈل
   `AdocSourceFile` بنیادی ڈھانچے سے الگ تھلگ)۔

## لائسنس

Apache License 2.0 — دیکھیں [لائسنس](../LICENCE)。

---

_CCCP Education ماحولیاتی نظام کا حصہ — `groupId: education.cccp`۔_