<!-- translated from README.md rev 0.0.1 -->
# readme-gradle — صارفین کا رہنما

> Gradle پلگ ان جو `README_truth.adoc` مصدر فائلوں سے GitHub-موافق
> `README.adoc` فائلیں پیدا کرتا ہے، اور GitHub Actions اور JGit کے
> ذریعے مقامی `[plantuml]` بلاکس کو مرتب PNG تصاویر میں رینڈر کرتا ہے۔

[![Maven Central](https://img.shields.io/maven-central/v/education.cccp/readme-plugin.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/education.cccp/readme-plugin)
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/education.cccp.readme.svg?label=Plugin%20Portal)](https://plugins.gradle.org/plugin/education.cccp.readme)
[![CI](https://img.shields.io/github/actions/workflow/status/cccp-education/readme-gradle/ci.yml?branch=main&label=CI)](https://github.com/cccp-education/readme-gradle/actions/workflows/ci.yml)
[![License](https://img.shields.io/github/license/cccp-education/readme-gradle?label=License)](../LICENCE)

- **نسخہ**: `0.0.1` · **Group**: `education.cccp` · **Plugin ID**: `education.cccp.readme`
- **ٹول چین**: Java 24 · Kotlin 2.3.20 · Gradle 9.5.1
- **بلڈ**: `./gradlew build` · **ٹیسٹ**: `./gradlew check` (JUnit5 + 56 Cucumber منظرنامے، 5 خصوصیات میں)
- **کوریج**: گیٹڈ نہیں (`check` میں کوئی Kover اصول منسلک نہیں)

🌐 Languages: [English](README.md) | [中文](README.zh.md) | [हिन्दी](README.hi.md) | [Español](README.es.md) | [Français](README.fr.md) | [العربية](README.ar.md) | [বাংলা](README.bn.md) | [Português](README.pt.md) | [Русский](README.ru.md) | **اردو**

---

## یہ کیا کرتا ہے

GitHub مقامی طور پر AsciiDoc فائلوں میں شامل PlantUML نحو کو رینڈر
نہیں کرتا۔ `readme-gradle` اس متبادل کو خودکار کرتا ہے: آپ واحد صداقت کا
مصدر (`README_truth.adoc`) ترمیم کرتے ہیں، پلگ ان ہر `[plantuml, name, png]`
بلاک نکالتا ہے، PNG تصاویر پیدا کرتا ہے، بلاک کو `image::` حوالہ کے طور
پر دوبارہ لکھتا ہے، اور JGit کے ذریعے دوبارہ لکھا گیا `README.adoc` اور
تصاویر کو آپ کے ذخیرے میں مرتب کرتا ہے — CI رنر پر سسٹم `git` بائنری کی
ضرورت نہیں۔

```
README_truth.adoc → generateReadme → transformReadme → commitGeneratedReadme → README.adoc + images/
                    (scaffolds yml)  (PNG + rewrite)    (JGit add + commit + push)
```

## فوری آغاز

### 1. پلگ ان لگائیں

```gradle
plugins {
    id("education.cccp.readme") version "0.0.1"
}
```

### 2. اپنا صداقت کا مصدر لکھیں

پروجیکٹ جڑ میں `README_truth.adoc` بنائیں۔ مقامی PlantUML بلاکس استعمال
کریں:

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

فرانسیسی تبدیلی کے لیے فائل کا نام `README_plantuml_fr.adoc` رکھیں ——
`_fr` لاحقہ پہچانا جاتا ہے اور `README_fr.adoc` پیدا کرتا ہے۔

### 3. `readme.yml` کے ذریعے ترتیب دیں

پلگ ان `readme.yml` کو پروجیکٹ جڑ سے پڑھتا ہے۔ **کبھی حقیقی ٹوکن مرتب
نہ کریں** — مکمل فائل (ٹوکن سمیت) GitHub secret `README_GRADLE_PLUGIN`
میں محفوظ کریں اور CI میں داخل کریں:

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

CI مرحلہ:

```yaml
- name: Inject plugin config
  run: echo "${{ secrets.README_GRADLE_PLUGIN }}" > readme.yml

- name: Generate README and commit via JGit
  run: ./gradlew -p readme-plugin commitGeneratedReadme --no-daemon
```

### 4. پائپ لائن چلائیں

```bash
./gradlew commitGeneratedReadme          # مکمل پائپ لائن (CI)
./gradlew transformReadme               # صرف PNG پیدا کرنا + دوبارہ لکھنا
./gradlew generateReadme                 # غیر حاضر ہو تو readme.yml + workflow سکفولڈ
```

## دستیاب کام

| کام | گروہ | تفصیل |
|------|-------|-------------|
| `generateReadme`         | generate  | غیر حاضر ہو تو `readme.yml` اور `.github/workflows/readme_action.yml` بناتا ہے |
| `transformReadme`        | transform | `README_truth*.adoc` سے PNG پیدا کرتا ہے اور `README*.adoc` دوبارہ لکھتا ہے (`generateReadme` پر منحصر) |
| `commitGeneratedReadme`  | deploy    | JGit کے ذریعے پیدا کردہ `README*.adoc` + تصاویر مرتب اور پش کرتا ہے (صرف CI، `transformReadme` پر منحصر) |

## توسیع DSL

پلگ ان کا **کوئی Gradle توسیع بلاک نہیں ہے**۔ تمام ترتیب پروجیکٹ جڑ پر
`readme.yml` فائل سے پڑھی جاتی ہے (Jackson YAML کے ذریعے
`ReadmePlantUmlConfig` میں تجزیہ)۔ اگر `readme.yml` غیر حاضر یا خالی ہو،
تو خاموشی سے معقول طے شدہ استعمال ہوتے ہیں۔

| ترتیب حصہ | میدان | طے شدہ |
|----------------|-------|---------|
| `source`  | `dir`         | `"."` |
| `source`  | `defaultLang` | `"en"` |
| `output`  | `imgDir`      | `".github/workflows/readmes/images"` |
| `git`     | `userName`        | `"github-actions[bot]"` |
| `git`     | `userEmail`       | `"github-actions[bot]@users.noreply.github.com"` |
| `git`     | `commitMessage`   | `"chore: generate readme [skip ci]"` |
| `git`     | `token`           | `""` (ضروری سیٹ کریں — خالی/placeholder ہو تو رن ٹائم خرابی) |
| `git`     | `watchedBranches`  | `["main", "master"]` |

## پیشگی شرائط

- **Java** 24+ (Kotlin 2.3.20 ٹول چین)
- **Gradle** 9.5.1+
- **GitHub** ذخیرہ تحریری رسائی کے ساتھ (JGit push کے لیے)
- **GitHub secret** `README_GRADLE_PLUGIN` جس میں قابل PAT کے ساتھ مکمل `readme.yml` ہو

## بلڈ اور ٹیسٹ

```bash
./gradlew -p readme-plugin build              # مکمل بلڈ (compile + unit + cucumber + functional)
./gradlew -p readme-plugin test               # صرف JUnit5 یونٹ ٹیسٹ
./gradlew -p readme-plugin cucumberTest       # Cucumber BDD (5 feature فائلیں، 56 منظرنامے)
./gradlew -p readme-plugin functionalTest     # GradleRunner فعال ٹیسٹ
./gradlew -p readme-plugin check              # test + functionalTest + cucumberTest
./gradlew -p readme-plugin publishToMavenLocal # مقامی اشاعت
```

## مسئلہ حل

| علامت | اصلاح |
|---------|-----|
| `GitHub token is empty or still a placeholder in readme.yml` | `README_GRADLE_PLUGIN` secret سیٹ کریں؛ یقینی بنائیں کہ CI کام چلنے سے پہلے داخل کرے |
| PNG نہیں بنا، README تبدیل نہیں | تصدیق کریں `README_truth*.adoc` کہ `source.dir` میں ہے؛ بلاک نحو جائز کریں `[plantuml, name, png]` |
| JGit push مسترد | شاخ `watchedBranches` میں ہونی چاہیے (طے شدہ `main`/`master`); تصدیق کریں PAT میں `contents: write` ہے |
| `Java heap space` | `export GRADLE_OPTS="-Xmx2g"` |
| Logback SLF4J تصادم | بلڈ خودکار طور پر `logback-classic` کو ٹیسٹ classpath سے خارج کرتا ہے — اسے دوبارہ شامل نہ کریں |

مکمل AsciiDoc حوالے کے لیے دیکھیں [README_truth.adoc](../README_truth.adoc)۔

## لائسنس

Apache License 2.0 — دیکھیں [لائسنس](../LICENCE)۔

---

_CCCP Education ماحولیاتی نظام کا حصہ — `groupId: education.cccp`۔_