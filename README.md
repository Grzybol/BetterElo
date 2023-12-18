# BetterElo
Minecraft PvP Elo rank system
Plugin created by Grzybol
Minecraft version 1.18.2+
Dependecies: PlaceholderAPI

There are no special permissions required to use below Player commands. 

BetterElo commands:
- /be                  [ENG]Returns your ranking info.                                     [PL]Zwraca info o Twoim rankingu
- /be <player>  [ENG]Returns ranking info about given player.             [PL]Zwraca info rankingu danego gracza.
- /be info              [ENG]Returns plugin info.                                             [PL]Zwraca info o pluginie.
- /be top<n>      [ENG]Returns ranking info abot player at given postion. [PL]Zwraca info o graczu na danym miejscu w rankingu
- /be top10      [ENG]Returns top10 players from ranking                 [PL]Zwraca info o top10 graczy rankingu
- /be claim      [ENG]Claim your rewards! Remeber to empty your eq!!   [PL]Odbierz swoje nagrody! Pameitaj wyczyścić eq przed!!
- /be timeleft      [ENG]Returns time left to giveaway                                 [PL]Zwraca pozostały czas do rozdania nagród
- /be daily      [ENG]Returns top10 daily ranking                                 [PL]Zwraca info o top10 rankingu dziennego
- /be weekly      [ENG]Returns top10 weekly ranking                                 [PL]Zwraca info o top10 rankingu tygodniowego
- /be monthly   [ENG]Returns top10 monthly ranking                         [PL]Zwraca info o top10 rankingu miesięcznego

Only for OP

- /be setrewards - Opens rewards GUI configuration for all timed rankings (daily,weekly,monthly) and positions (top1,top2,top3,top4-10). IMPORTANT! "Reset" button is resetting specified ranking type timer without handing out prizes. "Redeem" is both resetting timer and handing out prizes.

Placeholders:
IMPORTANT!! I hardcoded 10s interval for checking placeholders - lemme know if you want it to be configurable in config file
- %be_player% returns player's main ranking points
- %be_rank% returns player's main ranking
- %be_player% returns player's daily ranking points
- %be_rank% returns player's daily ranking
- %be_player% returns player's weekly rankingpoints
- %be_rank% returns player's weekly ranking
- %be_player% returns player's monthly ranking points
- %be_rank% returns player's monthly ranking
- %be_player_top<n>% returns player's nickname at <n> position in ranking
- %be_points_top<n>% returns player's points at <n> position in ranking
- %be_daily_tl% returns time left on daily rewards timer - automatically converts to D-M-S format
- %be_weekly_tl% returns time left on daily rewards timer - automatically converts to D-M-S format
- %be_monthly_tl% returns time left on daily rewards timer - automatically converts to D-M-S format
