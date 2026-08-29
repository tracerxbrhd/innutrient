package master.innutrient.api;

import master.innutrient.Innutrient;
import master.innutrient.nutrition.NutritionProfile;
import master.innutrient.nutrition.NutritionService;
import master.innutrient.nutrition.resolver.NutritionRecipeResolvers;
import master.innutrient.nutrition.resolver.NutritionResolver;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

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
    @Override public void registerRecipeResolver(NutritionRecipeResolver resolver) {
        NutritionRecipeResolvers.register(resolver);
    }
}
