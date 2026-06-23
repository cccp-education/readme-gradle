<!-- translated from README.md rev 0.0.1 -->
# readme-gradle — ভোক্তা গাইড

> Gradle প্লাগইন যা `README_truth.adoc` উৎস ফাইল থেকে GitHub-সামঞ্জস্যপূর্ণ
> `README.adoc` ফাইল তৈরি করে, এবং GitHub Actions ও JGit-এর মাধ্যমে মূল
> `[plantuml]` ব্লকগুলোকে কমিট করা PNG চিত্রে রেন্ডার করে।

[![Maven Central](https://img.shields.io/maven-central/v/education.cccp/readme-plugin.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/education.cccp/readme-plugin)
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/education.cccp.readme.svg?label=Plugin%20Portal)](https://plugins.gradle.org/plugin/education.cccp.readme)
[![CI](https://img.shields.io/github/actions/workflow/status/cccp-education/readme-gradle/ci.yml?branch=main&label=CI)](https://github.com/cccp-education/readme-gradle/actions/workflows/ci.yml)
[![License](https://img.shields.io/github/license/cccp-education/readme-gradle?label=License)](../LICENCE)

- **সংস্করণ**: `0.0.1` · **Group**: `education.cccp` · **Plugin ID**: `education.cccp.readme`
- **টুলচেইন**: Java 24 · Kotlin 2.3.20 · Gradle 9.5.1
- **বিল্ড**: `./gradlew build` · **পরীক্ষা**: `./gradlew check` (JUnit5 + 56 Cucumber পরিস্থিতি, 5 বৈশিষ্ট্যে)
- **কভারেজ**: গেটেড নয় (`check`-এ কোনো Kover নিয়ম যুক্ত নেই)

🌐 Languages: [English](README.md) | [中文](README.zh.md) | [हिन्दी](README.hi.md) | [Español](README.es.md) | [Français](README.fr.md) | [العربية](README.ar.md) | **বাংলা** | [Português](README.pt.md) | [Русский](README.ru.md) | [اردو](README.ur.md)

---

## এটি কী করে

GitHub স্থানীয়ভাবে AsciiDoc ফাইলে এম্বেড করা PlantUML সিনট্যাক্স রেন্ডার
করে না। `readme-gradle` এই সমাধানটি স্বয়ংক্রিয় করে: আপনি একটি সত্যের
একমাত্র উৎস (`README_truth.adoc`) সম্পাদনা করেন, প্লাগইন প্রতিটি
`[plantuml, name, png]` ব্লক বের করে, PNG চিত্র তৈরি করে, ব্লকটি
`image::` রেফারেন্স হিসেবে পুনরায় লেখে, এবং JGit-এর মাধ্যমে পুনরায় লেখা
`README.adoc` ও চিত্রগুলো আপনার রিপোজিটরিতে কমিট করে — CI রানারে কোনো
সিস্টেম `git` বাইনারির প্রয়োজন নেই।

```
README_truth.adoc → generateReadme → transformReadme → commitGeneratedReadme → README.adoc + images/
                    (scaffolds yml)  (PNG + rewrite)    (JGit add + commit + push)
```

## দ্রুত শুরু

### 1. প্লাগইন প্রয়োগ করুন

```gradle
plugins {
    id("education.cccp.readme") version "0.0.1"
}
```

### 2. আপনার সত্যের উৎস লিখুন

প্রজেক্ট রুটে `README_truth.adoc` তৈরি করুন। স্থানীয় PlantUML ব্লক
ব্যবহার করুন:

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

ফরাসি ভেরিয়েন্টের জন্য ফাইলের নাম `README_plantuml_fr.adoc` দিন ——
`_fr` প্রত্যয় শনাক্ত করা হয় এবং `README_fr.adoc` তৈরি হয়।

### 3. `readme.yml` এর মাধ্যমে কনফিগার করুন

প্লাগইন প্রজেক্ট রুট থেকে `readme.yml` পড়ে। **কখনও প্রকৃত টোকেন কমিট
করবেন না** — সম্পূর্ণ ফাইল (টোকেন সহ) GitHub সিক্রেট
`README_GRADLE_PLUGIN`-এ সংরক্ষণ করুন এবং CI-তে ইনজেক্ট করুন:

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

CI ধাপ:

```yaml
- name: Inject plugin config
  run: echo "${{ secrets.README_GRADLE_PLUGIN }}" > readme.yml

- name: Generate README and commit via JGit
  run: ./gradlew -p readme-plugin commitGeneratedReadme --no-daemon
```

### 4. পাইপলাইন চালান

```bash
./gradlew commitGeneratedReadme          # সম্পূর্ণ পাইপলাইন (CI)
./gradlew transformReadme               # শুধু PNG নির্মাণ + পুনর্লিখন
./gradlew generateReadme                 # অনুপস্থিত হলে readme.yml + workflow স্ক্যাফোল্ড
```

## উপলব্ধ কাজ

| কাজ | গোষ্ঠী | বিবরণ |
|------|-------|-------------|
| `generateReadme`         | generate  | অনুপস্থিত হলে `readme.yml` ও `.github/workflows/readme_action.yml` তৈরি করে |
| `transformReadme`        | transform | `README_truth*.adoc` থেকে PNG তৈরি করে ও `README*.adoc` পুনর্লিখিত করে (`generateReadme`-এর উপর নির্ভরশীল) |
| `commitGeneratedReadme`  | deploy    | JGit-এর মাধ্যমে নির্মিত `README*.adoc` + চিত্র কমিট ও পুশ করে (শুধু CI, `transformReadme`-এর উপর নির্ভরশীল) |

## এক্সটেনশন DSL

প্লাগইনে **কোনো Gradle এক্সটেনশন ব্লক নেই**। সমস্ত কনফিগারেশন প্রজেক্ট
রুটে `readme.yml` ফাইল থেকে পড়া হয় (Jackson YAML দ্বারা
`ReadmePlantUmlConfig`-এ পার্স করা)। `readme.yml` অনুপস্থিত বা খালি হলে,
নীরবে যৌক্তিক ডিফল্ট ব্যবহৃত হয়।

| কনফিগ বিভাগ | ক্ষেত্র | ডিফল্ট |
|----------------|-------|---------|
| `source`  | `dir`         | `"."` |
| `source`  | `defaultLang` | `"en"` |
| `output`  | `imgDir`      | `".github/workflows/readmes/images"` |
| `git`     | `userName`        | `"github-actions[bot]"` |
| `git`     | `userEmail`       | `"github-actions[bot]@users.noreply.github.com"` |
| `git`     | `commitMessage`   | `"chore: generate readme [skip ci]"` |
| `git`     | `token`           | `""` (অবশ্য সেট করতে হবে — খালি/placeholder হলে রানটাইমে ত্রুটি) |
| `git`     | `watchedBranches`  | `["main", "master"]` |

## পূর্বশর্ত

- **Java** 24+ (Kotlin 2.3.20 টুলচেইন)
- **Gradle** 9.5.1+
- **GitHub** রিপোজিটরি লেখার অ্যাক্সেস সহ (JGit push-এর জন্য)
- **GitHub সিক্রেট** `README_GRADLE_PLUGIN` যাতে বৈধ PAT সহ সম্পূর্ণ `readme.yml` থাকে

## বিল্ড ও পরীক্ষা

```bash
./gradlew -p readme-plugin build              # সম্পূর্ণ বিল্ড (compile + unit + cucumber + functional)
./gradlew -p readme-plugin test               # শুধু JUnit5 ইউনিট পরীক্ষা
./gradlew -p readme-plugin cucumberTest       # Cucumber BDD (5 feature ফাইল, 56 পরিস্থিতি)
./gradlew -p readme-plugin functionalTest     # GradleRunner ফাংশনাল পরীক্ষা
./gradlew -p readme-plugin check              # test + functionalTest + cucumberTest
./gradlew -p readme-plugin publishToMavenLocal # স্থানীয় প্রকাশনা
```

## সমস্যা সমাধান

| লক্ষণ | সমাধান |
|---------|-----|
| `GitHub token is empty or still a placeholder in readme.yml` | `README_GRADLE_PLUGIN` সিক্রেট সেট করুন; নিশ্চিত করুন CI কাজ চলার আগে ইনজেক্ট করে |
| PNG তৈরি হয়নি, README অপরিবর্তিত | নিশ্চিত করুন `README_truth*.adoc` যে `source.dir`-এ আছে; ব্লক সিনট্যাক্স যাচাই করুন `[plantuml, name, png]` |
| JGit push প্রত্যাখ্যাত | শাখা অবশ্য `watchedBranches`-এ থাকতে হবে (ডিফল্ট `main`/`master`); যাচাই করুন PAT-এ `contents: write` আছে |
| `Java heap space` | `export GRADLE_OPTS="-Xmx2g"` |
| Logback SLF4J দ্বন্দ্ব | বিল্ড স্বয়ংক্রিয়ভাবে পরীক্ষা ক্লাসপাথ থেকে `logback-classic` বাদ দেয় — পুনরায় যোগ করবেন না |

সম্পূর্ণ AsciiDoc তথ্যসূত্রের জন্য দেখুন [README_truth.adoc](../README_truth.adoc)।

## লাইসেন্স

Apache License 2.0 — দেখুন [লাইসেন্স](../LICENCE)।

---

_CCCP Education ইকোসিস্টেমের অংশ — `groupId: education.cccp`._