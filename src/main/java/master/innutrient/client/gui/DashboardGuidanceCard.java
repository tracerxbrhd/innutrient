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
        boolean hovered = bounds().contains(context.mouseX(), context.mouseY());
        int accent = warning ? theme().color(ColorToken.ACCENT_WARNING) : theme().color(ColorToken.ACCENT_SUCCESS);
        DashboardRender.card(context.graphics(), bounds(), hovered, accent);
        Component heading = Component.translatable("screen.innutrient.dashboard.guidance");
        DashboardRender.drawTrimmed(context.graphics(), context.font(), heading, bounds().x() + 10,
            bounds().y() + 6, Math.max(0, bounds().width() / 3), theme().color(ColorToken.TEXT_MUTED), false);
        int messageX = bounds().width() >= 430 ? bounds().x() + Math.max(92, bounds().width() / 4)
            : bounds().x() + 10;
        int messageY = bounds().width() >= 430 ? bounds().y() + 6 : bounds().y() + 22;
        DashboardRender.drawTrimmed(context.graphics(), context.font(), message, messageX, messageY,
            bounds().right() - messageX - 8, theme().color(ColorToken.TEXT_PRIMARY), true);
    }

    @Override
    public List<Component> tooltipAt(double mouseX, double mouseY) {
        return bounds().contains(mouseX, mouseY) ? List.of(message) : List.of();
    }
}
