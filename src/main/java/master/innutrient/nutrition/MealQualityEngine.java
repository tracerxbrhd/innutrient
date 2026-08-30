package master.innutrient.nutrition;

import master.innutrient.config.InnutrientServerConfig;

/** Pure meal-quality calculation shared by server gains and the synchronized tooltip catalog. */
public final class MealQualityEngine {
    private MealQualityEngine() {}

    public static MealQuality classify(NutritionProfile profile) {
        return classify(profile, InnutrientServerConfig.MEAL_MINIMUM_GROUP_SHARE.get());
    }

    public static MealQuality classify(NutritionProfile profile, double minimumShare) {
        if (profile == null || !profile.resolved()) return MealQuality.BASIC;
        double threshold = Math.max(0, Math.min(1, minimumShare));
        int groups = (int) profile.nutrients().values().stream().filter(value -> value >= threshold).count();
        return MealQuality.fromGroupCount(groups);
    }

    public static double multiplier(NutritionProfile profile) {
        if (!InnutrientServerConfig.MEAL_ENABLED.get()) return 1.0;
        return multiplier(classify(profile), InnutrientServerConfig.MEAL_MIXED_BONUS.get(),
            InnutrientServerConfig.MEAL_COMPLETE_BONUS.get(), InnutrientServerConfig.MEAL_DIVERSE_BONUS.get(),
            InnutrientServerConfig.MEAL_MAXIMUM_BONUS.get());
    }

    public static double multiplier(MealQuality quality, double mixedBonus, double completeBonus,
                                    double diverseBonus, double maximumBonus) {
        double bonus = switch (quality) {
            case BASIC -> 0;
            case MIXED -> mixedBonus;
            case COMPLETE -> completeBonus;
            case DIVERSE -> diverseBonus;
        };
        return 1.0 + Math.max(0, Math.min(maximumBonus, bonus));
    }
}
