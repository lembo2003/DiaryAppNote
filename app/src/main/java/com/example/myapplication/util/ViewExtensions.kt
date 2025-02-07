package com.example.myapplication.util

import android.content.Context
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

fun View.findFragment(): Fragment {
    val context = context
    if (context is FragmentActivity) {
        val fragmentManager = context.supportFragmentManager
        fragmentManager.fragments.forEach { fragment ->
            if (fragment.view?.findViewById<View>(this.id) != null) {
                return fragment
            }
        }
    }
    throw IllegalStateException("View is not attached to a Fragment")
} 