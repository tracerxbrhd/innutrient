package master.innutrient.nutrition;

import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NutritionProfileTest {
    private static final Identifier PROTEIN = Identifier.parse("innutrient:proteins");
    private static final Identifier GRAIN = Identifier.parse("innutrient:grains");

    @Test
    void normalizesPositiveWeights() {
        NutritionProfile profile = NutritionProfile.of(Map.of(PROTEIN, 2.0, GRAIN, 1.0),
            NutritionProfileSource.EXPLICIT);
        assertEquals(2.0 / 3.0, profile.nutrients().get(PROTEIN), 1.0e-9);
        assertEquals(1.0 / 3.0, profile.nutrients().get(GRAIN), 1.0e-9);
    }

    @Test
    void rejectsInvalidContributionsWithoutProducingNan() {
        Map<Identifier, Double> input = new LinkedHashMap<>();
        input.put(PROTEIN, Double.NaN);
        input.put(GRAIN, -1.0);
        assertTrue(NutritionProfile.normalize(input).isEmpty());
    }

    @Test
    void recipeIngredientCompositionIsOrderIndependent() {
        Map<Identifier, Double> first = new LinkedHashMap<>();
        first.merge(PROTEIN, 1.0, Double::sum);
        first.merge(GRAIN, 1.0, Double::sum);
        Map<Identifier, Double> second = new LinkedHashMap<>();
        second.merge(GRAIN, 1.0, Double::sum);
        second.merge(PROTEIN, 1.0, Double::sum);
        assertEquals(NutritionProfile.normalize(first), NutritionProfile.normalize(second));
    }

    @Test
    void recipeIngredientsAndAlternativesCombineEvenly() {
        Identifier vegetables = Identifier.parse("innutrient:vegetables");
        Map<Identifier, Double> result = NutritionProfile.averageNutrients(List.of(
            Map.of(GRAIN, 1.0),
            Map.of(PROTEIN, 0.5, vegetables, 0.5),
            Map.of()
        ));

        assertEquals(0.5, result.get(GRAIN), 1.0e-9);
        assertEquals(0.25, result.get(PROTEIN), 1.0e-9);
        assertEquals(0.25, result.get(vegetables), 1.0e-9);
        assertEquals(List.of(GRAIN, PROTEIN, vegetables), result.keySet().stream().toList());
    }
}
