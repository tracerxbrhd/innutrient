package master.innutrient.nutrition;

import java.util.Locale;

/** Meal complexity derived from the number of meaningful nutrient groups in a profile. */
public enum MealQuality {
    BASIC(1),
    MIXED(2),
    COMPLETE(3),
    DIVERSE(4);

    private final int minimumGroups;

    MealQuality(int minimumGroups) {
        this.minimumGroups = minimumGroups;
    }

    public int minimumGroups() {
        return minimumGroups;
    }

    public String serializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public String translationKey() {
        return "meal_quality.innutrient." + serializedName();
    }

    public static MealQuality fromSerializedName(String name) {
        if (name != null) for (MealQuality quality : values())
            if (quality.serializedName().equalsIgnoreCase(name)) return quality;
        return BASIC;
    }

    public static MealQuality fromGroupCount(int groups) {
        if (groups >= DIVERSE.minimumGroups) return DIVERSE;
        if (groups >= COMPLETE.minimumGroups) return COMPLETE;
        if (groups >= MIXED.minimumGroups) return MIXED;
        return BASIC;
    }
}
