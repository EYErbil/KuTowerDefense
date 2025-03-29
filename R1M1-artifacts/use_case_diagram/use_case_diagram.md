```mermaid
%% This syntax is experimental and may not be supported in all Mermaid environments
useCaseDiagram
title: KU Tower Defense - Phase I Use Case Diagram

actor Player as "Player"
actor GameSystem as "Game System"

usecase UC1 as "Start New Game"
usecase UC2 as "Open Level Editor"
usecase UC3 as "Open Options"
usecase UC4 as "Quit Game"
usecase UC5 as "Create Custom Map"
usecase UC6 as "Save Map"
usecase UC7 as "Load Map"
usecase UC8 as "Construct Tower"
usecase UC9 as "Sell Tower"
usecase UC10 as "Pause Game"
usecase UC11 as "Toggle Game Speed"
usecase UC12 as "View Tower Range"
usecase UC13 as "Configure Game Options"
usecase UC14 as "Game Over"

Player --> UC1
Player --> UC2
Player --> UC3
Player --> UC4

UC2 --> UC5
UC5 --> UC6

UC1 --> UC7

Player --> UC8
Player --> UC9
Player --> UC10
Player --> UC11
Player --> UC12

UC3 --> UC13

GameSystem --> UC14

%% "include" relationships:
UC5 ..> place_path : <<include>>
UC5 ..> place_tower_slots : <<include>>
UC5 ..> mark_start_end : <<include>>

UC8 ..> select_tower_type : <<include>>

%% "extend" relationship:
validate_map ..|> UC6 : <<extend>>
``` 