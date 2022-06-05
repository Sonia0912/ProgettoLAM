package com.sonianicoletti.progettolam.ui.game

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.sonianicoletti.progettolam.databinding.ActivityGameBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGameBinding
    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setOngoingGame()
    }

    private fun setOngoingGame() {
        val gameID = intent.getStringExtra(INTENT_EXTRA_GAME_ID) ?: throw IllegalStateException("GameActivity was started with no game id")
        viewModel.setOngoingGame(gameID)
    }

    companion object {
        const val INTENT_EXTRA_GAME_ID = "INTENT_EXTRA_GAME_ID"
    }
}
