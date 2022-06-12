package com.sonianicoletti.progettolam.ui.game.scores

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sonianicoletti.entities.Score
import com.sonianicoletti.usecases.repositories.GameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ScoresViewModel @Inject constructor(
    gameRepository: GameRepository
) : ViewModel() {

    private val viewStateEmitter = MutableLiveData<ViewState>()
    val viewState: LiveData<ViewState> = viewStateEmitter

    init {
        val game = gameRepository.getOngoingGame()
        val players = game.players

        val rankings = mutableListOf<Score>()
        rankings.add(Score(players.first { it.id == game.winner }.displayName, ordinalsList[0]))
        game.losers.forEachIndexed { index, loser ->
            val loserPlayer = players.find { it.id == loser}
            loserPlayer?.let { rankings.add(Score(loserPlayer.displayName, ordinalsList[index + 1])) }
        }

        val totalScores = mutableListOf(
            Score("Player 1", "100"),
            Score("Player 2", "200"),
            Score("Player 3", "300"),
        )

        viewStateEmitter.postValue(ViewState(rankings, totalScores))
    }

    data class ViewState(
        val rankings: List<Score>,
        val totalScores: List<Score>,
    )

    companion object {
        private val ordinalsList = listOf("1st", "2nd", "3rd", "4th", "5th", "6th")
    }
}