package master.innutrient.api;

import master.innutrient.nutrition.DietQuality;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

/** Immutable public view of authoritative player nutrition. */
public record PlayerNutritionSnapshot(Map<ResourceLocation, Double> levels, double balanceScore,
                                      DietQuality dietQuality, long dietQualitySince) {
    public PlayerNutritionSnapshot {
        levels = Map.copyOf(levels);
        balanceScore = Math.max(0, Math.min(100, balanceScore));
    }
}
