package com.education.quran8

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.viewpager2.widget.ViewPager2
import androidx.core.widget.NestedScrollView
import org.json.JSONObject
import android.os.Handler
import android.os.Looper // ADDED: For Handler

class MainActivity : AppCompatActivity(), SettingsFragment.SettingsChangeListener {

    private lateinit var viewPager: ViewPager2
    private lateinit var menuLayout: LinearLayout
    private lateinit var gestureDetector: GestureDetector
    private lateinit var closeButton: ImageView
    private lateinit var homeButton: ImageView
    private lateinit var openChaptersButton: ImageView
    private lateinit var savePageButton: ImageView
    private lateinit var shareAppButton: ImageView
    private lateinit var openBookmarkButton: ImageView
    private lateinit var openSettingsButton: ImageView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var adapter: ImageAdapter
    private lateinit var pageInfoBanner: LinearLayout
    private lateinit var pageInfoText: TextView
    private lateinit var titleText: TextView
    private lateinit var titleOverlay: FrameLayout
    private lateinit var bookConfig: BookConfig
    private lateinit var selectedBook: String

    private var imageList: MutableList<String> = mutableListOf()
    private var titleMap: MutableMap<Int, String> = mutableMapOf()
    private val savedPages = mutableListOf<Int>()
    private var isNightMode = false

    private val hideBannerRunnable = Runnable {
        pageInfoBanner.visibility = View.GONE
    }
    private val hideTitleRunnable = Runnable {
        titleOverlay?.visibility = View.GONE
        titleText?.visibility = View.GONE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.e("DEBUG_APP_Night", "🌙 Restoring Night Mode = $isNightMode")

        // Apply night mode before rendering UI
        sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE)
        isNightMode = sharedPreferences.getBoolean("NightMode", false)
        AppCompatDelegate.setDefaultNightMode(
            if (isNightMode) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )

        super.onCreate(savedInstanceState)

        try {
            setContentView(R.layout.activity_main)
            val scrollView: NestedScrollView? = findViewById(R.id.scrollView)
            Log.e("DEBUG_APP", "✅ MainActivity - onCreate() exécuté !")
        } catch (e: Exception) {
            Log.e("DEBUG_APP", "❌ ERREUR dans onCreate(): ${e.message}")
        }

        val settingsPrefs = getSharedPreferences("AppSettings", MODE_PRIVATE)
        selectedBook = intent.getStringExtra("selectedBook") ?: settingsPrefs.getString("defaultBook", "quran") ?: "quran"


        Log.d("MainActivity", "Selected book: $selectedBook")

