package com.sonianicoletti.progettolam.ui.game.lobby

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sonianicoletti.entities.GameStatus
import com.sonianicoletti.entities.Player
import com.sonianicoletti.progettolam.R
import com.sonianicoletti.progettolam.databinding.FragmentLobbyBinding
import com.sonianicoletti.progettolam.ui.game.GameViewModel
import com.sonianicoletti.progettolam.ui.game.lobby.LobbyViewModel.ViewEvent.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LobbyFragment : Fragment() {

    private lateinit var binding: FragmentLobbyBinding
    private val viewModel: LobbyViewModel by viewModels()
    private val gameViewModel: GameViewModel by viewModels()

    private val playerList = mutableListOf<Player>()
    private var playersAdapter = PlayersAdapter(playerList)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLobbyBinding.inflate(inflater)
        initPlayersList()
        setClickListeners()
        observeViewState()
        observeGameState()
        observeViewEvents()
        return binding.root
    }

    private fun initPlayersList() {
        binding.playersList.layoutManager = LinearLayoutManager(context)
        binding.playersList.adapter = playersAdapter
    }

    private fun setClickListeners() {
        binding.editTextAddPlayer.setOnEditorActionListener { textView, actionId, _ ->
            // quando clicca enter
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                viewModel.invitePlayer(textView.text.toString())
                true
            } else {
                false
            }
        }

        binding.buttonStartgame.setOnClickListener {
            viewModel.startGame()
        }
    }

    private fun observeViewState() = viewModel.viewState.observe(viewLifecycleOwner) { state ->
        binding.progressLayout.isVisible = state == LobbyViewModel.ViewState.Loading
        binding.editTextAddPlayer.isEnabled = state != LobbyViewModel.ViewState.AddingPlayer
    }

    private fun observeGameState() = gameViewModel.gameState.observe(viewLifecycleOwner) { state ->
        updatePlayersList(state.game.players)
        updateHostPrivileges(state.isHost)
        showGameID(state.game.id)
        viewModel.generateQrCode(state.game.id)

        if (state.game.status == GameStatus.CHARACTER_SELECT) {
            navigateToCharacterSelect()
        }
    }

    private fun updatePlayersList(playerList: List<Player>) {
        this.playerList.clear()
        this.playerList.addAll(playerList)
        playersAdapter.notifyDataSetChanged()
    }

    private fun updateHostPrivileges(isHost: Boolean) {
        binding.editTextAddPlayer.isVisible = isHost
        binding.buttonStartgame.isVisible = isHost
        binding.textView.isVisible = isHost
    }

    private fun showGameID(id: String) {
        binding.gameID.text = id
    }

    private fun navigateToCharacterSelect() {
        findNavController().navigate(R.id.charactersFragment)
    }

    private fun observeViewEvents() = viewModel.viewEvent.observe(viewLifecycleOwner) { event ->
        when (event) {
            ClearText -> clearText()
            ShowInvalidEmailAlert -> showInvalidEmailDialog()
            ShowNoNetworkConnectionAlert -> showNoNetworkConnectionDialog()
            ShowMaxPlayersAlert -> showMaxPlayersDialog()
            ShowUserNotFoundAlert -> showUserNotFoundDialog()
            ShowDuplicatePlayerAlert -> showDuplicatePlayerDialog()
            ShowNotEnoughPlayersAlert -> showNotEnoughPlayersDialog()
            ShowGeneralErrorAlert -> showGeneralErrorDialog()
            is SetQRCode -> setQRCode(event.qrCodeBitmap)
        }
    }

    private fun clearText() {
        binding.editTextAddPlayer.setText("")
    }

    private fun showInvalidEmailDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(getString(R.string.invalid_email_message))
            .setPositiveButton("OK") { _, _ -> }
            .show()
    }

    private fun showNoNetworkConnectionDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(getString(R.string.no_network_connection_message))
            .setPositiveButton("OK") { _, _ -> }
            .show()
    }

    private fun showMaxPlayersDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(getString(R.string.max_players_message))
            .setPositiveButton("OK") { _, _ -> }
            .show()
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

    private fun showGeneralErrorDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(getString(R.string.general_error_message))
            .setPositiveButton("OK") { _, _ -> }
            .show()
    }

    private fun setQRCode(qrCodeBitmap: Bitmap) {
        binding.gameQRCode.setImageBitmap(qrCodeBitmap)
    }
}
