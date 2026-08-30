package master.innutrient.nutrition;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MealQualityEngineTest {
    @Test
    void classifiesOneThroughFourMeaningfulGroups() {
        assertEquals(MealQuality.BASIC, MealQualityEngine.classify(profile(1), 0.08));
        assertEquals(MealQuality.MIXED, MealQualityEngine.classify(profile(2), 0.08));
        assertEquals(MealQuality.COMPLETE, MealQualityEngine.classify(profile(3), 0.08));
        assertEquals(MealQuality.DIVERSE, MealQualityEngine.classify(profile(4), 0.08));
        assertEquals(MealQuality.DIVERSE, MealQualityEngine.classify(profile(5), 0.08));
    }

    @Test
    void ignoresTraceGroupsAndCapsConfiguredBonus() {
        NutritionProfile profile = NutritionProfile.of(Map.of(id("main"), 0.96, id("trace"), 0.04),
            NutritionProfileSource.EXPLICIT);
        assertEquals(MealQuality.BASIC, MealQualityEngine.classify(profile, 0.08));
        assertEquals(1.25, MealQualityEngine.multiplier(MealQuality.DIVERSE, 0.08, 0.15, 0.40, 0.25), 1.0e-9);
    }

    private static NutritionProfile profile(int groups) {
        Map<ResourceLocation, Double> values = new LinkedHashMap<>();
        for (int index = 0; index < groups; index++) values.put(id("group_" + index), 1.0);
        return NutritionProfile.of(values, NutritionProfileSource.RECIPE_DERIVED);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("innutrient", path);
    }
}
