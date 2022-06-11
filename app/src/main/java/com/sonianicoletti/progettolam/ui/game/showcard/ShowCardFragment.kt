package com.sonianicoletti.progettolam.ui.game.showcard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.sonianicoletti.progettolam.databinding.FragmentShowCardBinding
import com.sonianicoletti.progettolam.ui.game.GameViewModel
import com.sonianicoletti.progettolam.ui.game.cards.CardItem
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShowCardFragment : Fragment() {

    private lateinit var binding: FragmentShowCardBinding
    private val viewModel: ShowCardViewModel by viewModels()
    private val gameViewModel: GameViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentShowCardBinding.inflate(inflater)
        observeGameUpdates()
        return binding.root
    }

    private fun observeGameUpdates() = gameViewModel.gameState.observe(viewLifecycleOwner) { state ->
        val cardItem = arguments?.getSerializable(KEY_CARD) as? CardItem

        if (cardItem != null) {
            val turnPlayer = state.game.players.first { it.id == state.game.turnPlayerId }
            binding.card.cardName.text = cardItem.card.name
            binding.card.card.setImageResource(cardItem.imageRes)
            binding.headerText.text = "Enable NFC and hold your phone close to the phone of ${turnPlayer.displayName} to show them your card"
        } else {
            val respondingPlayer = state.game.players.first { it.id == state.game.accusation?.responder }
            binding.card.root.isVisible = false
            binding.headerText.text = "Enable NFC and hold your phone close to the phone of ${respondingPlayer.displayName} to see their card"
        }
    }

    companion object {
        const val KEY_CARD = "KEY_CARD"
    }
}
