package com.education.quran8

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONObject

class ChaptersFragment(private val selectedBook: String) : DialogFragment() {

    private var onPageSelected: ((String) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chapters, container, false)

        val recyclerView: RecyclerView = view.findViewById(R.id.chapterRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Load chapters dynamically
        val chapters = loadChapters(selectedBook)
        recyclerView.adapter = CategoryAdapter(chapters) { subcategory ->
            onPageSelected?.invoke(subcategory.page)
            dismiss()
        }

        val closeButton: Button = view.findViewById(R.id.closeButton)
        closeButton.setOnClickListener { dismiss() }

        return view
    }

    private fun loadChapters(bookName: String): List<Category> {
        val categories = mutableListOf<Category>()
        try {
            val inputStream = requireContext().assets.open("books/$bookName/chapters.json")
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val jsonObject = JSONObject(jsonString)
            val jsonArray = jsonObject.getJSONArray("categories")

            for (i in 0 until jsonArray.length()) {
                val categoryJson = jsonArray.getJSONObject(i)
                val title = categoryJson.getString("title")
                val subcategoriesJson = categoryJson.getJSONArray("subcategories")
                val subcategories = mutableListOf<Subcategory>()

                for (j in 0 until subcategoriesJson.length()) {
                    val subcategoryJson = subcategoriesJson.getJSONObject(j)
                    subcategories.add(
                        Subcategory(
                            name = subcategoryJson.getString("name"),
                            page = subcategoryJson.getString("page")
                        )
                    )
                }
                categories.add(Category(title, subcategories))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return categories
    }

    fun setOnPageSelectedListener(listener: (String) -> Unit) {
        onPageSelected = listener
    }
}

// Data classes for Category and Subcategory
data class Category(
    val title: String,
    val subcategories: List<Subcategory> = emptyList()
)

data class Subcategory(
    val name: String,
    val page: String
)
