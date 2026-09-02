package master.innutrient.client.gui;

import master.innutrient.nutrition.FoodVariety;
import master.innutrient.nutrition.NutrientGroup;
import master.innutrient.nutrition.NutrientStatus;
import master.innutrient.nutrition.VarietyTier;
import master.innutrient.player.NutritionState;
import net.minecraft.network.chat.Component;

import java.util.List;

/** Selects one concise, deterministic player-facing message instead of flooding the dashboard. */
final class DashboardGuidance {
    private DashboardGuidance() {}

    static Result select(NutritionState state, List<NutrientGroup> groups, FoodVariety.Score variety) {
        NutrientGroup deficient = first(groups, state, NutrientStatus.DEFICIENT, true);
        if (deficient != null) return new Result(Component.translatable("screen.innutrient.context.low",
            Component.translatable(deficient.translationKey())), true);
        NutrientGroup excessive = first(groups, state, NutrientStatus.EXCESSIVE, false);
        if (excessive != null) return new Result(Component.translatable("screen.innutrient.context.excessive",
            Component.translatable(excessive.translationKey())), true);
        NutrientGroup below = first(groups, state, NutrientStatus.BELOW_TARGET, true);
        if (below != null) return new Result(Component.translatable("screen.innutrient.context.low",
            Component.translatable(below.translationKey())), true);
        NutrientGroup high = first(groups, state, NutrientStatus.ABOVE_TARGET, false);
        if (high != null) return new Result(Component.translatable("screen.innutrient.context.high",
            Component.translatable(high.translationKey())), true);
        if (variety != null && (variety.tier() == VarietyTier.REPETITIVE || variety.tier() == VarietyTier.LIMITED))
            return new Result(Component.translatable("screen.innutrient.context.variety_limited"), true);
        return new Result(Component.translatable("screen.innutrient.context.ok"), false);
    }

    private static NutrientGroup first(List<NutrientGroup> groups, NutritionState state, NutrientStatus status,
                                       boolean low) {
        return groups.stream().filter(group -> (low ? group.penalizeLow() : group.penalizeHigh()))
            .filter(group -> group.status(state.get(group)) == status).findFirst().orElse(null);
    }

    record Result(Component message, boolean warning) {}
}
