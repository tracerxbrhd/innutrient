package master.innutrient.nutrition.resolver;

import master.innutrient.Innutrient;
import master.innutrient.api.NutritionRecipeResolver;
import master.innutrient.config.InnutrientServerConfig;
import master.innutrient.nutrition.NutritionProfile;
import master.innutrient.nutrition.NutritionProfileSource;
import master.innutrient.nutrition.MealQualityEngine;
import master.innutrient.nutrition.NutritionGainCalculator;
import master.innutrient.network.NutritionFoodData;
import master.innutrient.registry.NutritionRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Server-side recursive resolver. Runtime lookups are Item-keyed cache hits after reload. */
public final class NutritionResolver {
    public static final NutritionResolver INSTANCE = new NutritionResolver();

    private final Map<Item, NutritionProfile> cache = new ConcurrentHashMap<>();
    private volatile Map<Item, List<RecipeHolder<?>>> recipesByOutput = Map.of();
    private volatile Map<ResourceLocation, NutritionProfile> edibleSnapshot = Map.of();
    private volatile Map<ResourceLocation, NutritionFoodData> foodDataSnapshot = Map.of();
    private volatile long lastRebuildMillis;
    private volatile int unresolvedEdibleItems;

    private NutritionResolver() {}

    public NutritionProfile resolve(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return NutritionProfile.unknown();
        return resolve(stack, 0, new ResolutionGuard<>(InnutrientServerConfig.MAX_RECIPE_DEPTH.get()));
    }

    public synchronized void rebuild(MinecraftServer server) {
        long started = System.nanoTime();
        Map<Item, List<RecipeHolder<?>>> index = new IdentityHashMap<>();
        server.getRecipeManager().getRecipes().stream()
            .sorted(Comparator.comparing(value -> value.id().toString()))
            .forEach(holder -> {
                try {
                    ItemStack result = holder.value().getResultItem(server.registryAccess());
                    if (!result.isEmpty()) index.computeIfAbsent(result.getItem(), ignored -> new ArrayList<>()).add(holder);
                } catch (RuntimeException exception) {
                    Innutrient.LOGGER.debug("Recipe {} cannot expose a nutrition result: {}", holder.id(), exception.getMessage());
                }
            });
        Map<Item, List<RecipeHolder<?>>> immutableIndex = new IdentityHashMap<>();
        index.forEach((item, recipes) -> immutableIndex.put(item, List.copyOf(recipes)));
        recipesByOutput = Map.copyOf(immutableIndex);
        cache.clear();

        Map<ResourceLocation, NutritionProfile> edible = new LinkedHashMap<>();
        Map<ResourceLocation, NutritionFoodData> foodData = new LinkedHashMap<>();
        int unresolved = 0;
        int explicit = 0;
        int direct = 0;
        int derived = 0;
        for (Item item : BuiltInRegistries.ITEM) {
            ItemStack stack = item.getDefaultInstance();
            if (!isFood(stack)) continue;
            NutritionProfile profile = resolve(stack);
            if (!profile.resolved()) unresolved++;
            else {
                ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
                edible.put(itemId, profile);
                FoodProperties properties = foodProperties(stack);
                double baseGain = NutritionGainCalculator.totalGain(properties);
                var mealQuality = MealQualityEngine.classify(profile);
                foodData.put(itemId, new NutritionFoodData(profile, baseGain, mealQuality,
                    MealQualityEngine.multiplier(profile)));
                switch (profile.source()) {
                    case EXPLICIT -> explicit++;
                    case DIRECT_TAGS -> direct++;
                    case RECIPE_DERIVED -> derived++;
                    default -> {}
                }
            }
        }
        edibleSnapshot = Collections.unmodifiableMap(edible);
        foodDataSnapshot = Collections.unmodifiableMap(foodData);
        unresolvedEdibleItems = unresolved;
        lastRebuildMillis = (System.nanoTime() - started) / 1_000_000L;
        Innutrient.LOGGER.info("Innutrient nutrition registry rebuilt: {} groups, {} explicit foods, {} directly classified foods, {} recipe-derived foods, {} unresolved edible items; {} ms",
            NutritionRegistry.groups().size(), explicit, direct, derived, unresolved, lastRebuildMillis);
    }

    public Map<ResourceLocation, NutritionProfile> edibleProfiles() {
        return edibleSnapshot;
    }

    public Map<ResourceLocation, NutritionFoodData> edibleFoodData() {
        return foodDataSnapshot;
    }

    public long lastRebuildMillis() {
        return lastRebuildMillis;
    }

    public int unresolvedEdibleItems() {
        return unresolvedEdibleItems;
    }

    public int cacheSize() {
        return cache.size();
    }

