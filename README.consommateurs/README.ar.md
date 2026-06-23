<!-- translated from README.md rev 0.0.1 -->
# readme-gradle — دليل المستهلك

> إضافة Gradle تولِّد ملفات `README.adoc` متوافقة مع GitHub من ملفات
> مصدر `README_truth.adoc`، مع تصيير كتل `[plantuml]` الأصلية إلى صور
> PNG مُلتزَمة عبر GitHub Actions وJGit.

[![Maven Central](https://img.shields.io/maven-central/v/education.cccp/readme-plugin.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/education.cccp/readme-plugin)
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/education.cccp.readme.svg?label=Plugin%20Portal)](https://plugins.gradle.org/plugin/education.cccp.readme)
[![CI](https://img.shields.io/github/actions/workflow/status/cccp-education/readme-gradle/ci.yml?branch=main&label=CI)](https://github.com/cccp-education/readme-gradle/actions/workflows/ci.yml)
[![License](https://img.shields.io/github/license/cccp-education/readme-gradle?label=License)](../LICENCE)

- **الإصدار**: `0.0.1` · **Group**: `education.cccp` · **Plugin ID**: `education.cccp.readme`
- **سلسلة الأدوات**: Java 24 · Kotlin 2.3.20 · Gradle 9.5.1
- **البناء**: `./gradlew build` · **الاختبارات**: `./gradlew check` (JUnit5 + 56 سيناريو Cucumber عبر 5 ميزات)
- **التغطية**: غير مقيّدة (لا توجد قاعدة Kover مربوطة بـ `check`)

🌐 Languages: [English](README.md) | [中文](README.zh.md) | [हिन्दी](README.hi.md) | [Español](README.es.md) | [Français](README.fr.md) | **العربية** | [বাংলা](README.bn.md) | [Português](README.pt.md) | [Русский](README.ru.md) | [اردو](README.ur.md)

---

## ماذا يفعل

GitHub لا يصيِّر أصلاً صياغة PlantUML المضمَّنة في ملفات AsciiDoc.
يقوم `readme-gradle` بأتمتة الحل البديل: تقوم بتحرير مصدر الحقيقة
الوحيد (`README_truth.adoc`)، تستخرج الإضافة كل كتلة
`[plantuml, name, png]`، تولِّد صور PNG، تعيد كتابة الكتلة كمرجع
`image::`، وتلتزم كلًّا من `README.adoc` المُعاد كتابته والصور مجددًا
إلى مستودعك عبر JGit — لا حاجة إلى ثنائي `git` على نظام CI runner.

```
README_truth.adoc → generateReadme → transformReadme → commitGeneratedReadme → README.adoc + images/
                    (scaffolds yml)  (PNG + rewrite)    (JGit add + commit + push)
```

## البدء السريع

### 1. طبّق الإضافة

```gradle
plugins {
    id("education.cccp.readme") version "0.0.1"
}
```

### 2. أنشئ مصدر الحقيقة الخاص بك

أنشئ `README_truth.adoc` في جذر المشروع. استخدم كتل PlantUML الأصلية:

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

للنسخة الفرنسية، سمِّ الملف `README_plantuml_fr.adoc` — يُكتشف اللاحقة
`_fr` وتُنتج `README_fr.adoc`.

### 3. التهيئة عبر `readme.yml`

تقرأ الإضافة `readme.yml` من جذر المشروع. **لا تلتزم أبدًا برمز
حقيقي** — خزِّن الملف الكامل (شاملاً الرمز) في سر GitHub
`README_GRADLE_PLUGIN` وحقنه في CI:

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

خطوة CI:

```yaml
- name: Inject plugin config
  run: echo "${{ secrets.README_GRADLE_PLUGIN }}" > readme.yml

- name: Generate README and commit via JGit
  run: ./gradlew -p readme-plugin commitGeneratedReadme --no-daemon
```

### 4. شغِّل خط الأنابيب

```bash
./gradlew commitGeneratedReadme          # خط الأنابيب الكامل (CI)
./gradlew transformReadme               # توليد PNG + إعادة الكتابة فقط
./gradlew generateReadme                 # سقالة readme.yml + workflow إذا غابت
```

## المهام المتاحة

| المهمة | المجموعة | الوصف |
|------|-------|-------------|
| `generateReadme`         | generate  | ينشئ `readme.yml` و`.github/workflows/readme_action.yml` عند غيابهما |
| `transformReadme`        | transform | يولِّد PNG ويعيد كتابة `README*.adoc` من `README_truth*.adoc` (يعتمد على `generateReadme`) |
| `commitGeneratedReadme`  | deploy    | يلتزم ويدفع `README*.adoc` + الصور المولَّدة عبر JGit (CI فقط، يعتمد على `transformReadme`) |

## DSL الامتداد

لا تملك الإضافة **أي كتلة امتداد Gradle**. تُقرأ كل التهيئة من ملف
`readme.yml` في جذر المشروع (يُحلَّل بواسطة Jackson YAML إلى
`ReadmePlantUmlConfig`). إذا غاب `readme.yml` أو كان فارغًا، تُستخدم
قيم افتراضية معقولة بصمت.

| قسم التهيئة | الحقل | الافتراضي |
|----------------|-------|---------|
| `source`  | `dir`         | `"."` |
| `source`  | `defaultLang` | `"en"` |
| `output`  | `imgDir`      | `".github/workflows/readmes/images"` |
| `git`     | `userName`        | `"github-actions[bot]"` |
| `git`     | `userEmail`       | `"github-actions[bot]@users.noreply.github.com"` |
| `git`     | `commitMessage`   | `"chore: generate readme [skip ci]"` |
| `git`     | `token`           | `""` (يجب ضبطه — خطأ وقت التشغيل إذا كان فارغًا/placeholder) |
| `git`     | `watchedBranches`  | `["main", "master"]` |

## المتطلبات المسبقة

- **Java** 24+ (سلسلة أدوات Kotlin 2.3.20)
- **Gradle** 9.5.1+
- **GitHub** مستودع مع وصول كتابة (لدفع JGit)
- **سر GitHub** `README_GRADLE_PLUGIN` يحتوي `readme.yml` كاملًا مع PAT صالح

## البناء والاختبار

```bash
./gradlew -p readme-plugin build              # بناء كامل (compile + unit + cucumber + functional)
./gradlew -p readme-plugin test               # اختبارات وحدة JUnit5 فقط
./gradlew -p readme-plugin cucumberTest       # Cucumber BDD (5 ملفات feature، 56 سيناريو)
./gradlew -p readme-plugin functionalTest     # اختبارات وظيفية GradleRunner
./gradlew -p readme-plugin check              # test + functionalTest + cucumberTest
./gradlew -p readme-plugin publishToMavenLocal # نشر محلي
```

## استكشاف الأخطاء

| العَرَض | الإصلاح |
|---------|-----|
| `GitHub token is empty or still a placeholder in readme.yml` | اضبط سر `README_GRADLE_PLUGIN`؛ تأكد أن CI يحقنه قبل تشغيل المهمة |
| لا PNG مولَّد، README دون تغيير | أكِّد وجود `README_truth*.adoc` في `source.dir`؛ تحقق من صياغة الكتلة `[plantuml, name, png]` |
| رفض دفع JGit | يجب أن تكون الفرع في `watchedBranches` (`main`/`master` افتراضيًا)؛ تحقق أن PAT يملك `contents: write` |
| `Java heap space` | `export GRADLE_OPTS="-Xmx2g"` |
| تعارض SLF4J مع Logback | يستثني البناء `logback-classic` من مسارات اختبار فئات تلقائيًا — لا تُعد إضافته |

راجع [README_truth.adoc](../README_truth.adoc) لمرجع AsciiDoc الكامل.

## الترخيص

Apache License 2.0 — راجع [الترخيص](../LICENCE).

---

_جزء من منظومة CCCP Education — `groupId: education.cccp`._