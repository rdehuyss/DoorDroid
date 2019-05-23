package be.rosoco.doordroid

import android.content.SharedPreferences
import android.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceGroup
import androidx.preference.PreferenceManager

abstract class AbstractPreference  : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {


    override fun onResume() {
        super.onResume()
        val prefs = PreferenceManager.getDefaultSharedPreferences(this.context)
        prefs.registerOnSharedPreferenceChangeListener(this)

        for (i in 0 until preferenceScreen.preferenceCount) {
            val preference = preferenceScreen.getPreference(i)
            if (preference is PreferenceGroup) {
                for (j in 0 until preference.preferenceCount) {
                    val singlePref = preference.getPreference(j)
                    updatePreference(singlePref, singlePref.key)
                }
            } else {
                updatePreference(preference, preference.key)

            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        updatePreference(findPreference(key), key)
    }

    private fun updatePreference(preference: Preference?, key: String) {
        if (preference == null) return
        if (preference is ListPreference) {
            val listPreference = preference as ListPreference?
            listPreference!!.summary = listPreference.entry
            return
        }
        val sharedPrefs = preferenceManager.sharedPreferences
        if(sharedPrefs.contains(key)) {
            preference.summary = sharedPrefs.getString(key, null)
        }
    }
}