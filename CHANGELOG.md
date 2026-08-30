# Changelog

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
