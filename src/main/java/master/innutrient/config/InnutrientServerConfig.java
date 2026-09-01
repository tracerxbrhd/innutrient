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

    public static final ModConfigSpec.DoubleValue DIET_SEVERE_ENTER = BUILDER.comment(
        "Balance score at or below which Severe becomes a candidate state.")
        .defineInRange("dietQuality.severeActivateBelow", 30.0, 0, 100);
    public static final ModConfigSpec.DoubleValue DIET_SEVERE_EXIT = BUILDER
        .defineInRange("dietQuality.severeDeactivateAbove", 35.0, 0, 100);
    public static final ModConfigSpec.DoubleValue DIET_POOR_ENTER = BUILDER
        .defineInRange("dietQuality.poorActivateBelow", 55.0, 0, 100);
    public static final ModConfigSpec.DoubleValue DIET_POOR_EXIT = BUILDER
        .defineInRange("dietQuality.poorDeactivateAbove", 60.0, 0, 100);
    public static final ModConfigSpec.DoubleValue DIET_BALANCED_ENTER = BUILDER.comment(
        "Balanced activates at this score and deactivates only below its separate exit threshold.")
        .defineInRange("dietQuality.balancedActivateAbove", 80.0, 0, 100);
    public static final ModConfigSpec.DoubleValue DIET_BALANCED_EXIT = BUILDER
        .defineInRange("dietQuality.balancedDeactivateBelow", 75.0, 0, 100);
    public static final ModConfigSpec.DoubleValue DIET_OPTIMAL_ENTER = BUILDER
        .defineInRange("dietQuality.optimalActivateAbove", 93.0, 0, 100);
    public static final ModConfigSpec.DoubleValue DIET_OPTIMAL_EXIT = BUILDER
        .defineInRange("dietQuality.optimalDeactivateBelow", 88.0, 0, 100);
    public static final ModConfigSpec.IntValue DIET_TRANSITION_TICKS = BUILDER.comment(
        "Sustained time required for Severe, Poor, or Stable transitions.")
        .defineInRange("dietQuality.transitionTicks", 1200, 0, 72000);
    public static final ModConfigSpec.IntValue DIET_BALANCED_TICKS = BUILDER
        .defineInRange("dietQuality.balancedSustainTicks", 6000, 0, 720000);
    public static final ModConfigSpec.IntValue DIET_OPTIMAL_TICKS = BUILDER
        .defineInRange("dietQuality.optimalSustainTicks", 12000, 0, 720000);

    public static final ModConfigSpec.BooleanValue VARIETY_ENABLED = BUILDER
        .define("variety.enabled", true);
    public static final ModConfigSpec.IntValue VARIETY_MEMORY_CAPACITY = BUILDER.comment(
        "Maximum number of recent foods retained in bounded Diet Memory.")
        .defineInRange("variety.memoryCapacity", 16, 4, 32);
    public static final ModConfigSpec.IntValue VARIETY_SCORE_WINDOW_TICKS = BUILDER.comment(
        "Only Diet Memory entries this recent contribute to the 0..100 Food Variety Score.")
        .defineInRange("variety.scoreWindowTicks", 48000, 1200, 2400000);
    public static final ModConfigSpec.DoubleValue VARIETY_REPEAT_PENALTY = BUILDER.comment(
        "Nutrition efficiency lost for each consecutive repeat of the same food.")
        .defineInRange("variety.penaltyPerRepeat", 0.10, 0, 1);
    public static final ModConfigSpec.DoubleValue VARIETY_MINIMUM_EFFICIENCY = BUILDER
        .defineInRange("variety.minimumEfficiency", 0.60, 0, 1);
    public static final ModConfigSpec.IntValue VARIETY_RECOVERY_TICKS = BUILDER.comment(
        "A repeated-food streak expires after this many ticks without eating that food. Eating another food resets it immediately.")
        .defineInRange("variety.recoveryTicks", 12000, 0, 720000);

    public static final ModConfigSpec.BooleanValue MEAL_ENABLED = BUILDER
        .define("mealQuality.enabled", true);
    public static final ModConfigSpec.DoubleValue MEAL_MINIMUM_GROUP_SHARE = BUILDER.comment(
        "Minimum normalized profile share for a nutrient group to count toward meal quality.")
        .defineInRange("mealQuality.minimumGroupShare", 0.08, 0, 1);
    public static final ModConfigSpec.DoubleValue MEAL_MIXED_BONUS = BUILDER
        .defineInRange("mealQuality.mixedEfficiencyBonus", 0.08, 0, 1);
    public static final ModConfigSpec.DoubleValue MEAL_COMPLETE_BONUS = BUILDER
        .defineInRange("mealQuality.completeEfficiencyBonus", 0.15, 0, 1);
    public static final ModConfigSpec.DoubleValue MEAL_DIVERSE_BONUS = BUILDER
        .defineInRange("mealQuality.diverseEfficiencyBonus", 0.22, 0, 1);
    public static final ModConfigSpec.DoubleValue MEAL_MAXIMUM_BONUS = BUILDER
        .defineInRange("mealQuality.maximumEfficiencyBonus", 0.25, 0, 2);

    public static final ModConfigSpec.BooleanValue ENABLE_BONUSES = BUILDER
        .define("effects.enableBonuses", true);
    public static final ModConfigSpec.BooleanValue ENABLE_PENALTIES = BUILDER
        .define("effects.enablePenalties", true);
    public static final ModConfigSpec.DoubleValue BALANCED_EXHAUSTION = BUILDER
        .defineInRange("consequences.balancedExhaustionMultiplier", 0.90, 0, 10);
    public static final ModConfigSpec.DoubleValue OPTIMAL_EXHAUSTION = BUILDER
        .defineInRange("consequences.optimalExhaustionMultiplier", 0.80, 0, 10);
    public static final ModConfigSpec.DoubleValue POOR_EXHAUSTION = BUILDER
        .defineInRange("consequences.poorExhaustionMultiplier", 1.15, 0, 10);
    public static final ModConfigSpec.DoubleValue SEVERE_EXHAUSTION = BUILDER
        .defineInRange("consequences.severeExhaustionMultiplier", 1.30, 0, 10);
    public static final ModConfigSpec.DoubleValue BALANCED_REGENERATION = BUILDER
        .defineInRange("consequences.balancedRegenerationMultiplier", 1.05, 0, 10);
    public static final ModConfigSpec.DoubleValue OPTIMAL_REGENERATION = BUILDER
        .defineInRange("consequences.optimalRegenerationMultiplier", 1.10, 0, 10);
    public static final ModConfigSpec.DoubleValue POOR_REGENERATION = BUILDER
        .defineInRange("consequences.poorRegenerationMultiplier", 0.75, 0, 10);
    public static final ModConfigSpec.DoubleValue SEVERE_REGENERATION = BUILDER
        .defineInRange("consequences.severeRegenerationMultiplier", 0.50, 0, 10);
    public static final ModConfigSpec.DoubleValue BALANCED_ABSORPTION = BUILDER
        .defineInRange("consequences.balancedNutritionEfficiency", 1.08, 0, 10);
    public static final ModConfigSpec.DoubleValue OPTIMAL_ABSORPTION = BUILDER
        .defineInRange("consequences.optimalNutritionEfficiency", 1.15, 0, 10);
    public static final ModConfigSpec.DoubleValue POOR_ABSORPTION = BUILDER
        .defineInRange("consequences.poorNutritionEfficiency", 0.92, 0, 10);
    public static final ModConfigSpec.DoubleValue SEVERE_ABSORPTION = BUILDER
        .defineInRange("consequences.severeNutritionEfficiency", 0.85, 0, 10);

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
