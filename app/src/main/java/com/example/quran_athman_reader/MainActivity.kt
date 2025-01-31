package com.example.quran_athman_reader

import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager

class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager
    private lateinit var menuLayout: LinearLayout
    private lateinit var gestureDetector: GestureDetector
    private lateinit var openChaptersButton: ImageView
    private lateinit var savePageButton: ImageView
    private lateinit var shareAppButton: ImageView
    private lateinit var openBookmarkButton: ImageView
    private lateinit var openSettingsButton: ImageView

    private val imageList = arrayListOf(
        R.drawable.page1,
        R.drawable.page2,
        R.drawable.page3
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewPager = findViewById(R.id.viewPager)
        menuLayout = findViewById(R.id.menuLayout)
        openChaptersButton = findViewById(R.id.openChapters)
        savePageButton = findViewById(R.id.savePage)
        shareAppButton = findViewById(R.id.shareApp)
        openBookmarkButton = findViewById(R.id.openBookmark)
        openSettingsButton = findViewById(R.id.openSettings)

        val adapter = ImageAdapter(this, imageList)
        viewPager.adapter = adapter
        viewPager.offscreenPageLimit = imageList.size

        gestureDetector = GestureDetector(this, GestureListener())
        viewPager.setOnTouchListener { _, event -> gestureDetector.onTouchEvent(event) }

        // Assign button actions
        openChaptersButton.setOnClickListener { ButtonActions.openChapters(it) }
        savePageButton.setOnClickListener { ButtonActions.savePage(it) }
        shareAppButton.setOnClickListener { ButtonActions.shareApp(it) }
        openBookmarkButton.setOnClickListener { ButtonActions.openBookmark(it) }
        openSettingsButton.setOnClickListener { ButtonActions.openSettings(it) }
    }

    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            menuLayout.visibility = if (menuLayout.visibility == View.GONE) View.VISIBLE else View.GONE
            return true
        }
    }
}