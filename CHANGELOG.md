# Changelog

## 1.1.0 - 2026-09-02

- Replaced the single-food repeat streak with bounded Diet Memory holding the latest 16 meals by default.
- Added a deterministic 0–100 Food Variety Score based on food identity, repeat frequency, nutrient
  composition, nutrient-group coverage, Meal Quality, and a configurable freshness window.
- Added Repetitive, Limited, Varied, Diverse, and Highly Diverse variety tiers in English and Russian.
- Kept the familiar gentle repetition penalty: it still affects only Innutrient efficiency, never vanilla
  hunger or saturation, and now derives its streak from Diet Memory.
- Added Food Variety to the U-API nutrition screen and `/innutrient show`, plus a bounded
  `/innutrient variety` history view.
- Expanded `NutritionApi` with immutable Variety Score, tier, and recent-food snapshots.
- Introduced player data version 3 with automatic migration from Innutrient 1.0 saves while preserving
  nutrient levels and sustained Diet Quality.
- Bumped the synchronized attachment protocol and added coverage for migration, capacity, expiry,
  repeated foods, composition diversity, and immutable state behavior.

## 1.0.1 - 2026-09-01

- Updated the required U-API line to the stable 3.0.0 release.
- Added branch CI so release artifacts are compiled and tested before tagging.
- Contains no gameplay or persisted-data changes from 1.0.0.

## 1.0.0

- Added sustained Diet Quality states with configurable thresholds, hysteresis, and transition time.
- Added Meal Quality and nutrition-efficiency bonuses for multi-group meals resolved from recipes.
- Added bounded Food Variety streaks that gently reduce repeated-food nutrition and recover through time or another food.
- Replaced the default Luck rule with survival-focused exhaustion, natural-regeneration, and nutrition-efficiency consequences.
- Added sustained penalties for severe deficiency and excessive sugar.
- Added synchronized numeric food tooltips with Meal Quality and advanced resolution-source details.
- Expanded the U-API nutrition screen with Diet Quality and contextual low/high guidance.
- Added verified NeoForge Common Tags and recursive effect-rule schema v2 with v1 compatibility.
- Expanded the public API with immutable player snapshots, Diet Quality, and Meal Quality.
- Preserved bounded, cached, deterministic, and cycle-safe recipe inheritance with recipe-first automatic resolution.
- Added feature parity for Minecraft 1.21.1 and 26.2 plus version-aware release automation.

## 0.2.0-beta.1 - 2026-08-29

- Ported Innutrient to Minecraft 26.2, NeoForge 26.2.0.28-beta, Java 25, and U-API 3.
- Adapted food components, recipes, networking, GUI extraction, and resource identifiers to current APIs.

## 0.1.0

- Initial Minecraft 1.21.1 / NeoForge architecture.
- Data-driven nutrient groups, food profiles, effect rules, and dynamic item tags.
- Persistent synchronized player nutrition, bounded recursive recipes, U-API UI/API, commands, and localization.
