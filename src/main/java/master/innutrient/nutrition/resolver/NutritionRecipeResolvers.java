package master.innutrient.nutrition.resolver;

import master.innutrient.api.NutritionRecipeResolver;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class NutritionRecipeResolvers {
    private static final Map<ResourceLocation, NutritionRecipeResolver> RESOLVERS = new LinkedHashMap<>();

    static {
        register(new GenericResolver());
    }

    private NutritionRecipeResolvers() {}

    public static synchronized void register(NutritionRecipeResolver resolver) {
        if (RESOLVERS.putIfAbsent(resolver.id(), resolver) != null)
            throw new IllegalStateException("Duplicate nutrition recipe resolver " + resolver.id());
    }

    public static synchronized List<NutritionRecipeResolver> all() {
        List<NutritionRecipeResolver> values = new ArrayList<>(RESOLVERS.values());
        values.sort(Comparator.comparingInt(NutritionRecipeResolver::priority).reversed()
            .thenComparing(value -> value.id().toString()));
        return List.copyOf(values);
    }

    private static final class GenericResolver implements NutritionRecipeResolver {
        private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("innutrient", "generic");

        @Override public ResourceLocation id() { return ID; }
        @Override public int priority() { return Integer.MIN_VALUE; }
        @Override public boolean supports(RecipeHolder<?> recipe) { return !recipe.value().getIngredients().isEmpty(); }
        @Override public List<Ingredient> ingredients(RecipeHolder<?> recipe) {
            return List.copyOf(recipe.value().getIngredients());
        }
    }
}
