package master.innutrient.client;

import master.innutrient.network.NutritionCatalogPayload;
import master.innutrient.nutrition.NutrientGroup;
import master.innutrient.nutrition.NutritionProfile;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;

public final class ClientNutritionCatalog {
    private static volatile List<NutrientGroup> groups = List.of();
    private static volatile Map<Identifier, NutritionProfile> profiles = Map.of();
    private static volatile long revision;

    private ClientNutritionCatalog() {}

    public static void replace(NutritionCatalogPayload payload) {
        groups = List.copyOf(payload.groups());
        profiles = Map.copyOf(payload.profiles());
        revision++;
    }

    public static List<NutrientGroup> groups() { return groups; }
    public static NutritionProfile profile(ItemStack stack) {
        return profiles.getOrDefault(BuiltInRegistries.ITEM.getKey(stack.getItem()), NutritionProfile.unknown());
    }
    public static long revision() { return revision; }
}
