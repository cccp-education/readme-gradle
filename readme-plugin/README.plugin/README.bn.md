<!-- translated from README.md rev 0.0.1 -->
# readme-gradle — প্লাগইন অভ্যন্তরীণ

> `readme-plugin` Gradle প্লাগইনের জন্য ডেভেলপার ও অবদানকারী গাইড।

[![Maven Central](https://img.shields.io/maven-central/v/education.cccp/readme-plugin.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/education.cccp/readme-plugin)
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/education.cccp.readme.svg?label=Plugin%20Portal)](https://plugins.gradle.org/plugin/education.cccp.readme)
[![CI](https://img.shields.io/github/actions/workflow/status/cccp-education/readme-gradle/ci.yml?branch=main&label=CI)](https://github.com/cccp-education/readme-gradle/actions/workflows/ci.yml)
[![License](https://img.shields.io/github/license/cccp-education/readme-gradle?label=License)](../LICENCE)

- **সংস্করণ**: `0.0.1` · **Group**: `education.cccp` · **Plugin ID**: `education.cccp.readme`
- **টুলচেইন**: Java 24 · Kotlin 2.3.20 · Gradle 9.5.1
- **বিল্ড**: `./gradlew -p readme-plugin build -x test` · **পরীক্ষা**: `./gradlew -p readme-plugin check` · **কভারেজ**: গেটেড নয়

🌐 Languages: [English](README.md) | [中文](README.zh.md) | [हिन्दी](README.hi.md) | [Español](README.es.md) | [Français](README.fr.md) | [العربية](README.ar.md) | **বাংলা** | [Português](README.pt.md) | [Русский](README.ru.md) | [اردو](README.ur.md)

---

## মডিউল বিন্যাস

```
readme-gradle/
├── build.gradle.kts                          # ভোক্তা হার্নেস — education.cccp.readme প্রয়োগ করে
├── settings.gradle.kts                        # pluginManagement: mavenLocal + portal + central
├── README_truth.adoc                          # EN সত্যের উৎস (এই README-এর জনক তথ্যসূত্র)
├── README_truth_fr.adoc                       # FR সত্যের উৎস
├── readme.yml                                 # gitignored — CI সিক্রেট থেকে ইনজেক্ট
└── readme-plugin/
    ├── build.gradle.kts                       # প্লাগইন মডিউল (signing, java-gradle-plugin, nmcp)
    ├── gradle/libs.versions.toml              # সংস্করণ তালিকা
    └── src/
        ├── main/kotlin/readme/
        │   ├── ReadmePlugin.kt                # প্লাগইন প্রবেশ বিন্দু — ৩টি কাজ নিবন্ধন করে
        │   ├── ReadmePlantUmlConfig.kt       # Jackson YAML কনফিগ মডেল (+ Source/Output/Git)
        │   ├── ScaffoldTask.kt               # generateReadme — readme.yml + workflow স্ক্যাফোল্ড
        │   ├── ProcessReadmeTask.kt          # transformReadme — PNG নির্মাণ + ব্লক পুনর্লিখন
        │   ├── CommitGeneratedReadmeTask.kt  # commitGeneratedReadme — JGit add/commit/push
        │   ├── AdocSourceFile.kt             # সত্য-উৎস মডেল + ভাষা শনাক্তকরণ
        │   ├── GitUtils.kt                   # git রুট থেকে imgDir সমাধান
        │   ├── GitRemoteValidator.kt         # রিমোট যাচাইয়ের ইন্টারফেস
        │   └── JGitRemoteValidator.kt        # JGit বাস্তবায়ন
        ├── test/
        │   ├── kotlin/…                      # JUnit5 ইউনিট পরীক্ষা
        │   ├── scenarios/…                   # Cucumber স্টেপ সংজ্ঞা
        │   └── features/                      # 5টি .feature ফাইল (56 পরিস্থিতি)
        │       ├── 1_minimal.feature
        │       ├── 2_scaffold.feature
        │       ├── 3_process.feature
        │       ├── 4_commit.feature
        │       └── 5_integration.feature
        └── functionalTest/kotlin/…           # GradleRunner ফাংশনাল পরীক্ষা
```

## N0 চুক্তি (`education.cccp.codebase` এর মাধ্যমে ভোক্তা)

প্লাগইন `education.cccp.codebase` সংস্করণ `0.0.2` প্রয়োগ করে
(`libs.versions.toml` থেকে)। codebase-gradle-এর মাধ্যমে এটি
`workspace-bom` MEMPHIS-এর প্রকাশিত সাঝা N0 চুক্তিতে ট্রানজিটিভ অ্যাক্সেস
পায়:

| চুক্তি | কলাকৃতি | প্রদান করে |
|----------|----------|----------|
| `codebase-contracts`          | `education.cccp:codebase-contracts`          | ContextChannel, ChannelBudget, CompositeContext |
| `agent-contracts`             | `education.cccp:agent-contracts`             | Epic, UserStory, GradleTask, AgentState |
| `llm-pool-contracts`          | `education.cccp:llm-pool-contracts`          | LlmInstancePool, LlmInstance, QuotaConfig |
| `opencode-session-contracts`  | `education.cccp:opencode-session-contracts`  | SessionPrompt, SessionResponse, AgentContext |
| `i18n-contracts`              | `education.cccp:i18n-contracts`              | SupportedLanguage, LanguageCatalog, I18nConfig |

> নোট: `readme-gradle` `education.cccp.codebase`-কে *plugin* হিসেবে
> প্রয়োগ করে (Gradle প্লাগইন পোর্টাল উপনাম `alias(libs.plugins.codebase)`
> এর মাধ্যমে), `implementation` নির্ভরতা হিসেবে নয়। প্রত্যক্ষ N0 চুক্তি
> কলাকৃতিগুলো codebase-gradle নিজেই ট্রানজিটিভভাবে ভোক্তা হয়।

## মূল নির্ভরতা

| লাইব্রেরি | সংস্করণ | ভূমিকা |
|---------|---------|------|
| `net.sourceforge.plantuml:plantuml` | `1.2026.0` | PlantUML ব্লক থেকে PNG নির্মাণ |
| `org.eclipse.jgit:org.eclipse.jgit` | `7.5.0.202512021534-r` | সিস্টেম git বাইনারি ছাড়া Git ক্রিয়াকলাপ |
| `com.fasterxml.jackson.module:jackson-module-kotlin` | `2.21.1` | YAML → Kotlin data class ম্যাপিং |
| `com.fasterxml.jackson.dataformat:jackson-dataformat-yaml` | `2.21.1` | `readme.yml` পার্স |
| `io.arrow-kt:arrow-core` | `2.2.2` | ফাংশনাল প্রকার |
| `io.arrow-kt:arrow-fx-coroutines` | `2.2.2` | কোরোটিন-ভিত্তিক প্রভাব |
| `com.github.node-gradle:gradle-node-plugin` | `7.1.0` | Node টুলিং (ভবিষ্যৎ HTML সেবা) |
| `dev.langchain4j:langchain4j` (+ providers) | `1.14.1` / `1.14.1-beta24` | LLM প্রদাতা (compileOnly — AI bundle) |
| `org.asciidoctor:asciidoctor-gradle-jvm` | `5.0.0-alpha.1` | Asciidoctor টুলিং (compileOnly) |

LLM প্রদাতা স্ট্যাক (bundle `readme-ai`, `compileOnly`):
`langchain4j-ollama`, `langchain4j-open-ai`, `langchain4j-google-ai-gemini`,
`langchain4j-mistral-ai`, `langchain4j-pgvector`, `langchain4j-embeddings-all-minilm-l6-v2`.

`koog-agents` বর্তমান তালিকায় readme-gradle-এর **প্রত্যক্ষ** নির্ভরতা
**নয়** — AI bundle `compileOnly` হিসেবে ঘোষিত এবং ভোক্তা বিল্ড (যেমন
codebase-gradle-এর রানটাইম) দ্বারা সরবরাহ করা হয়।

## পরীক্ষা ম্যাট্রিক্স

| কাজ | পরিসর | মন্তব্য |
|------|-------|-------|
| `test`           | JUnit5 ইউনিট পরীক্ষা | `readme.scenarios.**` এবং `readme.ReadmeGradlePluginFunctionalTests` বাদ দেয় |
| `cucumberTest`   | Cucumber BDD (5 ফিচার, 56 পরিস্থিতি) | `cucumber-junit-platform-engine` ব্যবহার করে, `junit-jupiter` ইঞ্জিন বাদ |
| `functionalTest`  | GradleRunner ফাংশনাল পরীক্ষা | পৃথক সোর্স সেট `functionalTest` যা `plugin-under-test-metadata.properties`-এর জন্য `gradlePlugin.testSourceSets`-এ নিবন্ধিত |
| `check`          | `test` + `functionalTest` + `cucumberTest` সমষ্টি | |

পরীক্ষা ফ্রেমওয়ার্ক সংস্করণ (`libs.versions.toml` থেকে):
- JUnit Platform `1.14.3` · Cucumber `7.34.3` · AssertJ `3.27.7`
- Mockito Kotlin `6.2.3` · Mockito Jupiter `5.23.0`
- Kotlinx Coroutines `1.10.2` · Testcontainers PostgreSQL `1.21.4`

> `logback-classic` কে `testRuntimeClasspath`,
> `testImplementation` এবং `functionalTest.runtimeClasspath` থেকে
> **বাদ দেওয়া** হয়েছে যাতে Gradle-এর নিজস্ব লগিং-এর সাথে SLF4J বাইন্ডিং
> দ্বন্দ্ব না হয়।

## JVM টিউনিং

প্লাগইন বিল্ড সকল `Test` কাজে `-XX:+EnableDynamicAgentLoading` ব্যবহার
করে (`tasks.withType<Test>`-এ সেট)। কোনো স্বতন্ত্র G1GC/SerialGC বিভাজন
প্রোফাইল নেই — আপনার রানারে বড় হিপ দরকার হলে `GRADLE_OPTS`মাযোজন করুন:

```bash
export GRADLE_OPTS="-Xmx2g"
```

## বিল্ড আদেশ

```bash
./gradlew -p readme-plugin build                            # সম্পূর্ণ বিল্ড (compile + সকল পরীক্ষা)
./gradlew -p readme-plugin build -x test                    # শুধু compile
./gradlew -p readme-plugin test                             # JUnit5 ইউনিট পরীক্ষা
./gradlew -p readme-plugin cucumberTest                     # Cucumber BDD
./gradlew -p readme-plugin functionalTest                   # GradleRunner ফাংশনাল
./gradlew -p readme-plugin check                            # test + functionalTest + cucumberTest
./gradlew -p readme-plugin publishToMavenLocal              # স্থানীয় প্রকাশনা
./gradlew -p readme-plugin publishAggregationToCentralPortal --no-daemon   # Maven Central (NMCP)
```

## CI পাইপলাইন

`.github/workflows/`-এ তিনটি workflow রয়েছে:

1. **`readme_action.yml`** — `সত্য উৎস থেকে README নির্মাণ`। যে কোনো শাখায়
   `README_truth*.adoc` স্পর্শ করে এমন push-এ ট্রিগার। `README_GRADLE_PLUGIN`
   সিক্রেট ইনজেক্ট করে, `./gradlew -p readme-plugin commitGeneratedReadme
   --no-daemon` চালায় (JGit commit + push)। JDK 23 (Temurin)।
2. **`ci.yml`** — `CI`। `readme_action.yml` সম্পূর্ণ হওয়ার পরে (`workflow_run`
   এর মাধ্যমে) এবং `README_truth*.adoc` ও `*.md` উপেক্ষা করে push/PR-এ চলে।
   `./gradlew -p readme-plugin clean check --no-daemon` চালায়, পরীক্ষা
   রিপোর্ট আপলোড করে। JDK 23 (Temurin)।
3. **`integration_test.yml`** — `ইন্টিগ্রেশন টেস্ট — commitGeneratedReadme`।
   শুধু ম্যানুয়াল ডিসপ্যাচ। `@integration` ট্যাগযুক্ত Cucumber পরিস্থিতি
   চালায়: `./gradlew -p readme-plugin cucumberTest -Dcucumber.filter.tags="@integration"`।

## প্রকাশনা (NMCP)

- **Plugin Portal**: `com.gradle.plugin-publish` `2.1.0`
  (`alias(libs.plugins.publish)`) এর মাধ্যমে প্রকাশিত। প্লাগইন মেটাডেটা:
  `id = education.cccp.readme`, `implementationClass = readme.ReadmePlugin`,
  displayName `"README helper Plugin"`, tags `asciidoc`, `plantuml`,
  `readme`, `documentation`, `github`, `diagram`.
- **Maven Central**: `publishing { repositories { mavenCentral() } }` প্রতিটি
  `MavenPublication`-এ POM সহ। `signing { useGpgCmd() }` দ্বারা স্বাক্ষর —
  যখন `CI=true` বা সংস্করণ `-SNAPSHOT`-এ শেষ তখন বাদ দেওয়া হয়।
  Developer `cccp-education`, SCM `github.com/cccp-education/readme-gradle`-এ
  নির্দেশ।
- প্রোজেক্ট বৈশিষ্ট্য `relocationGroup`, যদি সেট করা হয়, groupId স্থানান্তরের
  জন্য POM-এ `<distributionManagement><relocation>` ব্লক নির্গত করে।
- `java { withJavadocJar(); withSourcesJar() }` — উভয় jar প্যাকেজ করা।
- সংস্করণ তালিকায় **হার্ডকোডেড**: `readme = "0.0.1"`। মডিউল এটি
  `libs.plugins.readme.get().version` দ্বারা সমাধান করে।

> প্রকাশনার আগে নিশ্চিত করুন: সকল `implementation` নির্ভরতা অবশ্যই রিলিজড
> সংস্করণ হতে হবে (কোনো `-SNAPSHOT` নয়), এবং যে কোনো `education.cccp:*`
> ট্রানজিটিভ পূর্বে Central-এ থাকতে হবে।

## স্থাপত্য নথি

প্লাগইন একটি ষড়ভুজ বিন্যাস অনুসরণ করে (চালক অ্যাডাপ্টার = Gradle কাজ,
অ্যাপ্লিকেশন কোর = `ReadmePlugin` + কনফিগ, ডোমেইন = `AdocSourceFile`,
চালিত অ্যাডাপ্টার = FileSystem / PlantUML ইঞ্জিন / JGit):

- [README_truth.adoc](../README_truth.adoc) — AsciiDoc তথ্যসূত্র যাতে ৭টি
  এম্বেডেড PlantUML চিত্র রয়েছে (সমস্যা-সমাধান, সত্য-উৎস, পরিদৃশ্য-ক্রম,
  কর্ম, উপাদান, শ্রেণী-চিত্র, ষড়ভুজ)। এটি ক্যানোনিকাল স্থাপত্য বিবরণ।
- [README_truth_fr.adoc](../README_truth_fr.adoc) — তথ্যসূত্রের ফরাসি অনুবাদ।

`.github/workflows/readmes/images/{en,fr}/` ডিরেক্টরিতে প্লাগইনের নিজস্ব
পাইপলাইন দ্বারা কমিট করা নির্মিত PNG কলাকৃতি (৭ চিত্র × ২ ভাষা) রয়েছে।

## অবদান

1. বিল্ড কম্পাইল হয়: `./gradlew -p readme-plugin build -x test`
2. ইউনিট পরীক্ষা সবুজ: `./gradlew -p readme-plugin test`
3. Cucumber সবুজ: `./gradlew -p readme-plugin cucumberTest`
4. ফাংশনাল পরীক্ষা সবুজ: `./gradlew -p readme-plugin functionalTest`
5. `readme.yml`-এ কোনো সিক্রেট নেই — এটি gitignored এবং `README_GRADLE_PLUGIN`
   CI সিক্রেট থেকে ইনজেক্ট হয়। কখনও প্রকৃত PAT কমিট করবেন না।
6. ষড়ভুজ রীতিনীতি অনুসরণ করুন (কাজ চালক অ্যাডাপ্টার হিসেবে, ডোমেইন মডেল
   `AdocSourceFile` অবকাঠামো থেকে বিচ্ছিন্ন)।

## লাইসেন্স

Apache License 2.0 — দেখুন [লাইসেন্স](../LICENCE)।

---

_CCCP Education ইকোসিস্টেমের অংশ — `groupId: education.cccp`._