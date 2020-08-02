package com.example.snapsolvesudoku.fragments

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.example.snapsolvesudoku.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

    }
}