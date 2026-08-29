package master.innutrient.registry;

import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;

public final class NutritionReloadListener {
    private NutritionReloadListener() {}

    @SubscribeEvent
    public static void addListener(AddServerReloadListenersEvent event) {
        event.addListener(Identifier.fromNamespaceAndPath("innutrient", "nutrition"),
            (ResourceManagerReloadListener) NutritionRegistry::reload);
    }
}
