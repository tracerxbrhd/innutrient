package master.innutrient.nutrition;

import master.innutrient.config.InnutrientServerConfig;
import net.minecraft.world.food.FoodProperties;

/** Centralized gain formula. FoodProperties.saturation() is already an absolute restored amount. */
public final class NutritionGainCalculator {
    private NutritionGainCalculator() {}

    public static double totalGain(FoodProperties food) {
        if (food == null) return 0;
        double base = Math.max(0, food.nutrition()) + Math.max(0, food.saturation()) * 0.5;
        double result = base * InnutrientServerConfig.GLOBAL_GAIN.get();
        return Double.isFinite(result) ? Math.max(0, result) : 0;
    }

    public static double totalGain(FoodProperties food, NutritionProfile profile, double varietyEfficiency,
                                   DietQuality dietQuality) {
        double result = totalGain(food) * MealQualityEngine.multiplier(profile)
            * Math.max(0, Math.min(1, varietyEfficiency))
            * NutritionConsequences.absorptionMultiplier(dietQuality);
        return Double.isFinite(result) ? Math.max(0, result) : 0;
    }
}
