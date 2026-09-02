# Nutrition Dashboard

Innutrient 1.2 presents all player nutrition information in one U-API panel. It does not register a HUD,
floating widget, detachable window, or extra gameplay state.

## Sections

- **Summary** combines Nutrition Balance, sustained Diet Quality, Food Variety, and the latest Meal Quality.
- **Guidance** selects one high-priority issue: severe deficiency, excessive nutrient, low/high group,
  limited variety, or a healthy confirmation.
- **Nutrient Balance** renders every synchronized datapack-defined group. Each scale shows distinct low,
  healthy-target, and high zones with text, patterns, boundary markers, a status symbol, and a value marker.
- **Recent Foods** displays at most six newest entries already present in bounded Diet Memory.
- **Current Nutrition Modifiers** explains the actual configured exhaustion, nutrition-efficiency, and
  natural-regeneration multipliers for the player's sustained Diet Quality.

## Responsive behavior

At wide GUI widths, nutrient rows occupy the main column and recent meals/modifiers share a supporting
column inside the same panel. Compact widths stack those sections. The dashboard uses one clipped scroll
viewport only when its content exceeds the available height. Long translated text is width-bounded and its
full meaning remains available through hover tooltips.

The layout is based on GUI-space dimensions, so Minecraft GUI scale is handled the same way as physical
resolution. Automated tests cover 320×180 through ultrawide GUI spaces and group counts from zero to 18.

## Multiplayer data

Nutrient levels, Diet Memory, and Diet Quality continue to use the synchronized player attachment. The
catalog payload additionally carries a small immutable server settings snapshot used only for display.
The screen does not traverse recipes, recompute food profiles, or send history requests per frame.
