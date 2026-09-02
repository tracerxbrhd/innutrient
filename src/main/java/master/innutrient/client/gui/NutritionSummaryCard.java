package master.innutrient.client.gui;

import dev.uapi.client.ui.animation.UIAnimation;
import dev.uapi.client.ui.animation.UIEasing;
import dev.uapi.client.ui.core.UIBounds;
import dev.uapi.client.ui.core.UIComponent;
import dev.uapi.client.ui.core.UIRenderContext;
import dev.uapi.client.ui.theme.UITheme.ColorToken;
import master.innutrient.network.NutritionDashboardSettings;
import master.innutrient.nutrition.DietQuality;
import master.innutrient.nutrition.FoodVariety;
import master.innutrient.nutrition.MealQuality;
import net.minecraft.network.chat.Component;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

final class NutritionSummaryCard extends UIComponent implements DashboardTooltipSource {
    private final UIAnimation balanceTransition = new UIAnimation(Duration.ofMillis(260), UIEasing.EASE_OUT_CUBIC);
    private double balance;
    private double displayedBalance;
    private double balanceStart;
    private DietQuality dietQuality = DietQuality.STABLE;
    private FoodVariety.Score variety = new FoodVariety.Score(0, master.innutrient.nutrition.VarietyTier.REPETITIVE,
        0, 0, 0, 0);
    private MealQuality lastMeal;
    private NutritionDashboardSettings settings = NutritionDashboardSettings.DEFAULT;

    void update(double balance, DietQuality quality, FoodVariety.Score variety, MealQuality lastMeal,
                NutritionDashboardSettings settings) {
        double nextBalance = Math.max(0, Math.min(100, balance));
        DietQuality nextQuality = quality == null ? DietQuality.STABLE : quality;
        NutritionDashboardSettings nextSettings = settings == null
            ? NutritionDashboardSettings.DEFAULT : settings;
        if (Double.compare(this.balance, nextBalance) == 0 && this.dietQuality == nextQuality
            && Objects.equals(this.variety, variety) && this.lastMeal == lastMeal
            && this.settings.equals(nextSettings)) return;
        if (Double.compare(this.balance, nextBalance) != 0) {
            balanceStart = animatedBalance();
            this.balance = nextBalance;
            balanceTransition.start();
        }
        this.dietQuality = nextQuality;
        this.variety = variety;
        this.lastMeal = lastMeal;
        this.settings = nextSettings;
        invalidateRender();
    }

    @Override
    protected void renderComponent(UIRenderContext context) {
        DashboardRender.accentedSection(context.graphics(), bounds(), theme().color(ColorToken.ACCENT_PRIMARY));
        int gaugeSize = Math.min(64, Math.max(54, bounds().height() - 14));
        int gaugeX = bounds().x() + 10;
        int gaugeY = bounds().y() + (bounds().height() - gaugeSize) / 2;
        drawGauge(context, gaugeX, gaugeY, gaugeSize);

        int metricsX = gaugeX + gaugeSize + 12;
        int available = Math.max(0, bounds().right() - metricsX - 9);
        // DashboardLayout removes one pixel on each side of the summary surface.
        boolean horizontal = bounds().width() >= DashboardLayout.WIDE_BREAKPOINT - 2;
        if (horizontal) {
            int gap = 4;
            int metricWidth = Math.max(1, (available - gap * 2) / 3);
            drawMetric(context, metricsX, bounds().y() + 9, metricWidth, 60,
                Component.translatable("screen.innutrient.dashboard.diet_quality"),
                Component.translatable(dietQuality.translationKey()));
            drawMetric(context, metricsX + metricWidth + gap, bounds().y() + 9, metricWidth, 60,
                Component.translatable("screen.innutrient.dashboard.food_variety"),
                Component.translatable("screen.innutrient.dashboard.variety_value",
                    DashboardRender.percent(variety.value()), Component.translatable(variety.tier().translationKey())));
            drawMetric(context, metricsX + (metricWidth + gap) * 2, bounds().y() + 9,
                Math.max(1, available - (metricWidth + gap) * 2), 60,
                Component.translatable("screen.innutrient.dashboard.last_meal"), lastMealName());
        } else {
            int metricHeight = 29;
            drawMetric(context, metricsX, bounds().y() + 8, available, metricHeight,
                Component.translatable("screen.innutrient.dashboard.diet_quality"),
                Component.translatable(dietQuality.translationKey()));
            drawMetric(context, metricsX, bounds().y() + 39, available, metricHeight,
                Component.translatable("screen.innutrient.dashboard.food_variety"),
                Component.translatable("screen.innutrient.dashboard.variety_value",
                    DashboardRender.percent(variety.value()), Component.translatable(variety.tier().translationKey())));
            drawMetric(context, metricsX, bounds().y() + 70, available, metricHeight,
                Component.translatable("screen.innutrient.dashboard.last_meal"), lastMealName());
        }
    }

