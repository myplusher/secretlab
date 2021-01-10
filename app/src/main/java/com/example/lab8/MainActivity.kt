package com.example.lab8

import android.graphics.Matrix
import android.graphics.RectF
import android.hardware.Camera
import android.hardware.Camera.getCameraInfo
import android.os.Bundle
import android.os.Environment
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    var sv: SurfaceView? = null
    var holder: SurfaceHolder? = null
    var holderCallback: HolderCallback? = null
    var camera: Camera? = null

    val CAMERA_ID: Int = 0;
    val FULL_SCREEN = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_main)

        sv = findViewById<View>(R.id.surfaceView) as SurfaceView
        holder = sv!!.holder
        holder?.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
        holderCallback = HolderCallback()
        holder?.addCallback(holderCallback)

//        val pictures: File = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
//        var photoFile = File(pictures, "myphoto.jpg")
//        camera!!.takePicture(null, null,
//                { data, camera ->
//                    try { val fos = FileOutputStream(photoFile)
//                        fos.write(data)
//                        fos.close()
//                    } catch (e: java.lang.Exception) {
//                        e.printStackTrace()
//                    }
//                }
//        )

    }

    override fun onResume() {
        super.onResume()
        camera = Camera.open(CAMERA_ID)
        setPreviewSize(FULL_SCREEN)
    }

    override fun onPause() {
        super.onPause()
        if (camera != null) camera!!.release()
        camera = null
    }


    inner class HolderCallback : SurfaceHolder.Callback {

        override fun surfaceCreated(holder: SurfaceHolder) {
            try {
                camera?.setPreviewDisplay(holder)
                camera?.startPreview()
            } catch (e: Exception) {
                e.stackTrace
            }
        }

        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            camera?.stopPreview()
            setCameraDisplayOrientation(CAMERA_ID)
            try {
                camera?.setPreviewDisplay(holder)
                camera?.startPreview()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun surfaceDestroyed(holder: SurfaceHolder) {

        }
    }
    fun setPreviewSize(fullScreen: Boolean) {

        val display = windowManager.defaultDisplay
        val widthIsMax = display.width > display.height


        val size: Camera.Size = camera!!.parameters.previewSize
        val rectDisplay = RectF()
        val rectPreview = RectF()


        rectDisplay.set(0F, 0F, display.width.toFloat(), display.height.toFloat())


        if (widthIsMax) {
            rectPreview.set(0F, 0F, size.width.toFloat(), size.height.toFloat())
        } else {

            rectPreview.set(0F, 0F, size.height.toFloat(), size.width.toFloat())
        }
        val matrix = Matrix()

        if (!fullScreen) {

            matrix.setRectToRect(rectPreview, rectDisplay, Matrix.ScaleToFit.START)
        } else {

            matrix.setRectToRect(rectDisplay, rectPreview, Matrix.ScaleToFit.START)
            matrix.invert(matrix)
        }

        matrix.mapRect(rectPreview)

        sv!!.layoutParams.height = rectPreview.bottom.toInt()
        sv!!.layoutParams.width = rectPreview.right.toInt()
    }

    fun setCameraDisplayOrientation(cameraId: Int) {

        val rotation = windowManager.defaultDisplay.rotation
        var degrees = 0
        when (rotation) {
            Surface.ROTATION_0 -> degrees = 0
            Surface.ROTATION_90 -> degrees = 90
            Surface.ROTATION_180 -> degrees = 180
            Surface.ROTATION_270 -> degrees = 270
        }
        var result = 0


        val info = Camera.CameraInfo()
        getCameraInfo(cameraId, info)
        if (info.facing === Camera.CameraInfo.CAMERA_FACING_BACK) {
            result = 360 - degrees + info.orientation
        } else if (info.facing === Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = 360 - degrees - info.orientation
            result += 360
        }
        result = result % 360
        camera!!.setDisplayOrientation(result)


    }

}
