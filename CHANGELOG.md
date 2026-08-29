# Changelog

## 0.2.0-beta.1 - 2026-08-29

- Ported Innutrient to Minecraft 26.2, NeoForge 26.2.0.28-beta and Java 25.
- Requires U-API 3.0.0-beta.5 and supports composite builds from the sibling 26.2 checkout.
- Migrated identifiers, recipe placement/display resolution, food components, reload listeners,
  attachments, networking, commands, keyboard input and retained UI rendering to their 26.2 APIs.
- Updated the tag-driven release pipeline to publish SemVer prereleases consistently to GitHub,
  Modrinth and optionally CurseForge.
- Farmer's Delight is no longer declared as a runtime-tested dependency on this branch; generic
  recipe inheritance and curated optional item tags remain available for compatible future ports.

## 0.1.0

- Initial Minecraft 1.21.1 / NeoForge architecture.
- Data-driven nutrient groups, food profiles, effect rules, and dynamic item tags.
- Persistent synchronized player nutrition with hunger-based decay and configurable death retention.
- Recursive cached recipe composition with cycle, depth, recipe-count, and ingredient-alternative bounds.
- Vanilla defaults and optional data-driven Farmer's Delight support.
- U-API inventory tab, retained nutrition screen, diagnostics, service API, and commands.
- English and Russian localization.
