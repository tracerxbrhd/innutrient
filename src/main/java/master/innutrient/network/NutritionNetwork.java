package master.innutrient.network;

import master.innutrient.client.ClientNutritionCatalog;
import master.innutrient.nutrition.resolver.NutritionResolver;
import master.innutrient.registry.NutritionRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class NutritionNetwork {
    public static final String PROTOCOL_VERSION = "3";
    private NutritionNetwork() {}

    public static void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar(PROTOCOL_VERSION);
        registrar.playToServer(NutritionRequestPayload.TYPE, NutritionRequestPayload.STREAM_CODEC,
            (payload, context) -> context.enqueueWork(() -> {
                if (context.player() instanceof ServerPlayer player) syncCatalog(player);
            }));
        if (FMLEnvironment.dist == Dist.CLIENT) {
            registrar.playToClient(NutritionCatalogPayload.TYPE, NutritionCatalogPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> ClientNutritionCatalog.replace(payload)));
        } else {
            registrar.playToClient(NutritionCatalogPayload.TYPE, NutritionCatalogPayload.STREAM_CODEC,
                (payload, context) -> {});
        }
    }

    public static void syncCatalog(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player,
            new NutritionCatalogPayload(NutritionRegistry.groups(), NutritionResolver.INSTANCE.edibleFoodData()));
    }
}
