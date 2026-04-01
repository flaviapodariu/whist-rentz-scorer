# Romanian Whist & Rentz Scorer - Technical Documentation

## Project Overview
A specialized Android score-tracking application for two Romanian trick-taking card games:
- **Romanian Whist (Nomination)** - A sequential round-based game with bidding
- **Rentz** - A compilation game consisting of 8 distinct sub-games

**Tech Stack:**
- Kotlin + Jetpack Compose (Material3)
- MVVM Architecture
- Room Database (SQLite)
- Hilt Dependency Injection
- Coroutines & Flow

---

## Game Rules & Scoring

### Romanian Whist (Nomination)

**Structure:**
- Sequence of rounds with varying card counts (e.g., 1→1→1→1→2→3...→8→8→...→1→1→1→1)
- Rounds with 1 or 8 cards are played N times (where N = number of players)
- Total rounds = `playerCount * 3 + 12`

**Bidding Phase:**
- Players predict exactly how many tricks they'll take
- **"Stuck" Rule:** Sum of all bids ≠ total cards dealt (ensures at least one player fails)
- Last player to bid cannot choose the value that would make the sum equal to cards dealt

**Scoring:**
- **Success (Bid == Made):** `5 + tricks_made`
- **Failure (Bid ≠ Made):** `-|bid - made|`

**Integrity Check:**
- Total tricks made by all players must equal cards dealt

---

### Rentz (The Compilation)

**Structure:**
- 8 sub-games total
- Each player "calls" one sub-game per round
- Once called, a sub-game cannot be called again by that player

**Sub-Games & Scoring:**

| Sub-Game | Scoring Rule | Total Points | Input Type |
|----------|--------------|--------------|------------|
| **King of Hearts** | Player who takes K♥ gets -200 | -200 | Single checkbox |
| **Queens (Dame)** | -40 per Queen taken | -160 | Count per player (max 4) |
| **Diamonds (Caro)** | -30 per Diamond taken | -390 | Count per player (max 13) |
| **Tricks (Levata)** | -50 per trick taken | -650 | Count per player (max 13) |
| **Totale** | K♥ + Queens + Diamonds + Tricks combined | -1,400 | Combined form |
| **10 of Clubs** | Player who takes 10♣ gets +200 | +200 | Single checkbox |
| **Whist** | +50 per trick taken | +650 | Count per player (max 13) |
| **Rentz (The Race)** | 1st: +400, 2nd: +200, 3rd: +100, 4th+: 0 | +700 | Rank assignment |

---

## Architecture

### Database Layer

**Entity:** `GameEntity`
```kotlin
@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val players: String,           // Comma-separated player names
    val isFinished: Boolean,
    val scoresJson: String,        // Serialized game state
    val gameType: String = "whist" // "whist" or "rentz"
)
```

**Database:** `AppDB`
- Version: 4
- Migration 3→4: Added `gameType` column with default "whist"

**DAO:** `GameDao`
- `getAllGames()` / `getAllGamesByType(gameType: String)`
- `getLastUnfinishedGame()` / `getLastUnfinishedGameByType(gameType: String)`
- `addGame()`, `updateScore()`, `deleteGame()`

**Repository:** `GameRepository`
- Wraps DAO with Flow-based reactive streams
- Provides filtered queries by game type

---

### ViewModels

#### `HomeViewModel`
**State:**
- `selectedGameMode: MutableStateFlow<String>` - "whist" or "rentz"
- `gameToResume: StateFlow<Game?>` - Last unfinished game (filtered by mode)
- `allGames: StateFlow<List<GameEntity>>` - All games (filtered by mode)

**Methods:**
- `loadLastUnfinishedGame()`
- `deleteGame(gameId: Int)`

---

#### `GameConfigViewModel`
**State:**
- `players: SnapshotStateList<String>` - Player names
- `gameType: String` - Round sequence (e.g., "11..88..11")
- `gameMode: String` - "whist" or "rentz"
- `currentPlayerIndex: Int` - For paginated player setup

