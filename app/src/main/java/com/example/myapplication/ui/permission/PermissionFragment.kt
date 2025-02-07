package com.example.myapplication.ui.permission

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.myapplication.R
import com.example.myapplication.base.BaseFragment
import com.example.myapplication.databinding.FragmentPermissionBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class PermissionFragment : BaseFragment<FragmentPermissionBinding>() {

    private val args: PermissionFragmentArgs by navArgs()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        handlePermissionResult(isGranted)
    }

    override fun inflateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentPermissionBinding {
        return FragmentPermissionBinding.inflate(inflater, container, false)
    }

    override fun setupUI() {
        binding.apply {
            // Set initial state
            switchPermission.isChecked = hasPermission()
            
            switchPermission.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked && !hasPermission()) {
                    requestPermission()
                } else if (!isChecked && hasPermission()) {
                    // Can't revoke permission programmatically
                    switchPermission.isChecked = true
                }
            }
        }

        // If permission is already granted, navigate to image screen
        if (hasPermission()) {
            navigateToImageScreen()
        }
    }

    private fun requestPermission() {
        val permission = getRequiredPermission()

        when {
            hasPermission() -> {
                navigateToImageScreen()
            }
            shouldShowRequestPermissionRationale(permission) -> {
                showPermissionRationale()
            }
            else -> {
                requestPermissionLauncher.launch(permission)
            }
        }
    }

    private fun hasPermission(): Boolean {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        return ContextCompat.checkSelfPermission(
            requireContext(),
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun showError() {
        binding.apply {
            tvError.isVisible = true
            switchPermission.isChecked = false
        }
    }

    private fun navigateToImageScreen() {
        findNavController().navigate(
            PermissionFragmentDirections.actionPermissionToImage(
                args.selectedImages
            )
        )
    }

    private fun showPermissionRationale() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.permission_required)
            .setMessage(R.string.permission_rationale)
            .setPositiveButton(R.string.grant) { _, _ ->
                requestPermissionLauncher.launch(getRequiredPermission())
            }
            .setNegativeButton(R.string.cancel) { _, _ ->
                binding.switchPermission.isChecked = false
            }
            .show()
    }

    private fun getRequiredPermission() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    private fun handlePermissionResult(isGranted: Boolean) {
        if (isGranted) {
            findNavController().navigate(
                PermissionFragmentDirections.actionPermissionToImage(
                    args.selectedImages
                )
            )
        } else {
            // Show error and navigate back to previous screen
            showError()
            findNavController().popBackStack()
        }
    }
} 