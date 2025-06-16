```mermaid
graph TD
    %% This is just a title node (invisible)
    title["KU Tower Defense - Use Case Diagram"]
    style title fill:none,stroke:none

    %% Actors
    Player(["/‾\\<br>|<br>/ \\<br><br>Player"]):::actor
    GameSystem(["Game System<br>(Actor)"]):::actor

    %% Main Use Cases (elliptical style)
    UC1["Start New Game"]:::usecase
    UC2["Open Level Editor"]:::usecase
    UC3["Open Options"]:::usecase
    UC4["Quit Game"]:::usecase
    UC5["Create Custom Map"]:::usecase
    UC6["Save Map"]:::usecase
    UC7["Load Map"]:::usecase
    UC8["Construct Tower"]:::usecase
    UC9["Sell Tower"]:::usecase
    UC10["Pause Game"]:::usecase
    UC11["Toggle Game Speed"]:::usecase
    UC12["View Tower Range"]:::usecase
    UC13["Configure Game Options"]:::usecase
    UC14["Game Over"]:::usecase
    UC15["Collect Dropped Gold"]:::usecase
    UC16["Upgrade Tower"]:::usecase
    

    %% Relationship lines
    Player --> UC1
    Player --> UC2
    Player --> UC3
    Player --> UC4
    
    UC2 --> UC5
    UC5 --> UC6
    
    UC1 --> UC7
    
    Player --> UC8
    UC8 --> UC9
    Player --> UC10
    Player --> UC11
    Player --> UC12
    
    UC3 --> UC13
    
    GameSystem --> UC14
    Player --> UC15
    UC8 --> UC16

    %% "include" relationships
    place_path["Place Path Tiles"]:::include
    place_tower_slots["Place Tower Slots"]:::include
    mark_start_end["Mark Start/End Points"]:::include
    UC5 -- include --> place_path
    UC5 -- include --> place_tower_slots
    UC5 -- include --> mark_start_end

    select_tower_type["Select Tower Type"]:::include
    UC8 -- include --> select_tower_type

    %% "extend" relationship
    validate_map["Validate Map"]:::extend
    validate_map -- extend --> UC6

    %% Class definitions for different “types” of nodes
    classDef actor stroke:#333,stroke-dasharray:3 3,fill:#fff,rx:5,ry:5
    classDef usecase stroke:#333,fill:#f8f8f8,rx:20,ry:20
    classDef include stroke-dasharray:1 1,fill:#fff,rx:20,ry:20
    classDef extend stroke-dasharray:2 2,fill:#fff,rx:20,ry:20
