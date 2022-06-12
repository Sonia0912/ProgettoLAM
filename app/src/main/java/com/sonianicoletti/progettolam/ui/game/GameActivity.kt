package com.sonianicoletti.progettolam.ui.game

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sonianicoletti.progettolam.R
import com.sonianicoletti.progettolam.databinding.ActivityGameBinding
import com.sonianicoletti.progettolam.ui.auth.AuthActivity
import com.sonianicoletti.progettolam.ui.game.GameViewModel.ViewEvent.*
import com.sonianicoletti.progettolam.ui.game.cards.CardItem
import com.sonianicoletti.progettolam.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGameBinding
    private val viewModel: GameViewModel by viewModels()

    private var isAccusationResponder = false
    private var shouldFabShowInDestination = false
    private var hasGameEnded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)
        changeStatusBarColour()
    }

    private fun changeStatusBarColour() {
        if (Build.VERSION.SDK_INT >= 21) {
            val window = this.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = this.resources.getColor(R.color.dark_russian_violet)
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        initActionBar()
        initNavigationFab()
        observeViewState()
        observeViewEvents()
        prepareNavDestinationListener()

        binding.displayCardOverlay.button.setOnClickListener { viewModel.onDisplayCardButtonClicked() }
        binding.defeatOverlay.button.setOnClickListener {
            hideDefeatOverlay()
            findNavController(R.id.fragment_container_view).apply {
                currentDestination?.id?.let { popBackStack(it, true) }
                navigate(R.id.scoresFragment)
            }
        }
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

        findNavController(R.id.fragment_container_view).addOnDestinationChangedListener { controller, destination, arguments ->
            when (destination.id) {
                R.id.rulesFragment -> displayRulesToolbar()
                R.id.lobbyFragment -> displayDefaultToolbar("Lobby")
                R.id.cardsFragment -> displayDefaultToolbar("Cards")
                R.id.notesFragment -> displayDefaultToolbar("Notes")
                R.id.accusationFragment -> displayDefaultToolbar("Accusation")
                else -> displayDefaultToolbar("Game")
            }
        }

        binding.imageViewBack.setOnClickListener {
            findNavController(R.id.fragment_container_view).navigateUp()
        }
    }

    private fun showLeaveGameDialog() {
        MaterialAlertDialogBuilder(this)
            .setMessage(getString(R.string.dialog_leave_message))
            .setPositiveButton("Yes") { _, _ -> viewModel.leaveGame() } // il secondo parametro e' una funzione callback di default
            .setNegativeButton("No") { _, _ -> }
            .show()
    }

    private fun openRulesFragment() {
        findNavController(R.id.fragment_container_view).navigate(R.id.rulesFragment)
    }

    private fun displayRulesToolbar() {
        binding.titleToolbar.text = "Rules"
        binding.imageViewExit.isVisible = false
        binding.imageViewBack.isVisible = true
        binding.imageViewRules.isVisible = false
    }

    private fun displayDefaultToolbar(title: String) {
        binding.titleToolbar.text = title
        binding.imageViewExit.isVisible = true
        binding.imageViewBack.isVisible = false
        binding.imageViewRules.isVisible = true
    }

    private fun initNavigationFab() {
        binding.navigationFab.setOnClickListener { viewModel.toggleNavigationFab() }
        binding.blackScreenOverlay.setOnClickListener { viewModel.toggleNavigationFab() }
        binding.cardsFragmentFab.setOnClickListener {
            findNavController(R.id.fragment_container_view).navigate(R.id.cardsFragment)
            viewModel.toggleNavigationFab()
        }
        binding.notesFragmentFab.setOnClickListener {
            findNavController(R.id.fragment_container_view).navigate(R.id.notesFragment)
            viewModel.toggleNavigationFab()
        }
        binding.accusationFragmentFab.setOnClickListener {
            findNavController(R.id.fragment_container_view).navigate(R.id.accusationFragment)
            viewModel.toggleNavigationFab()
        }
    }

    private fun prepareNavDestinationListener() {
        findNavController(R.id.fragment_container_view).addOnDestinationChangedListener { _, destination, _ ->
            shouldFabShowInDestination =
                destination.id != R.id.lobbyFragment && destination.id != R.id.charactersFragment && destination.id != R.id.rulesFragment
            handleNavigationFabVisibility()
        }
    }

    private fun handleNavigationFabVisibility() {
        binding.navigationFab.isVisible = shouldFabShowInDestination && !isAccusationResponder && !hasGameEnded
    }

    private fun observeViewState() = viewModel.viewState.observe(this) { state ->
        binding.cardsFragmentFab.isVisible = state.navigationFabOpened
        binding.notesFragmentFab.isVisible = state.navigationFabOpened
        binding.accusationFragmentFab.isVisible = state.navigationFabOpened
        binding.blackScreenOverlay.isVisible = state.navigationFabOpened
        isAccusationResponder = state.isAccusationResponder
        hasGameEnded = state.hasGameEnded
        handleNavigationFabVisibility()
    }

    private fun observeViewEvents() = viewModel.viewEvent.observe(this) { event ->
        when (event) {
            NavigateToAuth -> navigateToAuth()
            NavigateToMain -> navigateToMain()
            NavigateToCards -> navigateToCards()
            ShowGameNotRunningToast -> showGameNotRunningToast()
            ShowUserNotLoggedInToast -> showUserNotLoggedInToast()
            is ShowDisplayCard -> showDisplayCard(event.cardItem, event.isTurnPlayer, event.turnPlayerName)
            HideDisplayCard -> hideDisplayCard()
            is NavigateToSolutionVictory -> showVictory(event.wonByNoPlayersRemaining)
            is NavigateToSolutionDefeat -> showDefeat(event.solutionCards, event.winnerName, event.wonByDefault, event.lostByAccusation)
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

    private fun navigateToCards() {
        findNavController(R.id.fragment_container_view).apply {
            if (currentDestination?.id != R.id.cardsFragment) {
                navigate(R.id.cardsFragment)
            }
        }
    }

    private fun showUserNotLoggedInToast() {
        Toast.makeText(this, getString(R.string.user_not_logged_in_toast), Toast.LENGTH_SHORT).show()
    }

    private fun showGameNotRunningToast() {
        Toast.makeText(this, getString(R.string.game_not_running_toast), Toast.LENGTH_SHORT).show()
    }

    private fun showDisplayCard(cardItem: CardItem, isTurnPlayer: Boolean, turnPlayerName: String) {
        binding.displayCardOverlay.root.visibility = View.INVISIBLE
        binding.displayCardOverlay.card.image.setImageResource(cardItem.imageRes)
        binding.displayCardOverlay.card.playerName.text = cardItem.card.name
        binding.displayCardOverlay.button.isVisible = isTurnPlayer
        binding.displayCardOverlay.subtitle.text = getString(R.string.showing_card_to, turnPlayerName)
        binding.displayCardOverlay.subtitle.isVisible = !isTurnPlayer

        binding.displayCardOverlay.root.post {
            binding.displayCardOverlay.root.isVisible = true
            val cardY = binding.displayCardOverlay.card.root.y
            binding.displayCardOverlay.card.root.y = 0F - binding.displayCardOverlay.card.root.height
            binding.displayCardOverlay.root.alpha = 0F
            binding.displayCardOverlay.button.alpha = 0F
            binding.displayCardOverlay.subtitle.alpha = 0F

            binding.displayCardOverlay.root.animate().alpha(1F).setDuration(500).start()
            binding.displayCardOverlay.card.root.animate().y(cardY).setDuration(500).setStartDelay(100).setInterpolator(DecelerateInterpolator()).start()
            binding.displayCardOverlay.button.animate().alpha(1F).setDuration(500).setStartDelay(500).start()
            binding.displayCardOverlay.subtitle.animate().alpha(1F).setDuration(500).setStartDelay(500).start()
        }
    }

    private fun hideDisplayCard() {
        binding.displayCardOverlay.root.isVisible = false
    }

    private fun hideDefeatOverlay() {
        binding.defeatOverlay.root.isVisible = false
    }

    private fun showVictory(wonByNoPlayersRemaining: Boolean) {
        findNavController(R.id.fragment_container_view).apply {
            if (currentDestination?.id != R.id.solutionFragment) {
                navigate(R.id.solutionFragment)
            }
        }
    }

    private fun showDefeat(solutionCards: List<CardItem>, winnerName: String?, wonByNoPlayersRemaining: Boolean, lostByAccusation: Boolean) {
        binding.defeatOverlay.root.visibility = View.INVISIBLE
        binding.defeatOverlay.characterCard.image.setImageResource(solutionCards[0].imageRes)
        binding.defeatOverlay.weaponCard.image.setImageResource(solutionCards[1].imageRes)
        binding.defeatOverlay.roomCard.image.setImageResource(solutionCards[2].imageRes)
        binding.defeatOverlay.characterCard.playerName.text = solutionCards[0].card.name
        binding.defeatOverlay.weaponCard.playerName.text = solutionCards[1].card.name
        binding.defeatOverlay.roomCard.playerName.text = solutionCards[2].card.name
        binding.defeatOverlay.subtitle.text = when {
            winnerName != null && lostByAccusation -> getString(R.string.made_wrong_accusation, winnerName)
            winnerName != null && wonByNoPlayersRemaining -> getString(R.string.won_by_default, winnerName)
            winnerName != null -> getString(R.string.made_correct_accusation, winnerName)
            else -> getString(R.string.you_lost_but_continue_playing)
        }
        binding.defeatOverlay.button.text = if (winnerName != null) "View scores" else "Continue"

        binding.defeatOverlay.root.post {
            binding.defeatOverlay.root.isVisible = true
            val cardY = binding.defeatOverlay.characterCard.root.y
            binding.defeatOverlay.characterCard.root.y = cardY / 3 * 2
            binding.defeatOverlay.weaponCard.root.y = cardY / 3 * 2
            binding.defeatOverlay.roomCard.root.y = cardY / 3 * 2
            binding.defeatOverlay.root.alpha = 0F
            binding.defeatOverlay.button.alpha = 0F
            binding.defeatOverlay.subtitle.alpha = 0F

            binding.defeatOverlay.root.animate().alpha(1F).setDuration(500).start()
            binding.defeatOverlay.characterCard.root.animate().y(cardY).setDuration(1000).setStartDelay(100).start()
            binding.defeatOverlay.weaponCard.root.animate().y(cardY).setDuration(1000).setStartDelay(100).start()
            binding.defeatOverlay.roomCard.root.animate().y(cardY).setDuration(1000).setStartDelay(100).start()
            binding.defeatOverlay.button.animate().alpha(1F).setDuration(500).setStartDelay(500).start()
            binding.defeatOverlay.subtitle.animate().alpha(1F).setDuration(500).setStartDelay(500).start()
        }
    }

    override fun onBackPressed() {
        if (!isAccusationResponder) {
            super.onBackPressed()
        }
    }
}
