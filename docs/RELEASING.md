# Releasing Innutrient

Publishing remains tag-driven. Normal pushes never publish.

## Version branches

| Branch | Minecraft | Java | U-API line | Stable tag |
|---|---:|---:|---:|---|
| `master` | 1.21.1 | 21 | 2.x | `v1.0.0+mc1.21.1` |
| `port/26.2` | 26.2 | 25 | 3.x | `v1.0.0+mc26.2` |

Minecraft 26.2 requires the newer Java toolchain; the 1.21.1 branch remains Java 21. Each tag must point to the matching branch commit.

## Repository configuration

Required secrets: `MODRINTH_TOKEN`; and `CURSEFORGE_TOKEN` when CurseForge is enabled.

Required variables: `MODRINTH_PROJECT_ID`, `U_API_MODRINTH_PROJECT_ID`; plus `CURSEFORGE_PROJECT_ID` and `U_API_CURSEFORGE_PROJECT_ID` for CurseForge. `U_API_REPOSITORY` and `U_API_REF` may override the default matching U-API source. A private U-API repository also needs `U_API_REPOSITORY_TOKEN`.

The workflow reads Minecraft, Java, mod version, and U-API version from `gradle.properties`, validates the tag, runs `clean build`, selects exactly one release JAR, and publishes identical metadata to Modrinth, CurseForge (when configured), and GitHub Releases.

## Safe local flow

Commit and push the intended branch, then run:

```powershell
.\scripts\release.ps1 -DryRun
```

Only after the dry run and gameplay checks succeed:

```powershell
.\scripts\release.ps1
```

The script refuses dirty, detached, unsynchronized, or already-tagged states and creates the annotated tag only after a clean build. Never move a published tag; increment the mod version and publish a new tag instead.
