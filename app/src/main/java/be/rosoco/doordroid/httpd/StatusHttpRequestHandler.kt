package be.rosoco.doordroid.httpd

import be.rosoco.doordroid.MainActivity
import fi.iki.elonen.NanoHTTPD
import org.json.JSONObject

class StatusHttpRequestHandler(private val mainActivity: MainActivity) : HttpRequestHandler {

    override fun handleRequest(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val jsonObject = JSONObject()
            .put("status", mainActivity.doorSip.status.toString())
            .put(
                "settings", JSONObject()
                    .put("SIP server", mainActivity.doorSip.sipProfile.sipDomain)
                    .put("SIP username", mainActivity.doorSip.sipProfile.userName)
                    .put("SIP Default Callee", mainActivity.doorSip.sipCallee)
            )

        return NanoHTTPD.newFixedLengthResponse(
            NanoHTTPD.Response.Status.OK,
            "application/json",
            jsonObject.toString()
        )
    }

}
