package com.sonianicoletti.progettolam.ui.game.cards

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.sonianicoletti.progettolam.databinding.FragmentCardsBinding
import com.sonianicoletti.progettolam.ui.game.GameViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CardsFragment : Fragment() {

    private lateinit var binding: FragmentCardsBinding
    private val viewModel: CardsViewModel by viewModels()
    private val gameViewModel: GameViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCardsBinding.inflate(inflater)
        initCardsAdapters()
        observeGameState()
        observeViewState()
        return binding.root
    }

    private fun initCardsAdapters() {
        // Show the cards
        val yourCardsAdapter = CardsAdapter()
        val leftoverCardsAdapter = CardsAdapter()
        binding.recyclerViewYourCards.adapter = yourCardsAdapter
        binding.recyclerViewLeftoverCards.adapter = leftoverCardsAdapter
    }

    private fun observeGameState() {
        gameViewModel.gameState.observe(viewLifecycleOwner) {
            viewModel.handleGameState(it)
        }
    }

    private fun observeViewState() {
        viewModel.viewState.observe(viewLifecycleOwner) {
            binding.recyclerViewYourCards.updateList(it.yourCards)
            binding.recyclerViewLeftoverCards.updateList(it.leftoverCards)
            binding.turnPlayerText.text = "Turn player: ${it.turnPlayer?.displayName.orEmpty()}"

            if (it.respondingPlayer != null) {
                binding.accusingPlayerText.isVisible = true
                binding.accusationCardsLayout.isVisible = true
                binding.accusingPlayerText.text = "${it.respondingPlayer.displayName} is responding to the accusation"
                binding.accusationCharacterCard.cardImage.setImageResource(it.accusationCards[0].imageRes)
                binding.accusationCharacterText.text = it.accusationCards[0].card.name
                binding.accusationWeaponCard.cardImage.setImageResource(it.accusationCards[1].imageRes)
                binding.accusationWeaponText.text = it.accusationCards[1].card.name
                binding.accusationRoomCard.cardImage.setImageResource(it.accusationCards[2].imageRes)
                binding.accusationRoomText.text = it.accusationCards[2].card.name
                binding.denyButton.isVisible = true
                binding.denyButton.isEnabled = it.canDeny
            } else {
                binding.accusingPlayerText.isVisible = false
                binding.accusationCardsLayout.isVisible = false
                binding.denyButton.isVisible = false
            }
        }
    }

    private fun RecyclerView.updateList(cards: List<CardItem>) {
        (adapter as CardsAdapter).updateList(cards)
    }
}