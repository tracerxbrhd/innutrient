package master.innutrient.nutrition;

import master.innutrient.player.NutritionAttachments;
import master.innutrient.player.NutritionState;
import master.innutrient.registry.NutritionRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodProperties;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class NutritionService {
    private NutritionService() {}

    public static NutritionState get(ServerPlayer player) {
        NutritionState current = player.getData(NutritionAttachments.STATE);
        NutritionState reconciled = current.reconcile(NutritionRegistry.groups());
        if (reconciled != current) player.setData(NutritionAttachments.STATE, reconciled);
        return reconciled;
    }

    public static boolean set(ServerPlayer player, Identifier groupId, double value) {
        NutrientGroup group = NutritionRegistry.group(groupId).orElse(null);
        if (group == null || !Double.isFinite(value)) return false;
        NutritionState current = get(player);
        NutritionState changed = current.set(group, value);
        if (!changed.equals(current)) update(player, changed, true);
        return true;
    }

    public static boolean add(ServerPlayer player, Identifier groupId, double amount) {
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
        if (!profile.resolved()) return 0;
        double totalGain = NutritionGainCalculator.totalGain(food);
        if (totalGain <= 0) return 0;
        NutritionState changed = get(player);
        for (NutrientGroup group : NutritionRegistry.groups()) {
            double weight = profile.nutrients().getOrDefault(group.id(), 0.0);
            if (weight > 0) changed = changed.add(group, totalGain * weight * group.gainMultiplier());
        }
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

    public static Map<Identifier, Double> levels(ServerPlayer player) {
        NutritionState state = get(player);
        Map<Identifier, Double> values = new LinkedHashMap<>();
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

    private static void update(ServerPlayer player, NutritionState state, boolean evaluateEffects) {
        player.setData(NutritionAttachments.STATE, state);
        if (evaluateEffects) NutritionEffectsManager.evaluate(player, state);
    }
}
