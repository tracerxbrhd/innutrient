package master.innutrient.nutrition;

import master.innutrient.config.InnutrientServerConfig;

/** Configured survival-loop multipliers selected from the player's sustained Diet Quality. */
public final class NutritionConsequences {
    private NutritionConsequences() {}

    public static double exhaustionMultiplier(DietQuality quality) {
        if (quality == null) return 1.0;
        return switch (quality) {
            case SEVERE -> penalty(InnutrientServerConfig.SEVERE_EXHAUSTION.get());
            case POOR -> penalty(InnutrientServerConfig.POOR_EXHAUSTION.get());
            case STABLE -> 1.0;
            case BALANCED -> bonus(InnutrientServerConfig.BALANCED_EXHAUSTION.get());
            case OPTIMAL -> bonus(InnutrientServerConfig.OPTIMAL_EXHAUSTION.get());
        };
    }

    public static double regenerationMultiplier(DietQuality quality) {
        if (quality == null) return 1.0;
        return switch (quality) {
            case SEVERE -> penalty(InnutrientServerConfig.SEVERE_REGENERATION.get());
            case POOR -> penalty(InnutrientServerConfig.POOR_REGENERATION.get());
            case STABLE -> 1.0;
            case BALANCED -> bonus(InnutrientServerConfig.BALANCED_REGENERATION.get());
            case OPTIMAL -> bonus(InnutrientServerConfig.OPTIMAL_REGENERATION.get());
        };
    }

    public static double absorptionMultiplier(DietQuality quality) {
        if (quality == null) return 1.0;
        return switch (quality) {
            case SEVERE -> penalty(InnutrientServerConfig.SEVERE_ABSORPTION.get());
            case POOR -> penalty(InnutrientServerConfig.POOR_ABSORPTION.get());
            case STABLE -> 1.0;
            case BALANCED -> bonus(InnutrientServerConfig.BALANCED_ABSORPTION.get());
            case OPTIMAL -> bonus(InnutrientServerConfig.OPTIMAL_ABSORPTION.get());
        };
    }

    private static double bonus(double configured) {
        return InnutrientServerConfig.ENABLE_BONUSES.get() ? configured : 1.0;
    }

    private static double penalty(double configured) {
        return InnutrientServerConfig.ENABLE_PENALTIES.get() ? configured : 1.0;
    }
}
