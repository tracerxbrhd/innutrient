package master.innutrient.api;

import dev.uapi.api.services.UApiService;
import dev.uapi.api.services.UApiServices;
import master.innutrient.nutrition.NutritionProfile;
import master.innutrient.nutrition.DietQuality;
import master.innutrient.nutrition.MealQuality;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
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
    MealQuality getMealQuality(ItemStack stack);
    PlayerNutritionSnapshot getPlayerNutrition(ServerPlayer player);
    void registerRecipeResolver(NutritionRecipeResolver resolver);
}
