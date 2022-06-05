package com.sonianicoletti.progettolam.ui.game.characters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.sonianicoletti.entities.Character
import com.sonianicoletti.progettolam.R
import com.sonianicoletti.progettolam.databinding.FragmentCharactersBinding
import com.sonianicoletti.progettolam.ui.game.GameViewModel
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
}
