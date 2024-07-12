# BetterElo

https://www.spigotmc.org/resources/betterelo.114648/

Minecraft PvP Elo rank system Plugin created by Grzybol

Minecraft version 1.18.2+

Dependencies: PlaceholderAPI, BetterRanks

## Player Commands

There are no special permissions required to use the following Player commands.

### BetterElo commands:

## Plugin Commands

### Player Commands
- **/be** - Returns your ranking information.
- **/be `<player>`** - Returns ranking information about the specified player.
- **/be info** - Returns information about the plugin.
- **/be top `<n>`** - Returns ranking information about the player at the specified position.
- **/be top10** - Returns information about the top 10 players in the ranking.
- **/be claim** - Claim your rewards! Remember to empty your equipment before claiming!!
- **/be timeleft** - Returns the remaining time until the giveaway.
- **/be daily** - Returns information about the top 10 daily ranking.
- **/be weekly** - Returns information about the top 10 weekly ranking.
- **/be monthly** - Returns information about the top 10 monthly ranking.
- **/be event** - Returns information about the event.
- **/be reroll** - Opens the Re-Roll GUI for AvgDmg bonus items.

### Administrator Commands
- **/be setrewards** - Opens a GUI for changing the rewards.
- **/be reload** - Reloads the config file.
- **/be ban `<player>`** - Resets the player's ranking to 1000 and redistributes remaining points to victims.
- **/be add `<player> <points> <rankingtype>`** - Adds points to the specified player in a specific ranking (main, daily, weekly, monthly).
- **/be sub `<player> <points> <rankingtype>`** - Subtracts points from the specified player in a specific ranking.
- **/be startevent `<duration> <timeUnit>`** - Sets up the duration and time unit for an event.
- **/be stopevent** - Stops the current event (if active).
- **/be holo `<event/main/daily/weekly/monthly>`** - Creates a hologram at your position.
- **/be holo delete `<event/main/daily/weekly/monthly>`** - Deletes the specified hologram.
- **/be antyweb `<radius>`** - Creates an anti-web effect with the given radius.
- **/be firework `<power>`** - Creates an Infinite Firework with the given power.
- **/be flamethrower `<distance> <range>`** - Adds a Flamethrower effect.
- **/be zephyr `<power>`** - Adds a Zephyr effect.
- **/be addspawner `<spawnerName> <mobName> <cooldown(s)> <mobCountPerSpawn> <maxMobs>`** - Creates a custom mob spawner.
- **/be droptable `<name>`** - Opens a GUI to create a new drop table.
- **/be spawnmob `<mobname> <amount>`** - Spawns the specified custom mob.
- **/be enchantitem** - Gives you 1x Enchant Item.
- **/be forcespawn `<spawnerName>`** - Forces a respawn of the specified spawner.

## Placeholders

**IMPORTANT!! I hardcoded 10s interval for checking placeholders - lemme know if you want it to be configurable in config file**

- `%be_player%` returns the player's main ranking points
- `%be_player_daily%` returns the player's daily ranking points
- `%be_player_weekly%` returns the player's weekly ranking points
- `%be_player_monthly%` returns the player's monthly ranking points
- `%be_rank%` returns the player's main ranking
- `%be_rank_daily%` returns the player's daily ranking
- `%be_rank_weekly%` returns the player's weekly ranking
- `%be_rank_monthly%` returns the player's monthly ranking
- `%be_player_top<n>%` returns the player's nickname at position `<n>` in ranking
- `%be_points_top<n>%` returns the player's points at position `<n>` in ranking
- `%be_daily_tl%` returns time left on daily rewards timer - automatically converts to D-M-S format
- `%be_weekly_tl%` returns time left on weekly rewards timer - automatically converts to D-M-S format
- `%be_monthly_tl%` returns time left on monthly rewards timer - automatically converts to D-M-S format
- `%be_event_tl%` returns time left on event rewards timer - automatically converts to D-M-S format
- `%be_event_daily%` returns the player's event ranking
- `%be_event_monthly%` returns the player's event ranking points
