package com.sonianicoletti.progettolam.ui.game.characters

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.sonianicoletti.progettolam.R
import com.sonianicoletti.progettolam.databinding.FragmentCharactersBinding
import com.sonianicoletti.progettolam.ui.auth.AuthActivity
import com.sonianicoletti.progettolam.ui.game.characters.CharactersViewModel.ViewEvent.*
import com.sonianicoletti.progettolam.util.ItemOffsetDecoration
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CharactersFragment : Fragment() {

    private lateinit var binding: FragmentCharactersBinding
    private val viewModel: CharactersViewModel by viewModels()

    private lateinit var adapter: CharactersAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCharactersBinding.inflate(inflater)
        initCharacterGrid()
        observeViewEvents()
        return binding.root
    }

    private fun initCharacterGrid() {
        adapter = CharactersAdapter { viewModel.selectCharacter(it) }
        val itemOffsetDecoration = ItemOffsetDecoration(requireContext(), R.dimen.character_offset)
        binding.characterGrid.adapter = adapter
        binding.characterGrid.addItemDecoration(itemOffsetDecoration)
        observeCharacterItems()
    }

    private fun observeCharacterItems() = viewModel.selectedCharacters.observe(viewLifecycleOwner) { characterItems ->
        adapter.updateItems(characterItems)
    }

    private fun observeViewEvents() = viewModel.viewEvent.observe(viewLifecycleOwner) { event ->
        when (event) {
            is ShowCharacterTaken -> Unit // TODO: Implement
            NavigateToAuth -> navigateToAuth()
            NavigateToMain -> navigateToMain()
            ShowGameNotRunningToast -> showGameNotRunningToast()
            ShowUserNotLoggedInToast -> showUserNotLoggedInToast()
        }
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

    private fun showGameNotRunningToast() {
        Toast.makeText(requireContext(), getString(R.string.game_not_running_toast), Toast.LENGTH_SHORT).show()
    }

    private fun showUserNotLoggedInToast() {
        Toast.makeText(requireContext(), getString(R.string.user_not_logged_in_toast), Toast.LENGTH_SHORT).show()
    }
}
