package master.innutrient.mixin;

import master.innutrient.nutrition.NutritionConsequences;
import master.innutrient.nutrition.NutritionService;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Scales only vanilla natural-regeneration heals emitted by FoodData.tick. */
@Mixin(FoodData.class)
public abstract class FoodDataMixin {
    @Redirect(method = "tick", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/server/level/ServerPlayer;heal(F)V"))
    private void innutrient$modifyNaturalRegeneration(ServerPlayer player, float amount) {
        double multiplier = NutritionConsequences.regenerationMultiplier(NutritionService.dietQuality(player));
        player.heal((float) (amount * multiplier));
    }
}