            // Get selected book from intent
       // var selectedBook = intent.getStringExtra("selectedBook") ?: "quran" // Default to "quran"
        bookConfig = BookConfig.loadFromAssets(this, selectedBook) ?: run {
            Toast.makeText(this, "Error loading book configuration", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Load images
        imageList.clear()
        imageList.addAll((1..bookConfig.totalPages).map { "${bookConfig.assetsPath.trimEnd('/')}/page$it.jpg" })
        Log.d("DEBUG_IMAGE_LOADING", "Total Pages: ${bookConfig.totalPages}")
        Log.d("DEBUG_IMAGE_LOADING", "Assets Path: ${bookConfig.assetsPath}")

        // Initialize ViewPager
        viewPager = findViewById(R.id.viewPager)
        adapter = ImageAdapter(this, imageList)
        adapter.isNightMode = isNightMode
        viewPager.adapter = adapter
        viewPager.setCurrentItem(0, false) // Start at first page
        viewPager.offscreenPageLimit = 3
        viewPager.isUserInputEnabled = true

        // Load titles from titles.json
        loadTitles(selectedBook)

        // Fullscreen and immersive mode
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )

        // Load Keep Screen On Setting
        val isKeepScreenOn = sharedPreferences.getBoolean("KeepScreenOn", true)
        if (isKeepScreenOn) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        // Initialize Views
        menuLayout = findViewById(R.id.menuLayout)
        closeButton = findViewById(R.id.closeButton)
        homeButton = findViewById(R.id.homeButton)
        openChaptersButton = findViewById(R.id.openChapters)
        savePageButton = findViewById(R.id.savePage)
        shareAppButton = findViewById(R.id.shareApp)
        openBookmarkButton = findViewById(R.id.openBookmark)
        openSettingsButton = findViewById(R.id.openSettings)
        pageInfoBanner = findViewById(R.id.pageInfoBanner)
        pageInfoText = findViewById(R.id.pageInfoText)
        titleText = findViewById(R.id.titleText)
        titleOverlay = findViewById(R.id.titleOverlay)

        // Adjust banner position
        adjustBannerPosition()
        pageInfoBanner.visibility = View.GONE

        // Hide views initially
        closeButton.visibility = View.GONE
        menuLayout.visibility = View.GONE
        homeButton.visibility = View.GONE

        // Load scroll settings
        configureViewPagerOrientation(sharedPreferences.getBoolean("ScrollOrientation", true))
        loadSavedPages()

        // Set up button actions
        closeButton.setOnClickListener { finish() }
        homeButton.setOnClickListener {
            startActivity(Intent(this, com.education.quran8.HomeActivity::class.java))
        }
        openSettingsButton.setOnClickListener { SettingsFragment().show(supportFragmentManager, "SettingsFragment") }
        savePageButton.setOnClickListener { saveCurrentPage() }
        openBookmarkButton.setOnClickListener { showSavedPagesDialog() }
        openChaptersButton.setOnClickListener {
            val chaptersFragment = ChaptersFragment(selectedBook)
            chaptersFragment.setOnPageSelectedListener { page ->
                navigateToPage(page)
            }
            chaptersFragment.show(supportFragmentManager, "chaptersFragment")
        }
        shareAppButton.setOnClickListener { ButtonActions.shareApp(it) }

        // Gesture handling
        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                Log.d("DEBUG_MENU", "🚀 Tap detected! Toggling title visibility.")
                if (titleOverlay.visibility == View.VISIBLE) {
                    hideTitle()
                } else {
                    showTitleForLimitedTime()
                }
                toggleVisibility()
                return true
            }
        })

        viewPager.setOnTouchListener { _, event ->
            Log.d("DEBUG_MENU", "🔥 Touch detected: action = ${event.action}")
            gestureDetector.onTouchEvent(event)
            true
        }






        // Page change listener
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (titleText.visibility == View.VISIBLE) {
                    updateTitle(position + 1)
                }
                titleMap[position + 1]?.let { title ->
                    if (!(title.contains("حزب") && (title.contains("نصف") || title.endsWith("-   ")))) {
                        hidePageInfoBanner()
                    } else {
                        showPageInfoBanner(title)
                    }
                }
            }
        })

        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            if ((visibility and View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                closeButton.visibility = View.VISIBLE
                homeButton.visibility = View.VISIBLE
            }
        }

        // ADDED START: Call to show welcome message
        showWelcomeMessageIfNeeded()
    // ADDED END

        //END of oncreate
    }

    // Load titles from titles.json
    private fun loadTitles(selectedBook: String) {
        try {
            val inputStream = assets.open(bookConfig.titlesFile)
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val jsonObject = JSONObject(jsonString)
            titleMap.clear()
            for (key in jsonObject.keys()) {
                val pageNumber = key.toInt()
                val title = jsonObject.getString(key)
                titleMap[pageNumber] = title
                Log.d("DEBUG_TITLE_MAP", "✅ Loaded title: Page $pageNumber -> $title")
            }
        } catch (e: Exception) {
            Log.e("DEBUG_TITLE_MAP", "❌ Error loading titles: ${e.message}")
        }
    }
    // ADDED START: Updated function with delay
    private fun showWelcomeMessageIfNeeded() {
        val launchCount = sharedPreferences.getInt("launchCount", 0)
        val dontShowAgain = sharedPreferences.getBoolean("dontShowWelcome", false)

        if (!dontShowAgain && launchCount < 2) {
            Handler(Looper.getMainLooper()).postDelayed({
                val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_welcome, null)
                val learnAboutButton = dialogView.findViewById<Button>(R.id.learnAboutButton)
                val dontShowCheckBox = dialogView.findViewById<CheckBox>(R.id.dontShowCheckBox)
                val okButton = dialogView.findViewById<Button>(R.id.okButton)

                learnAboutButton.setOnClickListener {
                    val intent = Intent(this, AboutActivity::class.java)
                    startActivity(intent)
                }

                val dialog = AlertDialog.Builder(this)
                    .setView(dialogView)
                    .setCancelable(false)
                    .create()

                okButton.setOnClickListener {
                    if (dontShowCheckBox.isChecked) {
                        sharedPreferences.edit().putBoolean("dontShowWelcome", true).apply()
                    }
                    dialog.dismiss()
                }

                dialog.show()

                sharedPreferences.edit().putInt("launchCount", launchCount + 1).apply()
            }, 3000) // 3000 milliseconds = 3 seconds
        }
    }
