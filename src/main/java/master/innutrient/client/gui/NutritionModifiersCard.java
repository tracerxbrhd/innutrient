package master.innutrient.client.gui;

import dev.uapi.client.ui.core.UIComponent;
import dev.uapi.client.ui.core.UIRenderContext;
import dev.uapi.client.ui.theme.UITheme.ColorToken;
import master.innutrient.network.NutritionDashboardSettings;
import master.innutrient.nutrition.DietQuality;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Locale;

final class NutritionModifiersCard extends UIComponent implements DashboardTooltipSource {
    private DietQuality quality = DietQuality.STABLE;
    private NutritionDashboardSettings settings = NutritionDashboardSettings.DEFAULT;

    void update(DietQuality quality, NutritionDashboardSettings settings) {
        DietQuality nextQuality = quality == null ? DietQuality.STABLE : quality;
        NutritionDashboardSettings nextSettings = settings == null
            ? NutritionDashboardSettings.DEFAULT : settings;
        if (this.quality == nextQuality && this.settings.equals(nextSettings)) return;
        this.quality = nextQuality;
        this.settings = nextSettings;
        invalidateRender();
    }

    @Override
    protected void renderComponent(UIRenderContext context) {
        DashboardRender.card(context.graphics(), bounds(), bounds().contains(context.mouseX(), context.mouseY()),
            theme().color(ColorToken.ACCENT_PRIMARY));
        DashboardRender.drawTrimmed(context.graphics(), context.font(),
            Component.translatable("screen.innutrient.dashboard.modifiers"), bounds().x() + 10,
            bounds().y() + 7, Math.max(0, bounds().width() - 20), theme().color(ColorToken.TEXT_SECONDARY), true);
        Component qualityName = Component.translatable(quality.translationKey());
        DashboardRender.drawTrimmed(context.graphics(), context.font(), qualityName, bounds().x() + 10,
            bounds().y() + 20, Math.max(0, bounds().width() - 20), theme().color(ColorToken.TEXT_MUTED), false);
        var modifier = settings.modifier(quality);
        drawModifier(context, 0, "screen.innutrient.dashboard.modifier.exhaustion", modifier.exhaustion(), true);
        drawModifier(context, 1, "screen.innutrient.dashboard.modifier.efficiency",
            modifier.nutritionEfficiency(), false);
        drawModifier(context, 2, "screen.innutrient.dashboard.modifier.regeneration",
            modifier.naturalRegeneration(), false);
    }

    private void drawModifier(UIRenderContext context, int index, String key, double multiplier, boolean inverse) {
        int y = bounds().y() + 39 + index * 22;
        double impact = (multiplier - 1.0) * (inverse ? -1 : 1);
        int color = impact > 0.0001 ? DashboardRender.HEALTHY
            : impact < -0.0001 ? DashboardRender.LOW : theme().color(ColorToken.TEXT_MUTED);
        String glyph = impact > 0.0001 ? "+" : impact < -0.0001 ? "−" : "•";
        context.graphics().fill(bounds().x() + 9, y - 2, bounds().x() + 21, y + 11, 0xFF21162C);
        context.graphics().centeredText(context.font(), glyph, bounds().x() + 15, y, color);
        Component line = Component.translatable(key, DashboardRender.signedPercent(multiplier, inverse));
        DashboardRender.drawTrimmed(context.graphics(), context.font(), line, bounds().x() + 27, y,
            Math.max(0, bounds().width() - 36), color, false);
    }

    @Override
    public List<Component> tooltipAt(double mouseX, double mouseY) {
        if (!bounds().contains(mouseX, mouseY)) return List.of();
        var modifier = settings.modifier(quality);
        return List.of(
            Component.translatable("screen.innutrient.dashboard.modifiers.tooltip",
                Component.translatable(quality.translationKey())),
            Component.translatable("screen.innutrient.dashboard.multiplier.exhaustion", format(modifier.exhaustion())),
            Component.translatable("screen.innutrient.dashboard.multiplier.efficiency",
                format(modifier.nutritionEfficiency())),
            Component.translatable("screen.innutrient.dashboard.multiplier.regeneration",
                format(modifier.naturalRegeneration()))
        );
    }

    private static String format(double value) {
        return String.format(Locale.ROOT, "%.2f", value).replaceAll("0+$", "").replaceAll("\\.$", "");
    }
}
