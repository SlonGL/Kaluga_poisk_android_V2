package ru.kaluga_poisk.portalkalugapoisk

import androidx.core.content.ContextCompat.startActivity
import android.content.Intent
import android.webkit.JavascriptInterface
import android.app.Activity
import android.net.Uri
import android.os.Environment
import androidx.core.content.ContextCompat
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import cafe.adriel.androidaudiorecorder.AndroidAudioRecorder
import cafe.adriel.androidaudiorecorder.model.AudioChannel
import cafe.adriel.androidaudiorecorder.model.AudioSampleRate
import cafe.adriel.androidaudiorecorder.model.AudioSource
import android.R.id.message
import android.webkit.ConsoleMessage




class JavaScriptInterface(private val activity: Activity) {

    // Получение фото по кнопке в HTML и отправка его обратно
    @JavascriptInterface fun getPhoto() {
        photoSavingPlace = PHOTO_SAVING_PLACE_WEB
        val intent = Intent(activity.baseContext, CameraView::class.java)
        activity.startActivity(intent)
    }

    // Запись аудио по кнопке в HTML и отправка обратно
    @JavascriptInterface fun getAudio() {

        // callJavaScript(webView_internal, "window.audioFileReceiver", "TEST")
        // return

        val intent = Intent(activity.baseContext, RecordAudio::class.java)
        activity.startActivity(intent)
    }

    @JavascriptInterface fun onConsoleMessage(cm: ConsoleMessage): Boolean {
        Log.d(
            "MYWebClient", String.format(
                "%s @ %d: %s", cm.message(),
                cm.lineNumber(), cm.sourceId()
            )
        )
        return true
    }

    private fun callJavaScript(view: WebView, methodName: String, vararg params: Any) {
        val stringBuilder = StringBuilder()
        stringBuilder.append("javascript:try{")
        stringBuilder.append(methodName)
        stringBuilder.append("(")
        var separator = ""
        for (param in params) {
            stringBuilder.append(separator)
            separator = ","
            if (param is String) {
                stringBuilder.append("'")
            }
            stringBuilder.append(param.toString().replace("'", "\\'"))
            if (param is String) {
                stringBuilder.append("'")
            }

        }
        stringBuilder.append(")}catch(error){console.error(error.message);}")
        val call = stringBuilder.toString()
        Log.i("MYWebClient", "callJavaScript: call=$call")


        view.post(Runnable {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                view.evaluateJavascript(call, { unit ->
                    Log.d("CALL: ", "RESULT: $unit")
                })
            } else {
                view.loadUrl(call)
            }
        })

    }

}