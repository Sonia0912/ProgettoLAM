package com.sonianicoletti.progettolam.ui.auth.register

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sonianicoletti.progettolam.databinding.FragmentRegisterBinding
import com.sonianicoletti.progettolam.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterFragment : Fragment() {

    private lateinit var binding: FragmentRegisterBinding
    private val viewModel: RegisterViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterBinding.inflate(inflater)
        setClickListeners()
        observeViewState()
        observeViewEvents()
        return binding.root
    }

    private fun setClickListeners() {
        // Gestire il click su Register
        binding.buttonRegister.setOnClickListener {
            val email = binding.editTextEmail.text.toString()
            var name = binding.editTextInputUsername.text.toString()
            val password = binding.editTextPassword.text.toString()
            when {
                email.isNotBlank() && name.isNotBlank() && password.isNotBlank() -> {
                    viewModel.handleRegistration(email, name, password)
                }
                email.isBlank() -> {
                    binding.editTextEmail.error = "Insert e-mail"
                }
                name.isBlank() -> {
                    binding.editTextInputUsername.error = "Insert name"
                }
                else -> {
                    binding.editTextPassword.error = "Insert password"
                }
            }
        }
    }

    private fun observeViewState() = viewModel.viewState.observe(viewLifecycleOwner) { state ->
        when (state) {
            RegisterViewModel.ViewState.Loading -> binding.progressLayout.isVisible = true
            RegisterViewModel.ViewState.Idle -> binding.progressLayout.isVisible = false
        }
    }

    private fun observeViewEvents() = viewModel.viewEvent.observe(viewLifecycleOwner) { event ->
        when (event) {
            // se e' una classe serve is, se e' un oggetto no
            is RegisterViewModel.ViewEvent.HandleTakenUsername -> handleTakenUsername(event.errorMessage)
            RegisterViewModel.ViewEvent.NavigateToMain -> navigateToMain()
        }
    }

    private fun handleTakenUsername(errorMessage: String) {
        MaterialAlertDialogBuilder(requireContext()).setMessage(errorMessage).show()
    }

    private fun navigateToMain() {
        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK) // non fa tornare indietro
        context?.startActivity(intent)
        activity?.finish()
    }
}