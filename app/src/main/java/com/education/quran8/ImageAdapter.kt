package com.education.quran8

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import java.io.IOException

class ImageAdapter(
    private val context: Context,
    private val imagePaths: List<String>
) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    var isNightMode: Boolean = false

    inner class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.imageView)
        val scrollView: NestedScrollView? = view.findViewById(R.id.scrollView) // Only exists in landscape mode
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val orientation = context.resources.configuration.orientation
        val layoutRes = if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            R.layout.item_image // Uses res/layout-land/item_image.xml
        } else {
            R.layout.item_image // Uses res/layout/item_image.xml
        }

        val view = LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
        return ImageViewHolder(view)
    }
    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imagePath = imagePaths[position]
        try {
            val inputStream = context.assets.open(imagePath)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            holder.imageView.setImageBitmap(bitmap)
            inputStream.close()

            // Apply night mode if enabled
            if (isNightMode) applyNightMode(holder.imageView)

            val screenHeight = Resources.getSystem().displayMetrics.heightPixels
            val screenWidth = Resources.getSystem().displayMetrics.widthPixels
            val aspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()

            if (context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                holder.imageView.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                holder.imageView.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT // ✅ Ensure full width
            } else {
                val newHeight = (screenWidth / aspectRatio).toInt()
                holder.imageView.layoutParams.height = newHeight
                holder.imageView.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT // ✅ Ensure full width
            }

            holder.imageView.requestLayout() // ✅ Force layout refresh

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }





    override fun getItemCount(): Int = imagePaths.size

    private fun applyNightMode(imageView: ImageView) {
        Log.e("DEBUG_NIGHT_MODE", "🌙 Night mode applied to image!")
        val matrix = ColorMatrix().apply {
            set(
                floatArrayOf(
                    -1f, 0f, 0f, 0f, 255f,
                    0f, -1f, 0f, 0f, 255f,
                    0f, 0f, -1f, 0f, 255f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
        }
        imageView.colorFilter = ColorMatrixColorFilter(matrix)
    }
}
