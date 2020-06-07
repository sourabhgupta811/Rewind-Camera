package com.samnetworks.rewind

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.PointF
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import com.iammert.library.cameravideobuttonlib.CameraVideoButton
import com.otaliastudios.cameraview.*
import com.otaliastudios.cameraview.controls.Mode
import kotlinx.android.synthetic.main.activity_record.*
import net.alhazmy13.mediapicker.Video.VideoPicker
import java.io.File


/**
 * Created by Sourabh Gupta on 6/6/20.
 */
class RecordActivity : AppCompatActivity() {
    var permissionGranted = false
    private val CAMERA_PERMISSION_REQUEST_CODE = 0x11
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record)
        camera_view.addCameraListener(Listener())
        record_button.setVideoDuration(10000)
        record_button.enableVideoRecording(true)
        record_button.enablePhotoTaking(false)
        record_button.actionListener = object : CameraVideoButton.ActionListener {
            override fun onStartRecord() {
                captureVideo()
            }

            override fun onEndRecord() {
                stopVideo()
            }

            override fun onDurationTooShortError() {

            }

            override fun onSingleTap() {

            }
        }
        gallery.setOnClickListener {
            VideoPicker.Builder(this)
                .mode(VideoPicker.Mode.GALLERY)
                .directory(VideoPicker.Directory.DEFAULT)
                .extension(VideoPicker.Extension.MP4)
                .enableDebuggingMode(true)
                .build()
        }
        permissionGranted = checkPermission()
    }

    private fun checkPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true
        }
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), CAMERA_PERMISSION_REQUEST_CODE
            )
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_PERMISSION_REQUEST_CODE -> if (grantResults.isNotEmpty()){
                for(result in grantResults){
                    if(result != PERMISSION_GRANTED) {
                        Toast.makeText(
                            applicationContext,
                            "Permission not Granted",
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    }
                }
                permissionGranted = true
                camera_view.open()
            }
        }
    }

    private fun captureVideo() {
        if (camera_view.mode === Mode.PICTURE) {
            return
        }
        if (camera_view.isTakingPicture || camera_view.isTakingVideo) return
        camera_view.takeVideo(File(externalCacheDir, "video.mp4"))
    }

    override fun onPause() {
        super.onPause()
        if(permissionGranted)
            camera_view.close()
    }

    override fun onResume() {
        super.onResume()
        if(permissionGranted)
            camera_view.open()
    }

    override fun onDestroy() {
        super.onDestroy()
        if(permissionGranted)
            camera_view.destroy()
    }

    private fun stopVideo() {
        camera_view.stopVideo()
    }

    private inner class Listener : CameraListener() {
        override fun onCameraOpened(options: CameraOptions) {
        }

        override fun onCameraError(exception: CameraException) {
            super.onCameraError(exception)
        }

        override fun onPictureTaken(result: PictureResult) {
            super.onPictureTaken(result)
        }

        override fun onVideoTaken(result: VideoResult) {
            super.onVideoTaken(result)
            startPreview(result.file.absolutePath)
        }

        override fun onVideoRecordingStart() {
            super.onVideoRecordingStart()
        }

        override fun onVideoRecordingEnd() {
            super.onVideoRecordingEnd()
        }

        override fun onExposureCorrectionChanged(
            newValue: Float,
            bounds: FloatArray,
            @Nullable fingers: Array<PointF>?
        ) {
            super.onExposureCorrectionChanged(newValue, bounds, fingers)
        }

        override fun onZoomChanged(
            newValue: Float,
            bounds: FloatArray,
            @Nullable fingers: Array<PointF>?
        ) {
            super.onZoomChanged(newValue, bounds, fingers)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VideoPicker.VIDEO_PICKER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val mPaths: List<String>? =
                data!!.getStringArrayListExtra(VideoPicker.EXTRA_VIDEO_PATH)
            if(!mPaths.isNullOrEmpty()){
                startPreview(mPaths[0])
            }
        }
    }

    private fun startPreview(filepath:String){
        val intent = Intent(this@RecordActivity, VideoPreviewActivity::class.java)
        intent.putExtra("filePath",filepath)
        startActivity(intent)
    }
}