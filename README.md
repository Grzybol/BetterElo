# BetterElo

https://www.spigotmc.org/resources/betterelo.114648/

Minecraft PvP Elo rank system Plugin created by Grzybol

Minecraft version 1.18.2+

Dependencies: PlaceholderAPI, BetterRanks


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

## PlaceholderAPI Integration

This plugin integrates with PlaceholderAPI to provide various placeholders that can be used in other plugins or parts of your server setup. Here's a list of available placeholders and their descriptions:

### General Placeholders
- **%be_player%** - Returns the main ranking points of the player.
- **%be_rank%** - Returns the main rank of the player.

### Time Left Placeholders
- **%be_daily_tl%** - Returns the remaining time for daily rewards.
- **%be_weekly_tl%** - Returns the remaining time for weekly rewards.
- **%be_monthly_tl%** - Returns the remaining time for monthly rewards.
- **%be_event_tl%** - Returns the remaining time for the current event, if active.

### Player Specific Ranking Points
- **%be_player_daily%** - Returns the daily ranking points of the player.
- **%be_player_weekly%** - Returns the weekly ranking points of the player.
- **%be_player_monthly%** - Returns the monthly ranking points of the player.
- **%be_player_event%** - Returns the event ranking points of the player, if an event is active.

### Player Specific Ranks
- **%be_rank_daily%** - Returns the daily rank of the player.
- **%be_rank_weekly%** - Returns the weekly rank of the player.
- **%be_rank_monthly%** - Returns the monthly rank of the player.
- **%be_rank_event%** - Returns the event rank of the player, if an event is active.

### Top Player Placeholders
- **%be_points_top{n}%** - Returns the points of the player at the nth position in the main ranking.
- **%be_player_top{n}%** - Returns the name of the player at the nth position in the main ranking.

### Daily, Weekly, Monthly, and Event Specific
- **%be_daily_points_top{n}%** - Returns the points of the player at the nth position in the daily ranking.
- **%be_weekly_points_top{n}%** - Returns the points of the player at the nth position in the weekly ranking.
- **%be_monthly_points_top{n}%** - Returns the points of the player at the nth position in the monthly ranking.
- **%be_event_points_top{n}%** - Returns the points of the player at the nth position in the event ranking, if an event is active.

### Miscellaneous
- **%be_daily_player_top{n}%** - Returns the name of the player at the nth position in the daily ranking.
- **%be_weekly_player_top{n}%** - Returns the name of the player at the nth position in the weekly ranking.
- **%be_monthly_player_top{n}%** - Returns the name of the player at the nth position in the monthly ranking.
- **%be_event_player_top{n}%** - Returns the name of the player at the nth position in the event ranking, if an event is active.

These placeholders can be used to display dynamic content related to rankings and events within your server. Ensure that PlaceholderAPI is installed and configured properly to utilize these placeholders.
