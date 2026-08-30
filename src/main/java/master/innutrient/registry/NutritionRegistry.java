package master.innutrient.registry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import master.innutrient.Innutrient;
import master.innutrient.nutrition.NutrientGroup;
import master.innutrient.nutrition.NutritionProfile;
import master.innutrient.nutrition.NutritionProfileSource;
import master.innutrient.nutrition.NutrientStatus;
import master.innutrient.nutrition.NutritionEffectsManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemStack;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/** Immutable snapshots loaded from the versioned Innutrient datapack folders. */
public final class NutritionRegistry {
    public static final int FORMAT_VERSION = 2;
    private static final String GROUP_ROOT = "innutrient/nutrient_groups";
    private static final String PROFILE_ROOT = "innutrient/food_profiles";
    private static final String EFFECT_ROOT = "innutrient/effect_rules";
    private static volatile Snapshot snapshot = new Snapshot(List.of(), Map.of(), List.of(), List.of());

    private NutritionRegistry() {}

    public static void reload(ResourceManager manager) {
        long started = System.nanoTime();
        Map<ResourceLocation, NutrientGroup> groups = loadGroups(manager);
        List<FoodProfileRule> profiles = loadProfiles(manager, groups);
        List<NutritionEffectRule> effects = loadEffects(manager, groups);
        List<NutrientGroup> ordered = groups.values().stream()
            .sorted(Comparator.comparingInt(NutrientGroup::order).thenComparing(value -> value.id().toString()))
            .toList();
        snapshot = new Snapshot(ordered, Map.copyOf(groups), profiles, effects);
        NutritionEffectsManager.clearAll();
        Innutrient.LOGGER.info("Loaded Innutrient datapack definitions: {} groups, {} food rules, {} effect rules in {} ms",
            ordered.size(), profiles.size(), effects.size(), (System.nanoTime() - started) / 1_000_000L);
    }

    public static List<NutrientGroup> groups() {
        return snapshot.groups();
    }

    public static Optional<NutrientGroup> group(ResourceLocation id) {
        return Optional.ofNullable(snapshot.byId().get(id));
    }

    public static List<NutritionEffectRule> effects() {
        return snapshot.effects();
    }

    public static ExplicitResolution explicit(ItemStack stack) {
        Map<ResourceLocation, Double> values = new LinkedHashMap<>();
        boolean matched = false;
        boolean disabled = false;
        for (FoodProfileRule rule : snapshot.profiles()) {
            if (!rule.matches(stack)) continue;
            matched = true;
            disabled |= rule.disableAutomatic();
            if (rule.mode() == FoodProfileRule.Mode.REPLACE) values.clear();
            rule.nutrients().forEach((id, amount) -> values.merge(id, amount, Double::sum));
        }
        return new ExplicitResolution(matched,
            matched && !values.isEmpty() ? NutritionProfile.of(values, NutritionProfileSource.EXPLICIT) : NutritionProfile.unknown(),
            disabled);
    }

