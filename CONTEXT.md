# Romanian Whist and Rentz scorer app - Context

## Project Purpose
This application is a specialized score-tracking engine for two trick-taking card games:
Whist (specifically the Romanian/Nomination variant) and Rentz (a multi-round "compilation" game).
---

## Game Logic & Scoring Rules

### 1. Romanian Whist (Nomination)
* **Structure:** A sequence of rounds where players receive a specific number of cards (e.g., 1, 1, 1, 1, 2, 3... 8, 8, ... 1, 1, 1, 1).
* Each game with 1 or 8 cards in each player's hand will be played the number of times as there are players in the game. 
* **The Bidding Phase:** Players predict exactly how many tricks they will take.
    * **The "Stuck" Rule:** The sum of all bids **cannot** equal the total cards dealt in that round. This ensures at least one player fails.
* **The Scoring Phase:**
    * **Success (Bid == Made):** $5 + \text{Tricks Made}$
    * **Failure (Bid != Made):** $-\left| \text{Bid} - \text{Made} \right|$
* **Integrity Check:** Total `Tricks Made` by all players must equal `Cards Dealt`.

### 2. Rentz (The Compilation)
A match consists of 8 sub-games called by each player. Once a player "calls" a game, they cannot call it again.

| Sub-Game            | Point Value / Rules | Total Points |
|:--------------------| :--- | :--- |
| **King of Hearts**  | $K\heartsuit = -200$ points | -200 |
| **Queens (Dame)**   | -40 points per Queen | -160 |
| **Diamonds (Caro)** | -30 points per card | -390 |
| **Tricks (Levata)** | -50 points per trick taken | -650 |
| **Totale**          | Combined: $K\heartsuit$ + Queens + Diamonds + Tricks | -1,400 |
| **10 of Clubs**     | $10\clubsuit = +200$ points | +200 |
| **Whist**           | +50 points per trick taken | +650 |
| **Rentz**           | 1st: +400, 2nd: +200, 3rd: +100, 4th: 0 | +700 |
---

## 🏗️ Technical Architecture Constraints

todo
---

## 🤖 Instructions for AI Development
* **Logic First:** Prioritize the scoring engine (the "math") over the UI components.
* **UI/UX:** Design for "fat-finger" inputs. Card players often have snacks/drinks nearby; buttons should be large and inputs clear.
* **Terminology:** * `Bid`: Predicted tricks (Whist).
    * `Made`: Actual tricks won.
    * `Hands/ hands taken`: Tricks (this is a common translation from romanian)