package master.innutrient.nutrition;

import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FoodVarietyTest {
    private static final Identifier APPLE = Identifier.parse("minecraft:apple");
    private static final Identifier BREAD = Identifier.parse("minecraft:bread");
    private static final Identifier FRUITS = Identifier.parse("innutrient:fruits");
    private static final Identifier GRAINS = Identifier.parse("innutrient:grains");
    private static final Identifier PROTEINS = Identifier.parse("innutrient:proteins");
    private static final Identifier VEGETABLES = Identifier.parse("innutrient:vegetables");
    private static final Set<Identifier> REQUIRED = Set.of(FRUITS, GRAINS, PROTEINS, VEGETABLES);

    @Test
    void repeatedFoodDeclinesToConfiguredFloor() {
        var first = consume(List.of(), entry(APPLE, 100, 1, FRUITS));
        var second = consume(first.memory(), entry(APPLE, 200, 1, FRUITS));
        List<DietMemoryEntry> repeated = second.memory();
        FoodVariety.Result latest = second;
        for (int index = 0; index < 4; index++) {
            latest = consume(repeated, entry(APPLE, 300 + index * 100L, 1, FRUITS));
            repeated = latest.memory();
        }

        assertEquals(1.0, first.efficiency(), 1.0e-9);
        assertEquals(0.9, second.efficiency(), 1.0e-9);
        assertEquals(0.6, latest.efficiency(), 1.0e-9);
    }

    @Test
    void differentFoodOrElapsedRecoveryResetsTheStreak() {
        List<DietMemoryEntry> repeated = List.of(entry(APPLE, 100, 1, FRUITS), entry(APPLE, 200, 1, FRUITS));
        assertEquals(1.0, consume(repeated, entry(BREAD, 300, 2, GRAINS)).efficiency(), 1.0e-9);
        assertEquals(1.0, consume(repeated, entry(APPLE, 12200, 1, FRUITS)).efficiency(), 1.0e-9);
    }

    @Test
    void memoryDropsOldestEntriesAtCapacity() {
        List<DietMemoryEntry> memory = List.of();
        for (int index = 0; index < 7; index++) {
            Identifier food = Identifier.fromNamespaceAndPath("test", "food_" + index);
            memory = FoodVariety.consume(memory, entry(food, index * 20L, index + 1, FRUITS),
                4, 0.1, 0.6, 12000).memory();
        }
        assertEquals(4, memory.size());
        assertEquals(Identifier.parse("test:food_3"), memory.getFirst().foodId());
        assertEquals(Identifier.parse("test:food_6"), memory.getLast().foodId());
    }

    @Test
    void differentCompositionsScoreHigherThanDifferentFoodsWithSameComposition() {
        List<DietMemoryEntry> sameComposition = new ArrayList<>();
        List<DietMemoryEntry> differentComposition = new ArrayList<>();
        Identifier[] groups = {FRUITS, GRAINS, PROTEINS, VEGETABLES};
        for (int index = 0; index < 8; index++) {
            Identifier food = Identifier.fromNamespaceAndPath("test", "food_" + index);
            sameComposition.add(entry(food, 100 + index, 77, FRUITS));
            differentComposition.add(entry(food, 100 + index, 100 + index, groups[index % groups.length]));
        }
        double same = FoodVariety.score(sameComposition, 200, 48000, REQUIRED, 8).value();
        double different = FoodVariety.score(differentComposition, 200, 48000, REQUIRED, 8).value();

        assertTrue(different > same + 20, "composition and group diversity should materially improve score");
        assertEquals(VarietyTier.HIGHLY_DIVERSE, VarietyTier.fromScore(different));
    }

    @Test
    void frequentRepeatScoresAsRepetitiveAndOldEntriesExpire() {
        List<DietMemoryEntry> repeated = new ArrayList<>();
        for (int index = 0; index < 8; index++) repeated.add(entry(APPLE, index * 20L, 1, FRUITS));
        var repetitive = FoodVariety.score(repeated, 200, 48000, REQUIRED, 8);
        var expired = FoodVariety.score(repeated, 100000, 48000, REQUIRED, 8);

        assertEquals(VarietyTier.REPETITIVE, repetitive.tier());
        assertEquals(0, expired.value(), 1.0e-9);
        assertEquals(0, expired.consideredMeals());
    }

    @Test
    void scoreIsDeterministicAndBuildsConfidenceAcrossRecentMeals() {
        DietMemoryEntry apple = entry(APPLE, 100, 1, FRUITS);
        double oneMeal = FoodVariety.score(List.of(apple), 100, 48000, REQUIRED, 8).value();
        double repeatedCalculation = FoodVariety.score(List.of(apple), 100, 48000, REQUIRED, 8).value();
        assertEquals(oneMeal, repeatedCalculation, 0);
        assertTrue(oneMeal < 20);
    }

    private static FoodVariety.Result consume(List<DietMemoryEntry> memory, DietMemoryEntry entry) {
        return FoodVariety.consume(memory, entry, 16, 0.10, 0.60, 12000);
    }

    private static DietMemoryEntry entry(Identifier food, long gameTime, long composition,
                                         Identifier... groups) {
        return new DietMemoryEntry(food, gameTime, MealQuality.BASIC, List.of(groups), composition);
    }
}
