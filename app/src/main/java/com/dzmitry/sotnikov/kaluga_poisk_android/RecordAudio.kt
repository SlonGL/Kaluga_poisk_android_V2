package ru.kaluga_poisk.portalkalugapoisk

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import androidx.core.content.ContextCompat
import android.util.Log
import cafe.adriel.androidaudiorecorder.AndroidAudioRecorder
import cafe.adriel.androidaudiorecorder.model.AudioChannel
import cafe.adriel.androidaudiorecorder.model.AudioSampleRate
import cafe.adriel.androidaudiorecorder.model.AudioSource
import java.io.File
import java.net.URLEncoder

class RecordAudio : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record_audio)

        val storagePath = Environment.getExternalStorageDirectory()
        val filePath = "$storagePath/recorded_audio.wav"
        // val color = getResources().getColor(R.color.colorPrimaryDark)
        val color = ContextCompat.getColor(baseContext, R.color.colorPrimaryDark)
        val requestCode = 0;
        AndroidAudioRecorder.with(this)
            // Required
            .setFilePath(filePath)
            .setColor(color)
            .setRequestCode(requestCode)

            // Optional
            .setSource(AudioSource.MIC)
            .setChannel(AudioChannel.MONO)
            .setSampleRate(AudioSampleRate.HZ_8000)
            .setAutoStart(false)
            .setKeepDisplayOn(true)
            .setSource(AudioSource.MIC)

            // Start recording
            .record()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0) {
            if (resultCode == Activity.RESULT_OK) {
                // Great! User has recorded and saved the audio file
                val storagePath = Environment.getExternalStorageDirectory()
                // val storagePath = getApplicationContext().getFilesDir().getPath() //which returns the internal app files directory path
                val filePath = "$storagePath/recorded_audio.wav"
                saveAudioToWeb(filePath)
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // Oops! User has canceled the recording
                onBackPressed()
            }
        }
    }

    fun saveAudioToWeb(fileName : String) {
        val byteA = File(fileName).readBytes()
        val base64 = android.util.Base64.encodeToString(byteA, android.util.Base64.DEFAULT)
        // ВНИМАНИЮ ЗАКАЗЧИКА !!! URLEncoder !!! ИНаче не передает в функцию Java
        var url64 = URLEncoder.encode(base64,"UTF-8")

        webView_internal.post(Runnable {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                webView_internal.evaluateJavascript("javascript:window.audioFileReceiver('$url64')", { unit ->
                    Log.d("JAVA AUDIO: ", "RESULT: $unit")
                })
            } else {
                webView_internal.loadUrl("javascript:window.audioFileReceiver('$url64')") // ДЛя API 17 было. Не работало на новых
            }
        })
        onBackPressed()
    }


}
