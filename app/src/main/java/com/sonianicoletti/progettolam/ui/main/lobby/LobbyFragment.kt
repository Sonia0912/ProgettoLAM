package com.sonianicoletti.progettolam.ui.main.lobby

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sonianicoletti.entities.User
import com.sonianicoletti.progettolam.R
import com.sonianicoletti.progettolam.databinding.FragmentLobbyBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LobbyFragment : Fragment() {

    private lateinit var binding: FragmentLobbyBinding
    private val viewModel: LobbyViewModel by viewModels()

    val playerList = mutableListOf<User>()

    var playersAdapter = PlayersAdapter(playerList)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLobbyBinding.inflate(inflater)
        (activity as AppCompatActivity).apply {
            setSupportActionBar(binding.toolBar)
            supportActionBar?.setDisplayShowTitleEnabled(false)
        }

        binding.imageViewExit.setOnClickListener {
            openLeaveDialog()
        }

        binding.editTextTextPersonName.setOnEditorActionListener { textView, actionId, keyEvent ->
            // quando clicca enter
            if(actionId == EditorInfo.IME_ACTION_DONE) {
                viewModel.addPlayer(textView.text.toString())
                true
            }
            false
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(context)

        binding.recyclerView.adapter = playersAdapter

        observeViewEvents()

        return binding.root
    }

    private fun openLeaveDialog() {
        // Se context non e' nullo continua, il context diventa "it" e anche se cambia
        // si continua ad usare il context usato per la verifica
        context?.let {
            MaterialAlertDialogBuilder(it)
                .setMessage(getString(R.string.dialog_leave_message))
                .setPositiveButton("Yes") { _, _ -> findNavController().navigateUp() } // il secondo parametro e' una funzione callback di default
                .setNegativeButton("No") { _, _ -> }
                .show()
        }
    }

    private fun openMaxPlayersDialog() {
        context?.let {
            MaterialAlertDialogBuilder(it)
                .setMessage(getString(R.string.max_players_message))
                .setPositiveButton("OK") { _, _ -> }
                .show()
        }
    }

    private fun notFoundUserAlert() {
        MaterialAlertDialogBuilder(requireContext()).setMessage("No user found with that email").show()
    }

    private fun duplicatePlayerAlert() {
        MaterialAlertDialogBuilder(requireContext()).setMessage("Player already in game").show()
    }


    private fun updatePlayersList(playerList : List<User>) {
        this.playerList.clear()
        this.playerList.addAll(playerList)
        playersAdapter.notifyDataSetChanged()
    }

    // it e' l'evento
    private fun observeViewEvents() = viewModel.viewEvent.observe(viewLifecycleOwner) {
        when (it) {
            // se e' una classe serve is, se e' un oggetto no
            LobbyViewModel.ViewEvent.OpenMaxPlayersDialog -> openMaxPlayersDialog()
            LobbyViewModel.ViewEvent.NotFoundUserAlert -> notFoundUserAlert()
            LobbyViewModel.ViewEvent.DuplicatePlayerAlert -> duplicatePlayerAlert()
            is LobbyViewModel.ViewEvent.UpdatePlayersList -> updatePlayersList(it.players)
            LobbyViewModel.ViewEvent.ClearText -> binding.editTextTextPersonName.setText("")
        }
    }

}