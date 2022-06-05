package com.sonianicoletti.progettolam.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sonianicoletti.progettolam.databinding.ActivityMainBinding
import com.sonianicoletti.progettolam.ui.game.GameActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        navigateToGame()
    }

    private fun navigateToGame() {
        val intent = Intent(this, GameActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra(GameActivity.INTENT_EXTRA_GAME_ID, "404644")
        startActivity(intent)
        finish()
    }
}