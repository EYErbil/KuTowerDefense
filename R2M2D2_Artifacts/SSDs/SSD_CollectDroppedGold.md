```mermaid
sequenceDiagram
    title System Sequence Diagram: Collect Dropped Gold

    actor Player
    participant System

    alt Gold bag is present on map
        Player->>System: click on dropped gold bag
        alt Gold bag is still available
            System->>Player: remove gold bag from map, update player's gold balance, show collection animation
        else Gold bag already collected or expired
            System->>Player: display "gold not available" message
        end
    else No gold bag present
        Player->>System: click on empty space
        System->>Player: no action
    end
