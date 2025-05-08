package com.education.quran8

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

fun showCustomDialog(context: Context) {
    val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_menu, null)
    val dialog = AlertDialog.Builder(context)
        .setView(dialogView)
        .create()

    dialogView.findViewById<LinearLayout>(R.id.menu_donate)?.setOnClickListener {
        Toast.makeText(context, context.getString(R.string.contact_us), Toast.LENGTH_SHORT).show()
        dialog.dismiss()
    }
    dialogView.findViewById<LinearLayout>(R.id.menu_tafsir)?.setOnClickListener {
        Toast.makeText(context, context.getString(R.string.tafsir), Toast.LENGTH_SHORT).show()
        dialog.dismiss()
    }
    dialogView.findViewById<LinearLayout>(R.id.menu_audio)?.setOnClickListener {
        Toast.makeText(context, context.getString(R.string.audio), Toast.LENGTH_SHORT).show()
        dialog.dismiss()
    }
    dialogView.findViewById<LinearLayout>(R.id.menu_download)?.setOnClickListener {
        Toast.makeText(context, context.getString(R.string.download), Toast.LENGTH_SHORT).show()
        dialog.dismiss()
    }

    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    dialog.show()
}
