package master.innutrient.api;

import master.innutrient.nutrition.DietMemoryEntry;
import master.innutrient.nutrition.MealQuality;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/** Immutable public display view of one bounded Diet Memory entry. */
public record RecentFoodSnapshot(ResourceLocation itemId, long gameTime, MealQuality mealQuality,
                                 List<ResourceLocation> nutrientGroups) {
    public RecentFoodSnapshot {
        if (itemId == null) throw new IllegalArgumentException("itemId cannot be null");
        gameTime = Math.max(0, gameTime);
        mealQuality = mealQuality == null ? MealQuality.BASIC : mealQuality;
        nutrientGroups = nutrientGroups == null ? List.of() : List.copyOf(nutrientGroups);
    }

    public static RecentFoodSnapshot from(DietMemoryEntry entry) {
        return new RecentFoodSnapshot(entry.foodId(), entry.gameTime(), entry.mealQuality(),
            entry.nutrientGroups());
    }
}
