package master.innutrient.network;

import master.innutrient.nutrition.DietQuality;
import master.innutrient.nutrition.MealQuality;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NutritionDashboardSettingsTest {
    @Test
    void padsAndSanitizesBoundedEnumSnapshots() {
        NutritionDashboardSettings settings = new NutritionDashboardSettings(
            List.of(new NutritionDashboardSettings.DietModifier(0.9, 1.1, 1.05)),
            List.of(Double.NaN, 0.08), -1, 999);
        assertEquals(DietQuality.values().length, settings.dietModifiers().size());
        assertEquals(MealQuality.values().length, settings.mealEfficiencyBonuses().size());
        assertEquals(0.9, settings.modifier(DietQuality.SEVERE).exhaustion(), 1.0e-9);
        assertEquals(NutritionDashboardSettings.DietModifier.NONE, settings.modifier(DietQuality.OPTIMAL));
        assertEquals(0.0, settings.mealEfficiencyBonus(MealQuality.BASIC), 1.0e-9);
        assertEquals(0.08, settings.mealEfficiencyBonus(MealQuality.MIXED), 1.0e-9);
        assertEquals(1, settings.varietyWindowTicks());
        assertEquals(64, settings.varietySampleTarget());
    }
}

