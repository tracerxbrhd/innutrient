# Architecture

The server owns nutrition gain, decay, Food Variety, Diet Quality transitions, effects, exhaustion, and natural-regeneration modifiers. The synchronized player attachment supplies only display state to clients.

`NutritionRegistry` atomically replaces immutable datapack snapshots. `NutritionResolver` builds a deterministic recipe-output index and an item-keyed profile cache after reload. `NutritionService` is the mutation boundary. `NutritionState` data version 2 stores levels, sustained quality timestamps, and one bounded food streak; version-1 saves migrate through optional codec fields.

The client receives an immutable catalog of nutrient metadata plus resolved food profile, baseline gain, Meal Quality, and meal multiplier. Tooltips are cache lookups. The U-API screen reads the synchronized attachment and never performs authoritative calculations.

Network protocol version 2 reflects the richer food catalog. Login, respawn, dimension change, explicit request, and datapack sync refresh the client. Attachment synchronization handles state changes without a parallel packet protocol.

Survival hooks are narrow common mixins: activity exhaustion is adjusted at `Player#causeFoodExhaustion`, while only FoodData's vanilla natural-regeneration calls are scaled. Both handlers check for `ServerPlayer`; client-only classes are absent from common initialization.
