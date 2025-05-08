package com.education.quran8



import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
// ✅ Import de la classe Subcategory depuis

class SubcategoryAdapter(
    private val subcategories: List<Subcategory>,  // ✅ Assurez-vous que c'est bien List<Subcategory>
    private val onItemSelected: (Subcategory) -> Unit  // ✅ Accepte un Subcategory complet
) : RecyclerView.Adapter<SubcategoryAdapter.SubcategoryViewHolder>() {

    inner class SubcategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val subcategoryText: TextView = view.findViewById(R.id.subcategoryText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubcategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_subcategory, parent, false)
        return SubcategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: SubcategoryViewHolder, position: Int) {
        val subcategory = subcategories[position]
        holder.subcategoryText.text = subcategory.name
        holder.subcategoryText.setOnClickListener {
            onItemSelected(subcategory)
        }
    }

    override fun getItemCount(): Int = subcategories.size
}
