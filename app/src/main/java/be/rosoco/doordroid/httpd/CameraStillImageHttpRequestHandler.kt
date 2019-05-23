package be.rosoco.doordroid.httpd

import be.rosoco.doordroid.MainActivity
import fi.iki.elonen.NanoHTTPD
import java.io.ByteArrayInputStream

class CameraStillImageHttpRequestHandler (private val mainActivity: MainActivity) : HttpRequestHandler {


    override fun handleRequest(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val picture = mainActivity.camera.takePicture()

        return NanoHTTPD.newFixedLengthResponse(
            NanoHTTPD.Response.Status.OK,
            "image/jpeg",
            ByteArrayInputStream(picture),
            picture.size.toLong()
        )
    }
}