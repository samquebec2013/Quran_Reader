package com.education.quran8

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.io.FileOutputStream
import java.util.*
import java.util.zip.ZipInputStream
import android.app.ProgressDialog
import android.view.ViewGroup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException

class HomeActivity : AppCompatActivity(),
    com.education.quran8.SettingsFragment.SettingsChangeListener {

    private var books = mutableListOf<String>()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: com.education.quran8.BookCarouselAdapter
    private lateinit var closeButton: ImageView
    private lateinit var menuLayout: LinearLayout
    private lateinit var manageBooksButton: ImageView
    private lateinit var aboutLink: TextView
    private lateinit var progressDialog: ProgressDialog
    private lateinit var sharedPreferences: SharedPreferences

    private val pickZipFile = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri -> handleZipFile(uri) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("HomeActivity", "onCreate started")
        sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE)
        val language = sharedPreferences.getString("Language", "en") ?: "en"
        setLocale(language)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        Log.d("HomeActivity", "setContentView completed")

        loadBooksFromAssets()
        loadAdditionalBooks()

        recyclerView = findViewById(R.id.bookCarousel)
        adapter = com.education.quran8.BookCarouselAdapter(
            this,
            books
        ) { bookName ->
            val intent = Intent(
                this,
                com.education.quran8.MainActivity::class.java
            )
            intent.putExtra("selectedBook", bookName)
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            Log.d("HomeActivity", "Opening book: $bookName")
        }
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = adapter
        recyclerView.layoutDirection = View.LAYOUT_DIRECTION_LTR
        Log.d("HomeActivity", "RecyclerView initialized")

        sortBooksAlphabetically()
        adapter.notifyDataSetChanged()
        Log.d("HomeActivity", "Books sorted and adapter notified")

        closeButton = findViewById(R.id.closeButton)
        menuLayout = findViewById(R.id.menuLayout)
        manageBooksButton = findViewById(R.id.manageBooksButton)
        aboutLink = findViewById(R.id.aboutLink)

        closeButton.visibility = View.VISIBLE
        menuLayout.visibility = View.VISIBLE
        Log.d("HomeActivity", "Initial visibility set to VISIBLE for closeButton and menuLayout")

        closeButton.setOnClickListener { finish() }
        manageBooksButton.setOnClickListener { showManageBooksDialog() }
        aboutLink.setOnClickListener {
            val intent = Intent(this, com.education.quran8.AboutActivity::class.java)
            startActivity(intent)
        }
        Log.d("HomeActivity", "Click listeners set")

        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )
        Log.d("HomeActivity", "Immersive mode set")

        progressDialog = ProgressDialog(this).apply {
            setMessage("Installing book...")
            setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
            setCancelable(false)
            max = 100
        }



        Log.d("HomeActivity", "ProgressDialog initialized")
        Log.d("HomeActivity", "onCreate completed")
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d("HomeActivity", "onNewIntent called")
    }

    override fun onResume() {
        super.onResume()
        closeButton.visibility = View.VISIBLE
        menuLayout.visibility = View.VISIBLE
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )
        Log.d("HomeActivity", "onResume: visibility ensured for closeButton and menuLayout")
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        closeButton.visibility = View.VISIBLE
        menuLayout.visibility = View.VISIBLE
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.layoutDirection = View.LAYOUT_DIRECTION_LTR
        Log.d("HomeActivity", "onConfigurationChanged: visibility ensured for closeButton and menuLayout")
    }

    private fun showManageBooksDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_manage_books, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        val bookSpinner = dialogView.findViewById<Spinner>(R.id.bookSpinner)
        val addBookButton = dialogView.findViewById<Button>(R.id.addBookButton)
        val deleteBookButton = dialogView.findViewById<Button>(R.id.deleteBookButton)
        val selectDefaultBookButton = dialogView.findViewById<Button>(R.id.selectDefaultBookButton)
        val closeButton = dialogView.findViewById<Button>(R.id.closeButton)

        val bookTitles = books.map { bookName ->
            val config =
                com.education.quran8.BookConfig.Companion.loadFromAssets(
                    this,
                    bookName
                )
            config?.title ?: bookName
        }
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, bookTitles)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        bookSpinner.adapter = spinnerAdapter
        Log.d("HomeActivity", "ManageBooks dialog spinner initialized with ${bookTitles.size} books")

        addBookButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "application/zip"
            pickZipFile.launch(intent)
            Log.d("HomeActivity", "Add book button clicked, launching file picker")
        }

        deleteBookButton.setOnClickListener {
            val selectedIndex = bookSpinner.selectedItemPosition
            val selectedBook = books[selectedIndex]
            AlertDialog.Builder(this)
                .setTitle("Confirm Deletion")
                .setMessage("Are you sure you want to delete '${bookTitles[selectedIndex]}'?")
                .setPositiveButton("Yes") { _, _ ->
                    books.removeAt(selectedIndex)
                    sortBooksAlphabetically()
                    adapter.notifyDataSetChanged()
                    deleteBookFiles(selectedBook)
                    saveBooks()
                    Toast.makeText(this, "Book '${bookTitles[selectedIndex]}' deleted", Toast.LENGTH_SHORT).show()
                    Log.d("HomeActivity", "Book '$selectedBook' deleted and list saved")
                    dialog.dismiss()
                }
                .setNegativeButton("No", null)
                .show()
        }

        selectDefaultBookButton.setOnClickListener {
            val selectedIndex = bookSpinner.selectedItemPosition
            val selectedBook = books[selectedIndex]
            sharedPreferences.edit().putString("defaultBook", selectedBook).apply()
            Toast.makeText(this, "'${bookTitles[selectedIndex]}' set as default book", Toast.LENGTH_SHORT).show()
            Log.d("HomeActivity", "'$selectedBook' set as default book")
        }

        closeButton.setOnClickListener { dialog.dismiss() }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
        Log.d("HomeActivity", "ManageBooks dialog shown")
    }

    private fun deleteBookFiles(bookName: String) {
        val bookDir = File(getDir("books", MODE_PRIVATE), bookName)
        if (bookDir.exists()) {
            bookDir.deleteRecursively()
            Log.d("HomeActivity", "Deleted book directory: ${bookDir.absolutePath}")
        } else {
            Log.e("HomeActivity", "Book directory not found for deletion: ${bookDir.absolutePath}")
        }
    }

    private fun handleZipFile(uri: Uri) {
        progressDialog.setProgress(0)
        progressDialog.show()
        Log.d("HomeActivity", "Handling ZIP file from URI: $uri")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                contentResolver.openInputStream(uri)?.use { inputStream ->
                    val zipInputStream = ZipInputStream(inputStream)
                    var entry = zipInputStream.nextEntry

                    var configJson: String? = null
                    var configPath: String? = null
                    val entries = mutableListOf<Pair<String, ByteArray>>()

                    // Collect all entries and locate config.json
                    while (entry != null) {
                        val fileName = entry.name
                        if (!entry.isDirectory) {
                            val byteArrayOutputStream = java.io.ByteArrayOutputStream()
                            val buffer = ByteArray(1024)
                            var len: Int
                            while (zipInputStream.read(buffer).also { len = it } > 0) {
                                byteArrayOutputStream.write(buffer, 0, len)
                            }
                            val content = byteArrayOutputStream.toByteArray()
                            entries.add(fileName to content)
                            if (fileName.endsWith("config.json")) {
                                configJson = String(content)
                                configPath = fileName
                            }
                        }
                        zipInputStream.closeEntry()
                        entry = zipInputStream.nextEntry
                    }
                    zipInputStream.close()

                    if (configJson == null) throw IllegalArgumentException("Missing config.json")
                    if (entries.none { it.first.contains("pages/") }) {
                        throw IllegalArgumentException("Missing pages/ directory")
                    }

                    // Extract title
                    val jsonObject = JSONObject(configJson)
                    val title = jsonObject.getString("title")
                    Log.d("HomeActivity", "Extracted title from config.json: $title")

                    val bookDir = File(getDir("books", MODE_PRIVATE), title)
                    bookDir.mkdirs()
                    Log.d("HomeActivity", "Unzipping ZIP file to: ${bookDir.absolutePath} with book name: $title")

                    // Extract and flatten structure
                    for ((fileName, content) in entries) {
                        val targetFileName = when {
                            fileName.endsWith("config.json") -> "config.json"
                            fileName.contains("pages/") -> {
                                val pagePath = fileName.substringAfter("pages/")
                                if (pagePath.isEmpty()) continue
                                "pages/$pagePath"
                            }
                            else -> continue // Skip unrelated files
                        }
                        val targetFile = File(bookDir, targetFileName)
                        targetFile.parentFile?.mkdirs()
                        FileOutputStream(targetFile).use { output ->
                            output.write(content)
                        }
                        Log.d("HomeActivity", "Wrote file: ${targetFile.absolutePath}")
                    }

                    // Log extracted structure for debugging
                    val extractedFiles = bookDir.walk().map { it.relativeTo(bookDir).path }.joinToString(", ")
                    Log.d("HomeActivity", "Extracted structure in $title: $extractedFiles")

                    // Validate extracted structure
                    val configFile = File(bookDir, "config.json")
                    val pagesDirFile = File(bookDir, "pages")
                    if (!configFile.exists() || !pagesDirFile.exists() || !pagesDirFile.isDirectory || pagesDirFile.listFiles()?.isEmpty() != false) {
                        bookDir.deleteRecursively()
                        throw IllegalStateException("Invalid book structure after extraction: config.json=${configFile.exists()}, pages=${pagesDirFile.exists() && pagesDirFile.isDirectory}")
                    }

                    withContext(Dispatchers.Main) {
                        if (books.contains(title)) {
                            Toast.makeText(this@HomeActivity, "Book '$title' already exists", Toast.LENGTH_SHORT).show()
                            Log.w("HomeActivity", "Book '$title' already in list")
                        } else {
                            books.add(title)
                            sortBooksAlphabetically()
                            adapter.notifyDataSetChanged()
                            saveBooks()
                            Toast.makeText(this@HomeActivity, "Book '$title' added", Toast.LENGTH_SHORT).show()
                            Log.d("HomeActivity", "Book '$title' added and list saved")
                        }
                        progressDialog.dismiss()
                    }
                } ?: throw Exception("Failed to open ZIP file")
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@HomeActivity, "Error installing book: ${e.message}", Toast.LENGTH_LONG).show()
                    Log.e("HomeActivity", "Error unzipping: ${e.stackTraceToString()}")
                    progressDialog.dismiss()
                }
            }
        }
    }

    private fun loadBooksFromAssets() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val assetManager = assets
                val bookItems = assetManager.list("books") ?: emptyArray()
                Log.d("HomeActivity", "Found items in assets/books/: ${bookItems.joinToString(", ")}")

                for (item in bookItems) {
                    val bookName = item.removeSuffix(".zip")
                    val isZip = item.endsWith(".zip")
                    val bookDir = File(getDir("books", MODE_PRIVATE), bookName)

                    if (isZip) {
                        if (!bookDir.exists()) {
                            Log.d("HomeActivity", "Unzipping pre-installed book: $item")
                            assetManager.open("books/$item").use { inputStream ->
                                val zipInputStream = ZipInputStream(inputStream)
                                var entry = zipInputStream.nextEntry

                                var configJson: String? = null
                                var hasPages = false
                                var rootFolder: String? = null
                                val entries = mutableListOf<Pair<String, ByteArray>>()

                                while (entry != null) {
                                    val fileName = entry.name
                                    if (entry.isDirectory) {
                                        if (fileName.startsWith("pages/")) hasPages = true
                                    } else {
                                        val byteArrayOutputStream = java.io.ByteArrayOutputStream()
                                        val buffer = ByteArray(1024)
                                        var len: Int
                                        while (zipInputStream.read(buffer).also { len = it } > 0) {
                                            byteArrayOutputStream.write(buffer, 0, len)
                                        }
                                        val content = byteArrayOutputStream.toByteArray()
                                        entries.add(fileName to content)
                                        if (fileName.endsWith("config.json")) {
                                            configJson = String(content)
                                        }
                                        if (fileName.contains("pages/")) hasPages = true
                                    }
                                    zipInputStream.closeEntry()
                                    entry = zipInputStream.nextEntry
                                }
                                zipInputStream.close()

                                if (configJson == null) {
                                    Log.e("HomeActivity", "Skipping $bookName: Missing config.json in ZIP")
                                    //continue
                                }
                                if (!hasPages) {
                                    Log.e("HomeActivity", "Skipping $bookName: Missing pages/ directory in ZIP")
                                    //continue
                                }

                                if (entries.isNotEmpty()) {
                                    val firstEntryName = entries[0].first
                                    if (firstEntryName.contains("/")) {
                                        val potentialRoot = firstEntryName.substringBefore("/")
                                        if (entries.all { it.first.startsWith("$potentialRoot/") || it.first == potentialRoot }) {
                                            rootFolder = potentialRoot
                                            Log.d("HomeActivity", "Detected root folder in ZIP $item: $rootFolder")
                                        }
                                    }
                                }

                                for ((fileName, content) in entries) {
                                    var targetFileName = fileName
                                    if (rootFolder != null && targetFileName.startsWith("$rootFolder/")) {
                                        targetFileName = targetFileName.removePrefix("$rootFolder/")
                                    }
                                    if (targetFileName.isEmpty()) continue

                                    val targetFile = File(bookDir, targetFileName)
                                    targetFile.parentFile?.mkdirs()
                                    FileOutputStream(targetFile).use { output ->
                                        output.write(content)
                                    }
                                    Log.d("HomeActivity", "Wrote pre-installed file: ${targetFile.absolutePath}")
                                }
                            }
                        }
                    } else {
                        val hasConfig = try {
                            assetManager.open("books/$bookName/config.json").close()
                            true
                        } catch (e: IOException) {
                            false
                        }
                        val hasPages = assetManager.list("books/$bookName/pages")?.isNotEmpty() == true

                        if (!hasConfig) {
                            Log.e("HomeActivity", "Skipping $bookName: Missing config.json in directory")
                            continue
                        }
                        if (!hasPages) {
                            Log.e("HomeActivity", "Skipping $bookName: Missing pages/ directory")
                            continue
                        }
                        Log.d("HomeActivity", "Found valid unzipped book directory: $bookName")
                    }

                    if (!books.contains(bookName)) {
                        books.add(bookName)
                        Log.d("HomeActivity", "Added book to list: $bookName (from ${if (isZip) "ZIP" else "directory"})")
                    }
                }

                withContext(Dispatchers.Main) {
                    if (books.isEmpty()) {
                        books.addAll(listOf("quran", "Douaa", "souar_ayat_fadila"))
                        Log.d("HomeActivity", "No valid books found in assets, using defaults: ${books.joinToString(", ")}")
                    }
                    sortBooksAlphabetically()
                    adapter.notifyDataSetChanged()
                    Log.d("HomeActivity", "Books loaded and sorted: ${books.joinToString(", ")}")
                }
            } catch (e: Exception) {
                Log.e("HomeActivity", "Error loading books from assets: ${e.stackTraceToString()}")
                withContext(Dispatchers.Main) {
                    if (books.isEmpty()) {
                        books.addAll(listOf("quran", "Douaa", "souar_ayat_fadila"))
                        Log.d("HomeActivity", "Error occurred, using defaults: ${books.joinToString(", ")}")
                    }
                    sortBooksAlphabetically()
                    adapter.notifyDataSetChanged()
                }
            }
        }
    }

    private fun loadAdditionalBooks() {
        val savedBooks = sharedPreferences.getString("bookList", null)
        if (!savedBooks.isNullOrEmpty()) {
            val additionalBooks = savedBooks.split(",").map { it.trim() }.filter { it !in books }
            books.addAll(additionalBooks)
            Log.d("HomeActivity", "Loaded additional books from SharedPreferences: $additionalBooks")
        }
        saveBooks()
    }

    private fun sortBooksAlphabetically() {
        books.sortBy { bookName ->
            val config =
                com.education.quran8.BookConfig.Companion.loadFromAssets(
                    this,
                    bookName
                )
            config?.title?.lowercase() ?: bookName.lowercase()
        }
        Log.d("HomeActivity", "Books sorted alphabetically: ${books.joinToString(", ")}")
    }

    private fun saveBooks() {
        val editor = sharedPreferences.edit()
        editor.putString("bookList", books.joinToString(","))
        editor.apply()
        Log.d("HomeActivity", "Saved book list: ${books.joinToString(",")}")
    }

    private fun setLocale(language: String) {
        val validLanguage = if (language in listOf("en", "ar")) language else "en"
        val locale = Locale(validLanguage)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
        Log.d("HomeActivity", "Locale set to: $validLanguage")
    }

    override fun onNightModeChanged(isEnabled: Boolean) {
        recreate()
        Log.d("HomeActivity", "Night mode changed, recreating activity")
    }

    override fun onScrollOrientationChanged(isHorizontal: Boolean) {}
    override fun onScrollDirectionChanged(isRightToLeft: Boolean) {}
    override fun onFullscreenModeChanged(isEnabled: Boolean) {}
    override fun onKeepScreenOnChanged(isEnabled: Boolean) {}
}

