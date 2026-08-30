# Public API

Discover the globally registered service through U-API:

```java
NutritionApi.find().ifPresent(api -> {
    NutritionProfile food = api.getNutritionProfile(stack);
    MealQuality meal = api.getMealQuality(stack);
    PlayerNutritionSnapshot playerNutrition = api.getPlayerNutrition(player);
    DietQuality quality = playerNutrition.dietQuality();
});
```

## Stable operations

- `getNutritionProfile(ItemStack)` returns the immutable, server-resolved composition.
- `getNutritionLevel(ServerPlayer, Identifier)` reads one authoritative level.
- `getAllNutritionLevels(ServerPlayer)` returns an immutable map.
- `getBalanceScore(ServerPlayer)` returns `0..100`.
- `getDietQuality(ServerPlayer)` returns the sustained quality state.
- `getMealQuality(ItemStack)` classifies the resolved profile.
- `getPlayerNutrition(ServerPlayer)` returns an immutable snapshot containing levels, balance, Diet Quality, and its start time.
- `registerRecipeResolver(NutritionRecipeResolver)` registers a resolver before recipe-cache rebuild.

The service ID is `innutrient:nutrition`. Mutable attachment internals are deliberately not exposed. Gameplay mutations remain server-authoritative.

## Recipe resolvers

A resolver supplies ingredients for a non-standard recipe while the core graph engine owns recursion, bounds, cycle detection, caching, and normalization.

```java
public final class ExampleResolver implements NutritionRecipeResolver {
    public Identifier id() { return Identifier.parse("example:nutrition"); }
    public int priority() { return 100; }
    public boolean supports(RecipeHolder<?> recipe) { /* type check */ }
    public List<Ingredient> ingredients(RecipeHolder<?> recipe) { /* immutable list */ }
}
```

Register through `NutritionApi.registerRecipeResolver`. Optional integrations must be guarded by mod presence and keep absent classes out of always-loaded code.
