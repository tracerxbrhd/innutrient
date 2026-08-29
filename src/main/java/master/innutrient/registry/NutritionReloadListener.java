package master.innutrient.registry;

import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

public final class NutritionReloadListener {
    private NutritionReloadListener() {}

    @SubscribeEvent
    public static void addListener(AddReloadListenerEvent event) {
        event.addListener((ResourceManagerReloadListener) NutritionRegistry::reload);
    }
}
