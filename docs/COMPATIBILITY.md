# Compatibility

Compatibility status was audited for NeoForge releases available on 2026-08-30. “Automatic” means the mod is not referenced as a Java dependency: Innutrient consumes the tags and recipes it exposes. “Native” means Innutrient also ships targeted aliases or verified handling for that ecosystem.

| Mod / ecosystem | 1.21.1 | 26.2 | Support type |
|---|---|---|---|
| Vanilla Minecraft | Native | Native | Explicit exceptions + tags + recipes |
| Farmer's Delight | Native | N/A | Verified Common Tags + Cooking/Cutting recipes |
| Croptopia | Automatic | N/A | Common Tags + recipes |
| Pam's HarvestCraft 2 Food Core | Native | N/A | Optional legacy tag aliases + recipes |
| Pam's HarvestCraft 2 Crops | Native | N/A | Optional legacy tag aliases + recipes |
| Pam's HarvestCraft 2 Trees | Native | N/A | Optional legacy tag aliases + recipes |
| Pam's HarvestCraft 2 Food Extended | Native | N/A | Optional legacy tag aliases + recipes |
| Let's Do: Farm & Charm | Native | N/A | Verified custom recipes + optional legacy tag aliases |
| Let's Do: Vinery | Automatic / datapack | N/A | Recipes; drinks may need pack-specific profiles |
| Create: Food | Automatic / datapack | N/A | Create processing recipes where ingredients are exposed |
| Farmer's Delight addons | Automatic / datapack | N/A | Recipes + Common Tags; explicit exceptions supported |
| JEI | Tooltip compatible | Tooltip compatible | Synchronized item tooltips; no required API |
| EMI | Tooltip compatible | N/A | Synchronized item tooltips; no required API |

No listed food mod, addon, JEI, or EMI is required to start Innutrient. The 26.2 column is intentionally `N/A` where no compatible NeoForge build was found; it does not advertise placeholder support.

## Resolution guarantees

- A datapack `food_profile` override always has highest priority.
- Standard and specialized recipes are recursively resolved with deterministic bounds and cycle protection.
- Common Tags classify raw foods that have no useful recipe.
- Any unsupported item can be corrected by a compatibility datapack without changing Innutrient Java code.

For exact resolution order see [FOOD_RESOLUTION.md](FOOD_RESOLUTION.md). For custom recipe systems see [API.md](API.md).
