package master.innutrient.player;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import master.innutrient.nutrition.DietMemoryEntry;
import master.innutrient.nutrition.DietQuality;
import master.innutrient.nutrition.FoodVariety;
import master.innutrient.nutrition.MealQuality;
import master.innutrient.nutrition.NutrientGroup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Versioned immutable player attachment. Version 3 replaces the version-2 repeated-food streak
 * with bounded Diet Memory while retaining compatible legacy codec fields for save migration.
 */
public record NutritionState(
    int dataVersion,
    Map<ResourceLocation, Double> levels,
    DietQuality dietQuality,
    DietQuality candidateDietQuality,
    long dietQualitySince,
    long candidateSince,
    List<DietMemoryEntry> dietMemory
) {
    public static final int DATA_VERSION = 3;
    public static final int DEFAULT_MEMORY_CAPACITY = 16;
    private static final Codec<Map<ResourceLocation, Double>> LEVELS_CODEC =
        Codec.unboundedMap(ResourceLocation.CODEC, Codec.DOUBLE);
    private static final Codec<DietQuality> QUALITY_CODEC = Codec.STRING.xmap(
        DietQuality::fromSerializedName, DietQuality::serializedName);

    public static final Codec<NutritionState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.INT.optionalFieldOf("data_version", 1).forGetter(NutritionState::dataVersion),
        LEVELS_CODEC.optionalFieldOf("levels", Map.of()).forGetter(NutritionState::levels),
        QUALITY_CODEC.optionalFieldOf("diet_quality", DietQuality.STABLE).forGetter(NutritionState::dietQuality),
        QUALITY_CODEC.optionalFieldOf("candidate_diet_quality", DietQuality.STABLE)
            .forGetter(NutritionState::candidateDietQuality),
        Codec.LONG.optionalFieldOf("diet_quality_since", 0L).forGetter(NutritionState::dietQualitySince),
        Codec.LONG.optionalFieldOf("candidate_since", 0L).forGetter(NutritionState::candidateSince),
        DietMemoryEntry.CODEC.listOf().optionalFieldOf("recent_foods", List.of())
            .forGetter(NutritionState::dietMemory),
        ResourceLocation.CODEC.optionalFieldOf("last_food")
            .forGetter(state -> Optional.<ResourceLocation>empty()),
        Codec.INT.optionalFieldOf("repeat_count", 0).forGetter(state -> 0),
        Codec.LONG.optionalFieldOf("last_food_game_time", 0L).forGetter(state -> 0L)
    ).apply(instance, NutritionState::decode));

    public static final StreamCodec<RegistryFriendlyByteBuf, NutritionState> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public NutritionState decode(RegistryFriendlyByteBuf buffer) {
            int version = buffer.readVarInt();
            int size = Math.min(4096, Math.max(0, buffer.readVarInt()));
            Map<ResourceLocation, Double> values = new LinkedHashMap<>();
            for (int index = 0; index < size; index++)
                values.put(buffer.readResourceLocation(), NutrientGroup.clamp(buffer.readDouble()));
            DietQuality quality = quality(buffer.readUnsignedByte());
            DietQuality candidate = quality(buffer.readUnsignedByte());
            long qualitySince = buffer.readVarLong();
            long candidateSince = buffer.readVarLong();
            int encodedMemorySize = Math.max(0, buffer.readVarInt());
            int firstKeptIndex = Math.max(0, encodedMemorySize - FoodVariety.ABSOLUTE_MEMORY_CAPACITY);
            List<DietMemoryEntry> memory = new ArrayList<>(Math.min(encodedMemorySize,
                FoodVariety.ABSOLUTE_MEMORY_CAPACITY));
            for (int index = 0; index < encodedMemorySize; index++) {
                DietMemoryEntry entry = DietMemoryEntry.STREAM_CODEC.decode(buffer);
                if (index >= firstKeptIndex) memory.add(entry);
            }
            return new NutritionState(version, values, quality, candidate, qualitySince, candidateSince, memory);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, NutritionState state) {
            buffer.writeVarInt(state.dataVersion());
            buffer.writeVarInt(state.levels().size());
            state.levels().forEach((id, value) -> {
                buffer.writeResourceLocation(id);
                buffer.writeDouble(value);
            });
            buffer.writeByte(state.dietQuality().ordinal());
            buffer.writeByte(state.candidateDietQuality().ordinal());
            buffer.writeVarLong(state.dietQualitySince());
            buffer.writeVarLong(state.candidateSince());
            buffer.writeVarInt(state.dietMemory().size());
            state.dietMemory().forEach(entry -> DietMemoryEntry.STREAM_CODEC.encode(buffer, entry));
        }

        private DietQuality quality(int ordinal) {
            DietQuality[] values = DietQuality.values();
            return ordinal >= 0 && ordinal < values.length ? values[ordinal] : DietQuality.STABLE;
        }
    };

    public NutritionState {
        Map<ResourceLocation, Double> sanitized = new LinkedHashMap<>();
        if (levels != null) levels.forEach((id, value) -> {
            if (id != null && value != null && Double.isFinite(value)) sanitized.put(id, NutrientGroup.clamp(value));
        });
        levels = Collections.unmodifiableMap(sanitized);
        dietQuality = dietQuality == null ? DietQuality.STABLE : dietQuality;
        candidateDietQuality = candidateDietQuality == null ? dietQuality : candidateDietQuality;
        dietQualitySince = Math.max(0, dietQualitySince);
        candidateSince = Math.max(0, candidateSince);
        dietMemory = trimMemory(dietMemory, FoodVariety.ABSOLUTE_MEMORY_CAPACITY);
    }

    /** Compatibility constructor for callers compiled against the version-1 state shape. */
    public NutritionState(int dataVersion, Map<ResourceLocation, Double> levels) {
        this(dataVersion, levels, DietQuality.STABLE, DietQuality.STABLE, 0, 0, List.of());
    }

    /** Compatibility constructor and migration path for the version-2 state shape. */
    public NutritionState(int dataVersion, Map<ResourceLocation, Double> levels, DietQuality quality,
                          DietQuality candidate, long qualitySince, long candidateSince,
                          ResourceLocation lastFood, int repeatCount, long lastFoodGameTime) {
        this(dataVersion, levels, quality, candidate, qualitySince, candidateSince,
            migrateLegacyMemory(lastFood, repeatCount, lastFoodGameTime));
    }

    public static NutritionState empty() {
        return new NutritionState(DATA_VERSION, Map.of());
    }

    public double get(NutrientGroup group) {
        return levels.getOrDefault(group.id(), group.defaultLevel());
    }

    public NutritionState set(NutrientGroup group, double value) {
        Map<ResourceLocation, Double> changed = new LinkedHashMap<>(levels);
        changed.put(group.id(), NutrientGroup.clamp(value));
        return copy(changed, dietQuality, candidateDietQuality, dietQualitySince, candidateSince, dietMemory);
    }

    public NutritionState add(NutrientGroup group, double amount) {
        if (!Double.isFinite(amount)) return this;
        return set(group, get(group) + amount);
    }

    public NutritionState withDietQuality(DietQuality quality, DietQuality candidate, long qualitySince,
                                          long candidateSince) {
        return copy(levels, quality, candidate, qualitySince, candidateSince, dietMemory);
    }

    public NutritionState withDietMemory(List<DietMemoryEntry> memory) {
        return copy(levels, dietQuality, candidateDietQuality, dietQualitySince, candidateSince, memory);
    }

    /** @deprecated use {@link #withDietMemory(List)}; retained for source compatibility with 1.0 integrations. */
    @Deprecated(forRemoval = false)
    public NutritionState withFoodStreak(ResourceLocation food, int repeats, long gameTime) {
        return withDietMemory(migrateLegacyMemory(food, repeats, gameTime));
    }

    /** Derived compatibility accessor for the version-2 state shape. */
    public ResourceLocation lastFood() {
        return dietMemory.isEmpty() ? null : dietMemory.getLast().foodId();
    }

    /** Derived compatibility accessor; zero means the latest food has no consecutive repeat. */
    public int repeatCount() {
        if (dietMemory.isEmpty()) return 0;
        ResourceLocation latest = dietMemory.getLast().foodId();
        int count = 0;
        for (int index = dietMemory.size() - 2; index >= 0; index--) {
            if (!dietMemory.get(index).foodId().equals(latest)) break;
            count++;
        }
        return count;
    }

    /** Derived compatibility accessor for the version-2 state shape. */
    public long lastFoodGameTime() {
        return dietMemory.isEmpty() ? 0 : dietMemory.getLast().gameTime();
    }

    public NutritionState reconcile(List<NutrientGroup> groups) {
        return reconcile(groups, DEFAULT_MEMORY_CAPACITY);
    }

    public NutritionState reconcile(List<NutrientGroup> groups, int memoryCapacity) {
        Map<ResourceLocation, Double> changed = new LinkedHashMap<>(levels);
        boolean dirty = dataVersion != DATA_VERSION;
        for (NutrientGroup group : groups) {
            if (!changed.containsKey(group.id())) {
                changed.put(group.id(), group.defaultLevel());
                dirty = true;
            }
        }
        List<DietMemoryEntry> boundedMemory = trimMemory(dietMemory, memoryCapacity);
        if (!boundedMemory.equals(dietMemory)) dirty = true;
        return dirty ? copy(changed, dietQuality, candidateDietQuality, dietQualitySince, candidateSince,
            boundedMemory) : this;
    }

    public NutritionState reset(List<NutrientGroup> groups) {
        Map<ResourceLocation, Double> reset = new LinkedHashMap<>(levels);
        for (NutrientGroup group : groups) reset.put(group.id(), group.defaultLevel());
        return new NutritionState(DATA_VERSION, reset);
    }

    private NutritionState copy(Map<ResourceLocation, Double> changed, DietQuality quality, DietQuality candidate,
                                long qualitySince, long candidateSince, List<DietMemoryEntry> memory) {
        return new NutritionState(DATA_VERSION, changed, quality, candidate, qualitySince, candidateSince, memory);
    }

    private static NutritionState decode(int version, Map<ResourceLocation, Double> levels, DietQuality quality,
                                         DietQuality candidate, long qualitySince, long candidateSince,
                                         List<DietMemoryEntry> memory, Optional<ResourceLocation> lastFood,
                                         int repeats, long lastFoodTime) {
        List<DietMemoryEntry> migrated = memory == null || memory.isEmpty()
            ? migrateLegacyMemory(lastFood.orElse(null), repeats, lastFoodTime) : memory;
        return new NutritionState(version, levels, quality, candidate, qualitySince, candidateSince, migrated);
    }

    private static List<DietMemoryEntry> migrateLegacyMemory(ResourceLocation food, int repeats, long gameTime) {
        if (food == null) return List.of();
        int count = Math.min(DEFAULT_MEMORY_CAPACITY, Math.max(1, Math.max(0, repeats) + 1));
        DietMemoryEntry entry = new DietMemoryEntry(food, gameTime, MealQuality.BASIC, List.of(), 0);
        return java.util.Collections.nCopies(count, entry);
    }

    private static List<DietMemoryEntry> trimMemory(List<DietMemoryEntry> memory, int capacity) {
        if (memory == null || memory.isEmpty()) return List.of();
        int boundedCapacity = Math.max(1, Math.min(FoodVariety.ABSOLUTE_MEMORY_CAPACITY, capacity));
        List<DietMemoryEntry> sanitized = memory.stream().filter(java.util.Objects::nonNull).toList();
        return sanitized.size() <= boundedCapacity ? List.copyOf(sanitized)
            : List.copyOf(sanitized.subList(sanitized.size() - boundedCapacity, sanitized.size()));
    }
}
