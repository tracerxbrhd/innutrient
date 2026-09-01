package master.innutrient.api;

import dev.uapi.api.services.UApiService;
import dev.uapi.api.services.UApiServices;
import master.innutrient.nutrition.NutritionProfile;
import master.innutrient.nutrition.DietQuality;
import master.innutrient.nutrition.MealQuality;
import master.innutrient.nutrition.VarietyTier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.List;
import java.util.Optional;

public interface NutritionApi extends UApiService {
    static Optional<NutritionApi> find() {
        return UApiServices.find(NutritionApi.class);
    }

    NutritionProfile getNutritionProfile(ItemStack stack);
    double getNutritionLevel(ServerPlayer player, ResourceLocation nutrient);
    Map<ResourceLocation, Double> getAllNutritionLevels(ServerPlayer player);
    double getBalanceScore(ServerPlayer player);
    DietQuality getDietQuality(ServerPlayer player);
    default double getVarietyScore(ServerPlayer player) { return getPlayerNutrition(player).varietyScore(); }
    default VarietyTier getVarietyTier(ServerPlayer player) { return getPlayerNutrition(player).varietyTier(); }
    default List<RecentFoodSnapshot> getRecentFoods(ServerPlayer player) {
        return getPlayerNutrition(player).recentFoods();
    }
    MealQuality getMealQuality(ItemStack stack);
    PlayerNutritionSnapshot getPlayerNutrition(ServerPlayer player);
    void registerRecipeResolver(NutritionRecipeResolver resolver);
}
