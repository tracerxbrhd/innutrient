package master.innutrient.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.uapi.client.UApiScreenTabs;
import master.innutrient.Innutrient;
import master.innutrient.client.gui.NutritionScreen;
import master.innutrient.config.InnutrientClientConfig;
import master.innutrient.nutrition.NutritionProfileSource;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import org.lwjgl.glfw.GLFW;

import java.util.Locale;

public final class InnutrientClientEvents {
    public static final KeyMapping OPEN = new KeyMapping("key.innutrient.open", InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_N, "key.categories.innutrient");

    private InnutrientClientEvents() {}

    @EventBusSubscriber(modid = Innutrient.MOD_ID, value = Dist.CLIENT)
    public static final class ModBus {
        @SubscribeEvent
        public static void registerKeys(RegisterKeyMappingsEvent event) {
            event.register(OPEN);
            UApiScreenTabs.register(Innutrient.id("nutrition"), 150,
                Component.translatable("button.innutrient.nutrition"), () -> new ItemStack(Items.APPLE),
                minecraft -> new NutritionScreen());
        }
    }

    @EventBusSubscriber(modid = Innutrient.MOD_ID, value = Dist.CLIENT)
    public static final class GameBus {
        @SubscribeEvent
        public static void tick(ClientTickEvent.Post event) {
            while (OPEN.consumeClick()) Minecraft.getInstance().setScreen(new NutritionScreen());
        }

        @SubscribeEvent
        public static void tooltip(ItemTooltipEvent event) {
            if (!InnutrientClientConfig.SHOW_TOOLTIPS.get()) return;
            var food = ClientNutritionCatalog.food(event.getItemStack());
            var profile = food.profile();
            if (!profile.resolved()) return;
            event.getToolTip().add(Component.translatable("tooltip.innutrient.header"));
            profile.nutrients().forEach((id, weight) -> ClientNutritionCatalog.groups().stream()
                .filter(group -> group.id().equals(id)).findFirst().ifPresent(group -> event.getToolTip().add(
                    Component.translatable("tooltip.innutrient.entry", Component.translatable(group.translationKey()),
                        String.format(Locale.ROOT, "+%.1f", food.baseGain() * food.mealMultiplier()
                            * weight * group.gainMultiplier())))));
            if (food.mealQuality().minimumGroups() >= 2) {
                event.getToolTip().add(Component.empty());
                event.getToolTip().add(Component.translatable(food.mealQuality().translationKey()));
                event.getToolTip().add(Component.translatable("tooltip.innutrient.efficiency",
                    String.format(Locale.ROOT, "+%.0f%%", (food.mealMultiplier() - 1.0) * 100)));
            }
            boolean advanced = !InnutrientClientConfig.ADVANCED_ON_SHIFT.get() || Screen.hasShiftDown();
            if (advanced) {
                String sourceKey = switch (profile.source()) {
                    case EXPLICIT -> "tooltip.innutrient.source.explicit";
                    case DIRECT_TAGS -> "tooltip.innutrient.source.tags";
                    case RECIPE_DERIVED -> "tooltip.innutrient.source.recipe";
                    case UNKNOWN -> "tooltip.innutrient.source.unknown";
                };
                event.getToolTip().add(Component.translatable(sourceKey));
                if (profile.source() == NutritionProfileSource.RECIPE_DERIVED)
                    event.getToolTip().add(Component.translatable("tooltip.innutrient.recipe_depth",
                        profile.resolutionDepth()));
            } else if (InnutrientClientConfig.ADVANCED_ON_SHIFT.get()) {
                event.getToolTip().add(Component.translatable("tooltip.innutrient.shift"));
            }
        }
    }
}
