package com.example.whistrentzscorer.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.whistrentzscorer.objects.Game
import com.example.whistrentzscorer.storage.repository.GameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val gameRepository: GameRepository
) : ViewModel() {

    private val _gameToResume = MutableStateFlow<Game?>(null)
    val gameToResume: StateFlow<Game?> = _gameToResume.asStateFlow()

    init {
        loadLastUnfinishedGame()
    }

    private fun loadLastUnfinishedGame() {
        viewModelScope.launch {
            val toResume = gameRepository.getLastUnfinishedGame()
            _gameToResume.value = toResume
        }
    }

}