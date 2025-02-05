package com.example.quran_athman_reader

import android.content.SharedPreferences
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager

class MainActivity : AppCompatActivity(), SettingsFragment.SettingsChangeListener {

    private lateinit var viewPager: ViewPager
    private lateinit var menuLayout: LinearLayout
    private lateinit var gestureDetector: GestureDetector
    private lateinit var closeButton: ImageView
    private lateinit var openChaptersButton: ImageView
    private lateinit var savePageButton: ImageView
    private lateinit var shareAppButton: ImageView
    private lateinit var openBookmarkButton: ImageView
    private lateinit var openSettingsButton: ImageView
    private lateinit var sharedPreferences: SharedPreferences

    private val savedPages = mutableListOf<Int>() // Previous version restored()

    private val imageList = arrayListOf(
        R.drawable.page1,
        R.drawable.page2,
        R.drawable.page3
    )

    private lateinit var adapter: ImageAdapter
    private var isNightMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Enable fullscreen mode
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )

        // Prevent screen from sleeping
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Initialize UI components
        viewPager = findViewById(R.id.viewPager)
        menuLayout = findViewById(R.id.menuLayout)
        closeButton = findViewById(R.id.closeButton)
        openChaptersButton = findViewById(R.id.openChapters)
        savePageButton = findViewById(R.id.savePage)
        shareAppButton = findViewById(R.id.shareApp)
        openBookmarkButton = findViewById(R.id.openBookmark)
        openSettingsButton = findViewById(R.id.openSettings)

        // Get shared preferences for saving night mode state and bookmarks
        sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE)
        isNightMode = sharedPreferences.getBoolean("NightMode", false)
        loadSavedPages()

        // Initialize ViewPager with ImageAdapter
        adapter = ImageAdapter(this, imageList)
        adapter.isNightMode = isNightMode
        viewPager.adapter = adapter
        viewPager.offscreenPageLimit = imageList.size
        viewPager.layoutDirection = View.LAYOUT_DIRECTION_RTL

        // Close button is hidden by default
        closeButton.visibility = View.GONE

        // Close app when closeButton is clicked
        closeButton.setOnClickListener { finish() }

        // Open settings fragment
        openSettingsButton.setOnClickListener {
            val settingsFragment = SettingsFragment()
            settingsFragment.show(supportFragmentManager, "SettingsFragment")
        }

        // Save current page
        savePageButton.setOnClickListener {
            val currentPage = viewPager.currentItem
            if (!savedPages.contains(currentPage)) {
                savedPages.add(currentPage)
                savePages()
            }
        }

        // Open saved pages dialog
        openBookmarkButton.setOnClickListener {
            showSavedPagesDialog()
        }

        // Gesture detection to toggle menu & close button
        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                val isVisible = closeButton.visibility == View.VISIBLE
                closeButton.visibility = if (isVisible) View.GONE else View.VISIBLE
                menuLayout.visibility = if (isVisible) View.GONE else View.VISIBLE
                return true
            }
        })

        viewPager.setOnTouchListener { _, event -> gestureDetector.onTouchEvent(event) }

        // Assign actions to buttons
        openChaptersButton.setOnClickListener { ButtonActions.openChapters(it) }
        shareAppButton.setOnClickListener { ButtonActions.shareApp(it) }
    }

    private fun showSavedPagesDialog() {
        val dialog = SavedPagesDialog(savedPages, ::removeSavedPage) { page ->
            viewPager.currentItem = page
        }
        dialog.show(supportFragmentManager, "SavedPagesDialog")
    }

    private fun removeSavedPage(page: Int) {
        savedPages.remove(page)
        savePages()
    }

    private fun savePages() {
        val editor = sharedPreferences.edit()
        editor.putString("savedPages", savedPages.joinToString(","))
        editor.apply()
    }

    private fun loadSavedPages() {
        val savedString = sharedPreferences.getString("savedPages", "")
        if (!savedString.isNullOrEmpty()) {
            savedPages.clear()
            savedPages.addAll(savedString.split(",").mapNotNull { it.toIntOrNull() })
            savedPages.addAll(savedString.split(",").map { it.toInt() })
        }
    }

    override fun onNightModeChanged(isEnabled: Boolean) {
        isNightMode = isEnabled
        adapter.isNightMode = isNightMode
        adapter.notifyDataSetChanged()
        sharedPreferences.edit().putBoolean("NightMode", isNightMode).apply()
    }
}
