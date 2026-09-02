package master.innutrient.client.gui;

import dev.uapi.client.ui.core.UIComponent;
import dev.uapi.client.ui.core.UIRenderContext;
import dev.uapi.client.ui.theme.UITheme.ColorToken;
import net.minecraft.network.chat.Component;

final class DashboardSectionLabel extends UIComponent {
    private final Component text;

    DashboardSectionLabel(Component text) {
        this.text = text;
    }

    @Override
    protected void renderComponent(UIRenderContext context) {
        int accent = theme().color(ColorToken.ACCENT_PRIMARY);
        context.graphics().fill(bounds().x(), bounds().y() + 2, bounds().x() + 2, bounds().bottom() - 2, accent);
        DashboardRender.drawTrimmed(context.graphics(), context.font(), text, bounds().x() + 7, bounds().y() + 2,
            bounds().width() - 8, accent, false);
        int dividerX = Math.min(bounds().right(), bounds().x() + 12 + context.font().width(text));
        DashboardRender.divider(context.graphics(), dividerX, bounds().right(), bounds().y() + 7);
    }
}
