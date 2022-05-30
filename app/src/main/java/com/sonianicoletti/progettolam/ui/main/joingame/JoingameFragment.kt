package com.sonianicoletti.progettolam.ui.main.joingame

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sonianicoletti.progettolam.databinding.FragmentJoingameBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class JoingameFragment : Fragment() {

    private lateinit var binding: FragmentJoingameBinding
    private val viewModel: JoingameViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentJoingameBinding.inflate(inflater)

        // Torna alla schermata principale
        binding.imageViewBack.setOnClickListener {
            findNavController().navigateUp()
        }

        // Partecipa con l'ID
        binding.buttonJoin.setOnClickListener {
            var insertedGameID = binding.editTextGameID.text.toString()
            if(insertedGameID.isNotBlank() && insertedGameID.length == 6 && insertedGameID.all { char -> char.isDigit() }) {
                viewModel.joinGame(insertedGameID)
            } else {
                binding.editTextGameID.error = "Insert game ID"
            }

        }

        // Partecipa con il QR code
        // TODO

        observeViewEvents()
        return binding.root
    }

    private fun gameNotFoundAlert() {
        MaterialAlertDialogBuilder(requireContext()).setMessage("No active games found with this ID").show()
    }

    private fun observeViewEvents() = viewModel.viewEvent.observe(viewLifecycleOwner) {
        when (it) {
            JoingameViewModel.ViewEvent.GameNotFoundAlert -> gameNotFoundAlert()
        }
    }

}