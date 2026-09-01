# Architecture

The server owns nutrition gain, decay, Food Variety, Diet Quality transitions, effects, exhaustion, and natural-regeneration modifiers. The synchronized player attachment supplies only display state to clients.

`NutritionRegistry` atomically replaces immutable datapack snapshots. `NutritionResolver` builds a deterministic recipe-output index and an item-keyed profile cache after reload. `NutritionService` is the mutation boundary. `NutritionState` data version 3 stores levels, sustained quality timestamps, and bounded Diet Memory. Version-1 and version-2 saves migrate through optional codec fields; version-2 streaks become at most 16 compact legacy entries without changing nutrient levels or Diet Quality.

Each Diet Memory entry stores only item ID, game time, Meal Quality, significant nutrient-group IDs, and
a deterministic 64-bit fingerprint of 5%-quantized composition. The configured capacity defaults to 16
and is clamped to 32; codecs enforce a separate defensive maximum of 64. Recipe graphs, `ItemStack`s,
and full nutrition profiles are never persisted per player.

Food Variety calculation is O(memory capacity). Entries outside the freshness window do not count. The
score combines food uniqueness/repeat distribution (50%), composition uniqueness (25%), required-group
coverage (20%), and Meal Quality (5%), then applies a sample-confidence factor capped at eight meals.

The client receives an immutable catalog of nutrient metadata plus resolved food profile, baseline gain, Meal Quality, and meal multiplier. Tooltips are cache lookups. The U-API screen reads the synchronized attachment and never performs authoritative calculations.

Network protocol version 3 accompanies the bounded Diet Memory stream codec. Login, respawn, dimension change, explicit request, and datapack sync refresh the client. Attachment synchronization handles state changes without a parallel packet protocol. Memory is sent only as part of bounded attachment updates; no tick-time history packet exists.

Non-death clones retain Diet Memory. Death follows the established reset semantics: nutrient levels use
`deathRetentionPercent`, while Diet Memory and its old repeat state are cleared. Reconnect persistence is
handled by the attachment codec, and respawn/login/dimension hooks refresh synchronized display data.

Survival hooks are narrow common mixins: activity exhaustion is adjusted at `Player#causeFoodExhaustion`, while only FoodData's vanilla natural-regeneration calls are scaled. Both handlers check for `ServerPlayer`; client-only classes are absent from common initialization.
