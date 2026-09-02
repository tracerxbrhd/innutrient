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
        boolean hovered = bounds().contains(context.mouseX(), context.mouseY());
        DashboardRender.card(context.graphics(), bounds(), hovered, statusColor);
        context.graphics().renderItem(icon, x + 7, y + 5);

        int percentReserve = InnutrientClientConfig.SHOW_PERCENTAGES.get() ? 42 : 0;
        int nameWidth = Math.max(10, width - 39 - percentReserve - (width >= 310 ? 88 : 18));
        DashboardRender.drawTrimmed(context.graphics(), context.font(), Component.translatable(group.translationKey()),
            x + 29, y + 5, nameWidth, theme().color(ColorToken.TEXT_PRIMARY), true);

        String symbol = statusSymbol(status);
        if (width >= 310) {
            Component statusText = Component.translatable("screen.innutrient.dashboard.status",
                symbol, Component.translatable("screen.innutrient.status." + status.name().toLowerCase(Locale.ROOT)));
            int statusX = bounds().right() - percentReserve - 92;
            DashboardRender.drawTrimmed(context.graphics(), context.font(), statusText, statusX, y + 5, 84,
                statusColor, false);
        } else {
            context.graphics().drawString(context.font(), symbol, bounds().right() - percentReserve - 17, y + 5,
                statusColor, true);
        }
        if (InnutrientClientConfig.SHOW_PERCENTAGES.get()) {
            String percent = DashboardRender.percent(value);
            context.graphics().drawString(context.font(), percent,
                bounds().right() - context.font().width(percent) - 7, y + 5,
                theme().color(ColorToken.TEXT_SECONDARY), true);
        }

        drawRangeScale(context, x + 29, y + 21, Math.max(1, width - 38), 10, statusColor);
        drawScaleLabels(context, x + 29, y + 35, Math.max(1, width - 38));
    }

    private void drawRangeScale(UIRenderContext context, int x, int y, int width, int height, int markerColor) {
        int targetStart = x + (int) Math.round(width * group.healthyMin() / 100.0);
        int targetEnd = x + (int) Math.round(width * group.healthyMax() / 100.0);
        targetStart = Math.max(x, Math.min(x + width, targetStart));
        targetEnd = Math.max(targetStart, Math.min(x + width, targetEnd));
        context.graphics().fill(x, y, x + width, y + height, 0xFF120E18);
        context.graphics().fill(x, y, targetStart, y + height, 0xFF351C25);
        context.graphics().fill(targetStart, y, targetEnd, y + height, 0xFF203629);
        context.graphics().fill(targetEnd, y, x + width, y + height, 0xFF321C38);
        for (int hatch = x + 2; hatch < targetStart; hatch += 5)
            context.graphics().fill(hatch, y + 1, hatch + 1, y + height - 1, 0xFF75404A);
        for (int dot = targetEnd + 2; dot < x + width; dot += 5)
            context.graphics().fill(dot, y + 2, dot + 1, y + height - 2, 0xFF71467C);
        context.graphics().fill(targetStart, y, Math.min(targetStart + 1, x + width), y + height, 0xFFA8DDB7);
        context.graphics().fill(Math.max(x, targetEnd - 1), y, targetEnd, y + height, 0xFFA8DDB7);
        DashboardRender.border(context.graphics(), new dev.uapi.client.ui.core.UIBounds(x, y, width, height),
            DashboardRender.DIVIDER);

        int markerX = x + (int) Math.round(width * animatedValue() / 100.0);
        markerX = Math.max(x + 1, Math.min(x + width - 2, markerX));
        context.graphics().fill(markerX - 1, y - 2, markerX + 2, y + height + 2, markerColor);
        context.graphics().fill(markerX - 2, y - 2, markerX + 3, y - 1, markerColor);
    }

    private void drawScaleLabels(UIRenderContext context, int x, int y, int width) {
        int color = theme().color(ColorToken.TEXT_MUTED);
        Component low = Component.translatable("screen.innutrient.dashboard.scale.low");
        Component target = Component.translatable("screen.innutrient.dashboard.scale.target");
        Component high = Component.translatable("screen.innutrient.dashboard.scale.high");
        if (width < 180) {
            low = Component.literal("▼");
            target = Component.literal("◆");
            high = Component.literal("▲");
        }
        context.graphics().drawString(context.font(), low, x, y, color, false);
        context.graphics().drawCenteredString(context.font(), target, x + width / 2, y, color);
        context.graphics().drawString(context.font(), high, x + width - context.font().width(high), y, color, false);
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
