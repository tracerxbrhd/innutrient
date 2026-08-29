# Public API

Discover the service through U-API:

```java
NutritionApi.find().ifPresent(api -> {
    NutritionProfile profile = api.getNutritionProfile(stack);
    double protein = api.getNutritionLevel(player,
        ResourceLocation.parse("innutrient:proteins"));
    double balance = api.getBalanceScore(player);
});
```

Available operations:

- `getNutritionProfile(ItemStack)`
- `getNutritionLevel(ServerPlayer, ResourceLocation)`
- `getAllNutritionLevels(ServerPlayer)`
- `getBalanceScore(ServerPlayer)`
- `registerRecipeResolver(NutritionRecipeResolver)`

Returned maps and profiles are immutable. Gameplay mutation is intentionally not part of the public API in 0.1.0; server-authoritative commands and consumption events own state changes.

The service ID is `innutrient:nutrition`, registered globally through `UApiServices`.
