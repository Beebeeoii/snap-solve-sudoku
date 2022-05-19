package com.beebeeoii.snapsolvesudoku.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

        return binding.root
    }
}