package master.innutrient.nutrition;

import master.innutrient.config.InnutrientServerConfig;
import master.innutrient.player.NutritionState;
import master.innutrient.registry.NutritionRegistry;
import master.innutrient.registry.NutritionCondition;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class NutritionEffectsManager {
    private static final Map<UUID, Map<String, Long>> CONDITION_SINCE = new ConcurrentHashMap<>();

    private NutritionEffectsManager() {}

    public static void evaluate(ServerPlayer player, NutritionState state) {
        double balance = NutritionService.balanceScore(state);
        for (var rule : NutritionRegistry.effects()) {
            if (rule.beneficial() && !InnutrientServerConfig.ENABLE_BONUSES.get()) continue;
            if (!rule.beneficial() && !InnutrientServerConfig.ENABLE_PENALTIES.get()) continue;
            NutritionCondition.Evaluation evaluation = new NutritionCondition.Evaluation(state,
                NutritionRegistry.groups(), balance, (path, ticks, matches) -> maintained(player, rule.id().toString()
                    + path, ticks, matches));
            if (!rule.condition().matches(evaluation, "")) continue;
            BuiltInRegistries.MOB_EFFECT.getHolder(rule.effect()).ifPresent(effect -> player.addEffect(
                new MobEffectInstance(effect, rule.durationTicks(), rule.amplifier(), rule.ambient(),
                    rule.showParticles(), true)));
        }
    }

    public static void clear(ServerPlayer player) {
        CONDITION_SINCE.remove(player.getUUID());
    }

    public static void clearAll() {
        CONDITION_SINCE.clear();
    }

    private static boolean maintained(ServerPlayer player, String key, int ticks, boolean matches) {
        Map<String, Long> timers = CONDITION_SINCE.computeIfAbsent(player.getUUID(), ignored -> new ConcurrentHashMap<>());
        if (!matches) {
            timers.remove(key);
            return false;
        }
        long now = player.level().getGameTime();
        long since = timers.computeIfAbsent(key, ignored -> now);
        return now >= since && now - since >= ticks;
    }
}
