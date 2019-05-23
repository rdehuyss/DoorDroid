package be.rosoco.doordroid



import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.preference.PreferenceManager
import butterknife.ButterKnife
import butterknife.OnClick


class OnBoardingActivity2 : AppCompatActivity() {

    private val PERMISSION_ALL: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding_2)
        supportFragmentManager.beginTransaction().replace(R.id.sip_settings, SipSettingsFragment()).commit()
        ButterKnife.bind(this)

        val permissionsNeeded = arrayOf(
            Manifest.permission.CALL_PHONE,
            Manifest.permission.USE_SIP,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.MODIFY_AUDIO_SETTINGS,
            Manifest.permission.CAMERA)
        if(!hasPermissions(permissionsNeeded)) {
            ActivityCompat.requestPermissions(this, permissionsNeeded, PERMISSION_ALL)
        }
    }

    @OnClick(R.id.button_next)
    fun next() {
        if(allSettingsCompleted()) {
            startActivity(Intent(this, OnBoardingActivity3::class.java))
        } else {
            val toast = Toast.makeText(applicationContext, R.string.all_settings_required, Toast.LENGTH_LONG)
            toast.show()
        }
    }

    private fun allSettingsCompleted(): Boolean {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val sipServerCompleted = sharedPreferences.contains("sip_server")
        val sipUsernameCompleted = sharedPreferences.contains("sip_username")
        val sipPasswordCompleted = sharedPreferences.contains("sip_password")
        return sipServerCompleted && sipUsernameCompleted && sipPasswordCompleted
    }

    class SipSettingsFragment : AbstractPreference() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.settings_sip, rootKey)
        }
    }

    private fun hasPermissions(permissions: Array<String>): Boolean = permissions.all {
        ActivityCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }
}