```mermaid
sequenceDiagram
    title System Sequence Diagram: Collect Dropped Gold Bag

    actor Player
    participant System

    alt Gold bag is present on map
        alt Player clicks on the gold bag within 10 seconds
            Player->>System: click on dropped gold bag
            System->>Player: remove gold bag from map
            System->>System: update player's gold balance
        else Player don't click on the gold bag within 10 seconds
            System->>Player: remove gold bag from map
        end
    else No gold bag present
        Player->>System: click on empty space
        System->>Player: no action
    end
