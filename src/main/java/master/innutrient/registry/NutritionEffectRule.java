package master.innutrient.registry;

import master.innutrient.nutrition.NutrientGroup;
import master.innutrient.nutrition.NutrientStatus;
import master.innutrient.player.NutritionState;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record NutritionEffectRule(
    ResourceLocation id,
    ConditionType condition,
    ResourceLocation group,
    double threshold,
    int count,
    ResourceLocation effect,
    int durationTicks,
    int amplifier,
    boolean beneficial,
    boolean ambient,
    boolean showParticles
) {
    public enum ConditionType { GROUP_BELOW, GROUP_ABOVE, ALL_HEALTHY, COUNT_BELOW }

    public boolean matches(NutritionState state, List<NutrientGroup> groups) {
        NutrientGroup selectedGroup = find(groups);
        return switch (condition) {
            case GROUP_BELOW -> selectedGroup != null && state.get(selectedGroup) < threshold;
            case GROUP_ABOVE -> selectedGroup != null && state.get(selectedGroup) > threshold;
            case ALL_HEALTHY -> groups.stream().filter(NutrientGroup::requiredForBalance)
                .allMatch(value -> value.status(state.get(value)) == NutrientStatus.HEALTHY);
            case COUNT_BELOW -> groups.stream().filter(value -> state.get(value) < threshold).count() >= count;
        };
    }

    private NutrientGroup find(List<NutrientGroup> groups) {
        if (group == null) return null;
        return groups.stream().filter(value -> value.id().equals(group)).findFirst().orElse(null);
    }
}
