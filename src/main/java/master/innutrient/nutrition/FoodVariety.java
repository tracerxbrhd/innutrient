package master.innutrient.nutrition;

import net.minecraft.resources.ResourceLocation;

/** Bounded food-variety model: only the current repeated-food streak is persisted. */
public final class FoodVariety {
    private FoodVariety() {}

    public static Result consume(ResourceLocation previousFood, int previousRepeats, long previousGameTime,
                                 ResourceLocation consumedFood, long gameTime, double penaltyPerRepeat,
                                 double minimumEfficiency, long recoveryTicks) {
        boolean recovered = previousFood == null || !previousFood.equals(consumedFood)
            || gameTime < previousGameTime || gameTime - previousGameTime >= Math.max(0, recoveryTicks);
        int repeats = recovered ? 0 : Math.min(10_000, Math.max(0, previousRepeats) + 1);
        double efficiency = Math.max(clamp01(minimumEfficiency), 1.0 - repeats * clamp01(penaltyPerRepeat));
        return new Result(efficiency, consumedFood, repeats, Math.max(0, gameTime));
    }

    private static double clamp01(double value) {
        if (!Double.isFinite(value)) return 0;
        return Math.max(0, Math.min(1, value));
    }

    public record Result(double efficiency, ResourceLocation food, int repeatCount, long gameTime) {}
}
