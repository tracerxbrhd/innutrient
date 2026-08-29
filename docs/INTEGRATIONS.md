# Integrations

## Farmer's Delight

Farmer's Delight is not a declared dependency on the Minecraft 26.2 branch, and no 26.2 build has been runtime-validated.

Current native support consists of:

- curated optional tag entries for cabbage, tomato, onion, rice, dough, pasta, eggs, meat cuts, bacon, fish slices, ham, and mutton chops;
- generic analysis of recipes when they expose ordinary `Recipe#placementInfo()` ingredients and `Recipe#display()` outputs;
- recursive inheritance into meals assembled from those ingredients.

These data entries are optional and harmless when the mod is absent. A future compatible 26.2 port can use the generic path when it exposes standard placement and display data; unusual extra outputs still need a custom resolver or datapack override.

## Adding another recipe system

Implement `master.innutrient.api.NutritionRecipeResolver` and register it through `NutritionApi.registerRecipeResolver` during mod setup. Give a specialized resolver higher priority than the generic adapter. Optional integration code must remain behind the other mod's presence check and must not reference absent classes from always-loaded code.
