package master.innutrient.nutrition;

import net.minecraft.resources.Identifier;

import java.util.Objects;

/** Immutable, datapack-defined nutrient metadata. */
public record NutrientGroup(
    Identifier id,
    String translationKey,
    Identifier icon,
    Identifier itemTag,
    int color,
    int order,
    double defaultLevel,
    double healthyMin,
    double healthyMax,
    double lowThreshold,
    double highThreshold,
    double gainMultiplier,
    double decayMultiplier,
    boolean penalizeLow,
    boolean penalizeHigh,
    boolean requiredForBalance
) {
    public NutrientGroup {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(translationKey, "translationKey");
        Objects.requireNonNull(icon, "icon");
        Objects.requireNonNull(itemTag, "itemTag");
        finiteRange(defaultLevel, "defaultLevel");
        finiteRange(healthyMin, "healthyMin");
        finiteRange(healthyMax, "healthyMax");
        finiteRange(lowThreshold, "lowThreshold");
        finiteRange(highThreshold, "highThreshold");
        if (healthyMin > healthyMax) throw new IllegalArgumentException("healthyMin exceeds healthyMax");
        if (lowThreshold > healthyMin) throw new IllegalArgumentException("lowThreshold exceeds healthyMin");
        if (highThreshold < healthyMax) throw new IllegalArgumentException("highThreshold is below healthyMax");
        if (!Double.isFinite(gainMultiplier) || gainMultiplier < 0)
            throw new IllegalArgumentException("gainMultiplier must be finite and non-negative");
        if (!Double.isFinite(decayMultiplier) || decayMultiplier < 0)
            throw new IllegalArgumentException("decayMultiplier must be finite and non-negative");
        color &= 0xFFFFFF;
    }

    public NutrientStatus status(double value) {
        double level = clamp(value);
        if (level <= lowThreshold && level < healthyMin) return NutrientStatus.DEFICIENT;
        if (level < healthyMin) return NutrientStatus.BELOW_TARGET;
        if (level <= healthyMax) return NutrientStatus.HEALTHY;
        if (level >= highThreshold) return NutrientStatus.EXCESSIVE;
        return NutrientStatus.ABOVE_TARGET;
    }

    /** 0..1 target-range closeness used by the geometric balance score. */
    public double balanceCloseness(double value) {
        double level = clamp(value);
        if (level >= healthyMin && level <= healthyMax) return 1.0;
        if (level < healthyMin) {
            if (!penalizeLow || healthyMin <= 0) return 1.0;
            return level / healthyMin;
        }
        if (!penalizeHigh || healthyMax >= 100) return 1.0;
        return (100.0 - level) / (100.0 - healthyMax);
    }

    public static double clamp(double value) {
        if (!Double.isFinite(value)) return 0;
        return Math.max(0, Math.min(100, value));
    }

    private static void finiteRange(double value, String name) {
        if (!Double.isFinite(value) || value < 0 || value > 100)
            throw new IllegalArgumentException(name + " must be within 0..100");
    }
}
