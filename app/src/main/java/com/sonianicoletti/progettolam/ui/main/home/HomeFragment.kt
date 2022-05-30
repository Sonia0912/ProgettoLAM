package com.sonianicoletti.progettolam.ui.main.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.sonianicoletti.progettolam.R
import com.sonianicoletti.progettolam.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater)
        (activity as AppCompatActivity).apply {
            setSupportActionBar(binding.toolBar)
            supportActionBar?.setDisplayShowTitleEnabled(false)
        }

        binding.button1.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_lobbyFragment)
        }

        binding.imageViewPerson.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_profileFragment)
        }

        binding.button2.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_joingameFragment)
        }

        return binding.root
    }

}