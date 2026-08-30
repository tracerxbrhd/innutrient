package master.innutrient.registry;

import master.innutrient.nutrition.NutrientGroup;
import master.innutrient.nutrition.NutrientStatus;
import master.innutrient.nutrition.NutritionService;
import master.innutrient.player.NutritionState;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NutritionConditionTest {
    @Test
    void composesBalanceStatusAndBooleanPredicates() {
        NutrientGroup protein = group("proteins", true, false);
        NutrientGroup sugar = group("sugars", false, true);
        NutritionState state = new NutritionState(NutritionState.DATA_VERSION,
            Map.of(protein.id(), 60.0, sugar.id(), 95.0));
        List<NutrientGroup> groups = List.of(protein, sugar);
        NutritionCondition excessiveSugar = condition(NutritionCondition.Type.GROUP_STATUS, sugar.id(), 0,
            NutrientStatus.EXCESSIVE, 1, List.of());
        NutritionCondition balanceBelow = condition(NutritionCondition.Type.BALANCE_BELOW, null, 70,
            null, 1, List.of());
        NutritionCondition any = condition(NutritionCondition.Type.ANY, null, 0, null, 1,
            List.of(excessiveSugar, balanceBelow));
        var evaluation = new NutritionCondition.Evaluation(state, groups,
            NutritionService.balanceScore(state, groups), (path, ticks, matches) -> matches);

        assertTrue(excessiveSugar.matches(evaluation, "sugar"));
        assertTrue(any.matches(evaluation, "any"));
        assertFalse(condition(NutritionCondition.Type.NOT, null, 0, null, 1,
            List.of(excessiveSugar)).matches(evaluation, "not"));
    }

    @Test
    void maintainedForDelegatesToBoundedTimerContext() {
        NutrientGroup group = group("fruit", true, false);
        NutritionState state = new NutritionState(NutritionState.DATA_VERSION, Map.of(group.id(), 10.0));
        AtomicBoolean receivedMatch = new AtomicBoolean();
        NutritionCondition maintained = new NutritionCondition(NutritionCondition.Type.MAINTAINED_FOR,
            null, 0, null, 1, 1200, List.of(condition(NutritionCondition.Type.GROUP_STATUS,
            group.id(), 0, NutrientStatus.DEFICIENT, 1, List.of())));

        assertTrue(maintained.matches(new NutritionCondition.Evaluation(state, List.of(group), 25,
            (path, ticks, matches) -> {
                receivedMatch.set(matches);
                return matches && ticks == 1200;
            }), "rule"));
        assertTrue(receivedMatch.get());
    }

    private static NutritionCondition condition(NutritionCondition.Type type, ResourceLocation group,
                                                double value, NutrientStatus status, int count,
                                                List<NutritionCondition> children) {
        return new NutritionCondition(type, group, value, status, count, 0, children);
    }

    private static NutrientGroup group(String path, boolean penalizeLow, boolean penalizeHigh) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath("innutrient", path);
        return new NutrientGroup(id, "test", ResourceLocation.parse("minecraft:apple"),
            ResourceLocation.fromNamespaceAndPath("innutrient", "foods/" + path), 0xFFFFFF, 0,
            50, 40, 80, 20, 90, 1, 1, penalizeLow, penalizeHigh, true);
    }
}
