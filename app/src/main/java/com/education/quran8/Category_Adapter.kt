package com.education.quran8

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CategoryAdapter(
    private val categories: List<Category>,
    private val onItemSelected: (Subcategory) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    inner class CategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val categoryTitle: TextView = view.findViewById(R.id.categoryTitle)
        val subcategoriesRecyclerView: RecyclerView = view.findViewById(R.id.recyclerViewSubcategories)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]

        holder.categoryTitle?.text = category.title

        holder.categoryTitle?.setOnClickListener {
            holder.subcategoriesRecyclerView?.visibility =
                if (holder.subcategoriesRecyclerView?.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }

        val reversedSubcategories = category.subcategories.reversed()

        holder.subcategoriesRecyclerView?.apply {
            layoutManager = GridLayoutManager(holder.itemView.context, 3).apply {
                reverseLayout = true
            }
            adapter = SubcategoryAdapter(reversedSubcategories, onItemSelected)
        }
    }

    override fun getItemCount(): Int = categories.size
}
