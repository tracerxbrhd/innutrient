# Integrations

Innutrient has no required runtime dependency on food mods, JEI, or EMI. Integrations are data-driven first:

1. official NeoForge Common Tags;
2. recipes exposed through the standard recipe contract;
3. optional tag aliases for established ecosystems;
4. explicit datapack profiles for exceptions;
5. a specialized `NutritionRecipeResolver` only when a recipe hides its ingredients.

Farmer's Delight Cooking Pot/Cutting Board and Farm & Charm Cooking Pot, Crafting Bowl, Mincer, Roaster, Silo, and Stove recipe implementations on 1.21.1 expose standard ingredients, so they use the safe generic resolver. Pam's HarvestCraft 2 primarily uses normal crafting/smelting plus legacy common tags. Create: Food processing recipes are handled when their Create recipe implementation exposes ingredients; exceptions can be corrected with a compatibility datapack.

Normal Innutrient item tooltips also appear inside JEI and EMI item views without linking against either API. A custom recipe category is intentionally not added because it would duplicate the synchronized tooltip data and create version-specific hard dependencies.

See [COMPATIBILITY.md](COMPATIBILITY.md) for the version matrix and [API.md](API.md) for custom resolver registration.
