package com.education.quran8

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

class FormActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form)

        val webView = findViewById<WebView>(R.id.webView)

        // Enable JavaScript and DOM storage if needed
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true

        // Make links and redirects open in the same WebView
        webView.webViewClient = WebViewClient()

        // Load your Google Form URL
        val formUrl = "https://forms.gle/F5xXeSGYRkESDKX37"
        webView.loadUrl(formUrl)
    }
}
