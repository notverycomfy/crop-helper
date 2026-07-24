# Development workflow

This repository stays independent from the other mods, but follows the same maintenance routine.

## Project structure

- `.github/`: build automation and repository templates
- `assets/`: project artwork used by GitHub and publishing pages
- `gradle/wrapper/`: pinned Gradle wrapper
- `src/main/`: shipping Java code and resources
- `src/test/`: automated tests when the project has testable standalone logic

GitHub may visually combine single-child folders such as `gradle/wrapper` or `src/main`. That display does not change the actual directory structure.

## Branches

- `main`: NeoForge for Minecraft 26.1.2
- `codex/neoforge-26.2`: NeoForge for Minecraft 26.2
- `codex/fabric-26.1.2`: Fabric for Minecraft 26.1.2
- `codex/fabric-26.2`: Fabric for Minecraft 26.2

Make a fix in the branch where it was found. Port the finished fix to the other applicable branches, adapting loader-specific APIs instead of copying code blindly.

## Daily routine

1. Pull the branch and confirm the working tree is clean.
2. Make one focused change.
3. Run `./gradlew build` (`gradlew.bat build` on Windows).
4. Test behavior in-game when the change affects overlays, configuration, rendering, or block detection.
5. Review the diff for generated files, debug output, unrelated formatting, and accidental behavior changes.
6. Commit with a short action-based message, then push the branch.

## Code standard

- Use four spaces in Java and Gradle files and two spaces in data files.
- Prefer descriptive names and small methods over explanatory narration.
- Comment constraints and non-obvious decisions, not code that already explains itself.
- Keep loader-specific code isolated and preserve equivalent behavior across supported loaders.
- Log recoverable failures with useful context. Do not silently ignore exceptions.
- Avoid wildcard imports, debug printing, dead code, and unused template configuration.
- Keep gameplay constants named when their meaning is not obvious.

## Release routine

1. Update the version and changelog only when preparing a release.
2. Build every supported branch.
3. Verify the archive name identifies the mod, loader, Minecraft version, and mod version.
4. Keep only the newest replacement archive for a given loader and Minecraft version.
5. Upload release files to Modrinth and CurseForge; GitHub remains the source repository.
