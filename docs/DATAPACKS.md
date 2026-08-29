# Datapacks

All Innutrient JSON schemas currently use `"format_version": 1`. Invalid files or entries are skipped with a useful log message.

## Nutrient groups

Path:

```text
data/<namespace>/innutrient/nutrient_groups/<path>.json
```

The file above defines the group ID `<namespace>:<path>`.

```json
{
  "format_version": 1,
  "translation_key": "nutrient.example.magic",
  "icon": "ars_nouveau:source_berry",
  "item_tag": "example:foods/magic",
  "color": "#8C5CFF",
  "order": 60,
  "default_level": 50.0,
  "healthy_min": 35.0,
  "healthy_max": 75.0,
  "low_threshold": 15.0,
  "high_threshold": 90.0,
  "gain_multiplier": 1.0,
  "decay_multiplier": 0.8,
  "penalize_low": true,
  "penalize_high": false,
  "required_for_balance": true
}
```

The `item_tag` is checked dynamically; the engine does not contain a fixed list of nutrient tags.

## Food profiles

Path:

```text
data/<namespace>/innutrient/food_profiles/<name>.json
```

A file may contain one rule or a `profiles` array. A rule selects exactly one `item` or `tag`.

```json
{
  "format_version": 1,
  "item": "examplemod:super_burger",
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

- `replace` clears earlier matching rule values before applying this rule.
- `merge` adds its weights to earlier matching values before normalization.
- tag rules are applied before exact-item rules; within each kind, lower priority applies first and resource IDs break ties.
- `disable_automatic: true` with no `nutrients` explicitly leaves matching items unclassified.
- negative, non-finite, zero-sum, and unknown-group weights are rejected.

## Effect rules

Path:

```text
data/<namespace>/innutrient/effect_rules/<name>.json
```

Supported conditions are `group_below`, `group_above`, `all_healthy`, and `count_below`.

```json
{
  "format_version": 1,
  "beneficial": false,
  "condition": {
    "type": "group_below",
    "group": "innutrient:proteins",
    "threshold": 20.0
  },
  "effect": {
    "id": "minecraft:weakness",
    "duration_ticks": 260,
    "amplifier": 0,
    "ambient": true,
    "show_particles": false
  }
}
```

Datapack reloads replace the definition snapshot, invalidate recipe caches, reconcile online player states, and resynchronize clients.