class BookCarouselAdapter(
    private val context: Context,
    private val books: MutableList<String>,
    private val onBookSelected: (String) -> Unit
) : RecyclerView.Adapter<com.education.quran8.BookCarouselAdapter.BookViewHolder>() {

    inner class BookViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val bookImage: ImageView = view.findViewById(R.id.bookImage)
        val bookTitle: TextView = view.findViewById(R.id.bookTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): com.education.quran8.BookCarouselAdapter.BookViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_book_carousel, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: com.education.quran8.BookCarouselAdapter.BookViewHolder, position: Int) {
        val bookName = books[position]
        try {
            val inputStream = try {
                val assetPathPage0 = "books/$bookName/pages/page0.jpg"
                Log.d("Carousel", "Trying assets for page0: $assetPathPage0")
                context.assets.open(assetPathPage0)
            } catch (e: Exception) {
                val internalPathPage0 = File(context.getDir("books", Context.MODE_PRIVATE), "$bookName/pages/page0.jpg")
                if (internalPathPage0.exists()) {
                    Log.d("Carousel", "Found page0 in internal storage: ${internalPathPage0.absolutePath}")
                    internalPathPage0.inputStream()
                } else {
                    val assetPathPage1 = "books/$bookName/pages/page1.jpg"
                    Log.d("Carousel", "Falling back to assets for page1: $assetPathPage1")
                    try {
                        context.assets.open(assetPathPage1)
                    } catch (e: Exception) {
                        val internalPathPage1 = File(context.getDir("books", Context.MODE_PRIVATE), "$bookName/pages/page1.jpg")
                        Log.d("Carousel", "Trying internal storage for page1: ${internalPathPage1.absolutePath}")
                        if (internalPathPage1.exists()) {
                            internalPathPage1.inputStream()
                        } else {
                            throw IOException("Neither page0.jpg nor page1.jpg found for $bookName")
                        }
                    }
                }
            }
            val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
            holder.bookImage.setImageBitmap(bitmap)
            inputStream.close()
            Log.d("Carousel", "Loaded image for $bookName successfully")
        } catch (e: Exception) {
            Log.e("Carousel", "Failed to load image for $bookName: ${e.message}")
            e.printStackTrace()
            holder.bookImage.setImageResource(R.drawable.placeholder)
        }

        val config =
            com.education.quran8.BookConfig.Companion.loadFromAssets(
                context,
                bookName
            )
        val title = config?.title ?: bookName
        holder.bookTitle.text = title
        Log.d("Carousel", "Set title for $bookName: $title")

        holder.bookImage.setOnClickListener { onBookSelected(bookName) }
    }

    override fun getItemCount(): Int = books.size
}