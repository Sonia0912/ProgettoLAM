package com.sonianicoletti.progettolam.ui.game.characters

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.sonianicoletti.entities.Character
import com.sonianicoletti.progettolam.R
import com.sonianicoletti.progettolam.databinding.FragmentCharactersBinding
import com.sonianicoletti.progettolam.ui.auth.AuthActivity
import com.sonianicoletti.progettolam.ui.game.GameViewModel
import com.sonianicoletti.progettolam.ui.game.characters.CharactersViewModel.ViewEvent.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CharactersFragment : Fragment() {

    private lateinit var binding: FragmentCharactersBinding
    private val viewModel: CharactersViewModel by viewModels()
    private val gameViewModel: GameViewModel by activityViewModels()

    private lateinit var adapter: CharactersAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCharactersBinding.inflate(inflater)
        initCharacterGrid()
        observeGameState()
        observeViewEvents()
        return binding.root
    }

    private fun initCharacterGrid() {
        adapter = CharactersAdapter { viewModel.selectCharacter(it) }
        binding.characterGrid.adapter = adapter
        observeViewState()
    }

    private fun observeViewState() = viewModel.viewState.observe(viewLifecycleOwner) { state ->
        when (state) {
            CharactersViewModel.ViewState.Loading -> {
                binding.progressLayout.isVisible = true
            }
            is CharactersViewModel.ViewState.Data -> {
                binding.progressLayout.isVisible = false
                adapter.updateItems(state.characters)
            }
        }
    }

    private fun observeGameState() = gameViewModel.gameState.observe(viewLifecycleOwner) { state ->
        viewModel.handleGameUpdate(state.game)
    }

    private fun observeViewEvents() = viewModel.viewEvent.observe(viewLifecycleOwner) { event ->
        when (event) {
            is ShowCharacterTaken -> shakeCard(event.character)
            NavigateToAuth -> navigateToAuth()
            NavigateToMain -> navigateToMain()
            NavigateToCards ->  navigateToCards()
            ShowGameNotRunningToast -> showGameNotRunningToast()
            ShowUserNotLoggedInToast -> showUserNotLoggedInToast()
        }
    }

    private fun shakeCard(takenCharacter: Character) {
        val animShake : Animation = AnimationUtils.loadAnimation(this.requireContext(), R.anim.shake_card)

        val viewToShake = when (takenCharacter) {
            Character.PEACOCK -> binding.characterGrid.findViewHolderForAdapterPosition(0)
            Character.MUSTARD -> binding.characterGrid.findViewHolderForAdapterPosition(1)
            Character.SCARLETT -> binding.characterGrid.findViewHolderForAdapterPosition(2)
            Character.PLUM -> binding.characterGrid.findViewHolderForAdapterPosition(3)
            Character.WHITE -> binding.characterGrid.findViewHolderForAdapterPosition(4)
            Character.GREEN -> binding.characterGrid.findViewHolderForAdapterPosition(5)
            else -> null
        }?.itemView

        viewToShake?.startAnimation(animShake);
    }

    private fun navigateToAuth() {
        val intent = Intent(requireContext(), AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun navigateToMain() {
        val intent = Intent(requireContext(), AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun navigateToCards() {
        findNavController().apply {
            popBackStack(R.id.game_nav_graph, inclusive = true)
            navigate(R.id.cardsFragment)
        }
    }

    private fun showGameNotRunningToast() {
        Toast.makeText(requireContext(), getString(R.string.game_not_running_toast), Toast.LENGTH_SHORT).show()
    }

    private fun showUserNotLoggedInToast() {
        Toast.makeText(requireContext(), getString(R.string.user_not_logged_in_toast), Toast.LENGTH_SHORT).show()
    }
}
