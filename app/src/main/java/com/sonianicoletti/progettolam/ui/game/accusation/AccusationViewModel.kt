package com.sonianicoletti.progettolam.ui.game.accusation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonianicoletti.entities.Player
import com.sonianicoletti.progettolam.extension.emit
import com.sonianicoletti.progettolam.ui.game.GameState
import com.sonianicoletti.progettolam.ui.game.cards.CardItem
import com.sonianicoletti.usecases.repositories.GameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccusationViewModel @Inject constructor(
    private val gameRepository: GameRepository,
) : ViewModel() {

    private val viewStateEmitter = MutableLiveData(ViewState())
    val viewState: LiveData<ViewState> = viewStateEmitter

    init {
        viewModelScope.launch {
            viewStateEmitter.postValue(ViewState(isTurnPlayer = gameRepository.isCurrentTurn()))
        }
    }

    fun selectCard(cardItem: CardItem) {
        when (cardItem.card.type) {
            "character" -> viewState.value?.selectedCharacterCard = cardItem
            "weapon" -> viewState.value?.selectedWeaponCard = cardItem
            "room" -> viewState.value?.selectedRoomCard = cardItem
        }
        viewStateEmitter.emit()
    }

    fun accuse(isFinal: Boolean) = viewModelScope.launch {
        viewState.value?.apply {
            gameRepository.makeAccusation(
                selectedCharacterCard!!.card,
                selectedWeaponCard!!.card,
                selectedRoomCard!!.card,
                isFinal
            )
        }
    }

    fun handleGameState(gameState: GameState) {
        viewModelScope.launch {
            viewState.value?.isTurnPlayer = gameRepository.isCurrentTurn()
            viewState.value?.respondingPlayer = gameState.game.players.firstOrNull { it.id == gameState.game.accusation?.responder }
            viewStateEmitter.emit()
        }
    }

    data class ViewState(
        var isTurnPlayer: Boolean = false,
        var respondingPlayer: Player? = null,
        var selectedCharacterCard: CardItem? = null,
        var selectedWeaponCard: CardItem? = null,
        var selectedRoomCard: CardItem? = null,
    )
}