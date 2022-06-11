package com.sonianicoletti.progettolam.ui.game.accusation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sonianicoletti.progettolam.extension.emit
import com.sonianicoletti.progettolam.ui.game.cards.CardItem
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AccusationViewModel @Inject constructor() : ViewModel() {

    private val viewStateEmitter = MutableLiveData(ViewState())
    val viewState: LiveData<ViewState> = viewStateEmitter

    fun selectCard(cardItem: CardItem) {
        when (cardItem.card.type) {
            "character" -> viewState.value?.selectedCharacterCard = cardItem
            "weapon" -> viewState.value?.selectedWeaponCard = cardItem
            "room" -> viewState.value?.selectedRoomCard = cardItem
        }
        viewStateEmitter.emit()
    }

    fun accuse() {

    }

    data class ViewState(
        var selectedCharacterCard: CardItem? = null,
        var selectedWeaponCard: CardItem? = null,
        var selectedRoomCard: CardItem? = null,
    )
}