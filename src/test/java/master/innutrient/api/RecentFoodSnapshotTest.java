package master.innutrient.api;

import master.innutrient.nutrition.DietQuality;
import master.innutrient.nutrition.MealQuality;
import master.innutrient.nutrition.VarietyTier;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RecentFoodSnapshotTest {
    @Test
    void publicSnapshotsDoNotExposeMutableCollections() {
        List<ResourceLocation> groups = new ArrayList<>();
        groups.add(ResourceLocation.parse("innutrient:fruits"));
        RecentFoodSnapshot food = new RecentFoodSnapshot(ResourceLocation.parse("minecraft:apple"), 100,
            MealQuality.BASIC, groups);
        List<RecentFoodSnapshot> foods = new ArrayList<>(List.of(food));
        PlayerNutritionSnapshot player = new PlayerNutritionSnapshot(Map.of(), 80, DietQuality.BALANCED,
            40, 55, VarietyTier.VARIED, foods);

        groups.clear();
        foods.clear();
        assertEquals(1, food.nutrientGroups().size());
        assertEquals(1, player.recentFoods().size());
        assertThrows(UnsupportedOperationException.class,
            () -> player.recentFoods().add(food));
    }
}
