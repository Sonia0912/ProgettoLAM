package com.sonianicoletti.progettolam.ui.main.joingame

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sonianicoletti.progettolam.R
import com.sonianicoletti.progettolam.databinding.FragmentJoinGameBinding
import com.sonianicoletti.progettolam.ui.auth.AuthActivity
import com.sonianicoletti.zxing.QRCodeScannerActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class JoinGameFragment : Fragment() {

    private lateinit var binding: FragmentJoinGameBinding
    private val viewModel: JoinGameViewModel by viewModels()

    // inizia l'Activity per scannerizzare il codice QR
    private val qrCodeScannerLauncher = registerForActivityResult(StartActivityForResult()) { onQrCodeScanned(it) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentJoinGameBinding.inflate(inflater)
        setClickListeners()
        observeViewEvents()
        return binding.root
    }

    private fun setClickListeners() {
        // Torna alla schermata principale
        binding.imageViewBack.setOnClickListener { findNavController().navigateUp() }

        // Partecipa con l'ID
        binding.buttonJoin.setOnClickListener { joinGameById() }

        // Partecipa con il QR code
        binding.joinByQrButton.setOnClickListener { launchQRCodeScanner() }
    }

    private fun joinGameById() {
        val gameID = binding.editTextGameID.text.toString()
        viewModel.joinGame(gameID)
    }

    private fun launchQRCodeScanner() {
        val intent = Intent(requireContext(), QRCodeScannerActivity::class.java)
        qrCodeScannerLauncher.launch(intent)
    }

    private fun observeViewEvents() = viewModel.viewEvent.observe(viewLifecycleOwner) {
        when (it) {
            is JoinGameViewModel.ViewEvent.NavigateToLobby -> navigateToLobby()
            JoinGameViewModel.ViewEvent.ShowGameNotFoundAlert -> showGameNotFoundAlert()
            JoinGameViewModel.ViewEvent.ShowBlankFieldError -> binding.editTextGameID.error = getString(R.string.join_game_error_blank_field)
            JoinGameViewModel.ViewEvent.ShowMinCharsNotAddedError -> binding.editTextGameID.error = getString(R.string.join_game_error_invalid_id)
            JoinGameViewModel.ViewEvent.ShowUnableToJoinGameAlert -> showUnableToJoinGameAlert()
            JoinGameViewModel.ViewEvent.ShowUserNotLoggedInAlert -> showUserNotFoundAlert()
        }
    }

    private fun navigateToLobby() {
        findNavController().navigate(R.id.lobbyFragment)
    }

    private fun showGameNotFoundAlert() {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(getString(R.string.join_game_error_game_not_found))
            .show()
    }

    private fun showUnableToJoinGameAlert() {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(getString(R.string.join_game_error_game_full))
            .show()
    }

    private fun showUserNotFoundAlert() {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(getString(R.string.please_log_in))
            .setPositiveButton(android.R.string.ok) { _, _ -> navigateToAuthActivity() }
            .setOnDismissListener { navigateToAuthActivity() }
            .show()
    }

    private fun navigateToAuthActivity() {
        val intent = Intent(requireContext(), AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun onQrCodeScanned(result: ActivityResult) {
        if (result.resultCode == RESULT_OK) {
            val intent = result.data
            val qrCodeContent = intent?.getStringExtra(QRCodeScannerActivity.EXTRA_QR_CONTENT)
            qrCodeContent?.let { viewModel.joinGame(it) }
        }
    }
}
