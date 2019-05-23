package be.rosoco.doordroid.httpd

import be.rosoco.doordroid.MainActivity
import fi.iki.elonen.NanoHTTPD

class Httpd(private val mainActivity: MainActivity, port: Int) : NanoHTTPD("0.0.0.0", port) {

    val urlHandlers = hashMapOf(
        "/" to HelloWorldHttpRequestHandler(mainActivity),
        "/status" to StatusHttpRequestHandler(mainActivity),
        "/call" to CallHttpRequestHandler(mainActivity),
        "/camera/stream" to CameraStreamImageHttpRequestHandler(mainActivity),
        "/camera/still" to CameraStillImageHttpRequestHandler(mainActivity))

    override fun serve(session: IHTTPSession): Response {
        return if(urlHandlers.containsKey(session.uri)) urlHandlers.get(session.uri)!!.handleRequest(session)
        else newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "Not found")
    }
}