package be.rosoco.doordroid

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.PowerManager
import android.text.format.Formatter.formatIpAddress
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import be.rosoco.doordroid.fotoapparat.Camera
import be.rosoco.doordroid.httpd.Httpd
import be.rosoco.doordroid.sip.DoorSip

const val port = 8060

class MainActivity : AppCompatActivity() {

    val server: Httpd = Httpd(this, port)
    val doorSip: DoorSip = DoorSip(this)
    val camera: Camera = Camera(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setText("Starting...")
        turnOffScreen()
    }

    override fun onStart() {
        super.onStart()
        server.start()
        doorSip.connect()
        camera.start()
    }

    override fun onStop() {
        super.onStop()
        camera.stop()
        doorSip.disconnect()
        server.stop()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // action with ID action_settings was selected
            R.id.action_setup_wizard -> startActivity(Intent(this, OnBoardingActivity1::class.java))
            else -> {}
        }

        return true
    }


    fun turnOnScreen() {
        // turn on screen
        Log.v("ProximityActivity", "ON!")
        val powerManager = applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        val mWakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP, "doordroid:wakelock")
        mWakeLock.acquire()
    }

    @TargetApi(21) //Suppress lint error for PROXIMITY_SCREEN_OFF_WAKE_LOCK
    fun turnOffScreen() {
        // turn off screen
        Log.v("ProximityActivity", "OFF!")
        val powerManager = applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        val mWakeLock = powerManager.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, "doordroid:wakelock")
        mWakeLock.acquire()
    }

    fun setText(msg: String) {
        runOnUiThread {
            val wm = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val ip = formatIpAddress(wm.connectionInfo.ipAddress)
            val ipAddress = findViewById<TextView>(R.id.ip_address)
            ipAddress.text = ip + ":" + port + "\n" + msg
        }
    }

}
