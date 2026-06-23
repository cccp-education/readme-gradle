<!-- translated from README.md rev 0.0.1 -->
# readme-gradle — प्लगइन आंतरिक

> `readme-plugin` Gradle प्लगइन के लिए डेवलपर और योगदानकर्ता गाइड।

[![Maven Central](https://img.shields.io/maven-central/v/education.cccp/readme-plugin.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/education.cccp/readme-plugin)
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/education.cccp.readme.svg?label=Plugin%20Portal)](https://plugins.gradle.org/plugin/education.cccp.readme)
[![CI](https://img.shields.io/github/actions/workflow/status/cccp-education/readme-gradle/ci.yml?branch=main&label=CI)](https://github.com/cccp-education/readme-gradle/actions/workflows/ci.yml)
[![License](https://img.shields.io/github/license/cccp-education/readme-gradle?label=License)](../LICENCE)

- **संस्करण**: `0.0.1` · **Group**: `education.cccp` · **Plugin ID**: `education.cccp.readme`
- **टूलचेन**: Java 24 · Kotlin 2.3.20 · Gradle 9.5.1
- **बिल्ड**: `./gradlew -p readme-plugin build -x test` · **परीक्षण**: `./gradlew -p readme-plugin check` · **कवरेज**: गेटेड नहीं

🌐 Languages: [English](README.md) | [中文](README.zh.md) | **हिन्दी** | [Español](README.es.md) | [Français](README.fr.md) | [العربية](README.ar.md) | [বাংলা](README.bn.md) | [Português](README.pt.md) | [Русский](README.ru.md) | [اردو](README.ur.md)

---

## मॉड्यूल अभिन्यास

```
readme-gradle/
├── build.gradle.kts                          # उपभोक्ता हार्नेस — education.cccp.readme लागू करता है
├── settings.gradle.kts                        # pluginManagement: mavenLocal + portal + central
├── README_truth.adoc                          # EN सत्य स्रोत (इस README का जनक संदर्भ)
├── README_truth_fr.adoc                       # FR सत्य स्रोत
├── readme.yml                                 # gitignored — CI secret से इंजेक्ट
└── readme-plugin/
    ├── build.gradle.kts                       # प्लगइन मॉड्यूल (signing, java-gradle-plugin, nmcp)
    ├── gradle/libs.versions.toml              # संस्करण सूची
    └── src/
        ├── main/kotlin/readme/
        │   ├── ReadmePlugin.kt                # प्लगइन प्रवेश बिंदु — 3 कार्य पंजीकृत करता है
        │   ├── ReadmePlantUmlConfig.kt       # Jackson YAML विन्यास मॉडल (+ Source/Output/Git)
        │   ├── ScaffoldTask.kt               # generateReadme — readme.yml + workflow स्कैफ़ोल्ड
        │   ├── ProcessReadmeTask.kt          # transformReadme — PNG निर्माण + ब्लॉक पुनर्लेखन
        │   ├── CommitGeneratedReadmeTask.kt  # commitGeneratedReadme — JGit add/commit/push
        │   ├── AdocSourceFile.kt             # सत्य-स्रोत मॉडल + भाषा पहचान
        │   ├── GitUtils.kt                   # git root से imgDir हल
        │   ├── GitRemoteValidator.kt         # रिमोट सत्यापन इंटरफ़ेस
        │   └── JGitRemoteValidator.kt        # JGit कार्यान्वयन
        ├── test/
        │   ├── kotlin/…                      # JUnit5 यूनिट परीक्षण
        │   ├── scenarios/…                   # Cucumber स्टेप परिभाषाएँ
        │   └── features/                      # 5 .feature फ़ाइलें (56 परिदृश्य)
        │       ├── 1_minimal.feature
        │       ├── 2_scaffold.feature
        │       ├── 3_process.feature
        │       ├── 4_commit.feature
        │       └── 5_integration.feature
        └── functionalTest/kotlin/…           # GradleRunner फंक्शनल परीक्षण
```

## N0 अनुबंध (`education.cccp.codebase` के माध्यम से उपभुक्त)

प्लगइन `education.cccp.codebase` संस्करण `0.0.2` लागू करता है
(`libs.versions.toml` से)। codebase-gradle के माध्यम से इसे
`workspace-bom` MEMPHIS द्वारा प्रकाशित साझा N0 अनुबंधों तक पारगमन
पहुँच मिलती है:

| अनुबंध | कलाकृति | प्रदान करता है |
|----------|----------|----------|
| `codebase-contracts`          | `education.cccp:codebase-contracts`          | ContextChannel, ChannelBudget, CompositeContext |
| `agent-contracts`             | `education.cccp:agent-contracts`             | Epic, UserStory, GradleTask, AgentState |
| `llm-pool-contracts`          | `education.cccp:llm-pool-contracts`          | LlmInstancePool, LlmInstance, QuotaConfig |
| `opencode-session-contracts`  | `education.cccp:opencode-session-contracts`  | SessionPrompt, SessionResponse, AgentContext |
| `i18n-contracts`              | `education.cccp:i18n-contracts`              | SupportedLanguage, LanguageCatalog, I18nConfig |

> नोट: `readme-gradle` `education.cccp.codebase` को *plugin* के रूप में
> लागू करता है (Gradle प्लगइन पोर्टल उपनाम `alias(libs.plugins.codebase)`
> के माध्यम से), `implementation` निर्भरता के रूप में नहीं। प्रत्यक्ष N0
> अनुबंध कलाकृतियाँ codebase-gradle द्वारा स्वयं पारगमन रूप से उपभुक्त
> होती हैं।

## प्रमुख निर्भरताएँ

| लाइब्रेरी | संस्करण | भूमिका |
|---------|---------|------|
| `net.sourceforge.plantuml:plantuml` | `1.2026.0` | PlantUML ब्लॉकों से PNG निर्माण |
| `org.eclipse.jgit:org.eclipse.jgit` | `7.5.0.202512021534-r` | सिस्टम git बायनरी के बिना Git संक्रियाएँ |
| `com.fasterxml.jackson.module:jackson-module-kotlin` | `2.21.1` | YAML → Kotlin data class मानचित्रण |
| `com.fasterxml.jackson.dataformat:jackson-dataformat-yaml` | `2.21.1` | `readme.yml` का पार्स |
| `io.arrow-kt:arrow-core` | `2.2.2` | फंक्शनल प्रकार |
| `io.arrow-kt:arrow-fx-coroutines` | `2.2.2` | कोरूटीन-आधारित प्रभाव |
| `com.github.node-gradle:gradle-node-plugin` | `7.1.0` | Node टूलिंग (भविष्य HTML सेवा) |
| `dev.langchain4j:langchain4j` (+ providers) | `1.14.1` / `1.14.1-beta24` | LLM प्रदाता (compileOnly — AI bundle) |
| `org.asciidoctor:asciidoctor-gradle-jvm` | `5.0.0-alpha.1` | Asciidoctor टूलिंग (compileOnly) |

LLM प्रदाता स्टैक (bundle `readme-ai`, `compileOnly`):
`langchain4j-ollama`, `langchain4j-open-ai`, `langchain4j-google-ai-gemini`,
`langchain4j-mistral-ai`, `langchain4j-pgvector`, `langchain4j-embeddings-all-minilm-l6-v2`.

वर्तमान सूची में `koog-agents` readme-gradle की **प्रत्यक्ष** निर्भरता
**नहीं** है — AI bundle `compileOnly` घोषित है और उपभोक्ता बिल्ड
(जैसे codebase-gradle का रनटाइम) द्वारा प्रदान किया जाता है।

## परीक्षण आव्यूह

| कार्य | क्षेत्र | टिप्पणियाँ |
|------|-------|-------|
| `test`           | JUnit5 यूनिट परीक्षण | `readme.scenarios.**` और `readme.ReadmeGradlePluginFunctionalTests` को बाहर रखता है |
| `cucumberTest`   | Cucumber BDD (5 विशेषताएँ, 56 परिदृश्य) | `cucumber-junit-platform-engine` उपयोग करता है, `junit-jupiter` इंजन बाहर |
| `functionalTest`  | GradleRunner फंक्शनल परीक्षण | पृथक स्रोत समूह `functionalTest` जो `plugin-under-test-metadata.properties` हेतु `gradlePlugin.testSourceSets` में पंजीकृत |
| `check`          | `test` + `functionalTest` + `cucumberTest` समेकित | |

परीक्षण ढाँचा संस्करण (`libs.versions.toml` से):
- JUnit Platform `1.14.3` · Cucumber `7.34.3` · AssertJ `3.27.7`
- Mockito Kotlin `6.2.3` · Mockito Jupiter `5.23.0`
- Kotlinx Coroutines `1.10.2` · Testcontainers PostgreSQL `1.21.4`

> `logback-classic` को `testRuntimeClasspath`,
> `testImplementation` और `functionalTest.runtimeClasspath` से **बाहर
> रखा** गया है ताकि Gradle के अपने लॉगिंग से SLF4J बाइंडिंग टकराव न हो।

## JVM ट्यूनिंग

प्लगइन बिल्ड सभी `Test` कार्यों पर `-XX:+EnableDynamicAgentLoading`
उपयोग करता है (`tasks.withType<Test>` में सेट)। कोई विशिष्ट
G1GC/SerialGC विभाजन प्रोफ़ाइल नहीं है — यदि आपके रनर को बड़ा हीप
चाहिए तो `GRADLE_OPTS` समायोजित करें:

```bash
export GRADLE_OPTS="-Xmx2g"
```

## बिल्ड आदेश

```bash
./gradlew -p readme-plugin build                            # पूर्ण बिल्ड (कंपाइल + सभी परीक्षण)
./gradlew -p readme-plugin build -x test                    # केवल कंपाइल
./gradlew -p readme-plugin test                             # JUnit5 यूनिट परीक्षण
./gradlew -p readme-plugin cucumberTest                     # Cucumber BDD
./gradlew -p readme-plugin functionalTest                   # GradleRunner फंक्शनल
./gradlew -p readme-plugin check                            # test + functionalTest + cucumberTest
./gradlew -p readme-plugin publishToMavenLocal              # स्थानीय प्रकाशन
./gradlew -p readme-plugin publishAggregationToCentralPortal --no-daemon   # Maven Central (NMCP)
```

## CI पाइपलाइन

`.github/workflows/` में तीन वर्कफ़्लो हैं:

1. **`readme_action.yml`** — `सत्य स्रोतों से README उत्पन्न करें`। किसी भी
   शाखा पर `README_truth*.adoc` को स्पर्श करने वाले पुश पर ट्रिगर।
   `README_GRADLE_PLUGIN` secret इंजेक्ट करता है, `./gradlew -p readme-plugin
   commitGeneratedReadme --no-daemon` चलाता है (JGit commit + push)।
   JDK 23 (Temurin)।
2. **`ci.yml`** — `CI`। `readme_action.yml` पूर्ण होने के बाद (`workflow_run`
   द्वारा) और `README_truth*.adoc` तथा `*.md` को अनदेखा करते हुए पुश/PR पर
   चलता है। `./gradlew -p readme-plugin clean check --no-daemon` चलाता है,
   परीक्षण रिपोर्ट अपलोड करता है। JDK 23 (Temurin)।
3. **`integration_test.yml`** — `एकीकरण परीक्षण — commitGeneratedReadme`।
   केवल मैन्युअल डिस्पैच। `@integration` टैग किए Cucumber परिदृश्य चलाता
   है: `./gradlew -p readme-plugin cucumberTest -Dcucumber.filter.tags="@integration"`।

## प्रकाशन (NMCP)

- **Plugin Portal**: `com.gradle.plugin-publish` `2.1.0`
  (`alias(libs.plugins.publish)`) के माध्यम से प्रकाशित। प्लगइन मेटाडेटा:
  `id = education.cccp.readme`, `implementationClass = readme.ReadmePlugin`,
  displayName `"README helper Plugin"`, tags `asciidoc`, `plantuml`,
  `readme`, `documentation`, `github`, `diagram`.
- **Maven Central**: `publishing { repositories { mavenCentral() } }` प्रत्येक
  `MavenPublication` पर POM के साथ। `signing { useGpgCmd() }` द्वारा हस्ताक्षर —
  जब `CI=true` या संस्करण `-SNAPSHOT` पर समाप्त हो तो छोड़ा जाता है।
  Developer `cccp-education`, SCM `github.com/cccp-education/readme-gradle` को संकेत।
- प्रोजेक्ट गुण `relocationGroup`, यदि सेट हो, तो groupId प्रवास के लिए POM
  में `<distributionManagement><relocation>` ब्लॉक उत्सर्जित करता है।
- `java { withJavadocJar(); withSourcesJar() }` — दोनों jars संपैकेज।
- संस्करण सूची में **हार्डकोडेड** है: `readme = "0.0.1"`। मॉड्यूल इसे
  `libs.plugins.readme.get().version` से हल करता है।

> प्रकाशन से पहले पुष्टि: सभी `implementation` निर्भरताएँ रिलीज़्ड संस्करण
> होनी चाहिए (कोई `-SNAPSHOT` नहीं), और कोई भी `education.cccp:*` पारगमन
> पहले से Central पर हो।

## वास्तुकला दस्तावेज़

प्लगइन षट्भुज अभिन्यास का अनुसरण करता है (ड्राइविंग एडेप्टर = Gradle कार्य,
अनुप्रयोग कोर = `ReadmePlugin` + विन्यास, डोमेन = `AdocSourceFile`,
ड्रिवन एडेप्टर = FileSystem / PlantUML इंजन / JGit):

- [README_truth.adoc](../README_truth.adoc) — AsciiDoc संदर्भ जिसमें 7 एम्बेडेड
  PlantUML आरेख हैं (समस्या-समाधान, सत्य-स्रोत, अवलोकन-अनुक्रम,
  गतिविधि, घटक, वर्ग-आरेख, षट्भुज)। यह वास्तुकला का विहित विवरण है।
- [README_truth_fr.adoc](../README_truth_fr.adoc) — संदर्भ का फ़्रेंच अनुवाद।

`.github/workflows/readmes/images/{en,fr}/` निर्देशिका में प्लगइन के अपने
पाइपलाइन द्वारा सौंपे गए उत्पन्न PNG कलाकृति (7 आरेख × 2 भाषाएँ) सुरक्षित हैं।

## योगदान

1. बिल्ड कंपाइल हो: `./gradlew -p readme-plugin build -x test`
2. यूनिट परीक्षण हरित: `./gradlew -p readme-plugin test`
3. Cucumber हरित: `./gradlew -p readme-plugin cucumberTest`
4. फंक्शनल परीक्षण हरित: `./gradlew -p readme-plugin functionalTest`
5. `readme.yml` में कोई secret नहीं — यह gitignored है और `README_GRADLE_PLUGIN`
   CI secret से इंजेक्ट होता है। कभी वास्तविक PAT सौंपें नहीं।
6. षट्भुज परिपाटियों का पालन करें (कार्य ड्राइविंग एडेप्टर के रूप में, डोमेन मॉडल
   `AdocSourceFile` अवसंरचना से विलग)।

## लाइसेंस

Apache License 2.0 — देखें [लाइसेंस](../LICENCE)।

---

_CCCP Education पारिस्थितिकी तंत्र का भाग — `groupId: education.cccp`._