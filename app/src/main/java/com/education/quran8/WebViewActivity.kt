package com.education.quran8

import android.os.Bundle
import android.webkit.WebView
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class WebViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)

        val webView = findViewById<WebView>(R.id.webView)
        val closeButton = findViewById<Button>(R.id.closeButton)

        val htmlFile = intent.getStringExtra("htmlFile") ?: "how_to_create.html"
        webView.settings.javaScriptEnabled = true
        webView.loadUrl("file:///android_asset/$htmlFile")


        closeButton.setOnClickListener { finish() }
    }
}