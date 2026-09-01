package master.innutrient.api;

import master.innutrient.Innutrient;
import master.innutrient.nutrition.NutritionProfile;
import master.innutrient.nutrition.NutritionService;
import master.innutrient.nutrition.DietQuality;
import master.innutrient.nutrition.MealQuality;
import master.innutrient.nutrition.MealQualityEngine;
import master.innutrient.nutrition.resolver.NutritionRecipeResolvers;
import master.innutrient.nutrition.resolver.NutritionResolver;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.List;

public final class DefaultNutritionApi implements NutritionApi {
    @Override public ResourceLocation serviceId() { return Innutrient.id("nutrition"); }
    @Override public NutritionProfile getNutritionProfile(ItemStack stack) { return NutritionResolver.INSTANCE.resolve(stack); }
    @Override public double getNutritionLevel(ServerPlayer player, ResourceLocation nutrient) {
        return NutritionService.levels(player).getOrDefault(nutrient, 0.0);
    }
    @Override public Map<ResourceLocation, Double> getAllNutritionLevels(ServerPlayer player) {
        return NutritionService.levels(player);
    }
    @Override public double getBalanceScore(ServerPlayer player) { return NutritionService.balanceScore(player); }
    @Override public DietQuality getDietQuality(ServerPlayer player) { return NutritionService.dietQuality(player); }
    @Override public double getVarietyScore(ServerPlayer player) { return NutritionService.varietyScore(player); }
    @Override public master.innutrient.nutrition.VarietyTier getVarietyTier(ServerPlayer player) {
        return NutritionService.varietyTier(player);
    }
    @Override public List<RecentFoodSnapshot> getRecentFoods(ServerPlayer player) {
        return NutritionService.get(player).dietMemory().stream().map(RecentFoodSnapshot::from).toList();
    }
    @Override public MealQuality getMealQuality(ItemStack stack) {
        return MealQualityEngine.classify(getNutritionProfile(stack));
    }
    @Override public PlayerNutritionSnapshot getPlayerNutrition(ServerPlayer player) {
        var state = NutritionService.get(player);
        var variety = NutritionService.variety(player);
        return new PlayerNutritionSnapshot(NutritionService.levels(player), NutritionService.balanceScore(state),
            state.dietQuality(), state.dietQualitySince(), variety.value(), variety.tier(),
            state.dietMemory().stream().map(RecentFoodSnapshot::from).toList());
    }
    @Override public void registerRecipeResolver(NutritionRecipeResolver resolver) {
        NutritionRecipeResolvers.register(resolver);
    }
}
