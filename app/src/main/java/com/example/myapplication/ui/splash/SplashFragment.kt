package com.example.myapplication.ui.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R
import com.example.myapplication.base.BaseFragment
import com.example.myapplication.databinding.FragmentSplashBinding
import com.example.myapplication.data.PreferencesDataStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SplashFragment : BaseFragment<FragmentSplashBinding>() {
    private val preferencesDataStore by lazy { 
        PreferencesDataStore.getInstance(requireContext()) 
    }

    override fun inflateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSplashBinding {
        return FragmentSplashBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.root.startAnimation(
            AnimationUtils.loadAnimation(requireContext(), R.anim.splash_fade_in)
        )

        // Check language selection after a delay
        viewLifecycleOwner.lifecycleScope.launch {
            delay(1500) // Splash screen delay
            preferencesDataStore.selectedLanguage.collectLatest { languageCode: String ->
                if (languageCode.isBlank()) {
                    // No language selected, go to language screen
                    findNavController().navigate(
                        SplashFragmentDirections.actionSplashToLanguage()
                    )
                } else {
                    // Language already selected, go to home
                    findNavController().navigate(
                        SplashFragmentDirections.actionSplashToHome()
                    )
                }
            }
        }
    }
} 