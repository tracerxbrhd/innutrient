package master.innutrient.nutrition;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NutrientGroupTest {
    @Test
    void ordinaryGroupPenalizesSevereDeficiency() {
        NutrientGroup group = group(true, false, 40, 80);
        assertEquals(1.0, group.balanceCloseness(60), 1.0e-9);
        assertEquals(0.25, group.balanceCloseness(10), 1.0e-9);
        assertEquals(1.0, group.balanceCloseness(100), 1.0e-9);
    }

    @Test
    void asymmetricSugarGroupIgnoresLowAndPenalizesHigh() {
        NutrientGroup group = group(false, true, 0, 60);
        assertEquals(1.0, group.balanceCloseness(0), 1.0e-9);
        assertEquals(0.5, group.balanceCloseness(80), 1.0e-9);
    }

    private static NutrientGroup group(boolean low, boolean high, double min, double max) {
        return new NutrientGroup(ResourceLocation.parse("innutrient:test"), "test",
            ResourceLocation.parse("minecraft:apple"), ResourceLocation.parse("innutrient:foods/test"),
            0xFFFFFF, 0, 50, min, max, Math.min(20, min), Math.max(90, max),
            1, 1, low, high, true);
    }
}
