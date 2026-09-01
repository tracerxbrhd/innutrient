package master.innutrient.nutrition;

import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Deterministic bounded Diet Memory, repetition efficiency, and explainable 0..100 variety score. */
public final class FoodVariety {
    public static final int ABSOLUTE_MEMORY_CAPACITY = 64;

    private FoodVariety() {}

    public static Result consume(List<DietMemoryEntry> previous, DietMemoryEntry consumed, int capacity,
                                 double penaltyPerRepeat, double minimumEfficiency, long recoveryTicks) {
        int boundedCapacity = Math.max(1, Math.min(ABSOLUTE_MEMORY_CAPACITY, capacity));
        List<DietMemoryEntry> history = sanitize(previous, boundedCapacity);
        if (!history.isEmpty() && consumed.gameTime() < history.getLast().gameTime()) history = List.of();

        int repeats = 0;
        if (!history.isEmpty()) {
            DietMemoryEntry latest = history.getLast();
            boolean recovered = !latest.foodId().equals(consumed.foodId())
                || consumed.gameTime() - latest.gameTime() >= Math.max(0, recoveryTicks);
            if (!recovered) {
                for (int index = history.size() - 1; index >= 0; index--) {
                    if (!history.get(index).foodId().equals(consumed.foodId())) break;
                    repeats++;
                }
            }
        }

        double efficiency = Math.max(clamp01(minimumEfficiency),
            1.0 - repeats * clamp01(penaltyPerRepeat));
        List<DietMemoryEntry> changed = new ArrayList<>(history.size() + 1);
        changed.addAll(history);
        changed.add(consumed);
        if (changed.size() > boundedCapacity)
            changed = new ArrayList<>(changed.subList(changed.size() - boundedCapacity, changed.size()));
        return new Result(efficiency, List.copyOf(changed), repeats);
    }

    public static Score score(List<DietMemoryEntry> memory, long gameTime, long freshnessWindowTicks,
                              Set<Identifier> requiredGroups, int sampleTarget) {
        long window = Math.max(1, freshnessWindowTicks);
        List<DietMemoryEntry> recent = memory == null ? List.of() : memory.stream()
            .filter(entry -> entry != null && (gameTime < entry.gameTime()
                || gameTime - entry.gameTime() <= window))
            .toList();
        int count = recent.size();
        if (count == 0) return new Score(0, VarietyTier.REPETITIVE, 0, 0, 0, 0);

        Map<Identifier, Integer> foodFrequency = new HashMap<>();
        Set<Long> compositions = new HashSet<>();
        Set<Identifier> observedGroups = new HashSet<>();
        double mealQualityTotal = 0;
        for (DietMemoryEntry entry : recent) {
            foodFrequency.merge(entry.foodId(), 1, Integer::sum);
            compositions.add(entry.compositionFingerprint());
            observedGroups.addAll(entry.nutrientGroups());
            mealQualityTotal += mealQualityValue(entry.mealQuality());
        }

        double distinctFoodRatio = foodFrequency.size() / (double) count;
        int dominantCount = foodFrequency.values().stream().mapToInt(Integer::intValue).max().orElse(count);
        double frequencyBalance = count <= 1 ? 0
            : (1.0 - dominantCount / (double) count) / (1.0 - 1.0 / count);
        double foodDiversity = 0.60 * distinctFoodRatio + 0.40 * clamp01(frequencyBalance);
        double compositionDiversity = compositions.size() / (double) count;

        Set<Identifier> targets = requiredGroups == null ? Set.of() : requiredGroups;
        int coveredGroups;
        double groupCoverage;
        if (targets.isEmpty()) {
            coveredGroups = observedGroups.size();
            groupCoverage = Math.min(1.0, coveredGroups / 4.0);
        } else {
            coveredGroups = (int) targets.stream().filter(observedGroups::contains).count();
            groupCoverage = coveredGroups / (double) targets.size();
        }

        double mealQuality = mealQualityTotal / count;
        double readiness = Math.min(1.0, count / (double) Math.max(1, sampleTarget));
        double value = 100.0 * readiness * (0.50 * foodDiversity + 0.25 * compositionDiversity
            + 0.20 * groupCoverage + 0.05 * mealQuality);
        value = Math.max(0, Math.min(100, value));
        return new Score(value, VarietyTier.fromScore(value), count, foodFrequency.size(), compositions.size(),
            coveredGroups);
    }

    private static List<DietMemoryEntry> sanitize(List<DietMemoryEntry> history, int capacity) {
        if (history == null || history.isEmpty()) return List.of();
        List<DietMemoryEntry> valid = history.stream().filter(java.util.Objects::nonNull).toList();
        return valid.size() <= capacity ? List.copyOf(valid)
            : List.copyOf(valid.subList(valid.size() - capacity, valid.size()));
    }

    private static double mealQualityValue(MealQuality quality) {
        return switch (quality == null ? MealQuality.BASIC : quality) {
            case BASIC -> 0.25;
            case MIXED -> 0.50;
            case COMPLETE -> 0.80;
            case DIVERSE -> 1.00;
        };
    }

    private static double clamp01(double value) {
        if (!Double.isFinite(value)) return 0;
        return Math.max(0, Math.min(1, value));
    }

    public record Result(double efficiency, List<DietMemoryEntry> memory, int repeatCount) {
        public Result {
            efficiency = clamp01(efficiency);
            memory = List.copyOf(memory);
            repeatCount = Math.max(0, repeatCount);
        }
    }

    public record Score(double value, VarietyTier tier, int consideredMeals, int distinctFoods,
                        int distinctCompositions, int coveredGroups) {}
}
