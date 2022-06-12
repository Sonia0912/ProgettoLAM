package com.sonianicoletti.progettolam.ui.game.solution

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.sonianicoletti.progettolam.R
import com.sonianicoletti.progettolam.databinding.FragmentSolutionBinding
import com.sonianicoletti.progettolam.ui.game.GameViewModel
import com.sonianicoletti.progettolam.ui.game.cards.CardItem
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SolutionFragment : Fragment() {

    private lateinit var binding: FragmentSolutionBinding
    private val viewModel: SolutionViewModel by viewModels()
    private val gameViewModel: GameViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSolutionBinding.inflate(inflater)
        binding.viewScoresButton.setOnClickListener {
            findNavController().apply {
                popBackStack(R.id.game_nav_graph, true)
                findNavController().navigate(R.id.scoresFragment)
            }
        }
        observeViewEvents()
        return binding.root
    }

    private fun observeViewEvents() = viewModel.viewEvent.observe(viewLifecycleOwner) { event ->
        when (event) {
            is SolutionViewModel.ViewEvent.ShowResult -> showResult(event.won)
            is SolutionViewModel.ViewEvent.ShowSolutionCards -> showSolutionCards(event.card1, event.card2, event.card3)
            SolutionViewModel.ViewEvent.StopObservingGame -> gameViewModel.stopObservingGame()
        }
    }

    private fun showResult(won: Boolean) {
        val result = if (won) getString(R.string.you_won) else getString(R.string.you_lost)
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