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

- Disabled filtering for the pixel-art mod icon so it stays sharp in NeoForge mod lists.
- Verified the release build against U-API 2.1.2 without raising the existing compatible runtime
  dependency range.
- Added branch CI so release artifacts are compiled and tested before tagging.

## 1.0.0

- Added sustained Diet Quality states with configurable thresholds, hysteresis, and transition time.
- Added Meal Quality and nutrition-efficiency bonuses for multi-group meals resolved from recipes.
- Added bounded Food Variety streaks that gently reduce repeated-food nutrition and recover through time or another food.
- Replaced the default Luck rule with survival-focused exhaustion, natural-regeneration, and nutrition-efficiency consequences.
- Added sustained penalties for severe deficiency and excessive sugar.
- Added synchronized numeric food tooltips with Meal Quality and advanced resolution-source details.
- Expanded the U-API nutrition screen with Diet Quality and contextual low/high guidance.
- Added verified NeoForge Common Tags plus optional legacy tag aliases used by major 1.21.1 food ecosystems.
- Added recursive effect-rule schema v2 while retaining format-version 1 compatibility.
- Expanded the public API with immutable player snapshots, Diet Quality, and Meal Quality.
- Preserved bounded, cached, deterministic, and cycle-safe recipe inheritance with recipe-first automatic resolution.
- Added parity support for Minecraft 1.21.1 and 26.2, documentation, compatibility matrix, tests, and version-aware release automation.

## 0.1.0

- Initial Minecraft 1.21.1 / NeoForge architecture.
- Data-driven nutrient groups, food profiles, effect rules, and dynamic item tags.
- Persistent synchronized player nutrition with hunger-based decay and configurable death retention.
- Recursive cached recipe composition with cycle, depth, recipe-count, and ingredient-alternative bounds.
- U-API nutrition screen, diagnostics, service API, commands, and English/Russian localization.
