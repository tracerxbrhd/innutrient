package master.innutrient.registry;

import net.minecraft.resources.ResourceLocation;

public record NutritionEffectRule(
    ResourceLocation id,
    NutritionCondition condition,
    ResourceLocation effect,
    int durationTicks,
    int amplifier,
    boolean beneficial,
    boolean ambient,
    boolean showParticles
) {}
