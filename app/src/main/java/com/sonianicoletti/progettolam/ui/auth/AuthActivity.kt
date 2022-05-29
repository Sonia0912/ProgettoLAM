package com.sonianicoletti.progettolam.ui.auth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sonianicoletti.progettolam.databinding.ActivityAuthBinding
import dagger.hilt.android.AndroidEntryPoint

// entry point e' dove iniziano a essere iniettate le dependency
@AndroidEntryPoint
class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}