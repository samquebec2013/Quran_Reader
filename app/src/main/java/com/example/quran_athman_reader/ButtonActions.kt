package com.example.quran_athman_reader

import android.content.Intent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

object  ButtonActions {

    fun openChapters(view: View) {
        Toast.makeText(view.context, "Chapitres affichés", Toast.LENGTH_SHORT).show()
        val activity = view.context as? AppCompatActivity
        activity?.let {
            ChaptersFragment().show(it.supportFragmentManager, "chaptersFragment")
        }
    }

    fun savePage(view: View) {
        Toast.makeText(view.context, "Page enregistrée", Toast.LENGTH_SHORT).show()
        val activity = view.context as? AppCompatActivity
        activity?.let {
            BookmarkFragment().show(it.supportFragmentManager, "bookmarkFragment")
        }
    }

    fun shareApp(view: View) {
        val context = view.context
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, " D'oeuvre cette application de lecture de livres !")
        }
        context.startActivity(Intent.createChooser(intent, "Partager via"))
    }

    fun openBookmark(view: View) {
        Toast.makeText(view.context, "Ouverture du marque-page", Toast.LENGTH_SHORT).show()
        val activity = view.context as? AppCompatActivity
        activity?.let {
            BookmarkFragment().show(it.supportFragmentManager, "bookmarkFragment")
        }
    }

    fun openSettings(view: View) {
        Toast.makeText(view.context, "Ouverture des paramètres", Toast.LENGTH_SHORT).show()
        val activity = view.context as? AppCompatActivity
        activity?.let {
            SettingsFragment().show(it.supportFragmentManager, "settingsFragment")
        }
    }
}
