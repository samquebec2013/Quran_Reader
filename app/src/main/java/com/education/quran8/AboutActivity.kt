package com.education.quran8

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        val howToUseButton = findViewById<Button>(R.id.howToUseButton)
        val createBookButton = findViewById<Button>(R.id.createBookButton)
        val contactUsButton = findViewById<Button>(R.id.contactUsButton)

        howToUseButton.setOnClickListener {
            val intent = Intent(this, WebViewActivity::class.java)
            intent.putExtra("htmlFile", "how_to_use.html")
            startActivity(intent)
        }

        createBookButton.setOnClickListener {
            val intent = Intent(this, WebViewActivity::class.java)
            intent.putExtra("htmlFile", "how_to_create.html")
            startActivity(intent)
        }

        contactUsButton.setOnClickListener {
            val intent = Intent(this, WebViewActivity::class.java)
            // Pass the contact_us.html file to be loaded in WebViewActivity
            intent.putExtra("htmlFile", "contact_us.html")
            startActivity(intent)
        }
    }
}
