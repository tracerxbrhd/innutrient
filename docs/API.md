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
- `getNutritionLevel(ServerPlayer, ResourceLocation)` reads one authoritative level.
- `getAllNutritionLevels(ServerPlayer)` returns an immutable map.
- `getBalanceScore(ServerPlayer)` returns `0..100`.
- `getDietQuality(ServerPlayer)` returns the sustained quality state.
- `getMealQuality(ItemStack)` classifies the resolved profile.
- `getPlayerNutrition(ServerPlayer)` returns an immutable `PlayerNutritionSnapshot` containing levels, balance, Diet Quality, and the time at which that quality began.
- `registerRecipeResolver(NutritionRecipeResolver)` registers a resolver before recipe-cache rebuild.

The service ID is `innutrient:nutrition`. Mutable attachment internals are deliberately not exposed. Gameplay mutations remain server-authoritative through consumption, decay, commands, and datapack rules.

## Recipe resolvers

A resolver supplies ingredients for a non-standard recipe while keeping the core graph engine responsible for recursion, bounds, cycle detection, caching, and normalization.

```java
public final class ExampleResolver implements NutritionRecipeResolver {
    public ResourceLocation id() { return ResourceLocation.parse("example:nutrition"); }
    public int priority() { return 100; }
    public boolean supports(RecipeHolder<?> recipe) { /* type check */ }
    public List<Ingredient> ingredients(RecipeHolder<?> recipe) { /* immutable list */ }
}
```

Register through `NutritionApi.registerRecipeResolver`. Optional integration code must be guarded by a mod-presence check and must not reference absent classes from always-loaded code.
