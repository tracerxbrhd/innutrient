package master.innutrient;

import com.mojang.logging.LogUtils;
import dev.uapi.api.diagnostics.DiagnosticRegistration;
import dev.uapi.api.diagnostics.UApiDiagnostics;
import dev.uapi.api.services.ServiceRegistration;
import dev.uapi.api.services.ServiceScope;
import dev.uapi.api.services.UApiServices;
import master.innutrient.api.DefaultNutritionApi;
import master.innutrient.api.NutritionApi;
import master.innutrient.command.InnutrientCommands;
import master.innutrient.config.InnutrientClientConfig;
import master.innutrient.config.InnutrientServerConfig;
import master.innutrient.integration.farmersdelight.FarmersDelightIntegration;
import master.innutrient.network.NutritionNetwork;
import master.innutrient.nutrition.NutritionEvents;
import master.innutrient.nutrition.resolver.NutritionResolver;
import master.innutrient.player.NutritionAttachments;
import master.innutrient.registry.NutritionRegistry;
import master.innutrient.registry.NutritionReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(Innutrient.MOD_ID)
public final class Innutrient {
    public static final String MOD_ID = "innutrient";
    public static final Logger LOGGER = LogUtils.getLogger();
    private static ServiceRegistration apiRegistration;
    private static DiagnosticRegistration groupGauge;
    private static DiagnosticRegistration cacheGauge;
    private static DiagnosticRegistration unresolvedGauge;
    private static DiagnosticRegistration rebuildGauge;

    public Innutrient(IEventBus modBus, ModContainer container) {
        NutritionAttachments.register(modBus);
        modBus.addListener(NutritionNetwork::register);
        modBus.addListener(this::commonSetup);
        container.registerConfig(ModConfig.Type.SERVER, InnutrientServerConfig.SPEC,
            "uapi/innutrient/server.toml");
        container.registerConfig(ModConfig.Type.CLIENT, InnutrientClientConfig.SPEC,
            "uapi/innutrient/client.toml");

        InnutrientCommands.bootstrap();
        NeoForge.EVENT_BUS.register(InnutrientCommands.class);
        NeoForge.EVENT_BUS.register(NutritionEvents.class);
        NeoForge.EVENT_BUS.register(NutritionReloadListener.class);

        apiRegistration = UApiServices.register(NutritionApi.class, new DefaultNutritionApi(), ServiceScope.GLOBAL);
        groupGauge = UApiDiagnostics.registerGauge(id("nutrient_groups"), () -> NutritionRegistry.groups().size());
        cacheGauge = UApiDiagnostics.registerGauge(id("profile_cache"), () -> NutritionResolver.INSTANCE.cacheSize());
        unresolvedGauge = UApiDiagnostics.registerGauge(id("unresolved_foods"),
            () -> NutritionResolver.INSTANCE.unresolvedEdibleItems());
        rebuildGauge = UApiDiagnostics.registerGauge(id("last_rebuild_ms"),
            () -> NutritionResolver.INSTANCE.lastRebuildMillis());
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(FarmersDelightIntegration::report);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
