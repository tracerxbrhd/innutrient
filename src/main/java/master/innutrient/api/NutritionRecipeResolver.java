package master.innutrient.api;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;

/** Extension point for recipe systems whose ingredients are not exposed through Recipe#getIngredients(). */
public interface NutritionRecipeResolver {
    ResourceLocation id();

    default int priority() {
        return 0;
    }

    boolean supports(RecipeHolder<?> recipe);

    List<Ingredient> ingredients(RecipeHolder<?> recipe);
}
