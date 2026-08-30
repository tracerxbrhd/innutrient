package master.innutrient.network;

import master.innutrient.nutrition.MealQuality;
import master.innutrient.nutrition.NutritionProfile;

/** Immutable server-resolved food data used by cached client tooltips. */
public record NutritionFoodData(NutritionProfile profile, double baseGain, MealQuality mealQuality,
                                double mealMultiplier) {
    public NutritionFoodData {
        baseGain = Double.isFinite(baseGain) ? Math.max(0, baseGain) : 0;
        mealQuality = mealQuality == null ? MealQuality.BASIC : mealQuality;
        mealMultiplier = Double.isFinite(mealMultiplier) ? Math.max(1, mealMultiplier) : 1;
    }
}
