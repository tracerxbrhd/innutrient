package master.innutrient.nutrition;

import master.innutrient.player.NutritionState;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NutritionServiceTest {
    @Test
    void geometricBalanceCannotHideASevereDeficiency() {
        NutrientGroup healthy = group("healthy");
        NutrientGroup deficient = group("deficient");
        NutritionState state = new NutritionState(NutritionState.DATA_VERSION,
            Map.of(healthy.id(), 60.0, deficient.id(), 10.0));

        assertEquals(50.0, NutritionService.balanceScore(state, List.of(healthy, deficient)), 1.0e-9);
    }

    private static NutrientGroup group(String path) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath("innutrient", path);
        return new NutrientGroup(id, "test", ResourceLocation.parse("minecraft:apple"),
            ResourceLocation.fromNamespaceAndPath("innutrient", "foods/" + path), 0xFFFFFF, 0,
            50, 40, 80, 20, 90, 1, 1, true, false, true);
    }
}
