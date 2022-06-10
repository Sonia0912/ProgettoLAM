package com.sonianicoletti.progettolam.ui.game.cards

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.sonianicoletti.progettolam.R
import com.sonianicoletti.progettolam.databinding.FragmentCardsBinding
import com.sonianicoletti.progettolam.ui.game.GameViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CardsFragment : Fragment() {

    private lateinit var binding: FragmentCardsBinding
    private val viewModel: CardsViewModel by viewModels()
    private val gameViewModel: GameViewModel by activityViewModels()
    private var yourCards = mutableListOf<CardItem>()
    private var leftoverCards = mutableListOf<CardItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCardsBinding.inflate(inflater)
        // Show the cards
        val yourCardsAdapter = CardsAdapter(yourCards)
        val leftoverCardsAdapter = CardsAdapter(leftoverCards)
        binding.recyclerViewYourCards.adapter = yourCardsAdapter
        binding.recyclerViewLeftoverCards.adapter = leftoverCardsAdapter
        gameViewModel.gameState.observe(viewLifecycleOwner) {
           viewModel.handleGameState(it.game)
        }
        viewModel.cardsState.observe(viewLifecycleOwner) {
            yourCards.clear()
            yourCards.addAll(it.yourCards)
            leftoverCards.clear()
            leftoverCards.addAll(it.leftoverCards)
            binding.recyclerViewYourCards.adapter?.notifyDataSetChanged()
            binding.recyclerViewLeftoverCards.adapter?.notifyDataSetChanged()
        }
        // Handle leave button
        // TODO
        // Handle rules button
        // TODO
        // Handle notes button
        binding.floatingActionButton.setOnClickListener {
            findNavController().navigate(R.id.notesFragment)
        }
        return binding.root
    }


}