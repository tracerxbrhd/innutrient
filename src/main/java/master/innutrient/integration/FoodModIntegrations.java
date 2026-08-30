package master.innutrient.integration;

import dev.uapi.integration.IntegrationService;
import master.innutrient.Innutrient;

import java.util.LinkedHashMap;
import java.util.Map;

/** Optional, classloader-safe diagnostics for data-driven food ecosystem integrations. */
public final class FoodModIntegrations {
    private static final Map<String, String> SUPPORTED = supported();

    private FoodModIntegrations() {}

    public static void report() {
        SUPPORTED.forEach((modId, name) -> {
            if (IntegrationService.isLoaded(modId))
                Innutrient.LOGGER.info("{} detected; nutrition will resolve through Common Tags and cached recipe inheritance", name);
        });
    }

    private static Map<String, String> supported() {
        Map<String, String> integrations = new LinkedHashMap<>();
        integrations.put("farmersdelight", "Farmer's Delight");
        integrations.put("croptopia", "Croptopia");
        integrations.put("pamhc2foodcore", "Pam's HarvestCraft 2 Food Core");
        integrations.put("pamhc2crops", "Pam's HarvestCraft 2 Crops");
        integrations.put("pamhc2trees", "Pam's HarvestCraft 2 Trees");
        integrations.put("pamhc2foodextended", "Pam's HarvestCraft 2 Food Extended");
        integrations.put("farm_and_charm", "Let's Do: Farm & Charm");
        integrations.put("vinery", "Let's Do: Vinery");
        integrations.put("createfood", "Create: Food");
        return Map.copyOf(integrations);
    }
}
