package com.education.quran8

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SavedPagesAdapter(
    private val savedPages: MutableList<Int>,
    private val onPageSelected: (Int) -> Unit,
    private val onRemovePage: (Int) -> Unit
) : RecyclerView.Adapter<SavedPagesAdapter.SavedPageViewHolder>() {

    class SavedPageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val pageText: TextView = view.findViewById(R.id.pageNumber)
        val removeButton: Button = view.findViewById(R.id.removePageButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedPageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_saved_page, parent, false)
        return SavedPageViewHolder(view)
    }

    override fun onBindViewHolder(holder: SavedPageViewHolder, position: Int) {
        val page = savedPages[position]

        holder.pageText.text = "Page $page"
        holder.pageText.setOnClickListener {
            onPageSelected(page)
        }

        holder.removeButton.setOnClickListener {
            onRemovePage(page)
            savedPages.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, savedPages.size)
        }
    }

    override fun getItemCount(): Int = savedPages.size
}
