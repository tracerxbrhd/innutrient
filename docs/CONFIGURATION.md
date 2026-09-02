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
- `variety.memoryCapacity` (default `16`, range `4..32`)
- `variety.scoreWindowTicks` (default `48000`)
- `variety.penaltyPerRepeat` (default `0.10`)
- `variety.minimumEfficiency` (default `0.60`)
- `variety.recoveryTicks`

Diet Memory stores only the newest configured number of meals. `variety.enabled` controls the gameplay
efficiency penalty; bounded memory and its diagnostic score remain available when the penalty is disabled.
Eating another food resets the consecutive-repeat penalty immediately; waiting out `recoveryTicks` also
resets it. Entries older than `scoreWindowTicks` remain bounded save data until displaced but no longer
contribute to the score.

The score is deliberately simple: 50% food-identity diversity and repeat distribution, 25% distinct
quantized nutrient compositions, 20% coverage of groups required for balance, and 5% average Meal
Quality. The result builds confidence over up to eight recent meals, so one new food cannot instantly
produce a perfect score. Fixed tiers are Repetitive (`0–19`), Limited (`20–39`), Varied (`40–59`),
Diverse (`60–79`), and Highly Diverse (`80–100`).

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

The Nutrition Dashboard displays these effective server values, including disabled bonus/penalty gates,
from the synchronized catalog snapshot. Client configuration cannot alter the reported or applied values.

## Recipe resolution

- `resolution.enableRecipeInheritance`
- `resolution.maxDepth`
- `resolution.maxIngredientAlternatives`
- `resolution.maxRecipesPerOutput`

These bounds protect large modpacks from pathological or cyclic recipe graphs.

Client settings (`tooltips.showFoodNutrition`, `tooltips.advancedDetailsRequireShift`, and `screen.showPercentages`) never affect authoritative gameplay.