    public static NutritionProfile directTags(ItemStack stack) {
        Map<ResourceLocation, Double> values = new LinkedHashMap<>();
        for (NutrientGroup group : snapshot.groups()) {
            var key = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM, group.itemTag());
            if (stack.is(key)) values.put(group.id(), 1.0);
        }
        return values.isEmpty() ? NutritionProfile.unknown()
            : NutritionProfile.of(values, NutritionProfileSource.DIRECT_TAGS);
    }

    private static Map<ResourceLocation, NutrientGroup> loadGroups(ResourceManager manager) {
        Map<ResourceLocation, NutrientGroup> loaded = new LinkedHashMap<>();
        for (Map.Entry<ResourceLocation, Resource> entry : jsonResources(manager, GROUP_ROOT).entrySet()) {
            ResourceLocation id = logicalId(entry.getKey(), GROUP_ROOT);
            try (Reader reader = entry.getValue().openAsReader()) {
                JsonObject root = object(JsonParser.parseReader(reader), "root");
                requireFormat(root);
                loaded.put(id, parseGroup(id, root));
            } catch (Exception exception) {
                Innutrient.LOGGER.error("Skipping invalid nutrient group {}: {}", id, rootCause(exception));
            }
        }
        return loaded;
    }

    private static List<FoodProfileRule> loadProfiles(ResourceManager manager,
                                                       Map<ResourceLocation, NutrientGroup> groups) {
        List<FoodProfileRule> loaded = new ArrayList<>();
        for (Map.Entry<ResourceLocation, Resource> entry : jsonResources(manager, PROFILE_ROOT).entrySet()) {
            ResourceLocation id = logicalId(entry.getKey(), PROFILE_ROOT);
            try (Reader reader = entry.getValue().openAsReader()) {
                JsonObject root = object(JsonParser.parseReader(reader), "root");
                requireFormat(root);
                JsonArray rules = root.has("profiles") ? array(root.get("profiles"), "profiles") : null;
                if (rules == null) loaded.add(parseProfile(id, 0, root, groups));
                else for (int index = 0; index < rules.size(); index++) {
                    try {
                        loaded.add(parseProfile(id, index, object(rules.get(index), "profiles[" + index + "]"), groups));
                    } catch (Exception exception) {
                        Innutrient.LOGGER.warn("Skipping invalid food profile {} entry {}: {}", id, index, rootCause(exception));
                    }
                }
            } catch (Exception exception) {
                Innutrient.LOGGER.error("Skipping invalid food profile file {}: {}", id, rootCause(exception));
            }
        }
        loaded.sort(Comparator.comparingInt(FoodProfileRule::specificity)
            .thenComparingInt(FoodProfileRule::priority)
            .thenComparing(value -> value.definitionId().toString())
            .thenComparingInt(FoodProfileRule::entryIndex));
        return List.copyOf(loaded);
    }

    private static List<NutritionEffectRule> loadEffects(ResourceManager manager,
                                                           Map<ResourceLocation, NutrientGroup> groups) {
        List<NutritionEffectRule> loaded = new ArrayList<>();
        for (Map.Entry<ResourceLocation, Resource> entry : jsonResources(manager, EFFECT_ROOT).entrySet()) {
            ResourceLocation id = logicalId(entry.getKey(), EFFECT_ROOT);
            try (Reader reader = entry.getValue().openAsReader()) {
                JsonObject root = object(JsonParser.parseReader(reader), "root");
                requireFormat(root);
                NutritionEffectRule rule = parseEffect(id, root, groups);
                if (!BuiltInRegistries.MOB_EFFECT.containsKey(rule.effect()))
                    throw new IllegalArgumentException("unknown effect " + rule.effect());
                loaded.add(rule);
            } catch (Exception exception) {
                Innutrient.LOGGER.error("Skipping invalid nutrition effect rule {}: {}", id, rootCause(exception));
            }
        }
        loaded.sort(Comparator.comparing(value -> value.id().toString()));
        return List.copyOf(loaded);
    }

    private static NutrientGroup parseGroup(ResourceLocation id, JsonObject root) {
        double healthyMin = number(root, "healthy_min", 40);
        double healthyMax = number(root, "healthy_max", 80);
        return new NutrientGroup(id,
            string(root, "translation_key", "nutrient." + id.getNamespace() + "." + id.getPath().replace('/', '.')),
            ResourceLocation.parse(string(root, "icon", "minecraft:apple")),
            ResourceLocation.parse(string(root, "item_tag", id.getNamespace() + ":foods/" + id.getPath())),
            color(root, "color", 0xFFFFFF), integer(root, "order", 0),
            number(root, "default_level", 50), healthyMin, healthyMax,
            number(root, "low_threshold", Math.min(healthyMin, 20)),
            number(root, "high_threshold", Math.max(healthyMax, 90)),
            number(root, "gain_multiplier", 1), number(root, "decay_multiplier", 1),
            bool(root, "penalize_low", true), bool(root, "penalize_high", false),
            bool(root, "required_for_balance", true));
    }

    private static FoodProfileRule parseProfile(ResourceLocation id, int index, JsonObject root,
                                                 Map<ResourceLocation, NutrientGroup> groups) {
        ResourceLocation item = root.has("item") ? ResourceLocation.parse(root.get("item").getAsString()) : null;
        ResourceLocation tag = root.has("tag") ? ResourceLocation.parse(root.get("tag").getAsString()) : null;
        if (item != null && !BuiltInRegistries.ITEM.containsKey(item))
            throw new IllegalArgumentException("unknown item " + item);
        JsonObject nutrientObject = root.has("nutrients") ? object(root.get("nutrients"), "nutrients") : new JsonObject();
        Map<ResourceLocation, Double> nutrients = parseNutrients(nutrientObject, groups);
        FoodProfileRule.Mode mode = FoodProfileRule.Mode.valueOf(
            string(root, "mode", "replace").toUpperCase(Locale.ROOT));
        boolean disableAutomatic = bool(root, "disable_automatic", false);
        if (nutrients.isEmpty() && !disableAutomatic)
            throw new IllegalArgumentException("nutrients cannot be empty unless disable_automatic is true");
        return new FoodProfileRule(id, index, item, tag, nutrients, mode,
            integer(root, "priority", 0), disableAutomatic);
    }

    private static Map<ResourceLocation, Double> parseNutrients(JsonObject object,
                                                                 Map<ResourceLocation, NutrientGroup> groups) {
        Map<ResourceLocation, Double> values = new LinkedHashMap<>();
        object.entrySet().forEach(entry -> {
            ResourceLocation id = ResourceLocation.parse(entry.getKey());
            if (!groups.containsKey(id)) throw new IllegalArgumentException("unknown nutrient " + id);
            double value = entry.getValue().getAsDouble();
            if (!Double.isFinite(value) || value < 0) throw new IllegalArgumentException("invalid weight for " + id);
            if (value > 0) values.put(id, value);
        });
        if (values.isEmpty()) return Map.of();
        return NutritionProfile.normalize(values);
    }

    private static NutritionEffectRule parseEffect(ResourceLocation id, JsonObject root,
                                                     Map<ResourceLocation, NutrientGroup> groups) {
        JsonObject condition = object(root.get("condition"), "condition");
        JsonObject effect = object(root.get("effect"), "effect");
        return new NutritionEffectRule(id, parseCondition(condition, groups, 0),
            ResourceLocation.parse(string(effect, "id", "minecraft:weakness")),
            Math.max(20, integer(effect, "duration_ticks", 240)),
            Math.max(0, integer(effect, "amplifier", 0)), bool(root, "beneficial", false),
            bool(effect, "ambient", true), bool(effect, "show_particles", false));
    }

    private static NutritionCondition parseCondition(JsonObject object,
                                                       Map<ResourceLocation, NutrientGroup> groups, int depth) {
        if (depth > 16) throw new IllegalArgumentException("effect condition nesting exceeds 16");
        NutritionCondition.Type type = NutritionCondition.Type.valueOf(
            string(object, "type", "all_healthy").toUpperCase(Locale.ROOT));
        ResourceLocation group = object.has("group")
            ? ResourceLocation.parse(object.get("group").getAsString()) : null;
        if (group != null && !groups.containsKey(group)) throw new IllegalArgumentException("unknown nutrient " + group);
        if ((type == NutritionCondition.Type.GROUP_BELOW || type == NutritionCondition.Type.GROUP_ABOVE
            || type == NutritionCondition.Type.GROUP_STATUS) && group == null)
            throw new IllegalArgumentException(type.name().toLowerCase(Locale.ROOT) + " requires group");

        NutrientStatus status = null;
        if (type == NutritionCondition.Type.GROUP_STATUS || type == NutritionCondition.Type.COUNT_STATUS) {
            if (!object.has("status")) throw new IllegalArgumentException("condition requires status");
            status = NutrientStatus.valueOf(object.get("status").getAsString().toUpperCase(Locale.ROOT));
        }

        List<NutritionCondition> children = new ArrayList<>();
        if (type == NutritionCondition.Type.ALL || type == NutritionCondition.Type.ANY) {
            JsonArray array = array(object.get("conditions"), "conditions");
            if (array.isEmpty() || array.size() > 64)
                throw new IllegalArgumentException("conditions must contain 1..64 entries");
            for (JsonElement child : array) children.add(parseCondition(object(child, "condition"), groups, depth + 1));
        } else if (type == NutritionCondition.Type.NOT || type == NutritionCondition.Type.MAINTAINED_FOR) {
            children.add(parseCondition(object(object.get("condition"), "condition"), groups, depth + 1));
        }

        double value = object.has("value") ? number(object, "value", 0) : number(object, "threshold", 20);
        return new NutritionCondition(type, group, value, status,
            Math.max(1, integer(object, "count", 1)), Math.max(0, integer(object, "ticks", 0)), children);
    }

    private static Map<ResourceLocation, Resource> jsonResources(ResourceManager manager, String root) {
        return manager.listResources(root, id -> id.getPath().endsWith(".json"));
    }

    private static ResourceLocation logicalId(ResourceLocation file, String root) {
        String path = file.getPath().substring(root.length() + 1, file.getPath().length() - 5);
        return ResourceLocation.fromNamespaceAndPath(file.getNamespace(), path);
    }

    private static void requireFormat(JsonObject root) {
        if (!root.has("format_version")) throw new IllegalArgumentException("format_version is required");
        int version = root.get("format_version").getAsInt();
        if (version < 1 || version > FORMAT_VERSION)
            throw new IllegalArgumentException("supported format_version range is 1.." + FORMAT_VERSION);
    }

    private static JsonObject object(JsonElement element, String name) {
        if (element == null || !element.isJsonObject()) throw new IllegalArgumentException(name + " must be an object");
        return element.getAsJsonObject();
    }

    private static JsonArray array(JsonElement element, String name) {
        if (element == null || !element.isJsonArray()) throw new IllegalArgumentException(name + " must be an array");
        return element.getAsJsonArray();
    }

    private static String string(JsonObject object, String key, String fallback) {
        return object.has(key) ? object.get(key).getAsString() : fallback;
    }

    private static double number(JsonObject object, String key, double fallback) {
        double value = object.has(key) ? object.get(key).getAsDouble() : fallback;
        if (!Double.isFinite(value)) throw new IllegalArgumentException(key + " must be finite");
        return value;
    }

    private static int integer(JsonObject object, String key, int fallback) {
        return object.has(key) ? object.get(key).getAsInt() : fallback;
    }

    private static boolean bool(JsonObject object, String key, boolean fallback) {
        return object.has(key) ? object.get(key).getAsBoolean() : fallback;
    }

    private static int color(JsonObject object, String key, int fallback) {
        if (!object.has(key)) return fallback;
        JsonElement value = object.get(key);
        if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isNumber()) return value.getAsInt();
        String text = value.getAsString().trim();
        return Integer.parseInt(text.startsWith("#") ? text.substring(1) : text, 16);
    }

    private static String rootCause(Throwable throwable) {
        while (throwable.getCause() != null) throwable = throwable.getCause();
        return throwable.getMessage() == null ? throwable.getClass().getSimpleName() : throwable.getMessage();
    }

    public record ExplicitResolution(boolean matched, NutritionProfile profile, boolean disableAutomatic) {}

    private record Snapshot(List<NutrientGroup> groups, Map<ResourceLocation, NutrientGroup> byId,
                            List<FoodProfileRule> profiles, List<NutritionEffectRule> effects) {}
}
