package com.zetzaus.temiattend.ui

import android.os.Bundle
import android.text.InputType
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat
import com.zetzaus.temiattend.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        findPreference<EditTextPreference>("nfcSourceId")?.setOnBindEditTextListener {
            it.inputType = InputType.TYPE_CLASS_NUMBER
        }
    }
}