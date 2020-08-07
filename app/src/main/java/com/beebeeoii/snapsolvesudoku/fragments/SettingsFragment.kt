package com.beebeeoii.snapsolvesudoku.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentContainerView
import androidx.navigation.fragment.findNavController
import com.beebeeoii.snapsolvesudoku.preferences.Preferences
import com.beebeeoii.snapsolvesudoku.R
import com.google.android.material.appbar.MaterialToolbar

private lateinit var fragmentContainer: FragmentContainerView
private lateinit var appBar: MaterialToolbar

class SettingsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_settings, container, false)

        fragmentContainer = rootView.findViewById(R.id.settingsFragmentContainer)
        requireActivity()
            .supportFragmentManager
            .beginTransaction()
            .replace(R.id.settingsFragmentContainer, Preferences())
            .addToBackStack(null)
            .commit()
        appBar = rootView.findViewById(R.id.appBar)

        appBar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        appBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.about -> {
                    val action = SettingsFragmentDirections.actionSettingsFragmentToAboutFragment()
                    findNavController().navigate(action)
                    true
                }
                else -> false
            }
        }


        return rootView
    }
}