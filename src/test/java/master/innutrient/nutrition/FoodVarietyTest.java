package master.innutrient.nutrition;

import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FoodVarietyTest {
    private static final Identifier APPLE = Identifier.parse("minecraft:apple");
    private static final Identifier BREAD = Identifier.parse("minecraft:bread");

    @Test
    void repeatedFoodDeclinesToConfiguredFloor() {
        var first = FoodVariety.consume(null, 0, 0, APPLE, 100, 0.10, 0.60, 12000);
        var second = FoodVariety.consume(first.food(), first.repeatCount(), first.gameTime(), APPLE,
            200, 0.10, 0.60, 12000);
        var sixth = FoodVariety.consume(APPLE, 4, 500, APPLE, 600, 0.10, 0.60, 12000);

        assertEquals(1.0, first.efficiency(), 1.0e-9);
        assertEquals(0.9, second.efficiency(), 1.0e-9);
        assertEquals(0.6, sixth.efficiency(), 1.0e-9);
    }

    @Test
    void differentFoodOrElapsedRecoveryResetsTheStreak() {
        assertEquals(1.0, FoodVariety.consume(APPLE, 5, 100, BREAD, 200,
            0.10, 0.60, 12000).efficiency(), 1.0e-9);
        assertEquals(1.0, FoodVariety.consume(APPLE, 5, 100, APPLE, 12100,
            0.10, 0.60, 12000).efficiency(), 1.0e-9);
    }
}
