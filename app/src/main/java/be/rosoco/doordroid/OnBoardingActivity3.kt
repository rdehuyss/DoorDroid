package be.rosoco.doordroid



import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import butterknife.ButterKnife
import butterknife.OnClick


class OnBoardingActivity3 : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding_3)
        ButterKnife.bind(this)

        supportFragmentManager.beginTransaction().replace(R.id.homeassistant_settings, HomeAssistantSettingsFragment()).commit()
    }

    class HomeAssistantSettingsFragment : AbstractPreference() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.settings_homeassistant, rootKey)
        }
    }

    @OnClick(R.id.button_next)
    fun next() {
        if(allSettingsCompleted()) {
            markSetupAsCompleted()
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            val toast = Toast.makeText(applicationContext, R.string.all_settings_required, Toast.LENGTH_LONG)
            toast.show()
        }

    }

    private fun markSetupAsCompleted() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val edit = sharedPreferences?.edit()
        edit?.putBoolean("setup-complete", true)
        edit?.commit()
    }

    private fun allSettingsCompleted(): Boolean {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val hassServerCompleted = sharedPreferences.contains("hass_server")
        val hassTokenCompleted = sharedPreferences.contains("hass_token")
        val hassDoorbellEntityCompleted = sharedPreferences.contains("hass_doorbell_entity")
        return hassServerCompleted && hassTokenCompleted && hassDoorbellEntityCompleted
    }
}