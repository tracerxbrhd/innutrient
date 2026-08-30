package master.innutrient.registry;

import net.minecraft.resources.Identifier;

public record NutritionEffectRule(
    Identifier id,
    NutritionCondition condition,
    Identifier effect,
    int durationTicks,
    int amplifier,
    boolean beneficial,
    boolean ambient,
    boolean showParticles
) {}
