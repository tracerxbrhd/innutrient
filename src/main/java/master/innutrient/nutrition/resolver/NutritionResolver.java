package master.innutrient.nutrition.resolver;

import master.innutrient.Innutrient;
import master.innutrient.api.NutritionRecipeResolver;
import master.innutrient.config.InnutrientServerConfig;
import master.innutrient.nutrition.NutritionProfile;
import master.innutrient.nutrition.NutritionProfileSource;
import master.innutrient.registry.NutritionRegistry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;

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
    private volatile Map<Identifier, NutritionProfile> edibleSnapshot = Map.of();
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
        var displayContext = SlotDisplayContext.fromLevel(server.overworld());
        server.getRecipeManager().getRecipes().stream()
            .sorted(Comparator.comparing(value -> value.id().identifier().toString()))
            .forEach(holder -> {
                try {
                    holder.value().display().stream()
                        .flatMap(display -> display.result().resolveForStacks(displayContext).stream())
                        .filter(result -> !result.isEmpty())
                        .forEach(result -> {
                            List<RecipeHolder<?>> recipes = index.computeIfAbsent(result.getItem(), ignored -> new ArrayList<>());
                            if (!recipes.contains(holder)) recipes.add(holder);
                        });
                } catch (RuntimeException exception) {
                    Innutrient.LOGGER.debug("Recipe {} cannot expose a nutrition result: {}", holder.id(), exception.getMessage());
                }
            });
        Map<Item, List<RecipeHolder<?>>> immutableIndex = new IdentityHashMap<>();
        index.forEach((item, recipes) -> immutableIndex.put(item, List.copyOf(recipes)));
        recipesByOutput = Map.copyOf(immutableIndex);
        cache.clear();

        Map<Identifier, NutritionProfile> edible = new LinkedHashMap<>();
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
                edible.put(BuiltInRegistries.ITEM.getKey(item), profile);
                switch (profile.source()) {
                    case EXPLICIT -> explicit++;
                    case DIRECT_TAGS -> direct++;
                    case RECIPE_DERIVED -> derived++;
                    default -> {}
                }
            }
        }
        edibleSnapshot = Collections.unmodifiableMap(edible);
        unresolvedEdibleItems = unresolved;
        lastRebuildMillis = (System.nanoTime() - started) / 1_000_000L;
        Innutrient.LOGGER.info("Innutrient nutrition registry rebuilt: {} groups, {} explicit foods, {} directly classified foods, {} recipe-derived foods, {} unresolved edible items; {} ms",
            NutritionRegistry.groups().size(), explicit, direct, derived, unresolved, lastRebuildMillis);
    }

    public Map<Identifier, NutritionProfile> edibleProfiles() {
        return edibleSnapshot;
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

            NutritionProfile direct = NutritionRegistry.directTags(stack);
            if (direct.resolved()) return cache(item, direct);
            if (!InnutrientServerConfig.AUTO_RECIPES.get()) return cache(item, NutritionProfile.unknown());

            List<RecipeHolder<?>> candidates = recipesByOutput.getOrDefault(item, List.of());
            if (candidates.isEmpty()) return cache(item, NutritionProfile.unknown());
            int maximum = Math.min(candidates.size(), InnutrientServerConfig.MAX_RECIPES_PER_OUTPUT.get());
            List<ResolvedRecipe> resolved = new ArrayList<>();
            for (int index = 0; index < maximum; index++) {
                ResolvedRecipe recipe = resolveRecipe(candidates.get(index), depth, guard);
                if (recipe != null && !recipe.values().isEmpty()) resolved.add(recipe);
            }
            if (resolved.isEmpty()) return cache(item, NutritionProfile.unknown());

            int maxDepth = depth;
            for (ResolvedRecipe recipe : resolved) {
                maxDepth = Math.max(maxDepth, recipe.depth());
            }
            Identifier representative = resolved.getFirst().recipeId();
            return cache(item, NutritionProfile.recipe(
                NutritionProfile.averageNutrients(resolved.stream().map(ResolvedRecipe::values).toList()),
                representative, Math.max(1, maxDepth - depth + 1)));
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
        List<Map<Identifier, Double>> resolvedIngredients = new ArrayList<>();
        int maxDepth = depth;
        for (Ingredient ingredient : ingredients) {
            IngredientProfile profile = resolveIngredient(ingredient, depth + 1, guard);
            if (profile.values().isEmpty()) continue;
            resolvedIngredients.add(profile.values());
            maxDepth = Math.max(maxDepth, profile.depth());
        }
        if (resolvedIngredients.isEmpty()) return null;
        return new ResolvedRecipe(holder.id().identifier(), NutritionProfile.averageNutrients(resolvedIngredients), maxDepth + 1);
    }

    private IngredientProfile resolveIngredient(Ingredient ingredient, int depth, ResolutionGuard<Item> guard) {
        List<ItemStack> ordered;
        try {
            var items = ingredient.isCustom()
                ? java.util.Objects.requireNonNull(ingredient.getCustomIngredient()).items()
                : ingredient.getValues().stream();
            ordered = items.map(holder -> holder.value().getDefaultInstance()).filter(stack -> !stack.isEmpty())
                .sorted(Comparator.comparing(stack -> BuiltInRegistries.ITEM.getKey(stack.getItem()).toString()))
                .limit(InnutrientServerConfig.MAX_INGREDIENT_ALTERNATIVES.get()).toList();
        } catch (RuntimeException exception) {
            return new IngredientProfile(Map.of(), depth);
        }
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
        return stack.get(DataComponents.FOOD) != null;
    }

    private record IngredientProfile(Map<Identifier, Double> values, int depth) {}
    private record ResolvedRecipe(Identifier recipeId, Map<Identifier, Double> values, int depth) {}
}
