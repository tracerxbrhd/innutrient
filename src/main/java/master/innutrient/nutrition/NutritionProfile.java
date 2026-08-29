package master.innutrient.nutrition;

import net.minecraft.resources.Identifier;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Normalized, immutable nutrient composition for an item. */
public record NutritionProfile(
    Map<Identifier, Double> nutrients,
    NutritionProfileSource source,
    Identifier recipeId,
    int resolutionDepth
) {
    public NutritionProfile {
        Objects.requireNonNull(nutrients, "nutrients");
        Objects.requireNonNull(source, "source");
        nutrients = immutableSorted(nutrients);
        resolutionDepth = Math.max(0, resolutionDepth);
    }

    public static NutritionProfile of(Map<Identifier, Double> values, NutritionProfileSource source) {
        return new NutritionProfile(normalize(values), source, null, 0);
    }

    public static NutritionProfile recipe(Map<Identifier, Double> values, Identifier recipeId, int depth) {
        return new NutritionProfile(normalize(values), NutritionProfileSource.RECIPE_DERIVED, recipeId, depth);
    }

    public static NutritionProfile unknown() {
        return new NutritionProfile(Map.of(), NutritionProfileSource.UNKNOWN, null, 0);
    }

    public boolean resolved() {
        return !nutrients.isEmpty();
    }

    public static Map<Identifier, Double> normalize(Map<Identifier, Double> values) {
        double sum = 0;
        for (Map.Entry<Identifier, Double> entry : values.entrySet()) {
            Double value = entry.getValue();
            if (entry.getKey() != null && value != null && Double.isFinite(value) && value > 0) sum += value;
        }
        if (!Double.isFinite(sum) || sum <= 0) return Map.of();
        Map<Identifier, Double> normalized = new LinkedHashMap<>();
        final double divisor = sum;
        values.entrySet().stream().filter(entry -> entry.getKey() != null && entry.getValue() != null
                && Double.isFinite(entry.getValue()) && entry.getValue() > 0)
            .sorted(Map.Entry.comparingByKey(Comparator.comparing(Identifier::toString)))
            .forEach(entry -> normalized.put(entry.getKey(), entry.getValue() / divisor));
        return Collections.unmodifiableMap(normalized);
    }

    /** Evenly combines resolved ingredient or recipe compositions, then normalizes the result. */
    public static Map<Identifier, Double> averageNutrients(
        Collection<? extends Map<Identifier, Double>> profiles
    ) {
        Map<Identifier, Double> combined = new LinkedHashMap<>();
        int resolved = 0;
        for (Map<Identifier, Double> profile : profiles) {
            if (profile == null || profile.isEmpty()) continue;
            resolved++;
            profile.forEach((id, value) -> {
                if (id != null && value != null && Double.isFinite(value) && value > 0)
                    combined.merge(id, value, Double::sum);
            });
        }
        if (resolved == 0) return Map.of();
        return normalize(combined);
    }

    private static Map<Identifier, Double> immutableSorted(Map<Identifier, Double> values) {
        Map<Identifier, Double> result = new LinkedHashMap<>();
        values.entrySet().stream().sorted(Map.Entry.comparingByKey(Comparator.comparing(Identifier::toString)))
            .forEach(entry -> result.put(entry.getKey(), entry.getValue()));
        return Collections.unmodifiableMap(result);
    }
}
