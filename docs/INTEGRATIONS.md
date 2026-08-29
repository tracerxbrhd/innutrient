# Integrations

## Farmer's Delight

Farmer's Delight is optional and never loaded as a Java dependency.

Current native support consists of:

- curated optional tag entries for cabbage, tomato, onion, rice, dough, pasta, eggs, meat cuts, bacon, fish slices, ham, and mutton chops;
- automatic analysis of Cooking Pot and Cutting Board recipes when they expose ordinary `Recipe#getIngredients()` data;
- recursive inheritance into meals assembled from those ingredients.

This matches the Farmer's Delight 1.21 branch recipe format, where Cooking Pot recipes expose an ingredient array. Cutting recipes that expose only a primary result are resolved for that primary result; unusual extra outputs need a custom resolver or datapack override.

## Adding another recipe system

Implement `master.innutrient.api.NutritionRecipeResolver` and register it through `NutritionApi.registerRecipeResolver` during mod setup. Give a specialized resolver higher priority than the generic adapter. Optional integration code must remain behind the other mod's presence check and must not reference absent classes from always-loaded code.
