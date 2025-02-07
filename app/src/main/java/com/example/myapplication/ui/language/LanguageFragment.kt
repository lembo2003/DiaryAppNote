package com.example.myapplication.ui.language

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.DefaultItemAnimator
import com.example.myapplication.base.BaseFragment
import com.example.myapplication.databinding.FragmentLanguageBinding
import com.example.myapplication.data.PreferencesDataStore
import android.os.Bundle
import android.graphics.Color
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.google.android.material.transition.MaterialContainerTransform
import com.example.myapplication.R

class LanguageFragment : BaseFragment<FragmentLanguageBinding>() {

    private val languageAdapter = LanguageAdapter()
    private var selectedLanguage: Language? = null
    private val preferencesDataStore by lazy { 
        PreferencesDataStore.getInstance(requireContext()) 
    }
    private var currentLanguageCode = "gb" // Default to English

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = MaterialContainerTransform().apply {
            duration = 300L
            scrimColor = Color.TRANSPARENT
            setAllContainerColors(requireContext().getColor(R.color.background))
        }
        postponeEnterTransition()
    }

    override fun inflateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentLanguageBinding {
        return FragmentLanguageBinding.inflate(inflater, container, false)
    }

    override fun setupUI() {
        binding.rvLanguages.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = languageAdapter
            itemAnimator = DefaultItemAnimator().apply {
                addDuration = 200
                removeDuration = 200
                moveDuration = 200
                changeDuration = 200
            }
        }

        // First get the current language before setting up the list
        viewLifecycleOwner.lifecycleScope.launch {
            preferencesDataStore.selectedLanguage.collectLatest { languageCode ->
                currentLanguageCode = languageCode
                // Update the list with the current language
                languageAdapter.submitList(getSampleLanguages(languageCode))
                // Update the radio button selection
                languageAdapter.setSelectedLanguage(languageCode)
            }
        }

        languageAdapter.onLanguageSelected = { language ->
            selectedLanguage = language
            viewLifecycleOwner.lifecycleScope.launch {
                preferencesDataStore.setSelectedLanguage(language.countryCode)
            }
        }

        binding.btnDone.setOnClickListener {
            findNavController().navigate(
                LanguageFragmentDirections.actionLanguageToHome()
            )
        }

        startPostponedEnterTransition()
    }

    private fun getSampleLanguages(currentLanguageCode: String) = listOf(
        Language("English", "gb", currentLanguageCode == "gb"),
        Language("Hindi", "in", currentLanguageCode == "in"),
        Language("Spanish", "es", currentLanguageCode == "es"),
        Language("French", "fr", currentLanguageCode == "fr"),
        Language("Arabic", "sa", currentLanguageCode == "sa"),
        Language("Bengali", "bd", currentLanguageCode == "bd")
    )
} 