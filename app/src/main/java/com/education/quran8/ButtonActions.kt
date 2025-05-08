package com.education.quran8

import android.content.Intent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

object  ButtonActions {

    fun openChapters(view: View, selectedBook: String) {
        Toast.makeText(view.context, "Chapitres affichés", Toast.LENGTH_SHORT).show()
        val activity = view.context as? AppCompatActivity
        activity?.let {
            val chaptersFragment = ChaptersFragment(selectedBook)
            chaptersFragment.show(it.supportFragmentManager, "chaptersFragment")
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
        val playStoreLink = "https://play.google.com/store/apps/details?id=com.education.Quran8"
        val shareText = "Check out Quran Reader on Google Play: $playStoreLink"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        context.startActivity(Intent.createChooser(intent, "Share Quran Reader via"))
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
