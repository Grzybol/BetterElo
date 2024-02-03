# BetterElo

Minecraft PvP Elo rank system Plugin created by Grzybol

Minecraft version 1.18.2+

Dependencies: PlaceholderAPI, BetterRanks

## Player Commands

There are no special permissions required to use the following Player commands.

### BetterElo commands:

- `/be`
  - [ENG] Returns your ranking info.
  - [PL] Zwraca info o Twoim rankingu

- `/be <player>`
  - [ENG] Returns ranking info about the given player.
  - [PL] Zwraca info rankingu danego gracza.

- `/be info`
  - [ENG] Returns plugin info.
  - [PL] Zwraca info o pluginie.

- `/be top<n>`
  - [ENG] Returns ranking info about the player at the given position.
  - [PL] Zwraca info o graczu na danym miejscu w rankingu.

- `/be top10`
  - [ENG] Returns top 10 players from the ranking.
  - [PL] Zwraca info o top 10 graczy rankingu.

- `/be claim`
  - [ENG] Claim your rewards! Remember to empty your inventory!!
  - [PL] Odbierz swoje nagrody! Pamiętaj wyczyścić ekwipunek przed!!

- `/be timeleft`
  - [ENG] Returns time left to the giveaway.
  - [PL] Zwraca pozostały czas do rozdania nagród.

- `/be daily`
  - [ENG] Returns top 10 daily ranking.
  - [PL] Zwraca info o top 10 rankingu dziennego.

- `/be weekly`
  - [ENG] Returns top 10 weekly ranking.
  - [PL] Zwraca info o top 10 rankingu tygodniowego.

- `/be monthly`
  - [ENG] Returns top 10 monthly ranking.
  - [PL] Zwraca info o top 10 rankingu miesięcznego.

## Only for OP

- `/be setrewards`
  - Opens rewards GUI configuration for all timed rankings (daily, weekly, monthly) and positions (top1, top2, top3, top4-10). IMPORTANT! "Reset" button is resetting the specified ranking type timer without handing out prizes. "Redeem" is both resetting the timer and handing out prizes.

- `/be ban <player>`
- `/be add <player> <points> <main/daily/weekly/monthly>`
- `/be sub <player> <points> <main/daily/weekly/monthly>`

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
