package master.innutrient.client.gui;

import dev.uapi.client.ui.core.UIComponent;
import dev.uapi.client.ui.core.UIRenderContext;
import dev.uapi.client.ui.theme.UITheme.ColorToken;
import net.minecraft.network.chat.Component;

import java.util.List;

final class DashboardGuidanceCard extends UIComponent implements DashboardTooltipSource {
    private Component message = Component.translatable("screen.innutrient.context.ok");
    private boolean warning;

    void update(Component message, boolean warning) {
        if (this.message.equals(message) && this.warning == warning) return;
        this.message = message;
        this.warning = warning;
        invalidateRender();
    }

    @Override
    protected void renderComponent(UIRenderContext context) {
        int accent = warning ? theme().color(ColorToken.ACCENT_WARNING) : theme().color(ColorToken.ACCENT_SUCCESS);
        DashboardRender.accentedSection(context.graphics(), bounds(), accent);
        Component heading = Component.translatable("screen.innutrient.dashboard.guidance");
        DashboardRender.drawTrimmed(context.graphics(), context.font(), heading, bounds().x() + 10,
            bounds().y() + 8, Math.max(0, bounds().width() - 20),
            theme().color(ColorToken.ACCENT_PRIMARY), false);
        DashboardRender.divider(context.graphics(), bounds().x() + 9, bounds().right() - 9, bounds().y() + 24);
        context.graphics().fill(bounds().x() + 10, bounds().y() + 31, bounds().x() + 30,
            bounds().y() + 52, DashboardRender.METRIC_BACKGROUND);
        Component glyph = Component.literal(warning ? "!" : "◆");
        int glyphX = bounds().x() + 20 - context.font().width(glyph) / 2;
        DashboardRender.drawTrimmed(context.graphics(), context.font(), glyph, glyphX, bounds().y() + 37,
            10, accent, true);
        DashboardRender.drawWrapped(context.graphics(), context.font(), message, bounds().x() + 38,
            bounds().y() + 31, Math.max(0, bounds().width() - 47), 2,
            theme().color(ColorToken.TEXT_PRIMARY), true);
    }

    @Override
    public List<Component> tooltipAt(double mouseX, double mouseY) {
        return bounds().contains(mouseX, mouseY) ? List.of(message) : List.of();
    }
}
