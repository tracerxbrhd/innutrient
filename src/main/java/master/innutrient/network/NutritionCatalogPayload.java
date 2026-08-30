package master.innutrient.network;

import master.innutrient.Innutrient;
import master.innutrient.nutrition.NutrientGroup;
import master.innutrient.nutrition.NutritionProfile;
import master.innutrient.nutrition.NutritionProfileSource;
import master.innutrient.nutrition.MealQuality;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record NutritionCatalogPayload(List<NutrientGroup> groups,
                                      Map<ResourceLocation, NutritionFoodData> foods)
    implements CustomPacketPayload {
    public static final Type<NutritionCatalogPayload> TYPE = new Type<>(Innutrient.id("catalog"));
    public static final StreamCodec<RegistryFriendlyByteBuf, NutritionCatalogPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public NutritionCatalogPayload decode(RegistryFriendlyByteBuf buffer) {
            int groupCount = Math.min(1024, Math.max(0, buffer.readVarInt()));
            List<NutrientGroup> groups = new ArrayList<>(groupCount);
            for (int index = 0; index < groupCount; index++) groups.add(readGroup(buffer));
            int profileCount = Math.min(65536, Math.max(0, buffer.readVarInt()));
            Map<ResourceLocation, NutritionFoodData> foods = new LinkedHashMap<>();
            for (int index = 0; index < profileCount; index++) {
                ResourceLocation item = buffer.readResourceLocation();
                NutritionProfile profile = readProfile(buffer);
                double baseGain = buffer.readDouble();
                int qualityOrdinal = buffer.readUnsignedByte();
                MealQuality[] qualities = MealQuality.values();
                MealQuality quality = qualityOrdinal < qualities.length ? qualities[qualityOrdinal] : MealQuality.BASIC;
                foods.put(item, new NutritionFoodData(profile, baseGain, quality, buffer.readDouble()));
            }
            return new NutritionCatalogPayload(List.copyOf(groups), Map.copyOf(foods));
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, NutritionCatalogPayload payload) {
            buffer.writeVarInt(payload.groups().size());
            payload.groups().forEach(group -> writeGroup(buffer, group));
            buffer.writeVarInt(payload.foods().size());
            payload.foods().forEach((item, food) -> {
                buffer.writeResourceLocation(item);
                writeProfile(buffer, food.profile());
                buffer.writeDouble(food.baseGain());
                buffer.writeByte(food.mealQuality().ordinal());
                buffer.writeDouble(food.mealMultiplier());
            });
        }
    };

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    private static NutrientGroup readGroup(RegistryFriendlyByteBuf buffer) {
        return new NutrientGroup(buffer.readResourceLocation(), buffer.readUtf(256),
            buffer.readResourceLocation(), buffer.readResourceLocation(), buffer.readInt(), buffer.readVarInt(),
            buffer.readDouble(), buffer.readDouble(), buffer.readDouble(), buffer.readDouble(), buffer.readDouble(),
            buffer.readDouble(), buffer.readDouble(), buffer.readBoolean(), buffer.readBoolean(), buffer.readBoolean());
    }

    private static void writeGroup(RegistryFriendlyByteBuf buffer, NutrientGroup group) {
        buffer.writeResourceLocation(group.id());
        buffer.writeUtf(group.translationKey(), 256);
        buffer.writeResourceLocation(group.icon());
        buffer.writeResourceLocation(group.itemTag());
        buffer.writeInt(group.color());
        buffer.writeVarInt(group.order());
        buffer.writeDouble(group.defaultLevel());
        buffer.writeDouble(group.healthyMin());
        buffer.writeDouble(group.healthyMax());
        buffer.writeDouble(group.lowThreshold());
        buffer.writeDouble(group.highThreshold());
        buffer.writeDouble(group.gainMultiplier());
        buffer.writeDouble(group.decayMultiplier());
        buffer.writeBoolean(group.penalizeLow());
        buffer.writeBoolean(group.penalizeHigh());
        buffer.writeBoolean(group.requiredForBalance());
    }

    private static NutritionProfile readProfile(RegistryFriendlyByteBuf buffer) {
        int size = Math.min(1024, Math.max(0, buffer.readVarInt()));
        Map<ResourceLocation, Double> nutrients = new LinkedHashMap<>();
        for (int index = 0; index < size; index++) nutrients.put(buffer.readResourceLocation(), buffer.readDouble());
        int ordinal = buffer.readUnsignedByte();
        NutritionProfileSource[] sources = NutritionProfileSource.values();
        NutritionProfileSource source = ordinal < sources.length ? sources[ordinal] : NutritionProfileSource.UNKNOWN;
        ResourceLocation recipe = buffer.readBoolean() ? buffer.readResourceLocation() : null;
        return new NutritionProfile(nutrients, source, recipe, buffer.readVarInt());
    }

    private static void writeProfile(RegistryFriendlyByteBuf buffer, NutritionProfile profile) {
        buffer.writeVarInt(profile.nutrients().size());
        profile.nutrients().forEach((id, value) -> {
            buffer.writeResourceLocation(id);
            buffer.writeDouble(value);
        });
        buffer.writeByte(profile.source().ordinal());
        buffer.writeBoolean(profile.recipeId() != null);
        if (profile.recipeId() != null) buffer.writeResourceLocation(profile.recipeId());
        buffer.writeVarInt(profile.resolutionDepth());
    }
}
