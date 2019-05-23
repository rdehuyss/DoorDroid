package be.rosoco.doordroid.fotoapparat

import be.rosoco.doordroid.MainActivity
import io.fotoapparat.Fotoapparat
import io.fotoapparat.configuration.CameraConfiguration
import io.fotoapparat.parameter.Resolution
import io.fotoapparat.parameter.ScaleType
import io.fotoapparat.preview.Frame
import io.fotoapparat.selector.ResolutionSelector
import io.fotoapparat.selector.front
import io.fotoapparat.util.FrameProcessor
import io.fotoapparat.view.CameraView
import java.io.ByteArrayOutputStream
import android.graphics.*
import android.graphics.Bitmap
import android.graphics.YuvImage
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import be.rosoco.doordroid.R


class Camera(private val mainActivity: MainActivity) : FrameProcessor {


    private lateinit var fotoApparat : Fotoapparat

    var cameraStreamListener: CameraStreamListener? = null

    private fun averageResolution(): ResolutionSelector = { find { resolution -> findBestResolution(resolution) }}

    private fun findBestResolution(resolution: Resolution): Boolean {
        return(resolution.width >= 720) && (resolution.width <= 1024)
    }

    fun start() {
        fotoApparat = Fotoapparat(mainActivity, mainActivity.findViewById<CameraView>(R.id.camera_view),
            cameraConfiguration = CameraConfiguration(
                pictureResolution = averageResolution(),
                frameProcessor = this),
            scaleType = ScaleType.CenterCrop,    // (optional) we want the preview to fill the view
            lensPosition = front()
        )
        fotoApparat.start()
    }

    fun stop() {
        fotoApparat.stop()
    }

    fun takePicture(): ByteArray {
        return fotoApparat.takePicture()
            .toBitmap().await().bitmap
            .rotate(270F)
            .toJpeg()
            .invoke()
    }

    override fun invoke(frame: Frame) {
        if(cameraStreamListener == null) return

        frame
            .toBitmap()
            .rotate(270F)
            .toJpeg()
            .apply { cameraStreamListener?.onImageAvailable(this()) }
    }

    private fun Frame.toBitmap() : Bitmap {
        val out = ByteArrayOutputStream()
        val yuvImage = YuvImage(this.image, ImageFormat.NV21, this.size.width, this.size.height, null)
        yuvImage.compressToJpeg(Rect(0, 0, this.size.width, this.size.height), 100, out)
        val imageBytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    private fun Bitmap.toJpeg(): () -> ByteArray {
        return {
            val stream = ByteArrayOutputStream()
            this.compress(Bitmap.CompressFormat.JPEG, 90, stream)
            stream.toByteArray()
        }
    }

    private fun Bitmap.rotate(degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(
            this,
            0,
            0,
            this.width,
            this.height,
            matrix,
            true
        )
    }
}