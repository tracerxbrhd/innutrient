package master.innutrient.nutrition;

import master.innutrient.config.InnutrientServerConfig;
import master.innutrient.player.NutritionState;
import master.innutrient.registry.NutritionRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;

public final class NutritionEffectsManager {
    private NutritionEffectsManager() {}

    public static void evaluate(ServerPlayer player, NutritionState state) {
        for (var rule : NutritionRegistry.effects()) {
            if (rule.beneficial() && !InnutrientServerConfig.ENABLE_BONUSES.get()) continue;
            if (!rule.beneficial() && !InnutrientServerConfig.ENABLE_PENALTIES.get()) continue;
            if (!rule.matches(state, NutritionRegistry.groups())) continue;
            BuiltInRegistries.MOB_EFFECT.get(rule.effect()).ifPresent(effect -> player.addEffect(
                new MobEffectInstance(effect, rule.durationTicks(), rule.amplifier(), rule.ambient(),
                    rule.showParticles(), true)));
        }
    }
}
