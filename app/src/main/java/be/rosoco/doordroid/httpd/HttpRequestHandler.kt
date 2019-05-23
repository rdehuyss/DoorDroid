package be.rosoco.doordroid.httpd

import fi.iki.elonen.NanoHTTPD

interface HttpRequestHandler {

    fun handleRequest(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response
}