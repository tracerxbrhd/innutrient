package master.innutrient.client.gui;

import dev.uapi.client.ui.core.UIComponent;
import dev.uapi.client.ui.core.UIRenderContext;
import dev.uapi.client.ui.theme.UITheme.ColorToken;
import master.innutrient.network.NutritionDashboardSettings;
import master.innutrient.nutrition.DietQuality;
import master.innutrient.nutrition.FoodVariety;
import master.innutrient.nutrition.MealQuality;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Objects;

final class NutritionSummaryCard extends UIComponent implements DashboardTooltipSource {
    private double balance;
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
        this.balance = nextBalance;
        this.dietQuality = nextQuality;
        this.variety = variety;
        this.lastMeal = lastMeal;
        this.settings = nextSettings;
        invalidateRender();
    }

    @Override
    protected void renderComponent(UIRenderContext context) {
        boolean hovered = bounds().contains(context.mouseX(), context.mouseY());
        DashboardRender.card(context.graphics(), bounds(), hovered, theme().color(ColorToken.ACCENT_PRIMARY));
        int x = bounds().x();
        int y = bounds().y();
        int height = bounds().height();
        int gaugeSize = Math.min(64, Math.max(50, height - 16));
        int gaugeX = x + 11;
        int gaugeY = y + (height - gaugeSize) / 2;
        drawGauge(context, gaugeX, gaugeY, gaugeSize);

        int contentX = gaugeX + gaugeSize + 12;
        int available = Math.max(0, bounds().right() - contentX - 8);
        boolean roomy = bounds().width() >= 520;
        int columnWidth = roomy ? available / 3 : available;
        drawMetric(context, contentX, y + 11, columnWidth,
            Component.translatable("screen.innutrient.dashboard.diet_quality"),
            Component.translatable(dietQuality.translationKey()));
        if (roomy) {
            drawMetric(context, contentX + columnWidth, y + 11, columnWidth,
                Component.translatable("screen.innutrient.dashboard.food_variety"),
                Component.translatable("screen.innutrient.dashboard.variety_value",
                    DashboardRender.percent(variety.value()), Component.translatable(variety.tier().translationKey())));
            drawMetric(context, contentX + columnWidth * 2, y + 11,
                available - columnWidth * 2,
                Component.translatable("screen.innutrient.dashboard.last_meal"), lastMealName());
        } else {
            drawMetric(context, contentX, y + 37, available,
                Component.translatable("screen.innutrient.dashboard.food_variety"),
                Component.translatable("screen.innutrient.dashboard.variety_value",
                    DashboardRender.percent(variety.value()), Component.translatable(variety.tier().translationKey())));
            if (height >= 90) drawMetric(context, contentX, y + 63, available,
                Component.translatable("screen.innutrient.dashboard.last_meal"), lastMealName());
        }
    }

    private void drawGauge(UIRenderContext context, int x, int y, int size) {
        int centerX = x + size / 2;
        int centerY = y + size / 2;
        int radius = Math.max(8, size / 2 - 3);
        int segments = 48;
        int active = (int) Math.round(segments * balance / 100.0);
        for (int index = 0; index < segments; index++) {
            double angle = -Math.PI / 2 + index * Math.PI * 2 / segments;
            int px = centerX + (int) Math.round(Math.cos(angle) * radius);
            int py = centerY + (int) Math.round(Math.sin(angle) * radius);
            int color = index < active ? theme().color(ColorToken.ACCENT_PRIMARY) : 0xFF33263E;
            context.graphics().fill(px - 1, py - 1, px + 2, py + 2, color);
        }
        String value = DashboardRender.percent(balance);
        context.graphics().drawString(context.font(), value,
            centerX - context.font().width(value) / 2, centerY - 6,
            theme().color(ColorToken.TEXT_PRIMARY), true);
        Component label = Component.translatable("screen.innutrient.dashboard.balance_short");
        DashboardRender.drawTrimmed(context.graphics(), context.font(), label, x + 2, centerY + 8,
            size - 4, theme().color(ColorToken.TEXT_MUTED), false);
    }

    private void drawMetric(UIRenderContext context, int x, int y, int width, Component label, Component value) {
        DashboardRender.drawTrimmed(context.graphics(), context.font(), label, x, y, width,
            theme().color(ColorToken.TEXT_MUTED), false);
        DashboardRender.drawTrimmed(context.graphics(), context.font(), value, x, y + 12, width,
            theme().color(ColorToken.TEXT_PRIMARY), true);
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
