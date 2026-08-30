# Configuration

Server settings are generated per world at `<world>/serverconfig/uapi/innutrient/server.toml`. Client display settings are generated at `config/uapi/innutrient/client.toml`.

## Nutrition and decay

- `gain.globalMultiplier`
- `decay.mode`: `HUNGER`, `PERIODIC`, `HYBRID`, or `NONE`
- `decay.perHungerPoint`
- `decay.periodicIntervalTicks` and `decay.periodicAmount`
- `player.deathRetentionPercent`

The base formula is:

```text
(vanilla nutrition + absolute saturation restored × 0.5) × globalMultiplier
```

Meal Quality, Food Variety, and sustained Diet Quality then modify nutrition gain only. Innutrient does not change the food item's vanilla hunger or saturation.

## Diet Quality

- `dietQuality.severeActivateBelow` / `severeDeactivateAbove`
- `dietQuality.poorActivateBelow` / `poorDeactivateAbove`
- `dietQuality.balancedActivateAbove` / `balancedDeactivateBelow`
- `dietQuality.optimalActivateAbove` / `optimalDeactivateBelow`
- `dietQuality.transitionTicks`
- `dietQuality.balancedSustainTicks`
- `dietQuality.optimalSustainTicks`

Separate activation and deactivation values provide hysteresis. Candidate states must also remain valid for their configured duration.

## Food Variety

- `variety.enabled`
- `variety.penaltyPerRepeat` (default `0.10`)
- `variety.minimumEfficiency` (default `0.60`)
- `variety.recoveryTicks`

Only the current repeated-food streak is stored. Eating another food resets it immediately; waiting out the recovery time also resets it.

## Meal Quality

- `mealQuality.enabled`
- `mealQuality.minimumGroupShare`
- `mealQuality.mixedEfficiencyBonus`
- `mealQuality.completeEfficiencyBonus`
- `mealQuality.diverseEfficiencyBonus`
- `mealQuality.maximumEfficiencyBonus`

Trace recipe contributions below `minimumGroupShare` do not inflate meal tier.

## Consequences and effects

- `effects.enableBonuses` / `effects.enablePenalties`
- `consequences.<quality>ExhaustionMultiplier`
- `consequences.<quality>RegenerationMultiplier`
- `consequences.<quality>NutritionEfficiency`

`<quality>` is `balanced`, `optimal`, `poor`, or `severe`. Stable is always neutral. Natural-regeneration modifiers apply only to vanilla FoodData healing; external healing sources are untouched.

## Recipe resolution

- `resolution.enableRecipeInheritance`
- `resolution.maxDepth`
- `resolution.maxIngredientAlternatives`
- `resolution.maxRecipesPerOutput`

These bounds protect large modpacks from pathological or cyclic recipe graphs.

Client settings (`tooltips.showFoodNutrition`, `tooltips.advancedDetailsRequireShift`, and `screen.showPercentages`) never affect authoritative gameplay.
