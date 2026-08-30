# Food resolution

Every edible item is resolved in this order:

```text
explicit food profile / datapack override
→ specialized or generic recipe inheritance
→ configured nutrient item tags (including Common Tags)
→ unclassified
```

An explicit rule always wins and can set `disable_automatic`. Recipe inheritance precedes tags so a multi-ingredient meal keeps its complete composition instead of being reduced to a single output tag.

## Recipe algorithm

1. On server reload, recipes are indexed by output and deterministically sorted by recipe ID.
2. The highest-priority registered resolver exposes ingredients. The built-in fallback uses `Recipe#getIngredients()` (or the current-version equivalent).
3. Ingredient alternatives are sorted by item ID, capped, recursively resolved, and averaged.
4. Resolved ingredient profiles are combined and normalized. Multiple recipes for one output are averaged within the configured cap.
5. A visited-item guard stops cycles; maximum depth, alternatives, and recipes are bounded.
6. Profiles, Meal Quality, and baseline tooltip gain are cached by item and synchronized after reload.

Eating and client tooltips do not traverse the recipe graph. Datapack/recipe reload invalidates the server cache, rebuilds the synchronized catalog, reconciles online players, and refreshes clients.

Missing ingredients and unresolved alternatives do not invent nutrition. Output stack count does not change composition percentages or vanilla food properties.

## Common Tags

Both supported NeoForge versions use verified `c:foods/*` tags for fruit, berries, vegetables, bread, dough, raw/cooked meat, raw/cooked fish, cookies, candy, and pies, plus `c:eggs` and `c:crops/wheat`. The 1.21.1 branch also accepts optional legacy aliases used by Pam's HarvestCraft 2 and Farm & Charm. Compatibility datapacks can extend Innutrient's group item tags without Java code.
