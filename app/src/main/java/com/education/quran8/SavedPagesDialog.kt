package com.education.quran8

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SavedPagesDialog(
    private val savedPages: MutableList<Int>,
    private val onRemovePage: (Int) -> Unit,
    private val onPageSelected: (Int) -> Unit
) : DialogFragment() {

    private lateinit var adapter: SavedPagesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_saved_pages, container, false)
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerViewPages)
        val closeButton: Button = view.findViewById(R.id.closeButton)

        // Initialize Adapter
        adapter = SavedPagesAdapter(savedPages, onPageSelected, onRemovePage)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        if (savedPages.isEmpty()) {
            Toast.makeText(requireContext(), "Aucune page enregistrée", Toast.LENGTH_SHORT).show()
            dismiss()
        }

        closeButton.setOnClickListener { dismiss() }
        return view
    }
}
