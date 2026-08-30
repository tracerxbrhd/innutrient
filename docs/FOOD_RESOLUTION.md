# Food resolution

Every edible item is resolved in this order:

```text
explicit food profile / datapack override
→ specialized or generic recipe inheritance
→ configured nutrient item tags (including Common Tags)
→ unclassified
```

Explicit rules always win. Recipe inheritance precedes tags so a meal keeps multi-group composition instead of collapsing to one output tag.

On reload, recipes are indexed by displayed output and sorted by ID. The highest-priority resolver exposes placement ingredients; alternatives are sorted, bounded, recursively resolved, and averaged. Multiple producing recipes are averaged within the configured cap. A visited-item guard and depth bound stop cycles.

Profiles, Meal Quality, and baseline tooltip gain are cached by item. Eating and tooltips never traverse recipe graphs. Reload atomically invalidates and rebuilds caches, reconciles players, and resynchronizes clients.

Both supported branches use verified NeoForge `c:foods/*` tags plus `c:eggs` and crop tags. The 1.21.1 branch additionally carries optional legacy aliases for food ecosystems that actually exist there; the 26.2 branch does not claim unavailable mod integrations.
