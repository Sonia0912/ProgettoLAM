package com.sonianicoletti.progettolam.ui.main.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.sonianicoletti.progettolam.R
import com.sonianicoletti.progettolam.databinding.FragmentHomeBinding
import com.sonianicoletti.progettolam.ui.game.GameActivity
import com.sonianicoletti.progettolam.ui.main.home.HomeViewModel.ViewEvent.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater)
        (activity as AppCompatActivity).apply {
            setSupportActionBar(binding.toolBar)
            supportActionBar?.setDisplayShowTitleEnabled(false)
        }
        setClickListeners()
        observeViewEvents()
        return binding.root
    }

    private fun setClickListeners() {
        binding.createGameButton.setOnClickListener { viewModel.handleCreateGameButton() }
        binding.joinGameButton.setOnClickListener { viewModel.handleJoinGameButton() }
        binding.imageViewPerson.setOnClickListener { viewModel.handleProfileButton() }
    }

    private fun observeViewEvents() = viewModel.viewEvent.observe(viewLifecycleOwner) { event ->
        when (event) {
            NavigateToLobby -> navigateToLobby()
            NavigateToJoinGame -> findNavController().navigate(R.id.action_homeFragment_to_joingameFragment)
            NavigateToProfile -> findNavController().navigate(R.id.action_homeFragment_to_profileFragment)
        }
    }

    private fun navigateToLobby() {
        val intent = Intent(requireContext(), GameActivity::class.java)
        startActivity(intent)
    }
}
