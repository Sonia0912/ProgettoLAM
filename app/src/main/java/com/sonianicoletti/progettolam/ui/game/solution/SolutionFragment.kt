package com.sonianicoletti.progettolam.ui.game.solution

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.sonianicoletti.progettolam.databinding.FragmentSolutionBinding
import com.sonianicoletti.progettolam.ui.game.cards.CardItem
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SolutionFragment : Fragment() {

    private lateinit var binding: FragmentSolutionBinding
    private val viewModel: SolutionViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSolutionBinding.inflate(inflater)

        observeViewEvents()
        return binding.root
    }

    private fun observeViewEvents() = viewModel.viewEvent.observe(viewLifecycleOwner) { event ->
        when (event) {
            is SolutionViewModel.ViewEvent.ShowResult -> showResult(event.won)
            is SolutionViewModel.ViewEvent.ShowSolutionCards -> showSolutionCards(event.card1, event.card2, event.card3)
        }
    }

    private fun showResult(won: Boolean) {
        val result = if (won) "You won!" else "Game over!"
        binding.textViewResult.text = result
    }

    private fun showSolutionCards(card1: CardItem, card2: CardItem, card3: CardItem) {
        binding.imageView1.setImageResource(card1.imageRes)
        binding.imageView2.setImageResource(card2.imageRes)
        binding.imageView3.setImageResource(card3.imageRes)
        binding.name1.text = card1.card.name
        binding.name2.text = card2.card.name
        binding.name3.text = card3.card.name
    }

}