# Releasing Innutrient

Publishing is tag-driven. Normal branch pushes do not publish artifacts.

## GitHub configuration

Required repository secrets:

- `MODRINTH_TOKEN` — Modrinth API token.
- `CURSEFORGE_TOKEN` — CurseForge API token, only when CurseForge publishing is desired.

Required repository variables:

- `MODRINTH_PROJECT_ID` — Innutrient project ID or slug on Modrinth.
- `U_API_MODRINTH_PROJECT_ID` — U-API project ID or slug on Modrinth.
- `CURSEFORGE_PROJECT_ID` — optional Innutrient project ID on CurseForge.
- `U_API_CURSEFORGE_PROJECT_ID` — required when CurseForge publishing is configured.
- `U_API_REPOSITORY` — optional GitHub source repository, defaulting to `<owner>/u-api`.
- `U_API_REF` — optional U-API branch, tag, or commit, defaulting to `v<u_api_version>+mc<minecraft_version>`.

If U-API is private, `U_API_REPOSITORY_TOKEN` must grant read access. Project IDs belong in variables; API tokens belong in secrets.

## Local verification

From the repository root, after committing and pushing `port/26.2`:

```powershell
.\scripts\release.ps1 -DryRun
```

This verifies the clean and synchronized repository, version metadata, absent tag, and a complete clean Gradle build without creating a tag.

## Publishing

Release using the channel inferred from `mod_version`:

```powershell
.\scripts\release.ps1
```

An explicit channel can be supplied as a consistency check:

```powershell
.\scripts\release.ps1 -Channel beta
.\scripts\release.ps1 -Channel alpha
```

The script creates and pushes an annotated tag only after all checks pass. For version `0.2.0-beta.1` and Minecraft `26.2`, the tag is `v0.2.0-beta.1+mc26.2` and the publishing channel is `beta`.

GitHub Actions then:

1. checks out the tagged Innutrient source and the matching U-API tag;
2. validates metadata and publishing configuration;
3. builds and selects exactly one user-facing JAR;
4. publishes to Modrinth with U-API marked as required;
5. publishes to CurseForge when both CurseForge settings are present;
6. creates the GitHub Release with the same JAR and generated notes.

Do not move or reuse an already published tag. Fix the problem, increment `mod_version`, commit, and publish a new tag.
