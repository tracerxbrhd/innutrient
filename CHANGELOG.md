# Changelog

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
