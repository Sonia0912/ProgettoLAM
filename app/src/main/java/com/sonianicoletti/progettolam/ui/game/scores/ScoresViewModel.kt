package com.sonianicoletti.progettolam.ui.game.scores

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonianicoletti.entities.Score
import com.sonianicoletti.usecases.repositories.GameRepository
import com.sonianicoletti.usecases.servives.UserService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScoresViewModel @Inject constructor(
    gameRepository: GameRepository,
    userService: UserService,
) : ViewModel() {

    private val viewStateEmitter = MutableLiveData<ViewState>()
    val viewState: LiveData<ViewState> = viewStateEmitter

    init {
        viewModelScope.launch {
            val game = gameRepository.getOngoingGame()
            val players = game.players

            val rankings = mutableListOf<Score>()
            val winningPlayer = players.first { it.id == game.winner }
            rankings.add(Score(winningPlayer.displayName, ordinalsList[0]))

            var position = 1
            game.players.filterNot { it.id == winningPlayer.id }.let { players ->
                players.filterNot { it.id in game.losers }.forEach {
                    rankings.add(Score(it.displayName, ordinalsList[position++]))
                }
                players.filter { it.id in game.losers }.reversed().forEach {
                    rankings.add(Score(it.displayName, ordinalsList[position++]))
                }
            }

            val users = userService.getUsers(game.players.map { it.id }).toMutableList().apply { sortByDescending { it.score } }
            val totalScores = users.map { Score(it.displayName, it.score.toString()) }
            viewStateEmitter.postValue(ViewState(rankings, totalScores))
        }
    }

    data class ViewState(
        val rankings: List<Score>,
        val totalScores: List<Score>,
    )

    companion object {
        private val ordinalsList = listOf("1st", "2nd", "3rd", "4th", "5th", "6th")
    }
}