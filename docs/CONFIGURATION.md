# Configuration

Innutrient separates global gameplay tuning from datapack content.

## Server config

Generated per world at `<world>/serverconfig/uapi/innutrient/server.toml`.

- `gain.globalMultiplier`: multiplier for the central gain formula.
- `decay.mode`: `HUNGER`, `PERIODIC`, `HYBRID`, or `NONE`.
- `decay.perHungerPoint`: nutrient points removed when one hunger point is lost.
- `decay.periodicIntervalTicks` and `decay.periodicAmount`: optional fallback decay.
- `player.deathRetentionPercent`: nutrient percentage retained after death. Dimension clones retain all values.
- `resolution.enableRecipeInheritance`: enables automatic recipe analysis.
- `resolution.maxDepth`: recursive traversal limit.
- `resolution.maxIngredientAlternatives`: deterministic per-Ingredient cap.
- `resolution.maxRecipesPerOutput`: deterministic per-output recipe cap.
- `effects.enableBonuses` / `effects.enablePenalties`: global effect-rule switches.

For Minecraft 1.21.1, `FoodProperties.saturation()` is the absolute saturation restored, not the old saturation modifier. Innutrient therefore calculates:

```text
gain = (nutrition + saturation × 0.5) × globalMultiplier
```

The normalized food profile distributes this total across groups, after which each group's `gain_multiplier` is applied.

## Client config

Generated at `config/uapi/innutrient/client.toml`.

- `tooltips.showFoodNutrition`
- `tooltips.advancedDetailsRequireShift`
- `screen.showPercentages`

Client settings never change server-authoritative nutrition.
