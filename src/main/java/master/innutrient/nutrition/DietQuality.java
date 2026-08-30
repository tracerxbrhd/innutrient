package master.innutrient.nutrition;

import java.util.Locale;

/** Stable, synchronized description of the player's sustained diet state. */
public enum DietQuality {
    SEVERE,
    POOR,
    STABLE,
    BALANCED,
    OPTIMAL;

    public String serializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public String translationKey() {
        return "diet_quality.innutrient." + serializedName();
    }

    public static DietQuality fromSerializedName(String value) {
        if (value == null) return STABLE;
        try {
            return valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return STABLE;
        }
    }
}
