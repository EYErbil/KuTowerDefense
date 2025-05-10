```mermaid
sequenceDiagram
    title System Sequence Diagram: Upgrade Tower

    actor Player
    participant System

    Player->>System: select tower on map
    System->>Player: display tower information and upgrade option

    alt Player chooses to upgrade tower
        Player->>System: request tower upgrade
        alt Player has sufficient gold
            System->>Player: upgrade tower, update display with new stats and visuals
        else Player lacks sufficient gold
            System->>Player: display "insufficient gold" message
        end
    else Player cancels
        Player->>System: cancel upgrade
        System->>Player: close upgrade menu
    end
