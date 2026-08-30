package master.innutrient.nutrition;

import master.innutrient.player.NutritionState;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DietQualityEngineTest {
    private static final DietQualityEngine.Settings SETTINGS = new DietQualityEngine.Settings(
        30, 35, 55, 60, 80, 75, 93, 88, 20, 100, 200);

    @Test
    void balancedUsesHysteresis() {
        NutrientGroup group = group("fruit", true, false);
        NutritionState state = state(DietQuality.BALANCED, group, 60);

        assertEquals(DietQuality.BALANCED,
            DietQualityEngine.desired(DietQuality.BALANCED, state, List.of(group), 77, SETTINGS));
        assertEquals(DietQuality.STABLE,
            DietQualityEngine.desired(DietQuality.BALANCED, state, List.of(group), 74, SETTINGS));
    }

    @Test
    void balancedRequiresSustainedState() {
        NutrientGroup group = group("fruit", true, false);
        NutritionState state = state(DietQuality.STABLE, group, 60);
        NutritionState candidate = DietQualityEngine.update(state, List.of(group), 85, 100, SETTINGS);

        assertEquals(DietQuality.STABLE, candidate.dietQuality());
        assertEquals(DietQuality.BALANCED, candidate.candidateDietQuality());
        assertEquals(DietQuality.STABLE,
            DietQualityEngine.update(candidate, List.of(group), 85, 199, SETTINGS).dietQuality());
        assertEquals(DietQuality.BALANCED,
            DietQualityEngine.update(candidate, List.of(group), 85, 200, SETTINGS).dietQuality());
    }

    @Test
    void penalizedDeficiencyAndExcessDrivePoorOrSevere() {
        NutrientGroup low = group("protein", true, false);
        NutrientGroup sugar = group("sugar", false, true);
        NutritionState oneProblem = new NutritionState(NutritionState.DATA_VERSION,
            Map.of(low.id(), 10.0, sugar.id(), 30.0));
        NutritionState twoProblems = new NutritionState(NutritionState.DATA_VERSION,
            Map.of(low.id(), 10.0, sugar.id(), 95.0));

        assertEquals(DietQuality.POOR,
            DietQualityEngine.desired(DietQuality.STABLE, oneProblem, List.of(low, sugar), 70, SETTINGS));
        assertEquals(DietQuality.SEVERE,
            DietQualityEngine.desired(DietQuality.STABLE, twoProblems, List.of(low, sugar), 70, SETTINGS));
    }

    private static NutritionState state(DietQuality quality, NutrientGroup group, double value) {
        return new NutritionState(NutritionState.DATA_VERSION, Map.of(group.id(), value), quality, quality,
            0, 0, null, 0, 0);
    }

    private static NutrientGroup group(String path, boolean penalizeLow, boolean penalizeHigh) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath("innutrient", path);
        return new NutrientGroup(id, "test", ResourceLocation.parse("minecraft:apple"),
            ResourceLocation.fromNamespaceAndPath("innutrient", "foods/" + path), 0xFFFFFF, 0,
            50, 40, 80, 20, 90, 1, 1, penalizeLow, penalizeHigh, true);
    }
}
