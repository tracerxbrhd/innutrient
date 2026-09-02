package master.innutrient.nutrition;

import master.innutrient.player.NutritionAttachments;
import master.innutrient.player.NutritionState;
import master.innutrient.config.InnutrientServerConfig;
import master.innutrient.registry.NutritionRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodProperties;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class NutritionService {
    private NutritionService() {}

    public static NutritionState get(ServerPlayer player) {
        NutritionState current = player.getData(NutritionAttachments.STATE);
        NutritionState reconciled = current.reconcile(NutritionRegistry.groups(),
            InnutrientServerConfig.VARIETY_MEMORY_CAPACITY.get());
        if (reconciled != current) player.setData(NutritionAttachments.STATE, reconciled);
        return reconciled;
    }

    public static boolean set(ServerPlayer player, ResourceLocation groupId, double value) {
        NutrientGroup group = NutritionRegistry.group(groupId).orElse(null);
        if (group == null || !Double.isFinite(value)) return false;
        NutritionState current = get(player);
        NutritionState changed = current.set(group, value);
        if (!changed.equals(current)) update(player, changed, true);
        return true;
    }

    public static boolean add(ServerPlayer player, ResourceLocation groupId, double amount) {
        NutrientGroup group = NutritionRegistry.group(groupId).orElse(null);
        if (group == null || !Double.isFinite(amount)) return false;
        NutritionState current = get(player);
        NutritionState changed = current.add(group, amount);
        if (!changed.equals(current)) update(player, changed, true);
        return true;
    }

    public static void reset(ServerPlayer player) {
        update(player, get(player).reset(NutritionRegistry.groups()), true);
    }

    public static double consume(ServerPlayer player, NutritionProfile profile, FoodProperties food) {
        return consume(player, null, profile, food);
    }

    public static double consume(ServerPlayer player, ResourceLocation foodId, NutritionProfile profile,
                                 FoodProperties food) {
        if (!profile.resolved()) return 0;
        NutritionState current = get(player);
        double varietyEfficiency = 1.0;
        FoodVariety.Result variety = null;
        if (foodId != null) {
            long gameTime = player.level().getGameTime();
            DietMemoryEntry entry = DietMemoryEntry.from(foodId, gameTime, profile,
                MealQualityEngine.classify(profile), InnutrientServerConfig.MEAL_MINIMUM_GROUP_SHARE.get());
            double repeatPenalty = InnutrientServerConfig.VARIETY_ENABLED.get()
                ? InnutrientServerConfig.VARIETY_REPEAT_PENALTY.get() : 0;
            variety = FoodVariety.consume(current.dietMemory(), entry,
                InnutrientServerConfig.VARIETY_MEMORY_CAPACITY.get(), repeatPenalty,
                InnutrientServerConfig.VARIETY_MINIMUM_EFFICIENCY.get(),
                InnutrientServerConfig.VARIETY_RECOVERY_TICKS.get());
            varietyEfficiency = variety.efficiency();
        }
        double totalGain = NutritionGainCalculator.totalGain(food, profile, varietyEfficiency,
            current.dietQuality());
        if (totalGain <= 0) return 0;
        NutritionState changed = current;
        for (NutrientGroup group : NutritionRegistry.groups()) {
            double weight = profile.nutrients().getOrDefault(group.id(), 0.0);
            if (weight > 0) changed = changed.add(group, totalGain * weight * group.gainMultiplier());
        }
        if (variety != null)
            changed = changed.withDietMemory(variety.memory());
        changed = updateDietQuality(changed, player.level().getGameTime());
        update(player, changed, true);
        return totalGain;
    }

    public static void decay(ServerPlayer player, double baseAmount) {
        if (!Double.isFinite(baseAmount) || baseAmount <= 0) return;
        NutritionState changed = get(player);
        for (NutrientGroup group : NutritionRegistry.groups())
            changed = changed.add(group, -baseAmount * group.decayMultiplier());
        update(player, changed, false);
    }

    public static Map<ResourceLocation, Double> levels(ServerPlayer player) {
        NutritionState state = get(player);
        Map<ResourceLocation, Double> values = new LinkedHashMap<>();
        for (NutrientGroup group : NutritionRegistry.groups()) values.put(group.id(), state.get(group));
        return Map.copyOf(values);
    }

    /** Geometric mean of per-group target closeness, so one severe deficiency cannot be hidden by high values. */
    public static double balanceScore(NutritionState state) {
        return balanceScore(state, NutritionRegistry.groups());
    }

    public static double balanceScore(NutritionState state, List<NutrientGroup> groups) {
        double logSum = 0;
        int count = 0;
        for (NutrientGroup group : groups) {
            if (!group.requiredForBalance()) continue;
            double closeness = group.balanceCloseness(state.get(group));
            if (closeness <= 0) return 0;
            logSum += Math.log(closeness);
            count++;
        }
        return count == 0 ? 100.0 : Math.exp(logSum / count) * 100.0;
    }

    public static double balanceScore(ServerPlayer player) {
        return balanceScore(get(player));
    }

    public static DietQuality dietQuality(ServerPlayer player) {
        return get(player).dietQuality();
    }

    public static FoodVariety.Score variety(ServerPlayer player) {
        return variety(get(player), player.level().getGameTime(), NutritionRegistry.groups());
    }

    public static FoodVariety.Score variety(NutritionState state, long gameTime, List<NutrientGroup> groups) {
        return variety(state, gameTime, groups, InnutrientServerConfig.VARIETY_SCORE_WINDOW_TICKS.get(),
            Math.min(8, InnutrientServerConfig.VARIETY_MEMORY_CAPACITY.get()));
    }

    /** Pure display-compatible variant using the server settings synchronized with the catalog. */
    public static FoodVariety.Score variety(NutritionState state, long gameTime, List<NutrientGroup> groups,
                                            long scoreWindowTicks, int sampleTarget) {
        Set<ResourceLocation> requiredGroups = groups.stream().filter(NutrientGroup::requiredForBalance)
            .map(NutrientGroup::id).collect(java.util.stream.Collectors.toUnmodifiableSet());
        return FoodVariety.score(state.dietMemory(), gameTime, scoreWindowTicks, requiredGroups, sampleTarget);
    }

    public static double varietyScore(ServerPlayer player) {
        return variety(player).value();
    }

    public static VarietyTier varietyTier(ServerPlayer player) {
        return variety(player).tier();
    }

    public static void tickDietQuality(ServerPlayer player) {
        NutritionState current = get(player);
        NutritionState changed = updateDietQuality(current, player.level().getGameTime());
        if (!changed.equals(current)) update(player, changed, false);
    }

    private static NutritionState updateDietQuality(NutritionState state, long gameTime) {
        return DietQualityEngine.update(state, NutritionRegistry.groups(), balanceScore(state), gameTime);
    }

    private static void update(ServerPlayer player, NutritionState state, boolean evaluateEffects) {
        player.setData(NutritionAttachments.STATE, state);
        if (evaluateEffects) NutritionEffectsManager.evaluate(player, state);
    }
}
