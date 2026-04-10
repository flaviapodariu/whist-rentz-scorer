package com.example.whistrentzscorer.viewmodels.whist.state

data class RoundState(
    var score: Int? = null,
    var bid: Int? = null,
    var handsTaken: Int? = null,
    var consecutiveCorrectBids: Int = 0,
    var consecutiveFailedBids: Int = 0,
    var bonusAdjustment: ScoreAdjustment = ScoreAdjustment.NONE  // +1 for bonus awarded, -1 for bonus deducted, 0 for no adjustment
) {
    fun toVO(): RoundStateVO {
        return RoundStateVO(
            bid = bid,
            handsTaken = handsTaken,
            score = score,
            consecutiveCorrectBids = consecutiveCorrectBids,
            consecutiveFailedBids = consecutiveFailedBids,
            bonusAdjustment = bonusAdjustment
        )
    }
}