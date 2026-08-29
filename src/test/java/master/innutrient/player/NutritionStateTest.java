package master.innutrient.player;

import master.innutrient.nutrition.NutrientGroup;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NutritionStateTest {
    @Test
    void reconcileAddsNewGroupsAndPreservesRemovedValues() {
        Identifier removed = Identifier.parse("oldpack:removed");
        NutrientGroup added = group("newpack:magic", 37.0);
        NutritionState reconciled = new NutritionState(0, Map.of(removed, 12.0)).reconcile(java.util.List.of(added));
        assertEquals(12.0, reconciled.levels().get(removed));
        assertEquals(37.0, reconciled.get(added));
        assertEquals(NutritionState.DATA_VERSION, reconciled.dataVersion());
    }

    @Test
    void valuesAreClampedPerPlayerState() {
        NutrientGroup group = group("innutrient:test", 50);
        NutritionState state = NutritionState.empty().set(group, 500);
        assertEquals(100.0, state.get(group));
        assertTrue(state.levels().containsKey(group.id()));
    }

    private static NutrientGroup group(String id, double defaultValue) {
        Identifier location = Identifier.parse(id);
        return new NutrientGroup(location, "test", Identifier.parse("minecraft:apple"),
            Identifier.parse("innutrient:foods/test"), 0xFFFFFF, 0, defaultValue,
            40, 80, 20, 90, 1, 1, true, false, true);
    }
}
