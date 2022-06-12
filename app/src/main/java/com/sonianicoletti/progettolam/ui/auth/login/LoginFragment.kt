package com.sonianicoletti.progettolam.ui.auth.login

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sonianicoletti.progettolam.R
import com.sonianicoletti.progettolam.databinding.FragmentLoginBinding
import com.sonianicoletti.progettolam.ui.auth.login.LoginViewModel.ViewState.Idle
import com.sonianicoletti.progettolam.ui.auth.login.LoginViewModel.ViewState.Loading
import com.sonianicoletti.progettolam.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater)
        setOnClickListeners()
        observeViewState()
        observeViewEvents()
        return binding.root
    }

    private fun setOnClickListeners() {
        binding.button.setOnClickListener {
            when {
                binding.editTextUsername.text.toString().isNotBlank() && binding.editTextPassword.text.toString().isNotBlank() -> {
                    viewModel.handleLoginButton(binding.editTextUsername.text.toString(), binding.editTextPassword.text.toString())
                }
                binding.editTextUsername.text.toString().isBlank() -> {
                    binding.editTextUsername.error = "Insert username"
                }
                else -> {
                    binding.editTextPassword.error = "Insert password"
                }
            }
        }

        binding.textViewGoToRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }

    private fun observeViewState() = viewModel.viewState.observe(viewLifecycleOwner) { state ->
        when (state) {
            Loading -> binding.progressLayout.isVisible = true
            Idle -> binding.progressLayout.isVisible = false
        }
    }

    private fun observeViewEvents() = viewModel.viewEvent.observe(viewLifecycleOwner) { event ->
        when (event) {
            LoginViewModel.ViewEvent.HandleWrongCredentials -> handleWrongCredentials()
            LoginViewModel.ViewEvent.NavigateToMain -> navigateToMain()
        }
    }

    private fun handleWrongCredentials() {
        MaterialAlertDialogBuilder(requireContext()).setMessage("Wrong credentials").show()
    }

    private fun navigateToMain() {
        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK) // non fa tornare indietro
        context?.startActivity(intent)
        activity?.finish()
    }
}