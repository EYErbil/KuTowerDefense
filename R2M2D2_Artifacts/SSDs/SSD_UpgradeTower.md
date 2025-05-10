```mermaid
sequenceDiagram
    title System Sequence Diagram: Upgrade Tower

    actor Player
    participant System

    Player->>System: select tower on map
    System->>Player: display tower upgrade menu

    alt Player chooses to upgrade tower
        Player->>System: request tower upgrade
        alt Player has sufficient gold
            System->>System: upgrade tower features
            System->>Player: display new tower graphic
        else Player lacks sufficient gold
            System->>Player: display "insufficient gold" message
        end
    else Player cancels
        Player->>System: cancel upgrade
        System->>Player: close upgrade menu
    end
