<!-- translated from README.md rev 1 -->
# readme-gradle — Guide Consommateur

> Plugin Gradle générant des fichiers `README.adoc` compatibles GitHub à partir
> de sources `README_truth.adoc`, en rendant les blocs natifs `[plantuml]` en
> images PNG commitées via GitHub Actions et JGit.

[![Maven Central](https://img.shields.io/maven-central/v/education.cccp/readme-plugin.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/education.cccp/readme-plugin)
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/education.cccp.readme.svg?label=Plugin%20Portal)](https://plugins.gradle.org/plugin/education.cccp.readme)
[![CI](https://img.shields.io/github/actions/workflow/status/cccp-education/readme-gradle/ci.yml?branch=main&label=CI)](https://github.com/cccp-education/readme-gradle/actions/workflows/ci.yml)
[![Licence](https://img.shields.io/github/license/cccp-education/readme-gradle?label=Licence)](../LICENCE)

- **Version** : `0.0.1` · **Groupe** : `education.cccp` · **ID Plugin** : `education.cccp.readme`
- **Toolchain** : Java 24 · Kotlin 2.3.20 · Gradle 9.5.1
- **Build** : `./gradlew build` · **Tests** : `./gradlew check` (JUnit5 + 56 scénarios Cucumber sur 5 features)
- **Couverture** : non bloquante (aucune règle Kover câblée dans `check`)

🌐 Langues : [English](README.md) | **Français**

---

## Ce que fait le plugin

GitHub ne rend pas nativement la syntaxe PlantUML intégrée dans les fichiers
AsciiDoc. `readme-gradle` automatise ce contournement : vous éditez une seule
source de vérité (`README_truth.adoc`), le plugin extrait chaque bloc
`[plantuml, name, png]`, génère les images PNG, réécrit le bloc en référence
`image::` et commit à la fois le `README.adoc` réécrit et les images dans votre
dépôt via JGit — aucun binaire `git` système requis sur le runner CI.

```
README_truth.adoc → generateReadme → transformReadme → commitGeneratedReadme → README.adoc + images/
                   (scaffold yml)   (PNG + rewrite)    (JGit add + commit + push)
```

## Démarrage rapide

### 1. Appliquer le plugin

```gradle
plugins {
    id("education.cccp.readme") version "0.0.1"
}
```

### 2. Créer votre source de vérité

Créez `README_truth.adoc` à la racine du projet. Utilisez des blocs PlantUML
natifs :

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

Pour une variante française, nommez le fichier `README_plantuml_fr.adoc` — le
suffixe `_fr` est détecté et produit `README_fr.adoc`.

### 3. Configurer via `readme.yml`

Le plugin lit `readme.yml` à la racine du projet. **Ne committez jamais un vrai
token** — stockez le fichier complet (token inclus) dans le secret GitHub
`README_GRADLE_PLUGIN` et injectez-le en CI :

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

Étape CI :

```yaml
- name: Inject plugin config
  run: echo "${{ secrets.README_GRADLE_PLUGIN }}" > readme.yml

- name: Generate README and commit via JGit
  run: ./gradlew -p readme-plugin commitGeneratedReadme --no-daemon
```

### 4. Lancer le pipeline

```bash
./gradlew commitGeneratedReadme          # pipeline complet (CI)
./gradlew transformReadme               # génération PNG + rewrite uniquement
./gradlew generateReadme                # scaffold readme.yml + workflow si absents
```

## Tâches disponibles

| Tâche | Groupe | Description |
|-------|--------|-------------|
| `generateReadme`         | generate  | Crée `readme.yml` et `.github/workflows/readme_action.yml` si absents |
| `transformReadme`        | transform | Génère les PNG et réécrit `README*.adoc` depuis `README_truth*.adoc` (dépend de `generateReadme`) |
| `commitGeneratedReadme`  | deploy    | Commit et push des `README*.adoc` + images via JGit (CI uniquement, dépend de `transformReadme`) |

## DSL d'extension

Le plugin n'a **pas de bloc d'extension Gradle**. Toute la configuration est lue
depuis le fichier `readme.yml` à la racine du projet (parsé par Jackson YAML
dans `ReadmePlantUmlConfig`). Si `readme.yml` est absent ou vide, des valeurs
par défaut raisonnables sont utilisées silencieusement.

| Section config | Champ | Valeur par défaut |
|-----------------|-------|-------------------|
| `source`  | `dir`         | `"."` |
| `source`  | `defaultLang` | `"en"` |
| `output`  | `imgDir`      | `".github/workflows/readmes/images"` |
| `git`     | `userName`        | `"github-actions[bot]"` |
| `git`     | `userEmail`       | `"github-actions[bot]@users.noreply.github.com"` |
| `git`     | `commitMessage`   | `"chore: generate readme [skip ci]"` |
| `git`     | `token`           | `""` (doit être défini — erreur à l'exécution si vide/placeholder) |
| `git`     | `watchedBranches`  | `["main", "master"]` |

## Prérequis

- **Java** 24+ (toolchain Kotlin 2.3.20)
- **Gradle** 9.5.1+
- **Dépôt GitHub** avec accès en écriture (pour le push JGit)
- **Secret GitHub** `README_GRADLE_PLUGIN` contenant le `readme.yml` complet avec un PAT valide

## Build & tests

```bash
./gradlew -p readme-plugin build              # build complet (compile + unit + cucumber + fonctionnel)
./gradlew -p readme-plugin test               # tests unitaires JUnit5 uniquement
./gradlew -p readme-plugin cucumberTest       # Cucumber BDD (5 features, 56 scénarios)
./gradlew -p readme-plugin functionalTest     # tests fonctionnels GradleRunner
./gradlew -p readme-plugin check              # test + functionalTest + cucumberTest
./gradlew -p readme-plugin publishToMavenLocal # publication locale
```

## Dépannage

| Symptôme | Solution |
|----------|----------|
| `GitHub token is empty or still a placeholder in readme.yml` | Définir le secret `README_GRADLE_PLUGIN` ; vérifier l'injection CI avant la tâche |
| Aucun PNG généré, README inchangé | Confirmer la présence de `README_truth*.adoc` dans `source.dir` ; vérifier la syntaxe `[plantuml, name, png]` |
| Push JGit rejeté | La branche doit être dans `watchedBranches` (`main`/`master` par défaut) ; vérifier que le PAT a `contents: write` |
| `Java heap space` | `export GRADLE_OPTS="-Xmx2g"` |
| Conflit SLF4J Logback | Le build exclut `logback-classic` des classpaths de test automatiquement — ne pas le réajouter |

Voir [README_truth.adoc](../README_truth.adoc) pour la référence AsciiDoc complète.

## Licence

Licence Apache 2.0 — voir [LICENCE](../LICENCE).

---

_Partie de l'écosystème CCCP Education — `groupId: education.cccp`._