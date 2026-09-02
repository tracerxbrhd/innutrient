# Nutrition Dashboard

Innutrient 1.2.1 presents all player nutrition information inside one cohesive U-API panel. It does not
register a HUD, floating widget, detachable window, or extra gameplay state, and it leaves the shared U-API
tab navigation unchanged.

## Visual hierarchy

The Dashboard uses layered near-black surfaces and reserves strong purple for section headings, the
Nutrition Balance gauge, and focused accents. Informational sections do not react as large hover cards;
only rows that expose details receive a restrained hover background.

- **Summary** gives Nutrition Balance visual priority through an animated ring and groups sustained Diet
  Quality, Food Variety, and the latest Meal Quality into compact secondary metrics.
- **Nutrient Balance** is one section containing every synchronized datapack-defined group. Compact rows
  use each group's configured identity color for the current fill, while text and markers independently
  communicate status.
- **Diet Insight** presents the existing highest-priority guidance in the contextual column.
- **Recent Foods** displays at most six newest entries already present in bounded Diet Memory and provides
  a compact empty state before the first meal.
- **Nutrition Modifiers** reports only configured values that differ meaningfully from neutral. A stable
  diet with no active effect is stated directly instead of rendering `0%` rows.

## Nutrient ranges and accessibility

Each nutrient scale combines a neutral track, a subtle healthy target zone, thin target boundaries, and a
current-value marker. A single Low / Target Range / High legend explains the section instead of repeating
labels under every row. Status remains available as text and a glyph, exact target values remain available
through tooltips, and the design does not rely on color alone.

## Responsive behavior

At wide GUI widths, Nutrient Balance uses roughly two thirds of the lower area while Diet Insight, Recent
Foods, and Nutrition Modifiers form a compact contextual column. Compact widths stack those sections. The
Dashboard uses one clipped scroll viewport only when content exceeds the available height. Long translated
text is width-bounded and its full meaning remains available through hover tooltips.

Layout is calculated in GUI-space dimensions, so Minecraft GUI scale is handled consistently with physical
resolution. Automated tests cover GUI spaces from 320×180 through ultrawide layouts, zero to eighteen
datapack groups, empty and populated Diet Memory, and neutral or active modifier states.

## Multiplayer data

Nutrient levels, Diet Memory, Diet Quality, and the immutable server settings snapshot continue to use the
existing synchronization path. The 1.2.1 visual patch does not change save data, network protocol, gameplay
calculations, recipe resolution, or public API behavior.
