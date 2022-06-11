package com.sonianicoletti.progettolam.ui.game

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.view.WindowManager
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

    private var isAccusationStage = false
    private var shouldFabShowInDestination = false

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
        observeGameState()
        observeViewEvents()
        prepareNavDestinationListener()

        binding.displayCardButton.setOnClickListener { viewModel.onDisplayCardButtonClicked() }
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
            shouldFabShowInDestination = destination.id != R.id.lobbyFragment && destination.id != R.id.charactersFragment && destination.id != R.id.rulesFragment
            handleNavigationFabVisibility()
        }
    }

    private fun handleNavigationFabVisibility() {
        binding.navigationFab.isVisible = shouldFabShowInDestination && !isAccusationStage
    }

    private fun observeViewState() = viewModel.viewState.observe(this) { state ->
        binding.cardsFragmentFab.isVisible = state.navigationFabOpened
        binding.notesFragmentFab.isVisible = state.navigationFabOpened
        binding.accusationFragmentFab.isVisible = state.navigationFabOpened
        binding.blackScreenOverlay.isVisible = state.navigationFabOpened
    }

    private fun observeGameState() = viewModel.gameState.observe(this) { state ->
        isAccusationStage = state.game.accusation != null
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
        binding.displayCardLayout.visibility = View.INVISIBLE
        binding.displayCardImage.setImageResource(cardItem.imageRes)
        binding.displayCardName.text = cardItem.card.name
        binding.displayCardButton.isVisible = isTurnPlayer
        binding.displayCardSubtitle.text = "Showing card to $turnPlayerName"
        binding.displayCardSubtitle.isVisible = !isTurnPlayer

        binding.displayCard.post {
            binding.displayCardLayout.isVisible = true
            val cardY = binding.displayCard.y + binding.displayCard.height
            binding.displayCard.y = 0F - binding.displayCard.height
            binding.displayCardLayout.alpha = 0F
            binding.displayCardButton.alpha = 0F
            binding.displayCardSubtitle.alpha = 0F

            binding.displayCardLayout.animate().alpha(1F).setDuration(500).start()
            binding.displayCard.animate().yBy(cardY).setDuration(500).setStartDelay(100).setInterpolator(DecelerateInterpolator()).start()
            binding.displayCardButton.animate().alpha(1F).setDuration(500).setStartDelay(500).start()
            binding.displayCardSubtitle.animate().alpha(1F).setDuration(500).setStartDelay(500).start()
        }
    }

    private fun hideDisplayCard() {
        binding.displayCardLayout.isVisible = false
    }

    override fun onBackPressed() {
        if (!isAccusationStage) {
            super.onBackPressed()
        }
    }
}
