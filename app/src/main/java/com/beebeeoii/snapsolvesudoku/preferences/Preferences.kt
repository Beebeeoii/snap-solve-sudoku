package com.beebeeoii.snapsolvesudoku.preferences

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.beebeeoii.snapsolvesudoku.R

class Preferences : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_preferences, rootKey)
    }
}