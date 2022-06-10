package com.sonianicoletti.progettolam.ui.game

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sonianicoletti.progettolam.R
import com.sonianicoletti.progettolam.databinding.ActivityGameBinding
import com.sonianicoletti.progettolam.ui.auth.AuthActivity
import com.sonianicoletti.progettolam.ui.game.GameViewModel.ViewEvent.*
import com.sonianicoletti.progettolam.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGameBinding
    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        initActionBar()
        initNavigationFab()
        observeViewState()
        observeViewEvents()
        prepareNavDestinationListener()
    }

    private fun initActionBar() {
        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.imageViewExit.setOnClickListener {
            showLeaveGameDialog()
        }
        binding.imageViewRules.setOnClickListener {
            openRulesFragment()
        }
    }

    private fun showLeaveGameDialog() {
        // Se context non e' nullo continua, il context diventa "it" e anche se cambia
        // si continua ad usare il context usato per la verifica
        MaterialAlertDialogBuilder(this)
            .setMessage(getString(R.string.dialog_leave_message))
            .setPositiveButton("Yes") { _, _ -> viewModel.leaveGame() } // il secondo parametro e' una funzione callback di default
            .setNegativeButton("No") { _, _ -> }
            .show()
    }

    private fun openRulesFragment() {
        findNavController(R.id.fragment_container_view).navigate(R.id.rulesFragment)
    }

    private fun initNavigationFab() {
        binding.navigationFab.setOnClickListener { viewModel.handleNavigationFabClick() }
        binding.cardsFragmentFab.setOnClickListener { findNavController(R.id.fragment_container_view).navigate(R.id.cardsFragment) }
        binding.notesFragmentFab.setOnClickListener { findNavController(R.id.fragment_container_view).navigate(R.id.notesFragment) }
        binding.accusationFragmentFab.setOnClickListener { findNavController(R.id.fragment_container_view).navigate(R.id.accusationFragment) }
    }

    private fun prepareNavDestinationListener() {
        findNavController(R.id.fragment_container_view).addOnDestinationChangedListener { _, destination, _ ->
            binding.toolBar.isVisible = destination.id != R.id.notesFragment
            binding.navigationFab.isVisible = destination.id != R.id.lobbyFragment && destination.id != R.id.charactersFragment
        }
    }

    private fun observeViewState() = viewModel.viewState.observe(this) { state ->
        binding.cardsFragmentFab.isVisible = state.navigationFabOpened
        binding.notesFragmentFab.isVisible = state.navigationFabOpened
        binding.accusationFragmentFab.isVisible = state.navigationFabOpened
    }

    private fun observeViewEvents() = viewModel.viewEvent.observe(this) { event ->
        when (event) {
            NavigateToAuth -> navigateToAuth()
            NavigateToMain -> navigateToMain()
            ShowGameNotRunningToast -> showGameNotRunningToast()
            ShowUserNotLoggedInToast -> showUserNotLoggedInToast()
        }
    }

    private fun navigateToAuth() {
        val intent = Intent(this, AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun showUserNotLoggedInToast() {
        Toast.makeText(this, getString(R.string.user_not_logged_in_toast), Toast.LENGTH_SHORT).show()
    }

    private fun showGameNotRunningToast() {
        Toast.makeText(this, getString(R.string.game_not_running_toast), Toast.LENGTH_SHORT).show()
    }
}