    private void drawGauge(UIRenderContext context, int x, int y, int size) {
        double shownBalance = animatedBalance();
        int centerX = x + size / 2;
        int centerY = y + size / 2;
        int radius = Math.max(8, size / 2 - 4);
        int segments = 64;
        int active = (int) Math.round(segments * shownBalance / 100.0);
        for (int index = 0; index < segments; index++) {
            double angle = -Math.PI / 2 + index * Math.PI * 2 / segments;
            int px = centerX + (int) Math.round(Math.cos(angle) * radius);
            int py = centerY + (int) Math.round(Math.sin(angle) * radius);
            int color = index < active ? theme().color(ColorToken.ACCENT_PRIMARY) : 0xFF30263A;
            context.graphics().fill(px - 1, py - 1, px + 2, py + 2, color);
        }
        Component value = Component.literal(DashboardRender.percent(shownBalance));
        int valueWidth = context.font().width(value);
        DashboardRender.drawTrimmed(context.graphics(), context.font(), value, centerX - valueWidth / 2,
            centerY - 7, valueWidth + 1, theme().color(ColorToken.TEXT_PRIMARY), true);
        Component label = Component.translatable("screen.innutrient.dashboard.balance_short");
        int labelWidth = context.font().width(label);
        DashboardRender.drawTrimmed(context.graphics(), context.font(), label, centerX - labelWidth / 2,
            centerY + 7, labelWidth + 1, theme().color(ColorToken.TEXT_MUTED), false);
    }

    private void drawMetric(UIRenderContext context, int x, int y, int width, int height,
                            Component label, Component value) {
        DashboardRender.subtleCard(context.graphics(), new UIBounds(x, y, width, height));
        DashboardRender.drawTrimmed(context.graphics(), context.font(), label, x + 7, y + 5,
            Math.max(0, width - 14), theme().color(ColorToken.ACCENT_PRIMARY), false);
        DashboardRender.drawTrimmed(context.graphics(), context.font(), value, x + 7, y + 17,
            Math.max(0, width - 14), theme().color(ColorToken.TEXT_PRIMARY), true);
    }

    private double animatedBalance() {
        if (!balanceTransition.running()) return displayedBalance = balance;
        return displayedBalance = balanceTransition.interpolate(balanceStart, balance);
    }

    private Component lastMealName() {
        return lastMeal == null ? Component.translatable("screen.innutrient.dashboard.no_meals")
            : Component.translatable(lastMeal.translationKey());
    }

    @Override
    public List<Component> tooltipAt(double mouseX, double mouseY) {
        if (!bounds().contains(mouseX, mouseY)) return List.of();
        return List.of(
            Component.translatable("screen.innutrient.dashboard.summary.tooltip"),
            Component.translatable("screen.innutrient.tooltip.current", DashboardRender.percent(balance)),
            Component.translatable("screen.innutrient.dashboard.variety_detail", variety.consideredMeals(),
                variety.distinctFoods()),
            lastMeal == null ? Component.translatable("screen.innutrient.dashboard.no_meals")
                : Component.translatable("screen.innutrient.dashboard.meal_bonus",
                    DashboardRender.percent(settings.mealEfficiencyBonus(lastMeal) * 100.0))
        );
    }
}
