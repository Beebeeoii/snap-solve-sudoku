package com.beebeeoii.snapsolvesudoku.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentContainerView
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.beebeeoii.snapsolvesudoku.preferences.Preferences
import com.beebeeoii.snapsolvesudoku.R
import com.beebeeoii.snapsolvesudoku.databinding.FragmentSettingsBinding

private const val TAG = "SettingsFragment"

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentSettingsBinding.inflate(inflater, container, false)
        requireActivity()
            .supportFragmentManager
            .beginTransaction()
            .replace(R.id.settingsFragmentContainer, Preferences())
            .addToBackStack(null)
            .commit()

        binding.appBar.setNavigationOnClickListener {
            val action = SettingsFragmentDirections.actionSettingsFragmentPop()
            findNavController().navigate(action)
        }

//        appBar = rootView.findViewById(R.id.appBar)
//
//        appBar.setNavigationOnClickListener {
//            requireActivity().onBackPressed()
//        }
//
//        appBar.setOnMenuItemClickListener {
//            when (it.itemId) {
//                R.id.about -> {
//                    val action = SettingsFragmentDirections.actionSettingsFragmentToAboutFragment()
//                    findNavController().navigate(action)
//                    true
//                }
//                else -> false
//            }
//        }

        return binding.root
    }
}