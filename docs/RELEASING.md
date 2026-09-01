# Releasing Innutrient

Publishing is tag-driven; normal pushes never publish.

| Branch | Minecraft | Java | U-API line | Stable tag |
|---|---:|---:|---:|---|
| `master` | 1.21.1 | 21 | 2.x | `v1.1.0+mc1.21.1` |
| `port/26.2` | 26.2 | 25 | 3.x | `v1.1.0+mc26.2` |

Minecraft 26.2 uses its required Java 25 toolchain; the 1.21.1 branch remains Java 21. Each tag must point to its matching branch.

Required secrets are `MODRINTH_TOKEN` and, when enabled, `CURSEFORGE_TOKEN`. Required variables are `MODRINTH_PROJECT_ID`, `U_API_MODRINTH_PROJECT_ID`, and the corresponding CurseForge IDs when configured. `U_API_REPOSITORY`/`U_API_REF` can override matching U-API source.

The workflow reads Minecraft, Java, mod, and U-API versions from `gradle.properties`, validates the tag, runs `clean build`, selects one JAR, and publishes matching metadata to Modrinth, CurseForge, and GitHub Releases.

After committing and pushing the intended branch, verify without tagging:

```powershell
.\scripts\release.ps1 -DryRun
```

Only after build and gameplay checks succeed, run `.\scripts\release.ps1`. Never move an already published tag.
