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
import com.sonianicoletti.entities.Game
import com.sonianicoletti.entities.User
import com.sonianicoletti.progettolam.R
import com.sonianicoletti.progettolam.databinding.FragmentLobbyBinding
import com.sonianicoletti.progettolam.ui.main.lobby.LobbyViewModel.ViewEvent.*
import com.sonianicoletti.progettolam.ui.main.lobby.LobbyViewModel.ViewState.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LobbyFragment : Fragment() {

    private lateinit var binding: FragmentLobbyBinding
    private val viewModel: LobbyViewModel by viewModels()

    private val playerList = mutableListOf<User>()
    private var playersAdapter = PlayersAdapter(playerList)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLobbyBinding.inflate(inflater)
        initActionBar()
        initPlayersList()
        setClickListeners()
        observeViewState()
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

        binding.editTextTextPersonName.setOnEditorActionListener { textView, actionId, keyEvent ->
            // quando clicca enter
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                viewModel.addPlayer(textView.text.toString())
                true
            } else {
                false
            }
        }
    }

    private fun showLeaveGameDialog() {
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

    private fun observeViewState() = viewModel.viewState.observe(viewLifecycleOwner) { state ->
        when (state) {
            // se e' una classe serve is, se e' un oggetto no\
            Loading -> Unit
            is Loaded -> renderLoaded(state.game)
            is Error -> Unit
        }
    }

    private fun renderLoaded(game : Game) {
        updatePlayersList(game.players)
        showGameID(game.id)
        viewModel.generateQrCode(game.id)
    }

    private fun updatePlayersList(playerList : List<User>) {
        this.playerList.clear()
        this.playerList.addAll(playerList)
        playersAdapter.notifyDataSetChanged()
    }

    private fun showGameID(id: String) {
        binding.gameID.text = id
    }

    // it e' l'evento
    private fun observeViewEvents() = viewModel.viewEvent.observe(viewLifecycleOwner) {
        when (it) {
            OpenMaxPlayersDialog -> showMaxPlayersDialog()
            NotFoundUserAlert -> showUserNotFoundDialog()
            DuplicatePlayerAlert -> showDuplicatePlayerDialog()
            ClearText -> binding.editTextTextPersonName.setText("")
            is SetQRCode -> binding.gameQRCode.setImageBitmap(it.qrCodeBitmap)
        }
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
}
