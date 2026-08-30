package master.innutrient.player;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import master.innutrient.nutrition.DietQuality;
import master.innutrient.nutrition.NutrientGroup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Versioned, immutable player attachment. Version 2 adds sustained diet quality and a bounded
 * repeated-food streak while preserving unknown nutrient IDs for datapack rollback.
 */
public record NutritionState(
    int dataVersion,
    Map<Identifier, Double> levels,
    DietQuality dietQuality,
    DietQuality candidateDietQuality,
    long dietQualitySince,
    long candidateSince,
    Identifier lastFood,
    int repeatCount,
    long lastFoodGameTime
) {
    public static final int DATA_VERSION = 2;
    private static final Codec<Map<Identifier, Double>> LEVELS_CODEC =
        Codec.unboundedMap(Identifier.CODEC, Codec.DOUBLE);
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
        Identifier.CODEC.optionalFieldOf("last_food").forGetter(state -> Optional.ofNullable(state.lastFood())),
        Codec.INT.optionalFieldOf("repeat_count", 0).forGetter(NutritionState::repeatCount),
        Codec.LONG.optionalFieldOf("last_food_game_time", 0L).forGetter(NutritionState::lastFoodGameTime)
    ).apply(instance, (version, levels, quality, candidate, qualitySince, candidateSince, lastFood,
                       repeats, lastFoodTime) -> new NutritionState(version, levels, quality, candidate,
        qualitySince, candidateSince, lastFood.orElse(null), repeats, lastFoodTime)));

    public static final StreamCodec<RegistryFriendlyByteBuf, NutritionState> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public NutritionState decode(RegistryFriendlyByteBuf buffer) {
            int version = buffer.readVarInt();
            int size = Math.min(4096, Math.max(0, buffer.readVarInt()));
            Map<Identifier, Double> values = new LinkedHashMap<>();
            for (int index = 0; index < size; index++)
                values.put(buffer.readIdentifier(), NutrientGroup.clamp(buffer.readDouble()));
            DietQuality quality = quality(buffer.readUnsignedByte());
            DietQuality candidate = quality(buffer.readUnsignedByte());
            long qualitySince = buffer.readVarLong();
            long candidateSince = buffer.readVarLong();
            Identifier lastFood = buffer.readBoolean() ? buffer.readIdentifier() : null;
            int repeats = buffer.readVarInt();
            long lastFoodTime = buffer.readVarLong();
            return new NutritionState(version, values, quality, candidate, qualitySince, candidateSince,
                lastFood, repeats, lastFoodTime);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, NutritionState state) {
            buffer.writeVarInt(state.dataVersion());
            buffer.writeVarInt(state.levels().size());
            state.levels().forEach((id, value) -> {
                buffer.writeIdentifier(id);
                buffer.writeDouble(value);
            });
            buffer.writeByte(state.dietQuality().ordinal());
            buffer.writeByte(state.candidateDietQuality().ordinal());
            buffer.writeVarLong(state.dietQualitySince());
            buffer.writeVarLong(state.candidateSince());
            buffer.writeBoolean(state.lastFood() != null);
            if (state.lastFood() != null) buffer.writeIdentifier(state.lastFood());
            buffer.writeVarInt(state.repeatCount());
            buffer.writeVarLong(state.lastFoodGameTime());
        }

        private DietQuality quality(int ordinal) {
            DietQuality[] values = DietQuality.values();
            return ordinal >= 0 && ordinal < values.length ? values[ordinal] : DietQuality.STABLE;
        }
    };

    public NutritionState {
        Map<Identifier, Double> sanitized = new LinkedHashMap<>();
        if (levels != null) levels.forEach((id, value) -> {
            if (id != null && value != null && Double.isFinite(value)) sanitized.put(id, NutrientGroup.clamp(value));
        });
        levels = Collections.unmodifiableMap(sanitized);
        dietQuality = dietQuality == null ? DietQuality.STABLE : dietQuality;
        candidateDietQuality = candidateDietQuality == null ? dietQuality : candidateDietQuality;
        dietQualitySince = Math.max(0, dietQualitySince);
        candidateSince = Math.max(0, candidateSince);
        repeatCount = Math.min(10_000, Math.max(0, repeatCount));
        lastFoodGameTime = Math.max(0, lastFoodGameTime);
    }

    /** Compatibility constructor for callers compiled against the version-1 state shape. */
    public NutritionState(int dataVersion, Map<Identifier, Double> levels) {
        this(dataVersion, levels, DietQuality.STABLE, DietQuality.STABLE, 0, 0, null, 0, 0);
    }

    public static NutritionState empty() {
        return new NutritionState(DATA_VERSION, Map.of());
    }

    public double get(NutrientGroup group) {
        return levels.getOrDefault(group.id(), group.defaultLevel());
    }

    public NutritionState set(NutrientGroup group, double value) {
        Map<Identifier, Double> changed = new LinkedHashMap<>(levels);
        changed.put(group.id(), NutrientGroup.clamp(value));
        return copy(changed, dietQuality, candidateDietQuality, dietQualitySince, candidateSince,
            lastFood, repeatCount, lastFoodGameTime);
    }

    public NutritionState add(NutrientGroup group, double amount) {
        if (!Double.isFinite(amount)) return this;
        return set(group, get(group) + amount);
    }

    public NutritionState withDietQuality(DietQuality quality, DietQuality candidate, long qualitySince,
                                          long candidateSince) {
        return copy(levels, quality, candidate, qualitySince, candidateSince,
            lastFood, repeatCount, lastFoodGameTime);
    }

    public NutritionState withFoodStreak(Identifier food, int repeats, long gameTime) {
        return copy(levels, dietQuality, candidateDietQuality, dietQualitySince, candidateSince,
            food, repeats, gameTime);
    }

    public NutritionState reconcile(List<NutrientGroup> groups) {
        Map<Identifier, Double> changed = new LinkedHashMap<>(levels);
        boolean dirty = dataVersion != DATA_VERSION;
        for (NutrientGroup group : groups) {
            if (!changed.containsKey(group.id())) {
                changed.put(group.id(), group.defaultLevel());
                dirty = true;
            }
        }
        return dirty ? copy(changed, dietQuality, candidateDietQuality, dietQualitySince, candidateSince,
            lastFood, repeatCount, lastFoodGameTime) : this;
    }

    public NutritionState reset(List<NutrientGroup> groups) {
        Map<Identifier, Double> reset = new LinkedHashMap<>(levels);
        for (NutrientGroup group : groups) reset.put(group.id(), group.defaultLevel());
        return new NutritionState(DATA_VERSION, reset);
    }

    private NutritionState copy(Map<Identifier, Double> changed, DietQuality quality, DietQuality candidate,
                                long qualitySince, long candidateSince, Identifier food, int repeats,
                                long foodTime) {
        return new NutritionState(DATA_VERSION, changed, quality, candidate, qualitySince, candidateSince,
            food, repeats, foodTime);
    }
}
