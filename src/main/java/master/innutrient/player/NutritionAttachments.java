package master.innutrient.player;

import master.innutrient.Innutrient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class NutritionAttachments {
    private static final DeferredRegister<AttachmentType<?>> TYPES =
        DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Innutrient.MOD_ID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<NutritionState>> STATE = TYPES.register(
        "player_nutrition", () -> AttachmentType.builder(NutritionState::empty)
            .serialize(NutritionState.CODEC.fieldOf("data"))
            .sync((holder, player) -> holder == player, NutritionState.STREAM_CODEC)
            .build());

    private NutritionAttachments() {}

    public static void register(IEventBus bus) {
        TYPES.register(bus);
    }
}
