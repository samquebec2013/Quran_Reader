package com.education.quran8

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.DialogFragment

class BookmarkFragment : DialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_bookmark, container, false)

        val closeButton: Button = view.findViewById(R.id.closeButton)
        closeButton.setOnClickListener { dismiss() }

        return view
    }
}
