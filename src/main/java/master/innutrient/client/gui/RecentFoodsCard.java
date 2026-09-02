package master.innutrient.client.gui;

import dev.uapi.client.ui.core.UIComponent;
import dev.uapi.client.ui.core.UIRenderContext;
import dev.uapi.client.ui.theme.UITheme.ColorToken;
import master.innutrient.nutrition.DietMemoryEntry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class RecentFoodsCard extends UIComponent implements DashboardTooltipSource {
    private List<DisplayFood> foods = List.of();
    private List<DietMemoryEntry> memorySnapshot = List.of();
    private long gameTime;

    void update(List<DietMemoryEntry> memory, long gameTime) {
        long nextGameTime = Math.max(0, gameTime);
        List<DietMemoryEntry> newestFirst = new ArrayList<>(memory == null ? List.of() : memory);
        Collections.reverse(newestFirst);
        List<DietMemoryEntry> shown = newestFirst.stream().limit(DashboardLayout.MAX_RECENT_FOODS).toList();
        boolean foodsChanged = !shown.equals(memorySnapshot);
        boolean timeChanged = this.gameTime / 20 != nextGameTime / 20;
        this.gameTime = nextGameTime;
        if (foodsChanged) {
            memorySnapshot = List.copyOf(shown);
            foods = shown.stream().map(RecentFoodsCard::display).toList();
        }
        if (!foodsChanged && !timeChanged) return;
        invalidateRender();
    }

    int visibleFoodCount() {
        return foods.size();
    }

    @Override
    protected void renderComponent(UIRenderContext context) {
        DashboardRender.card(context.graphics(), bounds(), bounds().contains(context.mouseX(), context.mouseY()),
            theme().color(ColorToken.ACCENT_PRIMARY));
        DashboardRender.drawTrimmed(context.graphics(), context.font(),
            Component.translatable("screen.innutrient.dashboard.recent_foods"), bounds().x() + 10,
            bounds().y() + 8, bounds().width() - 20, theme().color(ColorToken.TEXT_SECONDARY), true);
        context.graphics().fill(bounds().x() + 9, bounds().y() + 24, bounds().right() - 8,
            bounds().y() + 25, DashboardRender.DIVIDER);
        if (foods.isEmpty()) {
            DashboardRender.drawTrimmed(context.graphics(), context.font(),
                Component.translatable("screen.innutrient.dashboard.no_recent_foods"), bounds().x() + 10,
                bounds().y() + 34, bounds().width() - 20, theme().color(ColorToken.TEXT_MUTED), false);
            return;
        }
        int rowY = bounds().y() + 29;
        for (DisplayFood food : foods) {
            boolean hovered = rowBounds(rowY).contains(context.mouseX(), context.mouseY());
            if (hovered) context.graphics().fill(bounds().x() + 5, rowY - 2, bounds().right() - 5,
                rowY + 23, DashboardRender.CARD_HOVER);
            context.graphics().item(food.stack(), bounds().x() + 8, rowY + 1);
            int textX = bounds().x() + 29;
            int timeWidth = Math.max(28, bounds().width() / 5);
            DashboardRender.drawTrimmed(context.graphics(), context.font(), food.name(), textX, rowY,
                Math.max(10, bounds().right() - textX - timeWidth - 7),
                theme().color(ColorToken.TEXT_PRIMARY), false);
            Component relative = relativeTime(food.entry().gameTime());
            context.graphics().text(context.font(), relative,
                bounds().right() - context.font().width(relative) - 8, rowY,
                theme().color(ColorToken.TEXT_MUTED), false);
            DashboardRender.drawTrimmed(context.graphics(), context.font(),
                Component.translatable(food.entry().mealQuality().translationKey()), textX, rowY + 12,
                bounds().right() - textX - 8, theme().color(ColorToken.TEXT_MUTED), false);
            rowY += 27;
        }
    }

    @Override
    public List<Component> tooltipAt(double mouseX, double mouseY) {
        if (!bounds().contains(mouseX, mouseY) || foods.isEmpty()) return List.of();
        double localY = mouseY - (bounds().y() + 27);
        if (localY < 0) return List.of();
        int index = (int) (localY / 27);
        if (index < 0 || index >= foods.size()) return List.of();
        DisplayFood food = foods.get(index);
        return List.of(
            food.name(),
            Component.translatable(food.entry().mealQuality().translationKey()),
            Component.translatable("screen.innutrient.dashboard.recent.groups", food.entry().nutrientGroups().size()),
            Component.translatable("screen.innutrient.dashboard.recent.time", relativeTime(food.entry().gameTime()))
        );
    }

    private dev.uapi.client.ui.core.UIBounds rowBounds(int rowY) {
        return new dev.uapi.client.ui.core.UIBounds(bounds().x() + 5, rowY - 2,
            Math.max(0, bounds().width() - 10), 25);
    }

    private Component relativeTime(long eatenAt) {
        long seconds = Math.max(0, gameTime - eatenAt) / 20;
        if (seconds < 10) return Component.translatable("screen.innutrient.dashboard.time.now");
        if (seconds < 60) return Component.translatable("screen.innutrient.dashboard.time.seconds", seconds);
        long minutes = seconds / 60;
        if (minutes < 60) return Component.translatable("screen.innutrient.dashboard.time.minutes", minutes);
        return Component.translatable("screen.innutrient.dashboard.time.hours", minutes / 60);
    }

    private static DisplayFood display(DietMemoryEntry entry) {
        var item = BuiltInRegistries.ITEM.getOptional(entry.foodId()).orElse(Items.BARRIER);
        ItemStack stack = new ItemStack(item);
        Component name = item == Items.BARRIER ? Component.literal(entry.foodId().toString()) : stack.getHoverName();
        return new DisplayFood(entry, stack, name);
    }

    private record DisplayFood(DietMemoryEntry entry, ItemStack stack, Component name) {}
}
