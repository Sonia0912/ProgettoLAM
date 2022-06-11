package com.sonianicoletti.progettolam.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sonianicoletti.progettolam.R
import com.sonianicoletti.progettolam.databinding.ActivityMainBinding
import com.sonianicoletti.progettolam.ui.auth.AuthActivity
import com.sonianicoletti.progettolam.ui.main.MainViewModel.ViewEvent.NavigateToAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        receiveFirebaseMessage()
        observeViewEvents()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        initActionBar()
    }

    private fun initActionBar() {
        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.imageViewPerson.setOnClickListener {
            findNavController(R.id.fragment_container_view).navigate(R.id.profileFragment)
        }
        binding.imageViewRules.setOnClickListener {
            findNavController(R.id.fragment_container_view).navigate(R.id.rulesFragment)
        }

        binding.imageViewBack.setOnClickListener {
            findNavController(R.id.fragment_container_view).navigateUp()
        }

        findNavController(R.id.fragment_container_view).addOnDestinationChangedListener { controller, destination, arguments ->
            when (destination.id) {
                R.id.rulesFragment -> displayRulesToolbar()
                R.id.profileFragment -> displayProfileToolbar()
                R.id.joingameFragment -> displayJoingameToolbar()
                else -> displayDefaultToolbar()
            }
        }

    }

    private fun displayRulesToolbar() {
        binding.titleToolbar.text = "Rules"
        binding.imageViewPerson.isVisible = false
        binding.imageViewBack.isVisible = true
        binding.imageViewRules.isVisible = false
    }

    private fun displayProfileToolbar() {
        binding.titleToolbar.text = "Profile"
        binding.imageViewPerson.isVisible = false
        binding.imageViewBack.isVisible = true
        binding.imageViewRules.isVisible = false
    }

    private fun displayJoingameToolbar() {
        binding.titleToolbar.text = "Join game"
        binding.imageViewPerson.isVisible = false
        binding.imageViewBack.isVisible = true
        binding.imageViewRules.isVisible = false
    }

    private fun displayDefaultToolbar() {
        binding.titleToolbar.text = "CLUEDO"
        binding.imageViewPerson.isVisible = true
        binding.imageViewBack.isVisible = false
        binding.imageViewRules.isVisible = true
    }

    private fun receiveFirebaseMessage() {
        val invitedGameID = intent.extras?.getString("gameID")
        if (invitedGameID != null) {
            binding.fragmentContainerView.post {
                val args = Bundle().apply { putString("GAME_ID", invitedGameID) }
                findNavController(binding.fragmentContainerView.id).navigate(R.id.joingameFragment, args)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkUserLoggedIn()
    }

    private fun observeViewEvents() = viewModel.viewEvent.observe(this) { event ->
        when (event) {
            NavigateToAuth -> navigateToAuth()
            is MainViewModel.ViewEvent.InvitationReceived -> showInvitationMessage(event.inviter, event.gameID)
        }
    }

    private fun navigateToAuth() {
        val intent = Intent(this, AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun showInvitationMessage(inviter: String, gameID: String) {
        MaterialAlertDialogBuilder(this)
            .setMessage("$inviter invited you to join the game")
            .setPositiveButton("Accept") { _, _ ->
                run {
                    binding.fragmentContainerView.post {
                        val args = Bundle().apply { putString("GAME_ID", gameID) }
                        findNavController(binding.fragmentContainerView.id).navigate(R.id.joingameFragment, args)
                    }
                }
            }
            .setNegativeButton("Decline") { _, _ -> }
            .show()
    }
}
