package master.innutrient.mixin;

import master.innutrient.nutrition.NutritionConsequences;
import master.innutrient.nutrition.NutritionService;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Applies Diet Quality to activity exhaustion without changing a food's vanilla hunger values. */
@Mixin(Player.class)
public abstract class PlayerExhaustionMixin {
    @Redirect(method = "causeFoodExhaustion", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/food/FoodData;addExhaustion(F)V"))
    private void innutrient$modifyExhaustion(FoodData foodData, float amount) {
        Player player = (Player) (Object) this;
        double multiplier = player instanceof ServerPlayer serverPlayer
            ? NutritionConsequences.exhaustionMultiplier(NutritionService.dietQuality(serverPlayer)) : 1.0;
        foodData.addExhaustion((float) (amount * multiplier));
    }
}
