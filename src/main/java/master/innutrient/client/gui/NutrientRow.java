package master.innutrient.client.gui;

import dev.uapi.client.ui.core.UIComponent;
import dev.uapi.client.ui.core.UIRenderContext;
import master.innutrient.config.InnutrientClientConfig;
import master.innutrient.nutrition.NutrientGroup;
import master.innutrient.nutrition.NutrientStatus;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.Locale;

final class NutrientRow extends UIComponent {
    private final NutrientGroup group;
    private final ItemStack icon;
    private double value;

    NutrientRow(NutrientGroup group) {
        this.group = group;
        this.icon = new ItemStack(BuiltInRegistries.ITEM.getOptional(group.icon()).orElse(Items.BARRIER));
        this.value = group.defaultLevel();
    }

    NutrientGroup group() { return group; }
    double value() { return value; }

    void setValue(double value) {
        double next = NutrientGroup.clamp(value);
        if (Double.compare(this.value, next) == 0) return;
        this.value = next;
        invalidateRender();
    }

    List<Component> tooltip() {
        return List.of(
            Component.translatable(group.translationKey()),
            Component.translatable("screen.innutrient.tooltip.current", String.format(Locale.ROOT, "%.1f%%", value)),
            Component.translatable("screen.innutrient.tooltip.target",
                String.format(Locale.ROOT, "%.0f–%.0f%%", group.healthyMin(), group.healthyMax())),
            Component.translatable("screen.innutrient.status." + group.status(value).name().toLowerCase(Locale.ROOT))
        );
    }

    @Override
    protected void renderComponent(UIRenderContext context) {
        int x = bounds().x();
        int y = bounds().y();
        int width = bounds().width();
        context.graphics().fill(x, y, bounds().right(), bounds().bottom(), 0xB8222222);
        int border = statusColor(group.status(value));
        context.graphics().fill(x, y, bounds().right(), y + 1, border);
        context.graphics().fill(x, bounds().bottom() - 1, bounds().right(), bounds().bottom(), border);
        context.graphics().fill(x, y, x + 1, bounds().bottom(), border);
        context.graphics().fill(bounds().right() - 1, y, bounds().right(), bounds().bottom(), border);
        context.graphics().renderItem(icon, x + 6, y + 10);

        Component name = Component.translatable(group.translationKey());
        context.graphics().drawString(context.font(), name, x + 28, y + 5, 0xFFF4F4F4, true);
        if (InnutrientClientConfig.SHOW_PERCENTAGES.get()) {
            String percent = String.format(Locale.ROOT, "%.0f%%", value);
            context.graphics().drawString(context.font(), percent,
                bounds().right() - context.font().width(percent) - 6, y + 5, 0xFFE8E8E8, true);
        }

        int barX = x + 28;
        int barY = y + 21;
        int barWidth = Math.max(1, width - 36);
        context.graphics().fill(barX, barY, barX + barWidth, barY + 9, 0xFF111111);
        int fill = (int) Math.round(barWidth * value / 100.0);
        context.graphics().fill(barX, barY, barX + fill, barY + 9, 0xFF000000 | group.color());
        int targetStart = barX + (int) Math.round(barWidth * group.healthyMin() / 100.0);
        int targetEnd = barX + (int) Math.round(barWidth * group.healthyMax() / 100.0);
        context.graphics().fill(targetStart, barY, Math.min(targetStart + 1, barX + barWidth), barY + 9, 0xFFFFFFFF);
        context.graphics().fill(Math.max(barX, targetEnd - 1), barY, targetEnd, barY + 9, 0xFFFFFFFF);
    }

    private static int statusColor(NutrientStatus status) {
        return switch (status) {
            case DEFICIENT -> 0xFFE24A4A;
            case BELOW_TARGET -> 0xFFE7A64A;
            case HEALTHY -> 0xFF55C96B;
            case ABOVE_TARGET -> 0xFFE7C84A;
            case EXCESSIVE -> 0xFFD55BE5;
        };
    }
}
