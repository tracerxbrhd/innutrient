package master.innutrient.client.gui;

import dev.uapi.client.ui.core.UIBounds;
import dev.uapi.client.ui.core.UIComponent;
import dev.uapi.client.ui.core.UIRenderContext;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

final class DashboardTooltipLayer extends UIComponent {
    private final Supplier<List<DashboardTooltipSource>> sources;
    private final Supplier<UIBounds> clip;

    DashboardTooltipLayer(Supplier<List<DashboardTooltipSource>> sources, Supplier<UIBounds> clip) {
        this.sources = sources;
        this.clip = clip;
    }

    @Override
    protected void renderComponent(UIRenderContext context) {
        UIBounds visible = clip.get();
        if (visible == null || !visible.contains(context.mouseX(), context.mouseY())) return;
        List<DashboardTooltipSource> snapshot = sources.get();
        for (int index = snapshot.size() - 1; index >= 0; index--) {
            List<Component> tooltip = snapshot.get(index).tooltipAt(context.mouseX(), context.mouseY());
            if (tooltip.isEmpty()) continue;
            context.graphics().setTooltipForNextFrame(context.font(), tooltip, Optional.empty(),
                context.mouseX(), context.mouseY());
            return;
        }
    }
}
