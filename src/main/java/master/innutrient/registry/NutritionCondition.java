package master.innutrient.registry;

import master.innutrient.nutrition.NutrientGroup;
import master.innutrient.nutrition.NutrientStatus;
import master.innutrient.player.NutritionState;
import net.minecraft.resources.Identifier;

import java.util.List;

/** Immutable recursive predicate used by format-version 2 effect rules. */
public record NutritionCondition(Type type, Identifier group, double value, NutrientStatus status,
                                 int count, int ticks, List<NutritionCondition> conditions) {
    public NutritionCondition {
        conditions = conditions == null ? List.of() : List.copyOf(conditions);
        count = Math.max(1, count);
        ticks = Math.max(0, ticks);
    }

    public boolean matches(Evaluation evaluation, String path) {
        return switch (type) {
            case GROUP_BELOW -> selected(evaluation.groups()) != null
                && evaluation.state().get(selected(evaluation.groups())) < value;
            case GROUP_ABOVE -> selected(evaluation.groups()) != null
                && evaluation.state().get(selected(evaluation.groups())) > value;
            case ALL_HEALTHY -> evaluation.groups().stream().filter(NutrientGroup::requiredForBalance)
                .allMatch(candidate -> candidate.status(evaluation.state().get(candidate)) == NutrientStatus.HEALTHY);
            case COUNT_BELOW -> evaluation.groups().stream()
                .filter(candidate -> evaluation.state().get(candidate) < value).count() >= count;
            case BALANCE_ABOVE -> evaluation.balanceScore() >= value;
            case BALANCE_BELOW -> evaluation.balanceScore() <= value;
            case GROUP_STATUS -> selected(evaluation.groups()) != null
                && selected(evaluation.groups()).status(evaluation.state().get(selected(evaluation.groups()))) == status;
            case COUNT_STATUS -> evaluation.groups().stream()
                .filter(candidate -> candidate.status(evaluation.state().get(candidate)) == status).count() >= count;
            case ALL -> evaluateAll(evaluation, path);
            case ANY -> evaluateAny(evaluation, path);
            case NOT -> !conditions.getFirst().matches(evaluation, path + "/0");
            case MAINTAINED_FOR -> evaluation.maintained().test(path, ticks,
                conditions.getFirst().matches(evaluation, path + "/condition"));
        };
    }

    private boolean evaluateAll(Evaluation evaluation, String path) {
        boolean result = true;
        for (int index = 0; index < conditions.size(); index++)
            result &= conditions.get(index).matches(evaluation, path + "/" + index);
        return result;
    }

    private boolean evaluateAny(Evaluation evaluation, String path) {
        boolean result = false;
        for (int index = 0; index < conditions.size(); index++)
            result |= conditions.get(index).matches(evaluation, path + "/" + index);
        return result;
    }

    private NutrientGroup selected(List<NutrientGroup> groups) {
        if (group == null) return null;
        return groups.stream().filter(candidate -> candidate.id().equals(group)).findFirst().orElse(null);
    }

    public enum Type {
        GROUP_BELOW,
        GROUP_ABOVE,
        ALL_HEALTHY,
        COUNT_BELOW,
        BALANCE_ABOVE,
        BALANCE_BELOW,
        GROUP_STATUS,
        COUNT_STATUS,
        MAINTAINED_FOR,
        ALL,
        ANY,
        NOT
    }

    public record Evaluation(NutritionState state, List<NutrientGroup> groups, double balanceScore,
                             MaintainedTest maintained) {}

    @FunctionalInterface
    public interface MaintainedTest {
        boolean test(String path, int ticks, boolean currentlyMatches);
    }
}
