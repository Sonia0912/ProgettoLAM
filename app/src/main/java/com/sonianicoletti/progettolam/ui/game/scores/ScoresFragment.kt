package com.sonianicoletti.progettolam.ui.game.scores

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.sonianicoletti.progettolam.databinding.FragmentScoresBinding
import com.sonianicoletti.progettolam.ui.game.GameViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ScoresFragment : Fragment() {

    private lateinit var binding: FragmentScoresBinding
    private val viewModel: ScoresViewModel by viewModels()
    private val gameViewModel: GameViewModel by activityViewModels()

    private lateinit var rankingsAdapter: ScoresAdapter
    private lateinit var totalScoresAdapter: ScoresAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentScoresBinding.inflate(inflater)
        initScoresLists()
        observeViewState()
        return binding.root
    }

    private fun initScoresLists() {
        rankingsAdapter = ScoresAdapter()
        totalScoresAdapter = ScoresAdapter()
        binding.gameRankings.adapter = rankingsAdapter
        binding.totalScores.adapter = totalScoresAdapter
    }

    private fun observeViewState() = viewModel.viewState.observe(viewLifecycleOwner) { state ->
        rankingsAdapter.updateList(state.rankings)
        totalScoresAdapter.updateList(state.totalScores)
        gameViewModel.stopObservingGame()
    }
}
