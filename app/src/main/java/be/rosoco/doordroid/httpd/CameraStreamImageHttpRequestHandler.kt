package be.rosoco.doordroid.httpd

import android.util.Log
import be.rosoco.doordroid.MainActivity
import be.rosoco.doordroid.fotoapparat.CameraStreamListener
import fi.iki.elonen.NanoHTTPD
import java.io.*

class CameraStreamImageHttpRequestHandler (private val mainActivity: MainActivity) : HttpRequestHandler, CameraStreamListener {


    private lateinit var mjpgStream : NanoHTTPD.MultipartStream

    override fun handleRequest(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        mjpgStream = NanoHTTPD.MultipartStream()
        mainActivity.camera.cameraStreamListener = this
        return NanoHTTPD.newMultipartResponse(NanoHTTPD.Response.Status.OK, mjpgStream)
    }

    override fun onImageAvailable(byteArray: ByteArray) {
        try {
            mjpgStream.writePart("image/jpeg", byteArray)
        } catch (e:IOException) {
            Log.w("DoorDroid", "Error sending image to client", e)
            mainActivity.camera.cameraStreamListener = null
            mjpgStream.close()
        }
    }

}