**Methods:**
- `setPlayerName(index: Int, name: String)`
- `getPlayerName(index: Int): String`
- `goToNextPlayer()` / `goToPrevPlayer()`
- `createNewGame(): Long` - Persists game to DB

---

#### `GameStateViewModel`
**State (Whist):**
- `game: GameState` - Round-by-round state map
- `playerList: List<String>`
- `totalRounds: Int`
- `currentRound: Int`
- `currentRoundCards: Int`
- `gameType: String`
- `gameId: Int`

**State (Rentz):**
- `gameMode: String` - "whist" or "rentz"
- `selectedMiniGame: RentzMiniGame?` - Currently selected sub-game
- `playedMiniGames: MutableSet<RentzMiniGame>` - Already played sub-games
- `rentzScores: MutableStateMap<String, Int>` - Cumulative scores per player

**Methods (Whist):**
- `init(players, gameType, gameMode)`
- `setBid(round, playerIndex, bid)`
- `setHandsTaken(round, playerIndex, hands)`
- `saveRoundScore(round)` - Calculates and persists scores
- `advanceRound()`
- `undoLastTurn()`
- `autoSave()` - Serializes to JSON and updates DB

**Methods (Rentz):**
- `selectMiniGame(miniGame: RentzMiniGame)`
- `submitRentzRoundScores(roundScores: Map<String, Int>)`
- `isRentzGame(): Boolean`

**Serialization:**
```kotlin
data class SaveData(
    val currentRound: Int,
    val gameType: String,
    val state: Map<String, Map<String, SerializableRoundState>>
)
```

---

### Navigation

**Routes:**
```kotlin
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object PlayersSetup : Screen("playersSetup/{game}")
    object GameSetup : Screen("gameSetup")
    object ScoreSheet : Screen("scoresheet")
    object RoundAction : Screen("round/{action}/{cards}")
    object GamesHistory : Screen("history")
    object MiniGameSelection : Screen("miniGameSelection")
    object RentzResult : Screen("rentzResult/{miniGame}")
}
```

**Flow (Whist):**
```
Home → PlayersSetup → GameSetup → ScoreSheet
                                      ↓
                                  RoundAction (Bid)
                                      ↓
                                  RoundAction (Results)
                                      ↓
                                  ScoreSheet (updated)
```

**Flow (Rentz):**
```
Home → PlayersSetup → GameSetup → ScoreSheet
                                      ↓
                                  MiniGameSelection
                                      ↓
                                  RentzResult (specific input screen)
                                      ↓
                                  ScoreSheet (updated scores)
```

---

## UI Components

### Home Screen
- Game mode toggle slider (Whist ↔ Rentz)
- "New Game" button
- "Resume" button (enabled if unfinished game exists)
- "Games History" button

### Players Setup Screen
- Paginated per-player input
- Text field for name entry
- Previous/Next arrows for navigation
- Player counter
- Duplicate name validation
- "Continue" button (enabled when ≥3 players)

### Game Setup Screen (Whist only)
- Game type selector (11..88..11, 88..11..88, etc.)
- "Start Game" button

### Score Sheet
**Whist Mode:**
- Landscape orientation
- Top bar with "Bid" and "Input Results" buttons
- Scrollable table: Round | Player1 | Player2 | ... | PlayerN
- Each cell shows: Bid / Hands Taken / Score
- Undo button

**Rentz Mode:**
- Top bar with "Select Mini Game" button
- Display of played mini-games (checkmarks)
- Cumulative score table per player

