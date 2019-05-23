package be.rosoco.doordroid.fotoapparat

interface CameraStreamListener {
    fun onImageAvailable(byteArray: ByteArray)
}