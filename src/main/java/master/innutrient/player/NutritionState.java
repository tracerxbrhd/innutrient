package master.innutrient.player;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import master.innutrient.nutrition.NutrientGroup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

import java.util.LinkedHashMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/** Versioned player attachment. Removed group IDs remain harmlessly preserved for datapack rollback. */
public record NutritionState(int dataVersion, Map<Identifier, Double> levels) {
    public static final int DATA_VERSION = 1;
    private static final Codec<Map<Identifier, Double>> LEVELS_CODEC =
        Codec.unboundedMap(Identifier.CODEC, Codec.DOUBLE);
    public static final Codec<NutritionState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.INT.optionalFieldOf("data_version", DATA_VERSION).forGetter(NutritionState::dataVersion),
        LEVELS_CODEC.optionalFieldOf("levels", Map.of()).forGetter(NutritionState::levels)
    ).apply(instance, NutritionState::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, NutritionState> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public NutritionState decode(RegistryFriendlyByteBuf buffer) {
            int version = buffer.readVarInt();
            int size = Math.min(4096, Math.max(0, buffer.readVarInt()));
            Map<Identifier, Double> values = new LinkedHashMap<>();
            for (int index = 0; index < size; index++)
                values.put(buffer.readIdentifier(), NutrientGroup.clamp(buffer.readDouble()));
            return new NutritionState(version, values);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, NutritionState state) {
            buffer.writeVarInt(state.dataVersion());
            buffer.writeVarInt(state.levels().size());
            state.levels().forEach((id, value) -> {
                buffer.writeIdentifier(id);
                buffer.writeDouble(value);
            });
        }
    };

    public NutritionState {
        Map<Identifier, Double> sanitized = new LinkedHashMap<>();
        if (levels != null) levels.forEach((id, value) -> {
            if (id != null && value != null && Double.isFinite(value)) sanitized.put(id, NutrientGroup.clamp(value));
        });
        levels = Collections.unmodifiableMap(sanitized);
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
        return new NutritionState(DATA_VERSION, changed);
    }

    public NutritionState add(NutrientGroup group, double amount) {
        if (!Double.isFinite(amount)) return this;
        return set(group, get(group) + amount);
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
        return dirty ? new NutritionState(DATA_VERSION, changed) : this;
    }

    public NutritionState reset(List<NutrientGroup> groups) {
        Map<Identifier, Double> reset = new LinkedHashMap<>(levels);
        for (NutrientGroup group : groups) reset.put(group.id(), group.defaultLevel());
        return new NutritionState(DATA_VERSION, reset);
    }
}
