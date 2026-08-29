# Food resolution

Innutrient resolves each item with this precedence:

```text
matching explicit food rules
→ configured nutrient-group item tags
→ recipe-derived composition
→ unresolved
```

Results are cached by Item. A datapack reload rebuilds the output-to-recipe index and invalidates the cache; eating and tooltips do not walk recipe graphs during normal play.

## Recipe algorithm

1. Recipes are indexed and sorted by recipe ID.
2. Recipe outputs are indexed through `Recipe#display()`, and the first registered `NutritionRecipeResolver` supporting a recipe exposes its ingredients. The built-in lowest-priority adapter accepts any recipe with non-empty `Recipe#placementInfo()` ingredients.
3. Ingredient alternatives are sorted by item ID, capped by `maxIngredientAlternatives`, recursively resolved, and averaged. Unresolved alternatives do not invent nutrition.
4. Resolved ingredient profiles are summed and normalized for that recipe.
5. If several capped recipes produce the same item, their resolved compositions are averaged in recipe-ID order.
6. A visited-item set stops cycles, and `maxDepth` stops pathological chains.

Output stack count does not alter composition percentages; it affects neither the normalized profile nor the food's vanilla hunger/saturation value used for total gain.

The derived balance score is the geometric mean of each required group's closeness to its configured healthy range. This prevents high groups from hiding one severe deficiency. Groups can independently opt out of low or high penalties, so sugar-like asymmetric ranges work naturally.
