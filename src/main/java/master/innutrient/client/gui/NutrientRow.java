package master.innutrient.client.gui;

import dev.uapi.client.ui.animation.UIAnimation;
import dev.uapi.client.ui.animation.UIEasing;
import dev.uapi.client.ui.core.UIComponent;
import dev.uapi.client.ui.core.UIRenderContext;
import dev.uapi.client.ui.theme.UITheme.ColorToken;
import master.innutrient.config.InnutrientClientConfig;
import master.innutrient.nutrition.NutrientGroup;
import master.innutrient.nutrition.NutrientStatus;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.time.Duration;
import java.util.List;
import java.util.Locale;

final class NutrientRow extends UIComponent implements DashboardTooltipSource {
    private final NutrientGroup group;
    private final ItemStack icon;
    private final UIAnimation transition = new UIAnimation(Duration.ofMillis(220), UIEasing.EASE_OUT_CUBIC);
    private double value;
    private double displayedValue;
    private double animationStart;

    NutrientRow(NutrientGroup group) {
        this.group = group;
        this.icon = new ItemStack(BuiltInRegistries.ITEM.getOptional(group.icon()).orElse(Items.BARRIER));
        this.value = group.defaultLevel();
        this.displayedValue = value;
    }

    NutrientGroup group() { return group; }
    double value() { return value; }

    void setValue(double value) {
        double next = NutrientGroup.clamp(value);
        if (Double.compare(this.value, next) == 0) return;
        animationStart = animatedValue();
        this.value = next;
        transition.start();
        invalidateRender();
    }

    @Override
    public List<Component> tooltipAt(double mouseX, double mouseY) {
        if (!bounds().contains(mouseX, mouseY)) return List.of();
        return List.of(
            Component.translatable(group.translationKey()),
            Component.translatable("screen.innutrient.tooltip.current", String.format(Locale.ROOT, "%.1f%%", value)),
            Component.translatable("screen.innutrient.tooltip.target",
                String.format(Locale.ROOT, "%.0f–%.0f%%", group.healthyMin(), group.healthyMax())),
            Component.translatable("screen.innutrient.status." + group.status(value).name().toLowerCase(Locale.ROOT)),
            Component.translatable("screen.innutrient.dashboard.scale.tooltip")
        );
    }

    @Override
    protected void renderComponent(UIRenderContext context) {
        int x = bounds().x();
        int y = bounds().y();
        int width = bounds().width();
        NutrientStatus status = group.status(value);
        int statusColor = statusColor(status);
        int nutrientColor = DashboardRender.opaque(group.color());
        boolean hovered = bounds().contains(context.mouseX(), context.mouseY());
        DashboardRender.interactiveRow(context.graphics(), bounds(), hovered, nutrientColor);
        DashboardRender.divider(context.graphics(), x + 4, bounds().right() - 4, bounds().bottom() - 1);
        context.graphics().fill(x + 3, y + 5, x + 25, y + 27, DashboardRender.METRIC_BACKGROUND);
        context.graphics().renderItem(icon, x + 6, y + 8);

        int percentReserve = InnutrientClientConfig.SHOW_PERCENTAGES.get() ? 42 : 0;
        int statusReserve = width >= 310 ? 102 : 18;
        int nameWidth = Math.max(10, width - 36 - percentReserve - statusReserve);
        DashboardRender.drawTrimmed(context.graphics(), context.font(), Component.translatable(group.translationKey()),
            x + 31, y + 4, nameWidth, theme().color(ColorToken.TEXT_PRIMARY), true);

        String symbol = statusSymbol(status);
        if (width >= 310) {
            Component statusText = Component.translatable("screen.innutrient.dashboard.status",
                symbol, Component.translatable("screen.innutrient.status." + status.name().toLowerCase(Locale.ROOT)));
            int statusX = bounds().right() - percentReserve - 102;
            DashboardRender.drawTrimmed(context.graphics(), context.font(), statusText, statusX, y + 4, 94,
                statusColor, false);
        } else {
            DashboardRender.drawTrimmed(context.graphics(), context.font(), Component.literal(symbol),
                bounds().right() - percentReserve - 17, y + 4, 16, statusColor, true);
        }
        if (InnutrientClientConfig.SHOW_PERCENTAGES.get()) {
            String percent = DashboardRender.percent(value);
            context.graphics().drawString(context.font(), percent,
                bounds().right() - context.font().width(percent) - 7, y + 4,
                theme().color(ColorToken.TEXT_SECONDARY), true);
        }

        drawRangeScale(context, x + 31, y + 22, Math.max(1, width - 38), 8, nutrientColor, statusColor);
    }

    private void drawRangeScale(UIRenderContext context, int x, int y, int width, int height, int nutrientColor,
                                int markerColor) {
        int targetStart = x + (int) Math.round(width * group.healthyMin() / 100.0);
        int targetEnd = x + (int) Math.round(width * group.healthyMax() / 100.0);
        targetStart = Math.max(x, Math.min(x + width, targetStart));
        targetEnd = Math.max(targetStart, Math.min(x + width, targetEnd));
        context.graphics().fill(x, y, x + width, y + height, DashboardRender.TRACK_EMPTY);
        int currentEnd = x + (int) Math.round(width * animatedValue() / 100.0);
        if (currentEnd > x) context.graphics().fill(x, y, currentEnd, y + height, nutrientColor);
        context.graphics().fill(targetStart, y + 1, targetEnd, y + height - 1, 0x473C7B55);
        context.graphics().fill(targetStart, y - 1, targetEnd, y, 0xFF79A888);
        context.graphics().fill(targetStart, y - 1, targetStart + 1, y + height + 1, 0xFF79A888);
        context.graphics().fill(Math.max(targetStart, targetEnd - 1), y - 1, targetEnd, y + height + 1,
            0xFF79A888);
        DashboardRender.border(context.graphics(), new dev.uapi.client.ui.core.UIBounds(x, y, width, height),
            DashboardRender.DIVIDER);

        int markerX = currentEnd;
        markerX = Math.max(x + 1, Math.min(x + width - 2, markerX));
        context.graphics().fill(markerX, y - 2, markerX + 1, y + height + 2, markerColor);
        context.graphics().fill(markerX - 1, y - 2, markerX + 2, y - 1, markerColor);
    }

    private double animatedValue() {
        if (!transition.running()) return displayedValue = value;
        return displayedValue = transition.interpolate(animationStart, value);
    }

    private static String statusSymbol(NutrientStatus status) {
        return switch (status) {
            case DEFICIENT -> "▼!";
            case BELOW_TARGET -> "▼";
            case HEALTHY -> "◆";
            case ABOVE_TARGET -> "▲";
            case EXCESSIVE -> "▲!";
        };
    }

    private static int statusColor(NutrientStatus status) {
        return switch (status) {
            case DEFICIENT -> DashboardRender.LOW;
            case BELOW_TARGET -> DashboardRender.BELOW;
            case HEALTHY -> DashboardRender.HEALTHY;
            case ABOVE_TARGET -> DashboardRender.ABOVE;
            case EXCESSIVE -> DashboardRender.EXCESSIVE;
        };
    }
}
