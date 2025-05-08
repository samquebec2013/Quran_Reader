package com.education.quran8

import android.content.Context
import android.util.Log
import org.json.JSONObject
import java.io.File
import java.io.IOException

data class BookConfig(
    val totalPages: Int,
    val assetsPath: String,
    val title: String,
    val titlesFile: String
) {
    companion object {
        fun loadFromAssets(context: Context, bookName: String): BookConfig? {
            return try {
                val jsonString: String
                val internalFile = File(context.getDir("books", Context.MODE_PRIVATE), "$bookName/config.json")

                if (internalFile.exists()) {
                    Log.d("BookConfig", "Loading config from internal storage: ${internalFile.absolutePath}")
                    jsonString = internalFile.bufferedReader().use { it.readText() }
                } else {
                    val assetPath = "books/$bookName/config.json"
                    Log.d("BookConfig", "Loading config from assets: $assetPath")
                    val inputStream = context.assets.open(assetPath)
                    jsonString = inputStream.bufferedReader().use { it.readText() }
                    inputStream.close()
                }

                val jsonObject = JSONObject(jsonString)
                val totalPages = jsonObject.getInt("totalPages")
                val assetsPath = jsonObject.getString("assetsPath")
                val title = jsonObject.getString("title")
                val titlesFile = jsonObject.getString("titlesFile")

                BookConfig(totalPages, assetsPath, title, titlesFile)
            } catch (e: IOException) {
                Log.e("BookConfig", "Failed to load config for $bookName: ${e.stackTraceToString()}")
                null
            } catch (e: Exception) {
                Log.e("BookConfig", "Error parsing config for $bookName: ${e.stackTraceToString()}")
                null
            }
        }
    }
}