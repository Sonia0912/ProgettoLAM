package com.sonianicoletti.progettolam.ui.game.accusation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.sonianicoletti.entities.Card
import com.sonianicoletti.entities.Cards
import com.sonianicoletti.progettolam.databinding.FragmentAccusationBinding
import com.sonianicoletti.progettolam.ui.game.cards.CardItem
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AccusationFragment : Fragment() {

    private lateinit var binding: FragmentAccusationBinding
    private val viewModel: AccusationViewModel by viewModels()

    private val charactersAdapter = AccusationCardsAdapter(Cards.characters.mapToCardItems()) { viewModel.selectCard(it) }
    private val weaponsAdapter = AccusationCardsAdapter(Cards.weapons.mapToCardItems()) { viewModel.selectCard(it) }
    private val roomsAdapter = AccusationCardsAdapter(Cards.rooms.mapToCardItems()) { viewModel.selectCard(it) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentAccusationBinding.inflate(inflater)
        initCardsLists()
        observeViewState()
        binding.accuseButton.setOnClickListener { viewModel.accuse() }
        return binding.root
    }

    private fun initCardsLists() {
        binding.characterCards.adapter = charactersAdapter
        binding.weaponCards.adapter = weaponsAdapter
        binding.roomCards.adapter = roomsAdapter
    }

    private fun List<Card>.mapToCardItems() = map { CardItem.fromCard(it) }

    private fun observeViewState() = viewModel.viewState.observe(viewLifecycleOwner) { state ->
        state.selectedCharacterCard?.let {
            charactersAdapter.setSelectedCard(it)
            binding.accusationCharacterCard.cardImage.setImageResource(it.imageRes)
            binding.accusationCharacterCard.root.isVisible = true
            binding.accusationCharacterText.text = it.card.name
        }
        state.selectedWeaponCard?.let {
            weaponsAdapter.setSelectedCard(it)
            binding.accusationWeaponCard.cardImage.setImageResource(it.imageRes)
            binding.accusationWeaponCard.root.isVisible = true
            binding.accusationWeaponText.text = it.card.name
        }
        state.selectedRoomCard?.let {
            roomsAdapter.setSelectedCard(it)
            binding.accusationRoomCard.cardImage.setImageResource(it.imageRes)
            binding.accusationRoomCard.root.isVisible = true
            binding.accusationRoomText.text = it.card.name
        }

        binding.accuseButton.isEnabled = state.selectedCharacterCard != null && state.selectedRoomCard != null && state.selectedWeaponCard != null
    }
}
