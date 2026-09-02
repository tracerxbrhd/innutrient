package master.innutrient.client.gui;

import dev.uapi.client.ui.core.UIComponent;
import dev.uapi.client.ui.core.UIRenderContext;
import dev.uapi.client.ui.theme.UITheme.ColorToken;
import master.innutrient.network.NutritionDashboardSettings;
import master.innutrient.nutrition.DietQuality;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class NutritionModifiersCard extends UIComponent implements DashboardTooltipSource {
    private DietQuality quality = DietQuality.STABLE;
    private NutritionDashboardSettings settings = NutritionDashboardSettings.DEFAULT;
    private List<DisplayModifier> activeModifiers = List.of();

    NutritionModifiersCard() {
        rebuildActiveModifiers();
    }

    void update(DietQuality quality, NutritionDashboardSettings settings) {
        DietQuality nextQuality = quality == null ? DietQuality.STABLE : quality;
        NutritionDashboardSettings nextSettings = settings == null
            ? NutritionDashboardSettings.DEFAULT : settings;
        if (this.quality == nextQuality && this.settings.equals(nextSettings)) return;
        this.quality = nextQuality;
        this.settings = nextSettings;
        rebuildActiveModifiers();
        invalidateRender();
    }

    int visibleModifierCount() {
        return activeModifiers.size();
    }

    @Override
    protected void renderComponent(UIRenderContext context) {
        DashboardRender.section(context.graphics(), bounds());
        DashboardRender.drawTrimmed(context.graphics(), context.font(),
            Component.translatable("screen.innutrient.dashboard.modifiers"), bounds().x() + 10,
            bounds().y() + 8, Math.max(0, bounds().width() - 20),
            theme().color(ColorToken.ACCENT_PRIMARY), false);
        DashboardRender.divider(context.graphics(), bounds().x() + 9, bounds().right() - 9, bounds().y() + 24);

        Component qualityName = Component.translatable("screen.innutrient.dashboard.diet_state",
            Component.translatable(quality.translationKey()));
        DashboardRender.drawTrimmed(context.graphics(), context.font(), qualityName, bounds().x() + 10,
            bounds().y() + 31, Math.max(0, bounds().width() - 20),
            theme().color(ColorToken.TEXT_PRIMARY), true);

        if (activeModifiers.isEmpty()) {
            DashboardRender.drawTrimmed(context.graphics(), context.font(),
                Component.translatable("screen.innutrient.dashboard.no_active_modifiers"), bounds().x() + 10,
                bounds().y() + 48, Math.max(0, bounds().width() - 20),
                theme().color(ColorToken.TEXT_MUTED), false);
            return;
        }

        int rowY = bounds().y() + 48;
        for (DisplayModifier modifier : activeModifiers) {
            drawModifier(context, rowY, modifier);
            rowY += 21;
        }
    }

    private void drawModifier(UIRenderContext context, int y, DisplayModifier modifier) {
        int color = modifier.impact() > 0 ? DashboardRender.HEALTHY : DashboardRender.LOW;
        String glyph = modifier.multiplier() < 1.0 ? "↓" : "↑";
        DashboardRender.drawTrimmed(context.graphics(), context.font(), Component.literal(glyph),
            bounds().x() + 10, y, 10, color, true);
        Component label = Component.translatable(modifier.labelKey());
        DashboardRender.drawTrimmed(context.graphics(), context.font(), label, bounds().x() + 23, y,
            Math.max(0, bounds().width() - 66), theme().color(ColorToken.TEXT_SECONDARY), false);
        Component value = Component.literal(DashboardRender.signedPercent(modifier.multiplier(), modifier.inverse()));
        int valueWidth = context.font().width(value);
        DashboardRender.drawTrimmed(context.graphics(), context.font(), value,
            bounds().right() - valueWidth - 9, y, valueWidth + 1, color, true);
    }

    private void rebuildActiveModifiers() {
        var configured = settings.modifier(quality);
        List<DisplayModifier> next = new ArrayList<>(3);
        addIfMeaningful(next, "screen.innutrient.dashboard.modifier.label.exhaustion",
            configured.exhaustion(), true);
        addIfMeaningful(next, "screen.innutrient.dashboard.modifier.label.efficiency",
            configured.nutritionEfficiency(), false);
        addIfMeaningful(next, "screen.innutrient.dashboard.modifier.label.regeneration",
            configured.naturalRegeneration(), false);
        activeModifiers = List.copyOf(next);
    }

    private static void addIfMeaningful(List<DisplayModifier> target, String key, double multiplier,
                                        boolean inverse) {
        if (Math.abs(multiplier - 1.0) <= 0.0001) return;
        double impact = (multiplier - 1.0) * (inverse ? -1 : 1);
        target.add(new DisplayModifier(key, multiplier, inverse, impact));
    }

    static int meaningfulModifierCount(NutritionDashboardSettings.DietModifier modifier) {
        if (modifier == null) return 0;
        int count = 0;
        if (Math.abs(modifier.exhaustion() - 1.0) > 0.0001) count++;
        if (Math.abs(modifier.nutritionEfficiency() - 1.0) > 0.0001) count++;
        if (Math.abs(modifier.naturalRegeneration() - 1.0) > 0.0001) count++;
        return count;
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

    private record DisplayModifier(String labelKey, double multiplier, boolean inverse, double impact) {}
}
