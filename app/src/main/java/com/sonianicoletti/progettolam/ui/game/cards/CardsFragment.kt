package com.sonianicoletti.progettolam.ui.game.cards

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.sonianicoletti.progettolam.R
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
        setClickListeners()
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
            binding.turnPlayerText.text = "Turn player: ${it.turnPlayer.displayName}"
        }
    }

    private fun RecyclerView.updateList(cards: List<CardItem>) {
        (adapter as CardsAdapter).updateList(cards)
    }

    private fun setClickListeners() {
        // Handle notes button
        binding.floatingActionButton.setOnClickListener {
            findNavController().navigate(R.id.notesFragment)
        }
    }
}