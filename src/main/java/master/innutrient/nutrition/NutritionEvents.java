package master.innutrient.nutrition;

import master.innutrient.config.InnutrientServerConfig;
import master.innutrient.network.NutritionNetwork;
import master.innutrient.nutrition.resolver.NutritionResolver;
import master.innutrient.player.NutritionAttachments;
import master.innutrient.player.NutritionState;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodProperties;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class NutritionEvents {
    private static final Map<UUID, Integer> LAST_HUNGER = new ConcurrentHashMap<>();

    private NutritionEvents() {}

    @SubscribeEvent
    public static void onFoodFinished(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        FoodProperties food = event.getItem().getFoodProperties(player);
        if (food == null) return;
        NutritionProfile profile = NutritionResolver.INSTANCE.resolve(event.getItem());
        NutritionService.consume(player, profile, food);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        InnutrientServerConfig.DecayMode mode = InnutrientServerConfig.decayMode();
        int currentFood = player.getFoodData().getFoodLevel();
        Integer previousFood = LAST_HUNGER.put(player.getUUID(), currentFood);
        if ((mode == InnutrientServerConfig.DecayMode.HUNGER || mode == InnutrientServerConfig.DecayMode.HYBRID)
            && previousFood != null && previousFood > currentFood) {
            NutritionService.decay(player, (previousFood - currentFood) * InnutrientServerConfig.DECAY_PER_HUNGER.get());
        }
        if ((mode == InnutrientServerConfig.DecayMode.PERIODIC || mode == InnutrientServerConfig.DecayMode.HYBRID)
            && player.tickCount > 0 && player.tickCount % InnutrientServerConfig.PERIODIC_INTERVAL.get() == 0) {
            NutritionService.decay(player, InnutrientServerConfig.PERIODIC_AMOUNT.get());
        }
        if (player.tickCount % 200 == 0)
            NutritionEffectsManager.evaluate(player, NutritionService.get(player));
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        NutritionState original = event.getOriginal().getData(NutritionAttachments.STATE);
        if (!event.isWasDeath()) {
            player.setData(NutritionAttachments.STATE, original);
            return;
        }
        double retention = InnutrientServerConfig.DEATH_RETENTION.get() / 100.0;
        NutritionState changed = NutritionState.empty();
        for (NutrientGroup group : master.innutrient.registry.NutritionRegistry.groups()) {
            double retained = original.get(group) * retention;
            changed = changed.set(group, retained);
        }
        player.setData(NutritionAttachments.STATE, changed);
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        NutritionService.get(player);
        NutritionNetwork.syncCatalog(player);
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            NutritionService.get(player);
            NutritionNetwork.syncCatalog(player);
        }
    }

    @SubscribeEvent
    public static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) NutritionNetwork.syncCatalog(player);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        LAST_HUNGER.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        if (event.getPlayer() == null || NutritionResolver.INSTANCE.edibleProfiles().isEmpty())
            NutritionResolver.INSTANCE.rebuild(event.getPlayerList().getServer());
        event.getRelevantPlayers().forEach(player -> {
            NutritionService.get(player);
            NutritionNetwork.syncCatalog(player);
        });
    }
}
