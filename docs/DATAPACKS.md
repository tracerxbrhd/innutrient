# Datapacks

Definitions live under `data/<namespace>/innutrient/`. Format versions 1 and 2 are accepted. Schema v2 is required only for the new recursive effect predicates; existing v1 nutrient groups, food profiles, and effect rules continue to load.

Invalid files or entries are skipped with the definition ID and a concise reason in the server log.

## Nutrient groups

Path: `innutrient/nutrient_groups/<path>.json`; the group ID is `<namespace>:<path>`.

```json
{
  "format_version": 1,
  "translation_key": "nutrient.example.magic",
  "icon": "example:magic_berry",
  "item_tag": "example:foods/magic",
  "color": "#8C5CFF",
  "order": 60,
  "default_level": 50,
  "healthy_min": 35,
  "healthy_max": 75,
  "low_threshold": 15,
  "high_threshold": 90,
  "gain_multiplier": 1.0,
  "decay_multiplier": 0.8,
  "penalize_low": true,
  "penalize_high": false,
  "required_for_balance": true
}
```

`penalize_low` and `penalize_high` are independent. Innutrient's sugar group deliberately disables the low penalty and enables the high penalty.

## Food profiles

Path: `innutrient/food_profiles/<name>.json`. A file may contain one rule or a `profiles` array. Each rule selects exactly one `item` or `tag`.

```json
{
  "format_version": 1,
  "item": "example:complete_meal",
  "mode": "replace",
  "priority": 100,
  "disable_automatic": true,
  "nutrients": {
    "innutrient:grains": 0.30,
    "innutrient:proteins": 0.40,
    "innutrient:vegetables": 0.30
  }
}
```

- `replace` clears values from earlier matching rules; `merge` adds before normalization.
- Tag rules apply before exact-item rules. Lower priority applies first; IDs and array index break ties deterministically.
- `disable_automatic: true` with no nutrients explicitly leaves the selection unclassified.
- Unknown groups and invalid or non-finite weights are rejected.

## Effect rules v2

Path: `innutrient/effect_rules/<name>.json`.

```json
{
  "format_version": 2,
  "beneficial": true,
  "condition": {
    "type": "maintained_for",
    "ticks": 12000,
    "condition": {
      "type": "all",
      "conditions": [
        { "type": "balance_above", "value": 80 },
        {
          "type": "not",
          "condition": {
            "type": "count_status",
            "status": "deficient",
            "count": 1
          }
        }
      ]
    }
  },
  "effect": {
    "id": "minecraft:regeneration",
    "duration_ticks": 260,
    "amplifier": 0,
    "ambient": true,
    "show_particles": false
  }
}
```

Predicates:

- v1-compatible: `group_below`, `group_above`, `all_healthy`, `count_below` (`threshold` remains accepted)
- numeric: `balance_above`, `balance_below` (`value`)
- status: `group_status`, `count_status`; status is `deficient`, `below_target`, `healthy`, `above_target`, or `excessive`
- boolean: `all`, `any`, `not`
- temporal: `maintained_for`, containing one nested `condition` and `ticks`

Condition nesting is capped at 16, boolean arrays at 64, and sustained timers exist only for online players/rules. Timers reset on logout and datapack reload, preventing unbounded persistent data.

Datapack reload atomically replaces definitions, clears sustained-rule timers and recipe caches, reconciles online states, rebuilds the tooltip catalog, and resynchronizes clients.
