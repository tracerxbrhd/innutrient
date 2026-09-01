package master.innutrient.nutrition;

import java.util.Locale;

/** Player-facing tier derived from the authoritative 0..100 Food Variety Score. */
public enum VarietyTier {
    REPETITIVE,
    LIMITED,
    VARIED,
    DIVERSE,
    HIGHLY_DIVERSE;

    public static VarietyTier fromScore(double score) {
        double clamped = Double.isFinite(score) ? Math.max(0, Math.min(100, score)) : 0;
        if (clamped >= 80) return HIGHLY_DIVERSE;
        if (clamped >= 60) return DIVERSE;
        if (clamped >= 40) return VARIED;
        if (clamped >= 20) return LIMITED;
        return REPETITIVE;
    }

    public String serializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public String translationKey() {
        return "variety_tier.innutrient." + serializedName();
    }
}
