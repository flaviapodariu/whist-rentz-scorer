package com.example.whistrentzscorer.objects

enum class RentzInputType {
    SINGLE_PLAYER_CHECKBOX,  // King of Hearts, 10 of Clubs
    COUNT_PER_PLAYER,        // Queens, Diamonds, Tricks
    WHIST,
    RENTZ,
    TOTALE
}

enum class RentzMiniGame(
    val displayName: String,
    val description: String,
    val inputType: RentzInputType,
    val pointsPerUnit: Int,
    val totalPoints: Int,
    val maxCountPerPlayer: Int = 0  // for COUNT_PER_PLAYER type: max items a single player can take
) {
    KING_OF_HEARTS(
        displayName = "King of Hearts",
        description = "K♥ = -200 points",
        inputType = RentzInputType.SINGLE_PLAYER_CHECKBOX,
        pointsPerUnit = -200,
        totalPoints = -200
    ),
    QUEENS(
        displayName = "Queens",
        description = "-40 points per Queen",
        inputType = RentzInputType.COUNT_PER_PLAYER,
        pointsPerUnit = -40,
        totalPoints = -160,
        maxCountPerPlayer = 4
    ),
    DIAMONDS(
        displayName = "Diamonds",
        description = "-30 points per Diamond",
        inputType = RentzInputType.COUNT_PER_PLAYER,
        pointsPerUnit = -30,
        totalPoints = -390,
        maxCountPerPlayer = 13
    ),
    TRICKS(
        displayName = "Tricks",
        description = "-50 points per trick",
        inputType = RentzInputType.COUNT_PER_PLAYER,
        pointsPerUnit = -50,
        totalPoints = -650,
        maxCountPerPlayer = 13
    ),
    TOTALE(
        displayName = "Totale",
        description = "K♥ + Queens + Diamonds + Tricks combined",
        inputType = RentzInputType.TOTALE,
        pointsPerUnit = 0,
        totalPoints = -1400
    ),
    TEN_OF_CLUBS(
        displayName = "10 of Clubs",
        description = "10♣ = +200 points",
        inputType = RentzInputType.SINGLE_PLAYER_CHECKBOX,
        pointsPerUnit = 200,
        totalPoints = 200
    ),
    WHIST(
        displayName = "Whist",
        description = "+50 points per trick",
        inputType = RentzInputType.WHIST,
        pointsPerUnit = 50,
        totalPoints = 650,
        maxCountPerPlayer = 13
    ),
    RENTZ(
        displayName = "Rentz",
        description = "1st: +400, 2nd: +200, 3rd: +100, 4th: 0",
        inputType = RentzInputType.RENTZ,
        pointsPerUnit = 0,
        totalPoints = 700
    );

    companion object {
        fun rankPoints(rank: Int): Int = when (rank) {
            1 -> 400
            2 -> 200
            3 -> 100
            else -> 0
        }
    }
}
