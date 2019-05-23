package be.rosoco.doordroid.sip

import android.net.sip.*
import android.os.Handler
import android.util.Log
import androidx.preference.PreferenceManager
import be.rosoco.doordroid.MainActivity


class DoorSip(private val mainActivity: MainActivity) {

    enum class Status {
        Registering,
        Ready,
        RegistrationFailed,
        Ringing,
        Calling,
        CallEstablished,
        CallEnded,
        Error
    }

    private lateinit var sipManager: SipManager
    lateinit var sipProfile: SipProfile
    lateinit var status: Status
    lateinit var sipCallee: String


    fun connect() {
        sipManager = SipManager.newInstance(mainActivity)
        sipProfile = createSIPProfile()
        val sipRegistrationListener = object : SipRegistrationListener {

            override fun onRegistering(localProfileUri: String) {
                updateStatus(Status.Registering)
            }

            override fun onRegistrationDone(localProfileUri: String, expiryTime: Long) {
                updateStatus(Status.Ready)
            }

            override fun onRegistrationFailed(
                localProfileUri: String,
                errorCode: Int,
                errorMessage: String
            ) {
                updateStatus(Status.RegistrationFailed)
            }
        }
        sipManager.open(sipProfile)
        sipManager.register(sipProfile, 30, sipRegistrationListener)

        val handler = Handler()
        handler.postDelayed(object : Runnable {

            override fun run() {
                if(!sipManager.isRegistered(sipProfile.uriString)) {
                    updateStatus(Status.Error)
                    try {
                        sipManager.open(sipProfile)
                        sipManager.register(sipProfile, 30, sipRegistrationListener)
                    } catch (e: SipException) {
                        //let's try again in a minute
                    }
                }
                handler.postDelayed(this, 60000)
            }
        }, 60000)
    }

    private fun createSIPProfile(): SipProfile {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mainActivity)
        var sipServer = sharedPreferences.getString("sip_server", null)
        var sipUsername = sharedPreferences.getString("sip_username", null)
        var sipPassword = sharedPreferences.getString("sip_password", null)
        var sipPort = 5060
        sipCallee = "sip:" + sharedPreferences.getString("hass_sip_username", null) + "@" + sipServer
        if(sipServer.contains(":")) {
            sipServer = sipServer.split(":")[0]
            sipPort = sipServer.split(":")[1].toInt()
        }
        return SipProfile.Builder(sipUsername, sipServer).setPassword(sipPassword).setPort(sipPort).setDisplayName("Doorbell").build();
    }

    fun disconnect() {
        sipManager.unregister(sipProfile, null)
    }

    fun call() {
        call(sipCallee)
    }

    fun call(sipNbr: String) {
        var listener: SipAudioCall.Listener = object : SipAudioCall.Listener() {

            override fun onCalling(call: SipAudioCall?) {
                super.onCalling(call)
                Log.i("DoorDroid", "onCalling")
                updateStatus(Status.Calling)
            }

            override fun onRinging(call: SipAudioCall?, caller: SipProfile?) {
                super.onRinging(call, caller)
                Log.i("DoorDroid", "onRinging")
                updateStatus(Status.Ringing)
            }

            override fun onError(call: SipAudioCall?, errorCode: Int, errorMessage: String?) {
                super.onError(call, errorCode, errorMessage)
                Log.i("DoorDroid", "onError: $errorMessage")
                updateStatus(Status.Error)
            }

            override fun onCallEstablished(call: SipAudioCall) {
                super.onCallEstablished(call)
                Log.i("DoorDroid", "onCallEstablished")
                updateStatus(Status.CallEstablished)

                call.apply {
                    startAudio()
                    setSpeakerMode(true)
                }
            }

            override fun onCallEnded(call: SipAudioCall) {
                super.onCallEnded(call)
                Log.i("DoorDroid", "onCallEnded")
                updateStatus(Status.CallEnded)
                Thread.sleep(1000)
                updateStatus(Status.Ready)
            }
        }

        val call: SipAudioCall? = sipManager?.makeAudioCall(
            sipProfile?.uriString,
            sipNbr,
            listener,
            30
        )
    }

    private fun updateStatus(status: Status) {
        this.status = status
        mainActivity.setText(status.toString())
    }

}