    private NutritionProfile resolve(ItemStack stack, int depth, ResolutionGuard<Item> guard) {
        Item item = stack.getItem();
        NutritionProfile cached = cache.get(item);
        if (cached != null) return cached;
        if (!guard.enter(item, depth)) return NutritionProfile.unknown();
        try {
            NutritionRegistry.ExplicitResolution explicit = NutritionRegistry.explicit(stack);
            if (explicit.matched() && explicit.profile().resolved()) return cache(item, explicit.profile());
            if (explicit.matched() && explicit.disableAutomatic()) return cache(item, NutritionProfile.unknown());

            if (InnutrientServerConfig.AUTO_RECIPES.get()) {
                List<RecipeHolder<?>> candidates = recipesByOutput.getOrDefault(item, List.of());
                int maximum = Math.min(candidates.size(), InnutrientServerConfig.MAX_RECIPES_PER_OUTPUT.get());
                List<ResolvedRecipe> resolved = new ArrayList<>();
                for (int index = 0; index < maximum; index++) {
                    ResolvedRecipe recipe = resolveRecipe(candidates.get(index), depth, guard);
                    if (recipe != null && !recipe.values().isEmpty()) resolved.add(recipe);
                }
                if (!resolved.isEmpty()) {
                    int maxDepth = depth;
                    for (ResolvedRecipe recipe : resolved) maxDepth = Math.max(maxDepth, recipe.depth());
                    ResourceLocation representative = resolved.getFirst().recipeId();
                    return cache(item, NutritionProfile.recipe(
                        NutritionProfile.averageNutrients(resolved.stream().map(ResolvedRecipe::values).toList()),
                        representative, Math.max(1, maxDepth - depth + 1)));
                }
            }

            NutritionProfile direct = NutritionRegistry.directTags(stack);
            return cache(item, direct.resolved() ? direct : NutritionProfile.unknown());
        } finally {
            guard.exit(item);
        }
    }

    private ResolvedRecipe resolveRecipe(RecipeHolder<?> holder, int depth, ResolutionGuard<Item> guard) {
        List<Ingredient> ingredients = null;
        for (NutritionRecipeResolver adapter : NutritionRecipeResolvers.all()) {
            try {
                if (adapter.supports(holder)) {
                    ingredients = adapter.ingredients(holder);
                    break;
                }
            } catch (RuntimeException exception) {
                Innutrient.LOGGER.debug("Nutrition recipe adapter {} rejected {}: {}",
                    adapter.id(), holder.id(), exception.getMessage());
            }
        }
        if (ingredients == null || ingredients.isEmpty()) return null;
        List<Map<ResourceLocation, Double>> resolvedIngredients = new ArrayList<>();
        int maxDepth = depth;
        for (Ingredient ingredient : ingredients) {
            IngredientProfile profile = resolveIngredient(ingredient, depth + 1, guard);
            if (profile.values().isEmpty()) continue;
            resolvedIngredients.add(profile.values());
            maxDepth = Math.max(maxDepth, profile.depth());
        }
        if (resolvedIngredients.isEmpty()) return null;
        return new ResolvedRecipe(holder.id(), NutritionProfile.averageNutrients(resolvedIngredients), maxDepth + 1);
    }

    private IngredientProfile resolveIngredient(Ingredient ingredient, int depth, ResolutionGuard<Item> guard) {
        ItemStack[] alternatives;
        try {
            alternatives = ingredient.getItems();
        } catch (RuntimeException exception) {
            return new IngredientProfile(Map.of(), depth);
        }
        List<ItemStack> ordered = java.util.Arrays.stream(alternatives).filter(stack -> !stack.isEmpty())
            .sorted(Comparator.comparing(stack -> BuiltInRegistries.ITEM.getKey(stack.getItem()).toString()))
            .limit(InnutrientServerConfig.MAX_INGREDIENT_ALTERNATIVES.get()).toList();
        List<NutritionProfile> profiles = new ArrayList<>();
        int maxDepth = depth;
        for (ItemStack alternative : ordered) {
            NutritionProfile profile = resolve(alternative, depth, guard);
            if (!profile.resolved()) continue;
            profiles.add(profile);
            maxDepth = Math.max(maxDepth, depth + profile.resolutionDepth());
        }
        if (profiles.isEmpty()) return new IngredientProfile(Map.of(), maxDepth);
        return new IngredientProfile(NutritionProfile.averageNutrients(
            profiles.stream().map(NutritionProfile::nutrients).toList()), maxDepth);
    }

    private NutritionProfile cache(Item item, NutritionProfile profile) {
        cache.put(item, profile);
        return profile;
    }

    private static boolean isFood(ItemStack stack) {
        return foodProperties(stack) != null;
    }

    private static FoodProperties foodProperties(ItemStack stack) {
        try {
            return stack.getFoodProperties(null);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private record IngredientProfile(Map<ResourceLocation, Double> values, int depth) {}
    private record ResolvedRecipe(ResourceLocation recipeId, Map<ResourceLocation, Double> values, int depth) {}
}
