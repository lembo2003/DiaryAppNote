package com.example.myapplication.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.DefaultItemAnimator
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.example.myapplication.base.BaseFragment
import com.example.myapplication.databinding.FragmentHomeBinding
import com.example.myapplication.data.PreferencesDataStore
import com.example.myapplication.ui.diary.DiaryViewModel
import com.example.myapplication.data.Result
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.example.myapplication.R
import android.os.Bundle
import androidx.activity.addCallback

class HomeFragment : BaseFragment<FragmentHomeBinding>() {

    private val viewModel: DiaryViewModel by viewModels()
    private val diaryAdapter = DiaryAdapter()
    private val preferencesDataStore by lazy { 
        PreferencesDataStore.getInstance(requireContext()) 
    }
    private var isLanguageSelected = false

    override fun inflateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentHomeBinding {
        return FragmentHomeBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Check if language is selected
        viewLifecycleOwner.lifecycleScope.launch {
            preferencesDataStore.selectedLanguage.collectLatest { languageCode ->
                if (languageCode.isBlank()) {
                    // No language selected, navigate to language screen
                    findNavController().navigate(
                        HomeFragmentDirections.actionHomeToLanguage()
                    )
                } else {
                    isLanguageSelected = true
                    val flagResId = requireContext().resources.getIdentifier(
                        "flag_$languageCode",
                        "drawable",
                        requireContext().packageName
                    )
                    if (flagResId != 0) {
                        binding.btnLanguage.setImageResource(flagResId)
                    }
                }
            }
        }
    }

    override fun setupUI() {
        binding.rvDiaries.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = diaryAdapter
            itemAnimator = DefaultItemAnimator()
        }

        binding.btnLanguage.setOnClickListener {
            findNavController().navigate(
                HomeFragmentDirections.actionHomeToLanguage()
            )
        }

        binding.fabAdd.setOnClickListener {
            findNavController().navigate(
                HomeFragmentDirections.actionHomeToCreate()
            )
        }

        // Setup adapter click listener
        diaryAdapter.onEntryClick = { entry ->
            findNavController().navigate(
                HomeFragmentDirections.actionHomeToEdit(entry.id)
            )
        }
    }

    override fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.entries.collect { result ->
                when (result) {
                    is Result.Success -> {
                        diaryAdapter.submitList(result.data)
                        if (result.data.isEmpty()) {
                            binding.emptyView.isVisible = true
                            binding.rvDiaries.isVisible = false
                        } else {
                            binding.emptyView.isVisible = false
                            binding.rvDiaries.isVisible = true
                        }
                    }
                    is Result.Error -> {
                        showError(result.exception.message ?: "Unknown error")
                    }
                    Result.Loading -> {
                        // Handle loading if needed
                    }
                }
            }
        }
    }

    private fun showError(message: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.error))
            .setMessage(message)
            .setPositiveButton(getString(R.string.ok), null)
            .show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            showExitConfirmationDialog()
        }
    }

    private fun showExitConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.app_name)
            .setMessage(getString(R.string.exit_confirmation))
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(getString(R.string.exit)) { _, _ ->
                requireActivity().finish()
            }
            .show()
    }
} 