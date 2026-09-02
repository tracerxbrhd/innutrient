package master.innutrient.client.gui;

import dev.uapi.client.ui.components.UIScrollContainer;
import dev.uapi.client.ui.core.UIComponent;
import dev.uapi.client.ui.core.UIRenderContext;
import dev.uapi.client.ui.theme.UITheme.ColorToken;

final class DashboardScrollBar extends UIComponent {
    private final UIScrollContainer scroll;

    DashboardScrollBar(UIScrollContainer scroll) {
        this.scroll = scroll;
    }

    @Override
    protected void renderComponent(UIRenderContext context) {
        int max = scroll.maxScroll();
        if (max <= 0 || bounds().height() <= 0) return;
        context.graphics().fill(bounds().x(), bounds().y(), bounds().right(), bounds().bottom(), 0x8021162C);
        int contentHeight = max + scroll.bounds().height();
        int thumbHeight = Math.max(18, bounds().height() * scroll.bounds().height() / Math.max(1, contentHeight));
        int travel = Math.max(0, bounds().height() - thumbHeight);
        int thumbY = bounds().y() + (int) Math.round(travel * scroll.scrollOffset() / (double) max);
        context.graphics().fill(bounds().x(), thumbY, bounds().right(), thumbY + thumbHeight,
            theme().color(ColorToken.ACCENT_PRIMARY));
    }
}
