package com.nullform.ashbox.ui.settings

import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.nullform.ashbox.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences_root, rootKey)

        // Find the 'app_theme' ListPreference
        val appThemePreference: ListPreference? = findPreference("app_theme")
        appThemePreference?.let {
            // Set the initial summary based on the current value
            setListPreferenceSummary(it, it.value)

            // Set a listener to update the summary when the value changes
            it.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
                setListPreferenceSummary(preference as ListPreference, newValue.toString())
                true // Indicate that the new value should be saved
            }
        }
    }

    /**
     * Helper function to update the summary of a ListPreference.
     */
    private fun setListPreferenceSummary(preference: ListPreference, value: String?) {
        val index = preference.findIndexOfValue(value)
        // Set the summary to the entry corresponding to the selected value
        preference.summary = if (index >= 0) preference.entries[index] else null
    }
}