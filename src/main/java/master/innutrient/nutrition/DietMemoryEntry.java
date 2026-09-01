package master.innutrient.nutrition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Compact, immutable description of one food consumption stored in bounded Diet Memory. */
public record DietMemoryEntry(
    Identifier foodId,
    long gameTime,
    MealQuality mealQuality,
    List<Identifier> nutrientGroups,
    long compositionFingerprint
) {
    public static final int MAX_GROUPS = 32;
    private static final Codec<MealQuality> QUALITY_CODEC = Codec.STRING.xmap(
        MealQuality::fromSerializedName, MealQuality::serializedName);

    public static final Codec<DietMemoryEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Identifier.CODEC.fieldOf("item").forGetter(DietMemoryEntry::foodId),
        Codec.LONG.optionalFieldOf("game_time", 0L).forGetter(DietMemoryEntry::gameTime),
        QUALITY_CODEC.optionalFieldOf("meal_quality", MealQuality.BASIC).forGetter(DietMemoryEntry::mealQuality),
        Identifier.CODEC.listOf().optionalFieldOf("nutrient_groups", List.of())
            .forGetter(DietMemoryEntry::nutrientGroups),
        Codec.LONG.optionalFieldOf("composition", 0L).forGetter(DietMemoryEntry::compositionFingerprint)
    ).apply(instance, DietMemoryEntry::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, DietMemoryEntry> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public DietMemoryEntry decode(RegistryFriendlyByteBuf buffer) {
            Identifier food = buffer.readIdentifier();
            long gameTime = buffer.readVarLong();
            int ordinal = buffer.readUnsignedByte();
            MealQuality[] qualities = MealQuality.values();
            MealQuality quality = ordinal < qualities.length ? qualities[ordinal] : MealQuality.BASIC;
            int encodedCount = Math.max(0, buffer.readVarInt());
            List<Identifier> groups = new ArrayList<>(Math.min(MAX_GROUPS, encodedCount));
            for (int index = 0; index < encodedCount; index++) {
                Identifier group = buffer.readIdentifier();
                if (index < MAX_GROUPS) groups.add(group);
            }
            return new DietMemoryEntry(food, gameTime, quality, groups, buffer.readLong());
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, DietMemoryEntry entry) {
            buffer.writeIdentifier(entry.foodId());
            buffer.writeVarLong(entry.gameTime());
            buffer.writeByte(entry.mealQuality().ordinal());
            buffer.writeVarInt(entry.nutrientGroups().size());
            entry.nutrientGroups().forEach(buffer::writeIdentifier);
            buffer.writeLong(entry.compositionFingerprint());
        }
    };

    public DietMemoryEntry {
        Objects.requireNonNull(foodId, "foodId");
        gameTime = Math.max(0, gameTime);
        mealQuality = mealQuality == null ? MealQuality.BASIC : mealQuality;
        LinkedHashSet<Identifier> sanitized = new LinkedHashSet<>();
        if (nutrientGroups != null) nutrientGroups.stream().filter(Objects::nonNull)
            .sorted(Comparator.comparing(Identifier::toString)).limit(MAX_GROUPS).forEach(sanitized::add);
        nutrientGroups = List.copyOf(sanitized);
        if (compositionFingerprint == 0) compositionFingerprint = fingerprint(foodId, Map.of());
    }

    public static DietMemoryEntry from(Identifier foodId, long gameTime, NutritionProfile profile,
                                       MealQuality mealQuality, double minimumGroupShare) {
        Map<Identifier, Double> nutrients = profile == null ? Map.of() : profile.nutrients();
        double threshold = Double.isFinite(minimumGroupShare)
            ? Math.max(0, Math.min(1, minimumGroupShare)) : 0;
        List<Identifier> groups = nutrients.entrySet().stream()
            .filter(entry -> entry.getValue() != null && Double.isFinite(entry.getValue())
                && entry.getValue() >= threshold)
            .map(Map.Entry::getKey)
            .sorted(Comparator.comparing(Identifier::toString))
            .limit(MAX_GROUPS)
            .toList();
        return new DietMemoryEntry(foodId, gameTime, mealQuality, groups, fingerprint(foodId, nutrients));
    }

    /** FNV-1a over sorted IDs and 5%-quantized normalized shares. */
    static long fingerprint(Identifier fallbackFood, Map<Identifier, Double> nutrients) {
        long hash = 0xcbf29ce484222325L;
        boolean added = false;
        for (Map.Entry<Identifier, Double> entry : nutrients.entrySet().stream()
            .filter(entry -> entry.getKey() != null && entry.getValue() != null
                && Double.isFinite(entry.getValue()) && entry.getValue() > 0)
            .sorted(Map.Entry.comparingByKey(Comparator.comparing(Identifier::toString))).toList()) {
            added = true;
            hash = fnv(hash, entry.getKey().toString());
            int bucket = Math.max(1, Math.min(20, (int) Math.round(entry.getValue() * 20.0)));
            hash ^= bucket;
            hash *= 0x100000001b3L;
        }
        return added ? hash : fnv(hash, fallbackFood.toString());
    }

    private static long fnv(long hash, String value) {
        for (byte character : value.getBytes(StandardCharsets.UTF_8)) {
            hash ^= character & 0xff;
            hash *= 0x100000001b3L;
        }
        return hash;
    }
}
