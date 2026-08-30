package master.innutrient.client;

import master.innutrient.network.NutritionCatalogPayload;
import master.innutrient.nutrition.NutrientGroup;
import master.innutrient.nutrition.NutritionProfile;
import master.innutrient.network.NutritionFoodData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;

public final class ClientNutritionCatalog {
    private static volatile List<NutrientGroup> groups = List.of();
    private static volatile Map<ResourceLocation, NutritionFoodData> foods = Map.of();
    private static volatile long revision;

    private ClientNutritionCatalog() {}

    public static void replace(NutritionCatalogPayload payload) {
        groups = List.copyOf(payload.groups());
        foods = Map.copyOf(payload.foods());
        revision++;
    }

    public static List<NutrientGroup> groups() { return groups; }
    public static NutritionProfile profile(ItemStack stack) {
        return food(stack).profile();
    }
    public static NutritionFoodData food(ItemStack stack) {
        return foods.getOrDefault(BuiltInRegistries.ITEM.getKey(stack.getItem()),
            new NutritionFoodData(NutritionProfile.unknown(), 0, null, 1));
    }
    public static long revision() { return revision; }
}
