![Innutrient banner](img/innutrient-banner.png)

# Innutrient

<p align="center">
  <a href="https://github.com/tracerxbrhd/innutrient/releases"><img alt="Release" src="https://img.shields.io/github/v/release/tracerxbrhd/innutrient?include_prereleases&sort=semver&label=release"></a>
  <a href="https://github.com/tracerxbrhd/innutrient/actions/workflows/ci.yml"><img alt="Build" src="https://github.com/tracerxbrhd/innutrient/actions/workflows/ci.yml/badge.svg?branch=master"></a>
  <a href="https://modrinth.com/mod/innutrient"><img alt="Modrinth" src="https://img.shields.io/badge/Modrinth-Download-00AF5C?logo=modrinth&logoColor=white"></a>
  <a href="https://www.curseforge.com/minecraft/mc-mods/innutrient"><img alt="CurseForge" src="https://img.shields.io/badge/CurseForge-Download-F16436?logo=curseforge&logoColor=white"></a>
</p>

**Innutrient is a configurable nutrition system that rewards varied, balanced and well-prepared diets without replacing Minecraft's hunger mechanics.**

Foods contribute to data-driven nutrient groups, recent meals affect a bounded Food Variety Score, repeated foods gradually lose nutrition efficiency, and complex meals can earn Meal Quality bonuses. Sustained diet quality can provide modest benefits, while long deficiencies or excessive sugar have practical consequences.

## Compatibility

| Minecraft | Innutrient | U-API | Java | Loader |
| --- | --- | --- | --- | --- |
| 1.21.1 | 1.2.x | 2.x | 21 | NeoForge |
| 26.2 | 1.2.x | 3.x | 25 | NeoForge |

The project supports both Minecraft release lines through compatible builds. The default `master` branch currently contains the 1.21.1 source line.

## Features

- data-driven nutrient groups such as fruits, vegetables, grains, proteins and sugars;
- a 0–100 **Food Variety Score** based on recent meals;
- diminishing nutrition efficiency for repeatedly eating the same food;
- **Meal Quality** bonuses for useful multi-group meals;
- sustained **Diet Quality** with configurable survival effects;
- automatic composition support for complex vanilla and modded recipes;
- food tooltips that explain nutritional contribution;
- English and Russian localization.

## Nutrition Dashboard

The U-API-powered Nutrition Dashboard presents nutrient balance, Diet Quality, Food Variety, recent meals, target zones and active modifiers in one cohesive interface rather than separate HUD widgets.

## Modpacks and datapacks

Modpack authors can redefine nutrient groups, override individual foods, add compatibility datapacks, tune healthy ranges and configure sustained effect rules. Compatibility is intentionally data-driven so large food mods do not need to become hard dependencies.

Technical documentation and the current compatibility matrix live in [`docs/`](docs/).

## Building from source

The default branch requires Java 21 and a compatible U-API 2.x development environment.

```bash
./gradlew build
```

On Windows:

```powershell
gradlew.bat build
```

## License

Innutrient source code is licensed under the [Mozilla Public License 2.0](LICENSE) (`MPL-2.0`). The Underworld Studio name, logos and branding are not licensed by the MPL.
