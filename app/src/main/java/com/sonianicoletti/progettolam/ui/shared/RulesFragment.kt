package com.sonianicoletti.progettolam.ui.shared

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.sonianicoletti.progettolam.databinding.FragmentRulesBinding
import com.sonianicoletti.progettolam.ui.game.GameViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RulesFragment : Fragment() {

    private lateinit var binding: FragmentRulesBinding
    private val gameViewModel: GameViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentRulesBinding.inflate(inflater)
        return binding.root
    }
}
