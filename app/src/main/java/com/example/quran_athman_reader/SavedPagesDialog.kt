package com.example.quran_athman_reader

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.DialogFragment

class SavedPagesDialog(
    private val savedPages: List<Int>,
    private val onRemovePage: (Int) -> Unit,
    private val onPageSelected: (Int) -> Unit
) : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_saved_pages, container, false)
        val pagesContainer: LinearLayout = view.findViewById(R.id.pagesContainer)
        val closeButton: Button = view.findViewById(R.id.closeButton)

        savedPages.forEach { page ->
            val pageView = layoutInflater.inflate(R.layout.item_saved_page, pagesContainer, false)
            val pageText: TextView = pageView.findViewById(R.id.pageNumber)
            val removeButton: Button = pageView.findViewById(R.id.removePageButton)

            pageText.text = "Page $page"
            pageText.setOnClickListener {
                onPageSelected(page)
                dismiss()
            }

            removeButton.setOnClickListener {
                onRemovePage(page)
                dismiss()
            }

            pagesContainer.addView(pageView)
        }

        closeButton.setOnClickListener { dismiss() }
        return view
    }
}
