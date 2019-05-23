package be.rosoco.doordroid.httpd

import android.content.Intent
import android.util.Log
import be.rosoco.doordroid.MainActivity
import fi.iki.elonen.NanoHTTPD
import org.json.JSONObject

class CallHttpRequestHandler(private val mainActivity: MainActivity) : HttpRequestHandler {

    override fun handleRequest(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        if(session.queryParameterString.isNullOrEmpty()) {
            mainActivity.doorSip.call()
            return NanoHTTPD.newFixedLengthResponse(
                NanoHTTPD.Response.Status.OK,
                "application/json",
                JSONObject()
                    .put("status", "ok")
                    .put("calling", mainActivity.doorSip.sipCallee)
                    .toString()
            )
        }

        val whoToCall = session.queryParameterString.removePrefix("who=")
        Log.i("DoorDroid", whoToCall)
        mainActivity.doorSip.call(whoToCall)
        return NanoHTTPD.newFixedLengthResponse(
            NanoHTTPD.Response.Status.OK,
            "application/json",
            JSONObject()
                .put("status", "ok")
                .put("calling", whoToCall)
                .toString()
        )
    }

}
