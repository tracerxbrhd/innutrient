package master.innutrient.client.gui;

import master.innutrient.network.NutritionDashboardSettings;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NutritionModifiersCardTest {
    @Test
    void neutralStateSuppressesZeroValueRows() {
        assertEquals(0, NutritionModifiersCard.meaningfulModifierCount(
            NutritionDashboardSettings.DietModifier.NONE));
    }

    @Test
    void activeStateShowsOnlyMeaningfulConfiguredValues() {
        assertEquals(2, NutritionModifiersCard.meaningfulModifierCount(
            new NutritionDashboardSettings.DietModifier(0.9, 1.1, 1.0)));
        assertEquals(0, NutritionModifiersCard.meaningfulModifierCount(
            new NutritionDashboardSettings.DietModifier(1.00001, 0.99999, 1.0)));
    }
}

