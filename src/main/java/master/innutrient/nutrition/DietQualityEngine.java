package master.innutrient.nutrition;

import master.innutrient.config.InnutrientServerConfig;
import master.innutrient.player.NutritionState;

import java.util.List;

/** Computes hysteretic, sustained diet-quality transitions without per-tick oscillation. */
public final class DietQualityEngine {
    private DietQualityEngine() {}

    public static NutritionState update(NutritionState state, List<NutrientGroup> groups, double balance,
                                        long gameTime) {
        return update(state, groups, balance, gameTime, Settings.fromConfig());
    }

    public static NutritionState update(NutritionState state, List<NutrientGroup> groups, double balance,
                                        long gameTime, Settings settings) {
        DietQuality desired = desired(state.dietQuality(), state, groups, balance, settings);
        if (desired == state.dietQuality()) {
            if (state.candidateDietQuality() == desired) return state;
            return state.withDietQuality(desired, desired, state.dietQualitySince(), gameTime);
        }

        long candidateSince = state.candidateDietQuality() == desired ? state.candidateSince() : gameTime;
        if (gameTime - candidateSince < settings.sustainTicks(desired))
            return state.withDietQuality(state.dietQuality(), desired, state.dietQualitySince(), candidateSince);
        return state.withDietQuality(desired, desired, gameTime, gameTime);
    }

    public static DietQuality desired(DietQuality current, NutritionState state, List<NutrientGroup> groups,
                                      double balance, Settings settings) {
        long deficient = groups.stream().filter(NutrientGroup::penalizeLow)
            .filter(group -> group.status(state.get(group)) == NutrientStatus.DEFICIENT).count();
        long excessive = groups.stream().filter(NutrientGroup::penalizeHigh)
            .filter(group -> group.status(state.get(group)) == NutrientStatus.EXCESSIVE).count();
        long penalized = deficient + excessive;

        double severeLimit = current == DietQuality.SEVERE ? settings.severeExit() : settings.severeEnter();
        if (balance <= severeLimit || penalized >= 2) return DietQuality.SEVERE;

        double poorLimit = current == DietQuality.POOR || current == DietQuality.SEVERE
            ? settings.poorExit() : settings.poorEnter();
        if (balance < poorLimit || penalized > 0) return DietQuality.POOR;

        double optimalLimit = current == DietQuality.OPTIMAL ? settings.optimalExit() : settings.optimalEnter();
        if (balance >= optimalLimit) return DietQuality.OPTIMAL;

        double balancedLimit = current == DietQuality.BALANCED || current == DietQuality.OPTIMAL
            ? settings.balancedExit() : settings.balancedEnter();
        return balance >= balancedLimit ? DietQuality.BALANCED : DietQuality.STABLE;
    }

    public record Settings(double severeEnter, double severeExit, double poorEnter, double poorExit,
                           double balancedEnter, double balancedExit, double optimalEnter, double optimalExit,
                           long transitionTicks, long balancedTicks, long optimalTicks) {
        public Settings {
            severeEnter = bounded(severeEnter);
            severeExit = Math.max(severeEnter, bounded(severeExit));
            poorEnter = Math.max(severeEnter, bounded(poorEnter));
            poorExit = Math.max(poorEnter, bounded(poorExit));
            balancedEnter = Math.max(poorEnter, bounded(balancedEnter));
            balancedExit = Math.min(balancedEnter, Math.max(poorExit, bounded(balancedExit)));
            optimalEnter = Math.max(balancedEnter, bounded(optimalEnter));
            optimalExit = Math.min(optimalEnter, Math.max(balancedExit, bounded(optimalExit)));
            transitionTicks = Math.max(0, transitionTicks);
            balancedTicks = Math.max(transitionTicks, balancedTicks);
            optimalTicks = Math.max(balancedTicks, optimalTicks);
        }

        public long sustainTicks(DietQuality quality) {
            return switch (quality) {
                case BALANCED -> balancedTicks;
                case OPTIMAL -> optimalTicks;
                default -> transitionTicks;
            };
        }

        public static Settings fromConfig() {
            return new Settings(InnutrientServerConfig.DIET_SEVERE_ENTER.get(),
                InnutrientServerConfig.DIET_SEVERE_EXIT.get(), InnutrientServerConfig.DIET_POOR_ENTER.get(),
                InnutrientServerConfig.DIET_POOR_EXIT.get(), InnutrientServerConfig.DIET_BALANCED_ENTER.get(),
                InnutrientServerConfig.DIET_BALANCED_EXIT.get(), InnutrientServerConfig.DIET_OPTIMAL_ENTER.get(),
                InnutrientServerConfig.DIET_OPTIMAL_EXIT.get(), InnutrientServerConfig.DIET_TRANSITION_TICKS.get(),
                InnutrientServerConfig.DIET_BALANCED_TICKS.get(), InnutrientServerConfig.DIET_OPTIMAL_TICKS.get());
        }

        private static double bounded(double value) {
            return Double.isFinite(value) ? Math.max(0, Math.min(100, value)) : 0;
        }
    }
}
