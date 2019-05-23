package be.rosoco.doordroid.httpd

import be.rosoco.doordroid.MainActivity
import fi.iki.elonen.NanoHTTPD

class HelloWorldHttpRequestHandler(private val mainActivity: MainActivity) : HttpRequestHandler {

    override fun handleRequest(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        return NanoHTTPD.newFixedLengthResponse("Hello world from DoorDroid")
    }

}
