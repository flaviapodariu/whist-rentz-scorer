package com.example.whistrentzscorer.viewmodels.whist.state

import androidx.compose.runtime.mutableStateMapOf

data class GameState(
    var state: MutableMap<Int, MutableMap<String, RoundState>> = mutableStateMapOf()
)