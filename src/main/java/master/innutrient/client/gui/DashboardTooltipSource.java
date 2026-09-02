package master.innutrient.client.gui;

import net.minecraft.network.chat.Component;

import java.util.List;

interface DashboardTooltipSource {
    List<Component> tooltipAt(double mouseX, double mouseY);
}
