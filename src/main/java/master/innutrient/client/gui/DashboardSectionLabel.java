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
        context.graphics().fill(bounds().x(), bounds().bottom() - 1, bounds().right(), bounds().bottom(),
            DashboardRender.DIVIDER);
        context.graphics().fill(bounds().x(), bounds().y() + 2, bounds().x() + 2, bounds().bottom() - 3,
            theme().color(ColorToken.ACCENT_PRIMARY));
        DashboardRender.drawTrimmed(context.graphics(), context.font(), text, bounds().x() + 7, bounds().y() + 2,
            bounds().width() - 8, theme().color(ColorToken.TEXT_SECONDARY), true);
    }
}

