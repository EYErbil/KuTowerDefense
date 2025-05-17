''' mermaid 
sequenceDiagram
    participant Player
    participant UI
    participant Tower
    participant GameState

    Player->>UI: Clicks on Tower
    UI->>Tower: Request upgrade info
    Tower->>UI: Provides upgrade cost and level
    UI->>GameState: Query player gold balance and upgrade cost
    GameState-->>UI: Return gold balance and upgrade cost
    UI->>UI: Determine if upgrade is affordable
    UI-->>Player: Display upgrade menu (option enabled/disabled)
    Player->>UI: Selects upgrade option
    UI->>Tower: Request upgrade
    Tower->>GameState: Deduct upgrade cost
    GameState->>GameState: Check gold >= cost
    alt Gold is sufficient
        GameState-->>Tower: Gold deducted, return success
        Tower->>Tower: Upgrade to Level 2
        Tower-->>UI: Notify upgrade complete
        UI->>UI: Update tower graphic/info display
    else Gold is insufficient
        GameState-->>Tower: Return failure (insufficient gold)
        Tower-->>UI: Notify upgrade failed
        UI-->>Player: Display "Insufficient Gold" message (Optional)
    end
