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
import com.sonianicoletti.progettolam.util.ItemOffsetDecoration
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CharactersFragment : Fragment() {

    private lateinit var binding: FragmentCharactersBinding
    private val viewModel: CharactersViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCharactersBinding.inflate(inflater)
        initCharacterGrid()
        return binding.root
    }

    private fun initCharacterGrid() {
        val characterItems = createCharacterItems()
        val adapter = CharactersAdapter(characterItems) { viewModel.selectCharacter(it) }
        val itemOffsetDecoration = ItemOffsetDecoration(requireContext(), R.dimen.character_offset)
        binding.characterGrid.adapter = adapter
        binding.characterGrid.addItemDecoration(itemOffsetDecoration)
    }

    private fun createCharacterItems() = mutableListOf(
        SelectCharacterItem(Character.PEACOCK, R.drawable.mrs_peacock, null),
        SelectCharacterItem(Character.MUSTARD, R.drawable.colonel_mustard, null),
        SelectCharacterItem(Character.SCARLETT, R.drawable.miss_scarlett, null),
        SelectCharacterItem(Character.PLUM, R.drawable.professor_plum, null),
        SelectCharacterItem(Character.WHITE, R.drawable.mrs_white, null),
        SelectCharacterItem(Character.GREEN, R.drawable.rev_green, null),
    )
}
