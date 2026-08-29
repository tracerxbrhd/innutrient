package master.innutrient.integration.farmersdelight;

import dev.uapi.integration.IntegrationService;
import master.innutrient.Innutrient;

/**
 * Data-driven compatibility boundary. Cooking Pot and Cutting Board recipes are consumed through
 * their ordinary Recipe#getIngredients() contract; curated optional item entries live in tags.
 */
public final class FarmersDelightIntegration {
    private FarmersDelightIntegration() {}

    public static void report() {
        if (IntegrationService.isLoaded("farmersdelight"))
            Innutrient.LOGGER.info("Farmer's Delight detected; enabling generic Cooking Pot/Cutting Board recipe inheritance and curated tags");
    }
}
