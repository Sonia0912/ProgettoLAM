package com.sonianicoletti.progettolam.ui.game.characters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.sonianicoletti.entities.Character
import com.sonianicoletti.progettolam.databinding.FragmentCharactersBinding
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

        binding.imageViewPeacock.setOnClickListener {
            viewModel.selectCharacter(Character.PEACOCK)
        }
        binding.imageViewGreen.setOnClickListener {
            viewModel.selectCharacter(Character.GREEN)
        }
        binding.imageViewMustard.setOnClickListener {
            viewModel.selectCharacter(Character.MUSTARD)
        }
        binding.imageViewPlum.setOnClickListener {
            viewModel.selectCharacter(Character.PLUM)
        }
        binding.imageViewScarlett.setOnClickListener {
            viewModel.selectCharacter(Character.SCARLETT)
        }
        binding.imageViewWhite.setOnClickListener {
            viewModel.selectCharacter(Character.WHITE)
        }

        return binding.root
    }
}
