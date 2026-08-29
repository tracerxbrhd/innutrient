package master.innutrient.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class InnutrientClientConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec.BooleanValue SHOW_TOOLTIPS = BUILDER
        .define("tooltips.showFoodNutrition", true);
    public static final ModConfigSpec.BooleanValue ADVANCED_ON_SHIFT = BUILDER
        .define("tooltips.advancedDetailsRequireShift", true);
    public static final ModConfigSpec.BooleanValue SHOW_PERCENTAGES = BUILDER
        .define("screen.showPercentages", true);
    public static final ModConfigSpec SPEC = BUILDER.build();

    private InnutrientClientConfig() {}
}