// ADDED END

    // Update title based on page number
    private fun updateTitle(pageNumber: Int) {
        Log.d("DEBUG_TITLE", "🔍 Checking title for page: $pageNumber")
        if (titleText.visibility == View.VISIBLE) {
            titleMap[pageNumber]?.let { title ->
                Log.d("DEBUG_TITLE", "✅ Found title: $title")
                titleText.text = title
                titleOverlay.visibility = View.VISIBLE
                titleText.visibility = View.VISIBLE
                titleOverlay.removeCallbacks(hideTitleRunnable)
                titleText.removeCallbacks(hideTitleRunnable)
                titleText.setOnClickListener { showTitleForLimitedTime() }
                if (title.contains("حزب") && (title.contains("نصف") || title.endsWith("-   "))) {
                    showPageInfoBanner(title)
                }
            } ?: Log.e("DEBUG_TITLE", "❌ No title found for page: $pageNumber")
        } else {
            Log.d("DEBUG_TITLE", "⚠️ Title text is not visible for page: $pageNumber")
        }
    }

    // Adjust banner position
    private fun adjustBannerPosition() {
        val screenHeight = resources.displayMetrics.heightPixels
        val newMarginTop = (screenHeight * 0.6).toInt() // 60% from top
        val layoutParams = pageInfoBanner.layoutParams as FrameLayout.LayoutParams
        layoutParams.topMargin = newMarginTop
        pageInfoBanner.layoutParams = layoutParams
    }

    // Show page info banner
    private fun showPageInfoBanner(title: String) {
        Log.e("DEBUG_BANNER", "Showing Hizb Banner: $title")
        pageInfoText.text = title
        pageInfoBanner.visibility = View.VISIBLE
        pageInfoBanner.removeCallbacks(hideBannerRunnable)
        pageInfoBanner.postDelayed(hideBannerRunnable, 1000)
    }

    // Hide page info banner
    private fun hidePageInfoBanner() {
        if (!::pageInfoBanner.isInitialized) return
        Log.e("DEBUG_BANNER", "Hiding Hizb Banner")
        pageInfoBanner.visibility = View.GONE
        pageInfoBanner.removeCallbacks(hideBannerRunnable)
    }

    override fun onResume() {
        super.onResume()
        adapter.notifyDataSetChanged() // Force refresh when reopening the app
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val newSelectedBook = intent?.getStringExtra("selectedBook")
        if (newSelectedBook != null && newSelectedBook != selectedBook) {
            selectedBook = newSelectedBook
            // Reload the book configuration and images
            bookConfig = BookConfig.loadFromAssets(this, selectedBook) ?: run {
                Toast.makeText(this, "Error loading book configuration", Toast.LENGTH_SHORT).show()
                finish()
                return
            }
            imageList.clear()
            imageList.addAll((1..bookConfig.totalPages).map { "${bookConfig.assetsPath.trimEnd('/')}/page$it.jpg" })
            adapter = ImageAdapter(this, imageList)
            adapter.isNightMode = isNightMode
            viewPager.adapter = adapter
            // Optionally, reset other states such as titles or saved pages
            loadTitles(selectedBook)
            loadSavedPages()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val currentPage = viewPager.currentItem
        viewPager.adapter = null
        viewPager.adapter = adapter
        viewPager.setCurrentItem(currentPage, false)
    }

    private fun getCurrentPage(): Int {
        return viewPager.currentItem
    }

    private fun navigateToPage(page: String) {
        val pageNumber = page.toIntOrNull() ?: return
        if (!::bookConfig.isInitialized) {
            Toast.makeText(this, "Book configuration not loaded!", Toast.LENGTH_SHORT).show()
            return
        }
        if (pageNumber in 1..bookConfig.totalPages) {
            viewPager.setCurrentItem(pageNumber - 1, true)
        } else {
            Toast.makeText(this, "Invalid page number: $pageNumber", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveCurrentPage() {
        val currentPage = getCurrentPage()
        if (!savedPages.contains(currentPage)) {
            savedPages.add(currentPage)
            savePages()
            Toast.makeText(this, "Page ${currentPage + 1} enregistrée", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Page déjà enregistrée", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showSavedPagesDialog() {
        if (savedPages.isEmpty()) {
            Toast.makeText(this, "Aucune page enregistrée", Toast.LENGTH_SHORT).show()
            return
        }
        SavedPagesDialog(savedPages.toMutableList(), ::removeSavedPage) { page ->
            viewPager.currentItem = page
        }.show(supportFragmentManager, "SavedPagesDialog")
    }

    private fun removeSavedPage(page: Int) {
        savedPages.remove(page)
        savePages()
    }


    private fun savePages() {
        // Use a unique key for each book – for example, "savedPages_quran", "savedPages_Douaa", etc.
        sharedPreferences.edit().putString("savedPages_$selectedBook", savedPages.joinToString(",")).apply()
    }

    private fun loadSavedPages() {
        sharedPreferences.getString("savedPages_$selectedBook", "")?.let {
            savedPages.clear()
            savedPages.addAll(it.split(",").mapNotNull { it.toIntOrNull() })
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        Log.e("DEBUG_MENU", "📱 Touch event detected: action=${event.action}")
        gestureDetector.onTouchEvent(event)
        return super.dispatchTouchEvent(event)
    }

    private fun toggleVisibility() {
        Log.e("DEBUG_MENU", "🔥 toggleVisibility() called!")
        val isCurrentlyVisible = menuLayout.visibility == View.VISIBLE
        Log.e("DEBUG_MENU", "Before toggle: menuLayout=${menuLayout.visibility}")
        closeButton.visibility = if (isCurrentlyVisible) View.GONE else View.VISIBLE
        homeButton.visibility = if (isCurrentlyVisible) View.GONE else View.VISIBLE
        menuLayout.visibility = if (isCurrentlyVisible) View.GONE else View.VISIBLE
        titleOverlay?.visibility = if (isCurrentlyVisible) View.GONE else View.VISIBLE
        titleText?.visibility = if (isCurrentlyVisible) View.GONE else View.VISIBLE
        Log.e("DEBUG_Title_MENU", "After toggle: menuLayout=${menuLayout.visibility}")
    }

    private fun configureViewPagerOrientation(isHorizontal: Boolean) {
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            viewPager.orientation = ViewPager2.ORIENTATION_VERTICAL
        } else {
            viewPager.orientation = if (isHorizontal) ViewPager2.ORIENTATION_HORIZONTAL else ViewPager2.ORIENTATION_VERTICAL
        }
        val isRightToLeft = sharedPreferences.getBoolean("ScrollDirection", true)
        viewPager.layoutDirection = if (isRightToLeft) View.LAYOUT_DIRECTION_RTL else View.LAYOUT_DIRECTION_LTR
    }

    override fun onNightModeChanged(isEnabled: Boolean) {
        isNightMode = isEnabled
        adapter.isNightMode = isNightMode
        adapter.notifyDataSetChanged()
        sharedPreferences.edit().putBoolean("NightMode", isNightMode).apply()
    }

    override fun onScrollDirectionChanged(isRightToLeft: Boolean) {
        configureViewPagerOrientation(sharedPreferences.getBoolean("ScrollOrientation", true))
    }

    override fun onScrollOrientationChanged(isHorizontal: Boolean) {
        configureViewPagerOrientation(isHorizontal)
    }

    override fun onFullscreenModeChanged(isEnabled: Boolean) {
        if (isEnabled) {
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    )
        } else {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        }
    }

    override fun onKeepScreenOnChanged(isEnabled: Boolean) {
        if (isEnabled) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    private fun hideTitle() {
        titleOverlay.visibility = View.GONE
        titleText.visibility = View.GONE
        titleOverlay.removeCallbacks(hideTitleRunnable)
        titleText.removeCallbacks(hideTitleRunnable)
    }

    private fun showTitleForLimitedTime() {
        titleOverlay.visibility = View.VISIBLE
        titleText.visibility = View.VISIBLE
        titleOverlay.removeCallbacks(hideTitleRunnable)
        titleText.removeCallbacks(hideTitleRunnable)
        titleOverlay.postDelayed(hideTitleRunnable, 2000)
        titleText.postDelayed(hideTitleRunnable, 2000)
    }
}
