# Configuration

Server settings are generated per world at `<world>/serverconfig/uapi/innutrient/server.toml`; client display settings live at `config/uapi/innutrient/client.toml`.

## Nutrition and decay

- `gain.globalMultiplier`
- `decay.mode`: `HUNGER`, `PERIODIC`, `HYBRID`, or `NONE`
- `decay.perHungerPoint`
- `decay.periodicIntervalTicks` and `decay.periodicAmount`
- `player.deathRetentionPercent`

```text
(vanilla nutrition + absolute saturation restored × 0.5) × globalMultiplier
```

Meal Quality, Food Variety, and sustained Diet Quality then modify nutrition gain only. Vanilla hunger and saturation remain unchanged.

## Diet Quality

- `dietQuality.severeActivateBelow` / `severeDeactivateAbove`
- `dietQuality.poorActivateBelow` / `poorDeactivateAbove`
- `dietQuality.balancedActivateAbove` / `balancedDeactivateBelow`
- `dietQuality.optimalActivateAbove` / `optimalDeactivateBelow`
- `dietQuality.transitionTicks`, `balancedSustainTicks`, and `optimalSustainTicks`

Separate activation/deactivation thresholds provide hysteresis; every candidate must remain valid for its configured duration.

## Food Variety

- `variety.enabled`
- `variety.penaltyPerRepeat` (default `0.10`)
- `variety.minimumEfficiency` (default `0.60`)
- `variety.recoveryTicks`

Only the current repeated-food streak is stored. Another food or elapsed recovery time resets it.

## Meal Quality

- `mealQuality.enabled`
- `mealQuality.minimumGroupShare`
- `mealQuality.mixedEfficiencyBonus`
- `mealQuality.completeEfficiencyBonus`
- `mealQuality.diverseEfficiencyBonus`
- `mealQuality.maximumEfficiencyBonus`

## Consequences and effects

- `effects.enableBonuses` / `effects.enablePenalties`
- `consequences.<quality>ExhaustionMultiplier`
- `consequences.<quality>RegenerationMultiplier`
- `consequences.<quality>NutritionEfficiency`

`<quality>` is `balanced`, `optimal`, `poor`, or `severe`; Stable is neutral. Only vanilla FoodData natural regeneration is scaled.

## Recipe resolution

- `resolution.enableRecipeInheritance`
- `resolution.maxDepth`
- `resolution.maxIngredientAlternatives`
- `resolution.maxRecipesPerOutput`

Client tooltip/screen settings never alter authoritative gameplay.
