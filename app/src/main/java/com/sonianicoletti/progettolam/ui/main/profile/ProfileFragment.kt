package com.sonianicoletti.progettolam.ui.main.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.sonianicoletti.entities.User
import com.sonianicoletti.progettolam.databinding.FragmentProfileBinding
import com.sonianicoletti.progettolam.ui.auth.AuthActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater)
        (activity as AppCompatActivity).apply {
            setSupportActionBar(binding.toolBar)
            supportActionBar?.setDisplayShowTitleEnabled(false)
        }

        // Torna alla schermata principale
        binding.imageViewBack.setOnClickListener {
            findNavController().navigateUp()
        }

        // Mostra l'e-mail e lo username dell'utente
        viewModel.userState.observe(viewLifecycleOwner) {
            setInformation(it.user)
        }

        // Salva le modifiche allo username
        binding.buttonSave.setOnClickListener {
            if(binding.editTextDisplayName.text.toString().isNotBlank()) {
                viewModel.saveNewDisplayName(binding.editTextDisplayName.text.toString())
            }
        }

        // Logout
        binding.buttonLogOut.setOnClickListener {
            viewModel.handleLogOut()
        }

        observeViewEvents()

        return binding.root
    }

    private fun navigateToLogin() {
        val intent = Intent(requireContext(), AuthActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK) // non fa tornare indietro
        context?.startActivity(intent)
    }

    private fun showSuccessfulUpdate() {
        Toast.makeText(context,"Information updated successfully",Toast.LENGTH_SHORT).show()
    }

    private fun observeViewEvents() = viewModel.viewEvent.observe(viewLifecycleOwner) {
        when (it) {
            ProfileViewModel.ViewEvent.NavigateToLogin -> navigateToLogin()
            ProfileViewModel.ViewEvent.SuccessfulUpdate -> showSuccessfulUpdate()
        }
    }

    private fun setInformation(user: User) {
        binding.textViewUserEmail.text = user.email
        binding.editTextDisplayName.setText(user.displayName)
    }

}