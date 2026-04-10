package com.example.whistrentzscorer.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.whistrentzscorer.objects.Game
import com.example.whistrentzscorer.storage.entity.GameEntity
import com.example.whistrentzscorer.storage.repository.IGameRepository
import com.example.whistrentzscorer.utils.GameMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val gameRepository: IGameRepository
) : ViewModel() {

    private val _selectedGameMode = MutableStateFlow(GameMode.WHIST)
    val selectedGameMode: StateFlow<GameMode> = _selectedGameMode.asStateFlow()

    private val _gameToResume = MutableStateFlow<Game?>(null)
    val gameToResume: StateFlow<Game?> = _gameToResume.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val allGames: StateFlow<List<GameEntity>> = _selectedGameMode
        .flatMapLatest { mode -> gameRepository.allGamesByMode(mode) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadLastUnfinishedGame()
    }

    fun setGameMode(mode: GameMode) {
        _selectedGameMode.value = mode
        loadLastUnfinishedGame()
    }

    fun deleteGame(gameId: Int) {
        viewModelScope.launch {
            gameRepository.deleteGame(gameId)
            loadLastUnfinishedGame()
        }
    }

    fun loadLastUnfinishedGame() {
        viewModelScope.launch {
            val toResume = gameRepository.getLastUnfinishedGameByMode(_selectedGameMode.value)
            _gameToResume.value = toResume
        }
    }

}