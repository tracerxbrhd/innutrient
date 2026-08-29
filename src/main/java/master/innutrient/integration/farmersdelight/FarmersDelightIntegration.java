package master.innutrient.integration.farmersdelight;

import dev.uapi.integration.IntegrationService;
import master.innutrient.Innutrient;

/**
 * Data-driven compatibility boundary. Compatible recipes are consumed through their standard
 * placement and display data; curated optional item entries live in tags.
 */
public final class FarmersDelightIntegration {
    private FarmersDelightIntegration() {}

    public static void report() {
        if (IntegrationService.isLoaded("farmersdelight"))
            Innutrient.LOGGER.info("Farmer's Delight detected; generic recipe inheritance and curated tags are available, but this 26.2 combination is not runtime-validated");
    }
}
