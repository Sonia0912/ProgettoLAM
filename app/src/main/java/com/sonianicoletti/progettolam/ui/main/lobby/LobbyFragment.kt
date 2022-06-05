package com.sonianicoletti.progettolam.ui.main.lobby

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sonianicoletti.entities.GameStatus
import com.sonianicoletti.entities.Player
import com.sonianicoletti.progettolam.R
import com.sonianicoletti.progettolam.databinding.FragmentLobbyBinding
import com.sonianicoletti.progettolam.ui.game.GameActivity
import com.sonianicoletti.progettolam.ui.main.lobby.LobbyViewModel.ViewEvent.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LobbyFragment : Fragment() {

    private lateinit var binding: FragmentLobbyBinding
    private val viewModel: LobbyViewModel by viewModels()

    private val playerList = mutableListOf<Player>()
    private var playersAdapter = PlayersAdapter(playerList)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLobbyBinding.inflate(inflater)

        initActionBar()
        initPlayersList()
        setClickListeners()
        observeGameState()
        observeViewEvents()
        return binding.root
    }

    private fun initActionBar() {
        (activity as AppCompatActivity).apply {
            setSupportActionBar(binding.toolBar)
            supportActionBar?.setDisplayShowTitleEnabled(false)
        }
    }

    private fun initPlayersList() {
        binding.playersList.layoutManager = LinearLayoutManager(context)
        binding.playersList.adapter = playersAdapter
    }

    private fun setClickListeners() {
        binding.imageViewExit.setOnClickListener {
            showLeaveGameDialog()
        }

        binding.editTextAddPlayer.setOnEditorActionListener { textView, actionId, _ ->
            // quando clicca enter
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                viewModel.addPlayer(textView.text.toString())
                true
            } else {
                false
            }
        }

        binding.buttonStartgame.setOnClickListener {
            viewModel.startGame()
        }
    }

    private fun showLeaveGameDialog() {
        // Se context non e' nullo continua, il context diventa "it" e anche se cambia
        // si continua ad usare il context usato per la verifica
        context?.let {
            MaterialAlertDialogBuilder(it)
                .setMessage(getString(R.string.dialog_leave_message))
                .setPositiveButton("Yes") { _, _ -> viewModel.handleLeaveGame() } // il secondo parametro e' una funzione callback di default
                .setNegativeButton("No") { _, _ -> }
                .show()
        }
    }

    private fun observeGameState() = viewModel.viewState.observe(viewLifecycleOwner) { state ->
        updatePlayersList(state.game.players)
        updateHostPrivileges(state.isHost)
        showGameID(state.game.id)
        viewModel.generateQrCode(state.game.id)

        if (state.game.status == GameStatus.CHARACTER_SELECT) {
            navigateToGame()
        }
    }

    private fun updatePlayersList(playerList: List<Player>) {
        this.playerList.clear()
        this.playerList.addAll(playerList)
        playersAdapter.notifyDataSetChanged()
    }

    private fun updateHostPrivileges(isHost: Boolean) {
        binding.editTextAddPlayer.isEnabled = isHost
        binding.buttonStartgame.isEnabled = isHost
    }

    private fun showGameID(id: String) {
        binding.gameID.text = id
    }

    // it e' l'evento
    private fun observeViewEvents() = viewModel.viewEvent.observe(viewLifecycleOwner) {
        when (it) {
            OpenMaxPlayersDialog -> showMaxPlayersDialog()
            ShowUserNotFoundAlert -> showUserNotFoundDialog()
            DuplicatePlayerAlert -> showDuplicatePlayerDialog()
            NotEnoughPlayersAlert -> showNotEnoughPlayersDialog()
            ClearText -> binding.editTextAddPlayer.setText("")
            ShowHostLeftToast -> showHostLeftToast()
            NavigateToGame -> navigateToGame()
            NavigateUp -> findNavController().navigateUp()
            is SetQRCode -> binding.gameQRCode.setImageBitmap(it.qrCodeBitmap)
        }
    }

    private fun navigateToGame() {
        val intent = Intent(requireContext(), GameActivity::class.java)
        context?.startActivity(intent)
        activity?.finish()
    }

    private fun showMaxPlayersDialog() {
        context?.let {
            MaterialAlertDialogBuilder(it)
                .setMessage(getString(R.string.max_players_message))
                .setPositiveButton("OK") { _, _ -> }
                .show()
        }
    }

    private fun showUserNotFoundDialog() {
        MaterialAlertDialogBuilder(requireContext()).setMessage("No user found with that email").show()
    }

    private fun showDuplicatePlayerDialog() {
        MaterialAlertDialogBuilder(requireContext()).setMessage("Player already in game").show()
    }

    private fun showNotEnoughPlayersDialog() {
        MaterialAlertDialogBuilder(requireContext()).setMessage("At least 3 players to start").show()
    }

    private fun showHostLeftToast() {
        Toast.makeText(requireContext(), getString(R.string.host_left_toast), Toast.LENGTH_SHORT).show()
    }
}
