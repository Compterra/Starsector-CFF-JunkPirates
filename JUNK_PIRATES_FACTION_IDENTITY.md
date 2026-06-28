# Junk Pirates Faction Identity

Use this as the north star when updating factions, relations, fleets, markets, and Nexerelin integration.

## Junk Pirates

Motley pseudo-kingdom based around Breh'Inni, Glory, Karkov Academy, and associated junk fields. They are not generic pirates: they are salvagers, artists, cadets, cults, engineers, and miscreants with a monarchic veneer and an instinctive dislike of large formal powers. Their fleets should feel improvised but serious, with bizarre mixes of low-tech junk, captured standards, and dangerous prototype hardware.

Relations: friendly or at least tolerant toward factions that accept frontier disorder; hostile to powers that police them, exploit them, or treat them as a technology mine.

## P.A.C.K.

The Post-Anarchist Canis Kollective is a post-collapse society emerging from Petra and Christchurch after the Exodus. They inherited Pre-PACK anarchist technology and trauma, but their modern identity is communal, defensive, and surprisingly diplomatic. Their fleets should emphasize mixed hull groups, escorts, drones, support craft, and coordinated stubbornness.

Relations: friendly to independents, League-style decentralists, and the Junk Pirates; suspicious of heavy corporate or authoritarian control; hostile to pirates, the Path, Remnants, and Pre-PACK holdouts when they threaten settlements.

## ASP Syndicate

The Associated Starline Parcels Syndicate is a courier combine, commercial state, and protection network. Paddington is the respectable public face; the Familia is the criminal machinery underneath. ASP should look useful and respectable to established powers while remaining dangerous, corrupt, and punitive when crossed.

Relations: friendly to powers that need logistics and look the other way; hostile to pirates, the Path, Junk Pirates, and groups that disrupt contracts or routes.

## Hidden Subfactions

Pre-PACK holdouts should stay hidden from the Intel faction list unless promoted deliberately. They exist for procgen systems, defenders, stations, and warning-beacon content:

- Junkboys: salvage bands, orbital squatters, and loose raider groups.
- Hounds: militant packs and violent station defenders.
- Technicians: cybernetic worker clades and old research enclaves.
- Arcane Technological Automata: spinerettes, mining automata, and industrial routines rather than a political faction.

## Compatibility Notes

Keep vanilla Corvus relations in `data/config/jpConfig/junk_pirates_Relations.json` and Nexerelin starts in `data/config/exerelinFactionConfig/*.json` aligned. If one side changes faction posture, update the other side in the same pass.

## Balance Notes

Balance by preserving faction texture first, then pricing outliers. Junk Pirates should remain irregular and sometimes rude for their size, P.A.C.K. should pay fair DP for coordinated destroyer/carrier strength, and ASP/Familia should pay for fast carrier-logistics hulls rather than getting them at bargain cruiser/capital prices.

First local ship pass raised deployment points on the clearest underpriced hulls instead of reducing OP or rewriting variants. This keeps existing loadouts intact while moving P.A.C.K. destroyers, Tangerine prototypes, heavy carriers, and 25-DP capital-carriers into more plausible Starsector bands. Follow-up passes should use simulator results before changing armor, speed, flux, or weapon OP.
