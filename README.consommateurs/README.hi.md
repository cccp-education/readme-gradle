<!-- translated from README.md rev 0.0.1 -->
# readme-gradle — उपभोक्ता गाइड

> Gradle प्लगइन जो `README_truth.adoc` स्रोत फ़ाइलों से GitHub-संगत
> `README.adoc` फ़ाइलें उत्पन्न करता है, और GitHub Actions तथा JGit के
> माध्यम से मूल `[plantuml]` ब्लॉकों को सौंपे गए PNG चित्रों में रेंडर
> करता है।

[![Maven Central](https://img.shields.io/maven-central/v/education.cccp/readme-plugin.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/education.cccp/readme-plugin)
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/education.cccp.readme.svg?label=Plugin%20Portal)](https://plugins.gradle.org/plugin/education.cccp.readme)
[![CI](https://img.shields.io/github/actions/workflow/status/cccp-education/readme-gradle/ci.yml?branch=main&label=CI)](https://github.com/cccp-education/readme-gradle/actions/workflows/ci.yml)
[![License](https://img.shields.io/github/license/cccp-education/readme-gradle?label=License)](../LICENCE)

- **संस्करण**: `0.0.1` · **Group**: `education.cccp` · **Plugin ID**: `education.cccp.readme`
- **टूलचेन**: Java 24 · Kotlin 2.3.20 · Gradle 9.5.1
- **बिल्ड**: `./gradlew build` · **परीक्षण**: `./gradlew check` (JUnit5 + 56 Cucumber परिदृश्य, 5 विशेषताओं में)
- **कवरेज**: गेटेड नहीं (`check` में कोई Kover नियम वायर्ड नहीं)

🌐 Languages: [English](README.md) | [中文](README.zh.md) | **हिन्दी** | [Español](README.es.md) | [Français](README.fr.md) | [العربية](README.ar.md) | [বাংলা](README.bn.md) | [Português](README.pt.md) | [Русский](README.ru.md) | [اردو](README.ur.md)

---

## यह क्या करता है

GitHub मूल रूप से AsciiDoc फ़ाइलों में एम्बेड किए गए PlantUML सिंटैक्स को
रेंडर नहीं करता। `readme-gradle` इस वर्कअराउंड को स्वचालित करता है: आप
एकमात्र सत्य स्रोत (`README_truth.adoc`) संपादित करते हैं, प्लगइन प्रत्येक
`[plantuml, name, png]` ब्लॉक निकालता है, PNG चित्र उत्पन्न करता है,
ब्लॉक को `image::` संदर्भ के रूप में फिर से लिखता है, और JGit के माध्यम
से पुनः लिखे गए `README.adoc` तथा चित्रों को आपके रिपॉजिटरी में सौंपता
है — CI रनर पर किसी सिस्टम `git` बायनरी की आवश्यकता नहीं।

```
README_truth.adoc → generateReadme → transformReadme → commitGeneratedReadme → README.adoc + images/
                    (scaffolds yml)  (PNG + rewrite)    (JGit add + commit + push)
```

## त्वरित प्रारंभ

### 1. प्लगइन लागू करें

```gradle
plugins {
    id("education.cccp.readme") version "0.0.1"
}
```

### 2. अपना सत्य स्रोत लिखें

प्रोजेक्ट रूट पर `README_truth.adoc` बनाएँ। मूल PlantUML ब्लॉक का उपयोग करें:

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

फ़्रेंच वैरिएंट के लिए फ़ाइल का नाम `README_plantuml_fr.adoc` रखें ——
`_fr` प्रत्यय पहचाना जाता है और `README_fr.adoc` उत्पन्न करता है।

### 3. `readme.yml` के माध्यम से विन्यास

प्लगइन प्रोजेक्ट रूट से `readme.yml` पढ़ता है। **कभी भी वास्तविक टोकन
सौंपें नहीं** —— पूरी फ़ाइल (टोकन सहित) GitHub secret `README_GRADLE_PLUGIN`
में रखें और CI में इंजेक्ट करें:

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

CI चरण:

```yaml
- name: Inject plugin config
  run: echo "${{ secrets.README_GRADLE_PLUGIN }}" > readme.yml

- name: Generate README and commit via JGit
  run: ./gradlew -p readme-plugin commitGeneratedReadme --no-daemon
```

### 4. पाइपलाइन चलाएँ

```bash
./gradlew commitGeneratedReadme          # पूर्ण पाइपलाइन (CI)
./gradlew transformReadme               # केवल PNG निर्माण + पुनर्लेखन
./gradlew generateReadme                 # अनुपस्थित होने पर readme.yml + workflow स्कैफ़ोल्ड
```

## उपलब्ध कार्य

| कार्य | समूह | विवरण |
|------|-------|-------------|
| `generateReadme`         | generate  | अनुपस्थित होने पर `readme.yml` और `.github/workflows/readme_action.yml` बनाता है |
| `transformReadme`        | transform | `README_truth*.adoc` से PNG उत्पन्न करता है और `README*.adoc` पुनर्लिखित करता है (`generateReadme` पर निर्भर) |
| `commitGeneratedReadme`  | deploy    | JGit द्वारा उत्पन्न `README*.adoc` + चित्र सौंपता और पुश करता है (केवल CI, `transformReadme` पर निर्भर) |

## एक्सटेंशन DSL

प्लगइन में **कोई Gradle एक्सटेंशन ब्लॉक नहीं है**। सभी विन्यास प्रोजेक्ट
रूट पर `readme.yml` फ़ाइल से पढ़े जाते हैं (Jackson YAML द्वारा
`ReadmePlantUmlConfig` में पार्स)। यदि `readme.yml` अनुपस्थित या रिक्त
है, तो चुपचाप उचित डिफ़ॉल्ट का उपयोग होता है।

| विन्यास अनुभाग | क्षेत्र | डिफ़ॉल्ट |
|----------------|-------|---------|
| `source`  | `dir`         | `"."` |
| `source`  | `defaultLang` | `"en"` |
| `output`  | `imgDir`      | `".github/workflows/readmes/images"` |
| `git`     | `userName`        | `"github-actions[bot]"` |
| `git`     | `userEmail`       | `"github-actions[bot]@users.noreply.github.com"` |
| `git`     | `commitMessage`   | `"chore: generate readme [skip ci]"` |
| `git`     | `token`           | `""` (अवश्य सेट करें — रिक्त/प्लेसहोल्डर होने पर रनटाइम त्रुटि) |
| `git`     | `watchedBranches`  | `["main", "master"]` |

## पूर्वापेक्षाएँ

- **Java** 24+ (Kotlin 2.3.20 टूलचेन)
- **Gradle** 9.5.1+
- **GitHub** रिपॉजिटरी लिखने की पहुँच सहित (JGit push के लिए)
- **GitHub secret** `README_GRADLE_PLUGIN` जिसमें वैध PAT के साथ पूरा `readme.yml` हो

## बिल्ड और परीक्षण

```bash
./gradlew -p readme-plugin build              # पूर्ण बिल्ड (कंपाइल + यूनिट + cucumber + फंक्शनल)
./gradlew -p readme-plugin test               # केवल JUnit5 यूनिट परीक्षण
./gradlew -p readme-plugin cucumberTest       # Cucumber BDD (5 विशेषता फ़ाइलें, 56 परिदृश्य)
./gradlew -p readme-plugin functionalTest     # GradleRunner फंक्शनल परीक्षण
./gradlew -p readme-plugin check              # test + functionalTest + cucumberTest
./gradlew -p readme-plugin publishToMavenLocal # स्थानीय प्रकाशन
```

## समस्या निवारण

| लक्षण | समाधान |
|---------|-----|
| `GitHub token is empty or still a placeholder in readme.yml` | `README_GRADLE_PLUGIN` secret सेट करें; सुनिश्चित करें कि CI कार्य चलने से पहले इंजेक्ट करे |
| PNG नहीं बना, README अपरिवर्तित | पुष्टि करें `README_truth*.adoc` कि `source.dir` में है; ब्लॉक सिंटैक्स जाँचें `[plantuml, name, png]` |
| JGit push अस्वीकृत | शाखा `watchedBranches` में होनी चाहिए (डिफ़ॉल्ट `main`/`master`); सत्यापित करें PAT में `contents: write` हो |
| `Java heap space` | `export GRADLE_OPTS="-Xmx2g"` |
| Logback SLF4J टकराव | बिल्ड स्वतः परीक्षण क्लासपाथ से `logback-classic` बाहर रखता है — इसे पुनः न जोड़ें |

पूर्ण AsciiDoc संदर्भ के लिए देखें [README_truth.adoc](../README_truth.adoc)।

## लाइसेंस

Apache License 2.0 — देखें [लाइसेंस](../LICENCE)।

---

_CCCP Education पारिस्थितिकी तंत्र का भाग — `groupId: education.cccp`._