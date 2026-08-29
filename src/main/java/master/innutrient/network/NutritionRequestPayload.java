package master.innutrient.network;

import master.innutrient.Innutrient;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public final class NutritionRequestPayload implements CustomPacketPayload {
    public static final NutritionRequestPayload INSTANCE = new NutritionRequestPayload();
    public static final Type<NutritionRequestPayload> TYPE = new Type<>(Innutrient.id("request_catalog"));
    public static final StreamCodec<RegistryFriendlyByteBuf, NutritionRequestPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override public NutritionRequestPayload decode(RegistryFriendlyByteBuf buffer) { return INSTANCE; }
        @Override public void encode(RegistryFriendlyByteBuf buffer, NutritionRequestPayload value) {}
    };

    private NutritionRequestPayload() {}
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
