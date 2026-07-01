# Changelog

## 1.0.3

- Hardened ASP courier, ASP hit squad, and Junk explorer campaign scripts against stale fleets and null campaign state.
- Fixed Junkjet/electrochaff and Tangerine Blue combat edge cases that could crash benchmark or large battles.
- Added safer Familia HQ patrol spawning, lost-tech defender cleanup, and anarchist beacon procgen guards.

## 1.0.2

- Fixed a campaign crash from `FamiliaHQ$1.class` being compiled for Java 21 instead of Starsector's Java 17 runtime.
- Rebuilt the affected Familia HQ classfiles with Java 17-compatible bytecode.

## 1.0.0

Initial CFF restoration release for Starsector `0.98a-RC8`.

- Updated dependency metadata for LazyLib and GraphicsLib.
- Added/updated Nexerelin faction setup and compatibility data.
- Added save-load recovery and relation cleanup work.
- Added LunaLib settings support.
- Added Starship Legends support.
- Added Industrial Evolution and New Beginnings compatibility hooks.
- Performed first-pass ship balance and variant cleanup.
- Cleaned release folder for GitHub upload by removing editor backups and duplicate source archives.

## Earlier 3.5.x Notes

- Added Junk Pirates explorer activity and related intel.
- Added ASP courier and hit-fleet activity.
- Expanded Familia prominence, combat ships, and variants.
- Added PACK Mesh built-in hullmod support for mixed fleets and mixed hulls.
- Added decorative weapons and small visual touches to several ships, especially Boxer and Boxenstein.
- Continued sprite and aesthetic cleanup across the fleet.
