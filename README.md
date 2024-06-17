# ~~Arclight~~ Fluorite（Forked from Arclight）

A Bukkit server implementation utilizing Mixin.

| Release | Forge  | Status |                            Build                             |
| :-----: | :----: | :----: | :----------------------------------------------------------: |
| 1.19.2  | 43.3.8 | ACTIVE | [![1.19 Status](https://img.shields.io/appveyor/build/IzzelAliz/arclight-19?style=flat-square)](https://ci.appveyor.com/project/IzzelAliz/arclight-19) |

## Installing

* Download the jar from [release page](https://github.com/IzzelAliz/Arclight/releases) or build server. (see the table
  above)
* Launch with command `java -jar arclight-forge-<mc>-<version>.jar nogui`. The `nogui` argument will disable the server
  control panel.

## Contributing

Please read this [wiki section](https://github.com/IzzelAliz/Arclight/wiki/Contributing).

## License

This project is licensed under [GPL v3](LICENSE).

## Differences with Arclight

* Fixed mixin conflict with chestcavity mod
* Add some unofficial branch warnings
* Simplified console log output style
* Minecraft Server Gui disabled by default
* More aggressively delete old cache files before running
* New entity death handler
* Coordinate the deaths of Bukkit and Forge
* Disable spigot `/restart` command
* Fixed bukkitEvent cannot be triggered asynchronously from another thread
* Broken `BlockBreakEvent` fix Block#popResource cannot dropping loot **(WIP) Fix bukkit BlockBreakEvent**
* Fixed add raiders in wrong code location
* Temporarily disable RaidSpawnWaveEvent
* Fixed mixin conflict with frozenup mod
* Fixed mixin conflict with sleep_tight mod
* Added missing LootContextParams#KILLER_ENTITY/THIS_ENTITY builder fixed LootJS#FISHING event cannot be called
* Add PlayerDataFixer
* Fixed worldBorder
* Updated to forge 43.4.0
* Add LivingEntity#getTargetEntity API from Paper
* Implement ChunkStats
* Fixed mixin conflict with fruitsdelight mod
* Fixed mixin conflict with bettercombat mod
* Fixed mixin conflict with spoornarmorattributes mod
* Fixed mixin conflict with caverns_and_chasms mod