package com.sonianicoletti.progettolam.ui.main.lobby

import android.content.Intent
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
import com.sonianicoletti.entities.Player
import com.sonianicoletti.progettolam.R
import com.sonianicoletti.progettolam.databinding.FragmentLobbyBinding
import com.sonianicoletti.progettolam.ui.game.GameActivity
import com.sonianicoletti.progettolam.ui.main.lobby.LobbyViewModel.ViewEvent.*
import com.sonianicoletti.progettolam.ui.main.lobby.LobbyViewModel.ViewState.*
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

        initGame()
        initActionBar()
        initPlayersList()
        setClickListeners()
        observeViewState()
        observeViewEvents()
        return binding.root
    }

    private fun initGame() {
        val gameID = arguments?.getString(ARG_GAME_ID)
        // quando qualcuno si unisce alla partita la lobby viene caricata
        if (gameID != null) {
            viewModel.loadGame(gameID)
        }
        // quando l'host crea la partita la lobby viene creata e viene generato l'ID della partita
        else {
            viewModel.createGame()
        }
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

    private fun renderLoaded(game: Game) {
        updatePlayersList(game.players)
        showGameID(game.id)
        viewModel.generateQrCode(game.id)
    }

    private fun updatePlayersList(playerList: List<Player>) {
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
            NotEnoughPlayersAlert -> showNotEnoughPlayersDialog()
            ClearText -> binding.editTextTextPersonName.setText("")
            is NavigateToGame -> navigateToGame(it.gameID)
            is SetQRCode -> binding.gameQRCode.setImageBitmap(it.qrCodeBitmap)
        }
    }

    private fun navigateToGame(gameID: String) {
        val intent = Intent(requireContext(), GameActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK) // non fa tornare indietro
        intent.putExtra("gameID", gameID)
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

    companion object {
        // assicura che la stringa chiamara nel viewmodel e nel fragment sia la stessa
        const val ARG_GAME_ID = "ARG_GAME_ID"
    }
}