### Round Action Screen (Whist)
- Player name display
- Value selector (0-8 buttons with animations)
- Previous/Next player navigation
- "Stuck" rule enforcement (last player's illegal bid crossed out)
- Validation: total hands taken must equal cards dealt

### Mini Game Selection Screen (Rentz)
- Grid of 8 cards showing all sub-games
- Each card displays:
  - Name (e.g., "King of Hearts")
  - Description (e.g., "K♥ = -200 points")
  - Total points (+/- value)
  - Checkmark if already played (dimmed)
- Tap to select (disabled if already played)

### Rentz Result Input Screens

**SinglePlayerCheckboxScreen** (King of Hearts, 10 of Clubs)
- List of players with checkboxes
- Only one player can be selected
- Submit button

**CountPerPlayerScreen** (Queens, Diamonds, Tricks)
- List of players with +/- buttons
- Current count display per player
- Total validation (must equal max items)
- Submit button (enabled when total is correct)

**RentzWhistScreen** (Whist sub-game)
- Same as CountPerPlayerScreen
- Must total 13 tricks
- Scoring: +50 per trick

**RentzRankScreen** (Rentz/The Race)
- List of players with rank dropdowns
- Dropdown shows: "1st — 400 pts", "2nd — 200 pts", etc.
- Used ranks are disabled
- Submit button (enabled when all ranks assigned)

**TotaleScreen**
- Combined form with 4 sections:
  1. King of Hearts (checkbox)
  2. Queens (count per player, total 4)
  3. Diamonds (count per player, total 13)
  4. Tricks (count per player, total 13)
- Submit button (enabled when all sections valid)

### Games History Screen
- List of completed/in-progress games
- Swipe-to-delete with confirmation dialog
- Tap to resume/view game
- Shows: Date, Players, Final Scores

---

## Data Models

### Rentz Mini-Games
```kotlin
enum class RentzInputType {
    SINGLE_PLAYER_CHECKBOX,  // One player selection
    COUNT_PER_PLAYER,        // Count items per player
    WHIST,                   // Tricks count (special case)
    RANK,                    // Position assignment
    TOTALE                   // Combined form
}

enum class RentzMiniGame(
    val displayName: String,
    val description: String,
    val inputType: RentzInputType,
    val pointsPerUnit: Int,
    val totalPoints: Int,
    val maxCountPerPlayer: Int = 0
) {
    KING_OF_HEARTS(...),
    QUEENS(...),
    DIAMONDS(...),
    TRICKS(...),
    TOTALE(...),
    TEN_OF_CLUBS(...),
    WHIST(...),
    RENTZ(...)
}
```

### Game State
```kotlin
data class GameState(
    var state: MutableMap<Int, MutableMap<String, RoundState>>
)

data class RoundState(
    var score: Int? = null,
    var bid: Int? = null,
    var handsTaken: Int? = null
)
```

---

## Key Implementation Details

### Whist Scoring Logic
```kotlin
fun whistScoring(bid: Int, handsTaken: Int): Int {
    if (bid == handsTaken) {
        return 5 + handsTaken
    }
    return -abs(bid - handsTaken)
}
```

### Round Card Calculation
```kotlin
fun cardsThisRound(round: Int, gameType: String): Int {
    // Parses gameType (e.g., "11..88..11")
    // Returns card count for given round based on:
    // - Starting rounds (1 or 8 cards × playerCount)
    // - Up phase (2-7 cards)
    // - Middle rounds (8 or 1 cards × playerCount)
    // - Down phase (7-2 cards)
    // - Ending rounds (1 or 8 cards × playerCount)
}
```

### Rentz Score Accumulation
```kotlin
fun submitRentzRoundScores(roundScores: Map<String, Int>) {
    val miniGame = selectedMiniGame ?: return
    roundScores.forEach { (player, score) ->
        rentzScores[player] = (rentzScores[player] ?: 0) + score
    }
    playedMiniGames.add(miniGame)
    selectedMiniGame = null
    autoSave()
}
```

### Auto-Save Mechanism
- Triggered on: `advanceRound()`, `undoLastTurn()`, `submitRentzRoundScores()`
- Serializes entire game state to JSON
- Updates `scoresJson` field in database
- Enables game resumption after app restart

### Game Restoration
```kotlin
fun restoreGame(id: Int, players: List<String>, scoresJson: String) {
    // Deserializes SaveData from JSON
    // Reconstructs GameState with all rounds
    // Restores currentRound, gameType, scores
    // Falls back to fresh init if parsing fails
}
```

---

## UI/UX Design Principles

1. **Fat-Finger Friendly:** Large buttons and touch targets (card players often have snacks/drinks)
2. **Landscape for Score Sheet:** Better table visibility
3. **Minimal Text Input:** Prefer buttons, sliders, checkboxes
4. **Clear Visual Feedback:** Animations, color coding, disabled states
5. **Validation Before Submit:** Prevent invalid data entry
6. **Confirmation Dialogs:** For destructive actions (delete, undo)

---

## Color Scheme
- **Primary:** Teal/Coral tones
- **Success:** Green (correct bids)
- **Error:** Red (failed bids, validation errors)
- **Disabled:** Gray with reduced opacity
- **Selected:** Orange40 with scale animation

---

## Known Limitations & Future Work

### Current Limitations
1. **Rentz Score Sheet Display:** Not yet implemented (scores tracked but not displayed)
2. **Rentz Persistence:** Save/restore logic for Rentz games needs extension
3. **Game Completion:** No "finish game" flow for Rentz
4. **Player Limit:** Hardcoded max 6 players (UI constraint)

### Planned Features
1. Rentz score sheet with cumulative totals
2. Mini-game history display
3. Statistics & analytics
4. Export scores (PDF, CSV)
5. Multiplayer sync (optional)
6. Custom game type configurations
7. Themes & customization

---

## Development Notes

### Common Pitfalls
1. **Platform Declaration Clash:** Avoid methods named `setXxx` when property has auto-generated setter
2. **Landscape Orientation:** Remember to lock orientation for ScoreSheet
3. **State Hoisting:** ViewModels are activity-scoped via Hilt
4. **Navigation Back Stack:** Use `popBackStack(route, inclusive=false)` to return to specific screen
5. **Gson Serialization:** Ensure all data classes are serializable

### Testing Checklist
- [ ] Whist: Full game from start to finish
- [ ] Whist: Undo functionality
- [ ] Whist: "Stuck" rule enforcement
- [ ] Whist: Save/resume game
- [ ] Rentz: All 8 mini-games input flows
- [ ] Rentz: Duplicate mini-game prevention
- [ ] Rentz: Score accumulation
- [ ] Both: Swipe-to-delete with confirmation
- [ ] Both: Game mode switching
- [ ] Both: Player name validation

---

## File Structure

```
app/src/main/java/com/example/whistrentzscorer/
├── components/
│   ├── HomeScreen.kt
│   ├── GameSetup.kt (PlayersSetupScreen, GameSetupScreen)
│   ├── ScoreSheet.kt
│   ├── RoundActionComponent.kt
│   ├── GamesHistory.kt
│   ├── MiniGameSelectionScreen.kt
│   └── RentzResultScreens.kt
├── navigation/
│   ├── AppNavigation.kt
│   └── Screen.kt
├── viewmodels/
│   ├── HomeViewModel.kt
│   ├── GameConfigViewModel.kt
│   └── GameStateViewModel.kt
├── storage/
│   ├── entity/
│   │   └── GameEntity.kt
│   ├── dao/
│   │   └── GameDao.kt
│   ├── repository/
│   │   └── GameRepository.kt
│   ├── AppDB.kt
│   └── RoomModule.kt
├── objects/
│   ├── Game.kt
│   └── RentzMiniGame.kt
├── ui/
│   ├── WhistTopBar.kt
│   └── theme/
│       ├── Color.kt
│       └── Theme.kt
└── utils/
    └── DateFormatter.kt
```

---

## Terminology

- **Bid:** Predicted number of tricks (Whist)
- **Made / Hands Taken:** Actual tricks won
- **Stuck Rule:** Sum of bids ≠ cards dealt
- **Round:** One complete cycle of bidding + playing
- **Mini-Game / Sub-Game:** One of the 8 Rentz game types
- **Totale:** Combined negative mini-game in Rentz

---

*Last Updated: March 31, 2026*
