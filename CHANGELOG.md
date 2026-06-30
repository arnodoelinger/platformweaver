# 1.2.0 Release

## Highlights

- New project name — Platform Weaver
- Now project is published to Maven Central
- `@Chameleon` now merges platform code, not just type names
- Some other minor improvements

## Features

- `@Chameleon` carriers can now be extension / computed properties and functions, not only type aliases, so accessor and
  behavior divergence (e.g. `player.uniqueId` vs `player.uuid`) collapses into a single shared declaration instead of a
  `@PaperOnly` / `@FabricOnly` pair
- Member-carrier bodies may reference imports from the carrier file; codegen carries each import through only when the
  generated declaration uses it
- Published to Maven Central (`io.github.arnodoelinger:platformweaver-plugin`, `:platformweaver-annotations`), so
  consumers no longer need the JitPack repository
- GitHub [wiki page](https://github.com/arnodoelinger/PlatformWeaver/wiki)
- Renamed project to Platform Weaver
- Now the author's nickname is arnodoelinger, not arsmotorin

## Improvements

- Updated Kotlin to 2.4.0
- Enhanced versionizing
- Enhanced CI process
- Enhanced README file, moved wiki to GitHub wiki
- New releases are now published in [Discord](https://discord.gg/J9neWMCdpR)

# 1.1.0 Release

## Highlights

- `@Chameleon` platform-resolved type aliases
- Gradle codegen for `@Chameleon` carriers

## Features

- `@Chameleon` — platform-resolved type aliases. One name that becomes a different concrete type per platform, with no `typealias` keyword and no redeclaration clash
- Gradle codegen for `@Chameleon` carriers (`generateChameleons` task) with configurable `platformweaver { chameleonsDir = "..." }`

## Fixes

- Fix dashes in changelog generation

# 1.0.1 Release

## Features

- Use the same changelog style as in [Dream Displays](https://github.com/arnodoelinger/dreamdisplays/blob/main/CHANGELOG.md)

## Improvements

- Enhance README file

# 1.0.0 Release

## Features

- Initial release
