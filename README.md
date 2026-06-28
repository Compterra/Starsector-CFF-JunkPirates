# Junk Pirates / ASP / PACK (Release)

A restored compatibility release of mendonca's Junk Pirates, PACK, and ASP Syndicate content for Starsector `0.98a-RC8`.

This release combines three related faction packs into one maintained mod package:

- **Junk Pirates**: scrapyard raiders, improvised hulls, salvaged station content, and rough-edged pirate engineering.
- **PACK**: canine-themed combat ships with PACK Mesh integration and mixed-fleet support.
- **ASP Syndicate**: underworld fleets, couriers, hit squads, and related Nexerelin activity.

## Current Restoration Notes

This fork is a local restoration pass for modern Starsector. It includes dependency metadata updates, Nexerelin faction setup, save-load recovery work, relation cleanup, LunaLib settings, Starship Legends support, first-pass ship balance, and compatibility work for `0.98a-RC8`.

## Requirements

- Starsector `0.98a-RC8`
- LazyLib
- GraphicsLib

## Optional Compatibility

The mod includes support/configuration for common ecosystem mods where present, including Nexerelin, LunaLib, Starship Legends, Industrial Evolution, and New Beginnings.

## Install

Place this folder in your Starsector `mods` directory and enable **Junk pirates / ASP / PACK (Release)** in the launcher.

## Credits

Junk Pirates is not the work of a single individual. Original and restoration credits include:

- **mendonca**: original mod author and primary content creator
- **MesoTronik**: custom sounds, quality control, flare code
- **HELMUT**, **Dark.Revenant**, **Avanitia**: major balance feedback
- **Xenoargh**: significant contributor to the Pitbull sprite
- **Versus The Ghost**: musical accompaniment
- **NetworkPesci**: support and presence
- **Histidine**: procgen insight
- **King Alfonzo**: Ridgeback sprite contribution
- **Vayra**: electrochaff, scrapjet missiles, Kadur-related contribution
- **MShadowy**: vector-code assistance

## Repository Hygiene

The repository keeps live game data, graphics, compiled jar, and Java source under `jars/src/`. Editor backup files, temporary files, logs, and generated source zips are intentionally ignored.
