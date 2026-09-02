package master.innutrient.client.gui;

import dev.uapi.client.ui.core.UIComponent;
import dev.uapi.client.ui.core.UIRenderContext;
import dev.uapi.client.ui.theme.UITheme.ColorToken;
import net.minecraft.network.chat.Component;

import java.util.List;

/** The single visual surface and shared scale legend for every data-driven nutrient row. */
final class NutrientBalanceSection extends UIComponent implements DashboardTooltipSource {
    @Override
    protected void renderComponent(UIRenderContext context) {
        DashboardRender.section(context.graphics(), bounds());
        int legendY = bounds().bottom() - 17;
        DashboardRender.divider(context.graphics(), bounds().x() + 9, bounds().right() - 9, legendY - 5);

        Component low = Component.translatable("screen.innutrient.dashboard.scale.low");
        Component target = Component.translatable("screen.innutrient.dashboard.scale.target_range");
        Component high = Component.translatable("screen.innutrient.dashboard.scale.high");
        int muted = theme().color(ColorToken.TEXT_MUTED);
        int left = bounds().x() + 10;
        int right = bounds().right() - 10;
        DashboardRender.drawTrimmed(context.graphics(), context.font(), low, left + 8, legendY,
            Math.max(8, bounds().width() / 4), muted, false);
        context.graphics().fill(left, legendY + 3, left + 4, legendY + 7, DashboardRender.LOW);

        int targetWidth = context.font().width(target);
        int targetX = bounds().x() + (bounds().width() - targetWidth) / 2;
        context.graphics().fill(targetX - 8, legendY + 3, targetX - 4, legendY + 7, DashboardRender.HEALTHY);
        DashboardRender.drawTrimmed(context.graphics(), context.font(), target, targetX, legendY,
            Math.max(8, bounds().width() / 2), muted, false);

        int highWidth = context.font().width(high);
        int highX = right - highWidth;
        context.graphics().fill(highX - 8, legendY + 3, highX - 4, legendY + 7, DashboardRender.ABOVE);
        DashboardRender.drawTrimmed(context.graphics(), context.font(), high, highX, legendY,
            Math.max(8, highWidth), muted, false);
    }

    @Override
    public List<Component> tooltipAt(double mouseX, double mouseY) {
        if (!bounds().contains(mouseX, mouseY) || mouseY < bounds().bottom() - 24) return List.of();
        return List.of(Component.translatable("screen.innutrient.dashboard.scale.tooltip"));
    }
}

