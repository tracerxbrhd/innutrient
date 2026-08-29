package master.innutrient.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class InnutrientServerConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.DoubleValue GLOBAL_GAIN = BUILDER.comment(
        "Multiplier applied to (food nutrition + half of absolute saturation restored).")
        .defineInRange("gain.globalMultiplier", 1.5, 0, 100);
    public static final ModConfigSpec.DoubleValue DECAY_PER_HUNGER = BUILDER.comment(
        "Nutrient points removed from every group per hunger point lost.")
        .defineInRange("decay.perHungerPoint", 0.35, 0, 100);
    public static final ModConfigSpec.ConfigValue<String> DECAY_MODE = BUILDER.comment(
        "HUNGER, PERIODIC, HYBRID, or NONE.")
        .define("decay.mode", "HUNGER", InnutrientServerConfig::validDecayMode);
    public static final ModConfigSpec.IntValue PERIODIC_INTERVAL = BUILDER
        .defineInRange("decay.periodicIntervalTicks", 1200, 20, 72000);
    public static final ModConfigSpec.DoubleValue PERIODIC_AMOUNT = BUILDER
        .defineInRange("decay.periodicAmount", 0.15, 0, 100);
    public static final ModConfigSpec.DoubleValue DEATH_RETENTION = BUILDER.comment(
        "Percentage of current nutrient values kept after death. Dimension clones always keep 100%.")
        .defineInRange("player.deathRetentionPercent", 100.0, 0, 100);
    public static final ModConfigSpec.BooleanValue AUTO_RECIPES = BUILDER
        .define("resolution.enableRecipeInheritance", true);
    public static final ModConfigSpec.IntValue MAX_RECIPE_DEPTH = BUILDER
        .defineInRange("resolution.maxDepth", 8, 1, 64);
    public static final ModConfigSpec.IntValue MAX_INGREDIENT_ALTERNATIVES = BUILDER
        .defineInRange("resolution.maxIngredientAlternatives", 64, 1, 1024);
    public static final ModConfigSpec.IntValue MAX_RECIPES_PER_OUTPUT = BUILDER
        .defineInRange("resolution.maxRecipesPerOutput", 16, 1, 256);
    public static final ModConfigSpec.BooleanValue ENABLE_BONUSES = BUILDER
        .define("effects.enableBonuses", true);
    public static final ModConfigSpec.BooleanValue ENABLE_PENALTIES = BUILDER
        .define("effects.enablePenalties", true);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private InnutrientServerConfig() {}

    public static DecayMode decayMode() {
        return DecayMode.valueOf(DECAY_MODE.get().toUpperCase(java.util.Locale.ROOT));
    }

    private static boolean validDecayMode(Object value) {
        if (!(value instanceof String string)) return false;
        try {
            DecayMode.valueOf(string.toUpperCase(java.util.Locale.ROOT));
            return true;
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }

    public enum DecayMode { HUNGER, PERIODIC, HYBRID, NONE }
}
