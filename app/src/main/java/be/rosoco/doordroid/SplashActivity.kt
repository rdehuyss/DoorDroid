package be.rosoco.doordroid

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity


class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (isDoorDroidSetup() == true) {
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            startActivity(Intent(this, OnBoardingActivity1::class.java))
        }
    }

    private fun isDoorDroidSetup(): Boolean? {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        return sharedPref?.contains("setup-complete")
    }

}