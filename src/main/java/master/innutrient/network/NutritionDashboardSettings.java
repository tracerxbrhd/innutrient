package master.innutrient.network;

import master.innutrient.config.InnutrientServerConfig;
import master.innutrient.nutrition.DietQuality;
import master.innutrient.nutrition.MealQuality;
import master.innutrient.nutrition.MealQualityEngine;
import master.innutrient.nutrition.NutritionConsequences;

import java.util.ArrayList;
import java.util.List;

/** Bounded server configuration snapshot used only to explain active Innutrient mechanics in the UI. */
public record NutritionDashboardSettings(
    List<DietModifier> dietModifiers,
    List<Double> mealEfficiencyBonuses,
    long varietyWindowTicks,
    int varietySampleTarget
) {
    public static final NutritionDashboardSettings DEFAULT = new NutritionDashboardSettings(
        java.util.Arrays.stream(DietQuality.values()).map(ignored -> DietModifier.NONE).toList(),
        java.util.Arrays.stream(MealQuality.values()).map(ignored -> 0.0).toList(),
        48_000L, 8);

    public NutritionDashboardSettings {
        List<DietModifier> sanitizedModifiers = new ArrayList<>(DietQuality.values().length);
        for (int index = 0; index < DietQuality.values().length; index++) {
            DietModifier modifier = dietModifiers != null && index < dietModifiers.size()
                ? dietModifiers.get(index) : null;
            sanitizedModifiers.add(modifier == null ? DietModifier.NONE : modifier);
        }
        dietModifiers = List.copyOf(sanitizedModifiers);

        List<Double> sanitizedMealBonuses = new ArrayList<>(MealQuality.values().length);
        for (int index = 0; index < MealQuality.values().length; index++) {
            Double value = mealEfficiencyBonuses != null && index < mealEfficiencyBonuses.size()
                ? mealEfficiencyBonuses.get(index) : null;
            sanitizedMealBonuses.add(finite(value) ? Math.max(0, Math.min(2, value)) : 0);
        }
        mealEfficiencyBonuses = List.copyOf(sanitizedMealBonuses);
        varietyWindowTicks = Math.max(1, varietyWindowTicks);
        varietySampleTarget = Math.max(1, Math.min(64, varietySampleTarget));
    }

    public DietModifier modifier(DietQuality quality) {
        DietQuality resolved = quality == null ? DietQuality.STABLE : quality;
        return dietModifiers.get(resolved.ordinal());
    }

    public double mealEfficiencyBonus(MealQuality quality) {
        MealQuality resolved = quality == null ? MealQuality.BASIC : quality;
        return mealEfficiencyBonuses.get(resolved.ordinal());
    }

    public static NutritionDashboardSettings fromServerConfig() {
        List<DietModifier> modifiers = java.util.Arrays.stream(DietQuality.values())
            .map(quality -> new DietModifier(
                NutritionConsequences.exhaustionMultiplier(quality),
                NutritionConsequences.absorptionMultiplier(quality),
                NutritionConsequences.regenerationMultiplier(quality)))
            .toList();
        List<Double> mealBonuses = java.util.Arrays.stream(MealQuality.values())
            .map(quality -> InnutrientServerConfig.MEAL_ENABLED.get()
                ? MealQualityEngine.multiplier(quality,
                    InnutrientServerConfig.MEAL_MIXED_BONUS.get(),
                    InnutrientServerConfig.MEAL_COMPLETE_BONUS.get(),
                    InnutrientServerConfig.MEAL_DIVERSE_BONUS.get(),
                    InnutrientServerConfig.MEAL_MAXIMUM_BONUS.get()) - 1.0
                : 0.0)
            .toList();
        return new NutritionDashboardSettings(modifiers, mealBonuses,
            InnutrientServerConfig.VARIETY_SCORE_WINDOW_TICKS.get(),
            Math.min(8, InnutrientServerConfig.VARIETY_MEMORY_CAPACITY.get()));
    }

    private static boolean finite(Double value) {
        return value != null && Double.isFinite(value);
    }

    public record DietModifier(double exhaustion, double nutritionEfficiency, double naturalRegeneration) {
        public static final DietModifier NONE = new DietModifier(1, 1, 1);

        public DietModifier {
            exhaustion = sanitizeMultiplier(exhaustion);
            nutritionEfficiency = sanitizeMultiplier(nutritionEfficiency);
            naturalRegeneration = sanitizeMultiplier(naturalRegeneration);
        }

        private static double sanitizeMultiplier(double value) {
            return Double.isFinite(value) ? Math.max(0, Math.min(10, value)) : 1;
        }
    }
}
