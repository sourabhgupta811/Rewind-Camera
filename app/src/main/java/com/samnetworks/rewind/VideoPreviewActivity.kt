package com.samnetworks.rewind

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL
import com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS
import com.arthenica.mobileffmpeg.FFmpeg
import kotlinx.android.synthetic.main.activity_video_preview.*
import kotlinx.coroutines.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import kotlin.coroutines.CoroutineContext


/**
 * Created by Sourabh Gupta on 6/6/20.
 */
class VideoPreviewActivity : AppCompatActivity(), CoroutineScope {
    lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_preview)
        job = Job()
        val outputPath = File(externalCacheDir, "reverse.mp4")
        val filePath = intent.getStringExtra("filePath")
        progress_bar.visibility = View.VISIBLE
        launch {
            var outputcode: Boolean
            withContext(Dispatchers.Default) {
                val intermediatePath = File(externalCacheDir,"intermediate.mp4")
                intermediatePath.delete()
                copy(File(filePath!!),intermediatePath)
                outputPath.delete()
                outputcode = reverseVideo(intermediatePath.absolutePath, outputPath.absolutePath)
            }
            if (outputcode) {
                progress_bar.visibility = View.GONE
                video_view.setVideoURI(Uri.parse(outputPath.absolutePath))
                video_view.setOnPreparedListener {
                    it.isLooping = true
                }
                video_view.start()
            }
        }
    }

    private suspend fun reverseVideo(filePath: String, outputPath: String): Boolean {
        when (FFmpeg.execute("-i $filePath -vf reverse ${outputPath}")) {
            RETURN_CODE_SUCCESS -> {
                return true
            }
            RETURN_CODE_CANCEL -> {
                return false
            }
            else -> {
                return false
            }
        }
    }

    override fun onPause() {
        super.onPause()
        video_view.pause()
    }

    override fun onResume() {
        super.onResume()
        video_view.resume()
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    @Throws(IOException::class)
    private suspend fun copy(src: File, dst: File) {
        FileInputStream(src).use { `in` ->
            FileOutputStream(dst).use { out ->
                val buf = ByteArray(1024)
                var len: Int = 0
                while (`in`.read(buf).also { len = it } > 0) {
                    out.write(buf, 0, len)
                }
            }
        }
    }
}