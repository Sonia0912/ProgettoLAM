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

    private lateinit var yourCardsAdapter: CardsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCardsBinding.inflate(inflater)
        initCardsAdapters()
        observeGameState()
        observeViewState()

        binding.skipButton.setOnClickListener {
            viewModel.skipAccusation()
        }

        return binding.root
    }

    private fun initCardsAdapters() {
        // Show the cards
        yourCardsAdapter = CardsAdapter()
        val leftoverCardsAdapter = CardsAdapter()
        yourCardsAdapter.setOnAccusationCardClickListener {
            viewModel.onAccusationCardClicked(it)
        }
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

            // se non ci sono carte rimanenti
            if (it.leftoverCards.size == 0) {
                binding.leftoverCardsText.isVisible = false
            }

            // se e' il proprio turno di fare l'accusa
            if (it.currentUserId == it.turnPlayer?.id) {
                binding.turnPlayerText.text = "Your turn"
                binding.skipButton.isVisible = false
            } else {
                binding.turnPlayerText.text = "Turn player: ${it.turnPlayer?.displayName.orEmpty()}"
            }

            if (it.respondingPlayer != null) {
                // se e' il proprio turno di rispondere alla accusa
                if (it.currentUserId == it.respondingPlayer.id) {
                    binding.accusingPlayerText.text = "You must now respond to the accusation"
                    yourCardsAdapter.setAccusationCards(it.accusationCards)
                    binding.skipButton.isVisible = it.canSkip
                } else {
                    binding.accusingPlayerText.text = "${it.respondingPlayer.displayName} is responding to the accusation"
                    binding.skipButton.isVisible = false
                }

                binding.accusingPlayerText.isVisible = true
                binding.accusationCardsLayout.isVisible = true
                binding.accusationCharacterCard.cardImage.setImageResource(it.accusationCards[0].imageRes)
                binding.accusationCharacterText.text = it.accusationCards[0].card.name
                binding.accusationWeaponCard.cardImage.setImageResource(it.accusationCards[1].imageRes)
                binding.accusationWeaponText.text = it.accusationCards[1].card.name
                binding.accusationRoomCard.cardImage.setImageResource(it.accusationCards[2].imageRes)
                binding.accusationRoomText.text = it.accusationCards[2].card.name
            } else {
                binding.accusingPlayerText.isVisible = false
                binding.accusationCardsLayout.isVisible = false
                binding.skipButton.isVisible = false
                yourCardsAdapter.setAccusationCards(null)
            }
        }
    }

    private fun RecyclerView.updateList(cards: List<CardItem>) {
        (adapter as CardsAdapter).updateList(cards)
    }
}