# Integrations

Innutrient has no required dependency on food mods, JEI, or EMI. Compatibility uses Common Tags, exposed recipes, datapack overrides, and a specialized resolver only when a recipe hides ingredients.

The 26.2 branch keeps the full Vanilla/Common Tags core. At the compatibility audit date, the major NeoForge food ecosystems targeted on 1.21.1 did not publish compatible 26.2 builds, so no placeholder Java integration is shipped. Future compatible ports using Common Tags and exposed recipes will work automatically; exceptions can be handled by datapacks or the public resolver API.

Standard Innutrient tooltips appear inside JEI item views without a JEI dependency. EMI has no audited NeoForge 26.2 build. See [COMPATIBILITY.md](COMPATIBILITY.md) for the matrix.
