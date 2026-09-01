package master.innutrient.player;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import master.innutrient.nutrition.DietMemoryEntry;
import master.innutrient.nutrition.DietQuality;
import master.innutrient.nutrition.MealQuality;
import master.innutrient.nutrition.NutrientGroup;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.List;

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

    @Test
    void versionTwoSaveMigratesWithoutLosingNutritionOrDietQuality() {
        var json = JsonParser.parseString("""
            {
              "data_version": 2,
              "levels": {"innutrient:test": 64.0},
              "diet_quality": "balanced",
              "candidate_diet_quality": "optimal",
              "diet_quality_since": 1200,
              "candidate_since": 2400,
              "last_food": "minecraft:apple",
              "repeat_count": 3,
              "last_food_game_time": 6000
            }
            """);
        NutritionState decoded = NutritionState.CODEC.parse(JsonOps.INSTANCE, json).result().orElseThrow();
        NutritionState migrated = decoded.reconcile(List.of(group("innutrient:test", 50)), 16);

        assertEquals(64.0, migrated.levels().get(Identifier.parse("innutrient:test")));
        assertEquals(DietQuality.BALANCED, migrated.dietQuality());
        assertEquals(DietQuality.OPTIMAL, migrated.candidateDietQuality());
        assertEquals(1200, migrated.dietQualitySince());
        assertEquals(2400, migrated.candidateSince());
        assertEquals(4, migrated.dietMemory().size());
        assertEquals(3, migrated.repeatCount());
        assertEquals(Identifier.parse("minecraft:apple"), migrated.lastFood());
        assertEquals(NutritionState.DATA_VERSION, migrated.dataVersion());
        var encoded = NutritionState.CODEC.encodeStart(JsonOps.INSTANCE, migrated).result().orElseThrow()
            .getAsJsonObject();
        assertTrue(encoded.has("recent_foods"));
        assertTrue(!encoded.has("last_food"));
        assertTrue(!encoded.has("repeat_count"));
        assertTrue(!encoded.has("last_food_game_time"));
    }

    @Test
    void reconcileBoundsMemoryAndResetClearsIt() {
        List<DietMemoryEntry> entries = java.util.stream.IntStream.range(0, 20)
            .mapToObj(index -> new DietMemoryEntry(Identifier.fromNamespaceAndPath("test", "food_" + index),
                index, MealQuality.BASIC, List.of(), index + 1L)).toList();
        NutritionState state = new NutritionState(3, Map.of(), DietQuality.STABLE, DietQuality.STABLE,
            0, 0, entries).reconcile(List.of(), 16);

        assertEquals(16, state.dietMemory().size());
        assertEquals(Identifier.parse("test:food_4"), state.dietMemory().getFirst().foodId());
        assertTrue(state.reset(List.of()).dietMemory().isEmpty());
    }

    private static NutrientGroup group(String id, double defaultValue) {
        Identifier location = Identifier.parse(id);
        return new NutrientGroup(location, "test", Identifier.parse("minecraft:apple"),
            Identifier.parse("innutrient:foods/test"), 0xFFFFFF, 0, defaultValue,
            40, 80, 20, 90, 1, 1, true, false, true);
    }
}
