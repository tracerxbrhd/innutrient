# Changelog

## 1.2.1 - 2026-09-02

- Refined the Nutrition Dashboard with layered near-black surfaces, restrained purple accents, and a
  clearer visual hierarchy without changing U-API navigation.
- Unified Nutrient Balance into one compact section with 38-pixel rows, subtle dividers, and a single
  shared Low / Target Range / High legend.
- Separated nutrient identity from status: bars now use each data-driven group's configured color while
  labels and markers communicate Healthy, Low, High, and Excessive states.
- Simplified target ranges to a neutral track, subtle healthy zone, thin boundaries, and a current-value
  marker; preserved text and tooltips for accessibility.
- Reworked the Summary into one primary animated balance gauge and three compact secondary metrics.
- Moved existing guidance into a compact Diet Insight card and improved wide-screen column balance.
- Polished Recent Foods with denser interactive rows and a purposeful empty state.
- Suppressed zero-value modifier rows and now shows only configured effects that differ from neutral.
- Expanded responsive layout coverage across compact, wide, ultrawide, empty, populated, and custom-group
  scenarios for both supported Minecraft branches.

## 1.2.0 - 2026-09-02

- Rebuilt the U-API nutrition screen as one cohesive, responsive Nutrition Dashboard.
- Added a compact overview for Nutrition Balance, Diet Quality, Food Variety, and the latest Meal Quality.
- Replaced ordinary progress bars with accessible LOW / HEALTHY TARGET / HIGH range scales using labels,
  patterns, status symbols, threshold markers, and subtle animated value transitions.
- Added concise contextual guidance, a bounded six-entry Recent Foods view, relative meal times, and
  Meal Quality context sourced from Diet Memory.
- Added a Current Nutrition Modifiers card that reports the server's configured exhaustion, nutrition
  efficiency, and natural-regeneration multipliers for the active Diet Quality.
- Added responsive wide and compact layouts, whole-dashboard scrolling for small GUI sizes, trimmed long
  translations, hover states, and detailed tooltips without floating widgets or a new HUD.
- Synchronized a compact dashboard settings snapshot so dedicated-server values are displayed accurately;
  bumped the network protocol to version 4 without changing the save format.
- Added layout and settings tests covering common aspect ratios, ultrawide, small GUI sizes, empty/default/
  custom group counts, scroll overflow, and bounded configuration data.

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
