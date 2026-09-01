package master.innutrient.api;

import master.innutrient.nutrition.DietQuality;
import master.innutrient.nutrition.VarietyTier;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;

/** Immutable public view of authoritative player nutrition. */
public record PlayerNutritionSnapshot(Map<ResourceLocation, Double> levels, double balanceScore,
                                      DietQuality dietQuality, long dietQualitySince,
                                      double varietyScore, VarietyTier varietyTier,
                                      List<RecentFoodSnapshot> recentFoods) {
    public PlayerNutritionSnapshot {
        levels = Map.copyOf(levels);
        balanceScore = Math.max(0, Math.min(100, balanceScore));
        varietyScore = Double.isFinite(varietyScore) ? Math.max(0, Math.min(100, varietyScore)) : 0;
        varietyTier = varietyTier == null ? VarietyTier.REPETITIVE : varietyTier;
        recentFoods = recentFoods == null ? List.of() : List.copyOf(recentFoods);
    }

    /** Compatibility constructor for integrations compiled against the Innutrient 1.0 snapshot. */
    public PlayerNutritionSnapshot(Map<ResourceLocation, Double> levels, double balanceScore,
                                   DietQuality dietQuality, long dietQualitySince) {
        this(levels, balanceScore, dietQuality, dietQualitySince, 0, VarietyTier.REPETITIVE, List.of());
    }
}
