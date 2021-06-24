package ru.kaluga_poisk.portalkalugapoisk

import android.webkit.WebView
import android.webkit.WebViewClient
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.webkit.WebResourceRequest


internal class MyWebViewClient : WebViewClient() {
    override fun shouldOverrideUrlLoading(webView: WebView, url: String): Boolean {
        if  ( url.startsWith("http:") || url.startsWith("https:") ) {
            return false
        }
        // Otherwise allow the OS to handle things like tel, mailto, etc.
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        webViewContext.startActivity(intent)
        return true
    }

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest) : Boolean {
        if (Build.VERSION.SDK_INT >= 24) {
            val url = request.getUrl().toString()
            if  ( url.startsWith("http:") || url.startsWith("https:") ) {
                return false
            }
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            webViewContext.startActivity(intent)
            return true
        }
        return false
    }
}