package com.example.quran_athman_reader

import android.content.Context
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter

class ImageAdapter(private val context: Context, private val images: List<Int>) : PagerAdapter() {

    var isNightMode: Boolean = false // Default: Day Mode

    override fun getCount(): Int {
        return images.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.item_image, container, false)
        val imageView: ImageView = view.findViewById(R.id.imageView)

        imageView.setImageResource(images[position])

        // Apply night mode filter if enabled
        if (isNightMode) {
            applyNightMode(imageView)
        }

        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    // Function to apply Night Mode (Invert Colors)
    fun applyNightMode(imageView: ImageView) {
        val matrix = ColorMatrix()
        matrix.set(
            floatArrayOf(
                -1f,  0f,  0f,  0f, 255f, // Red channel inversion
                0f, -1f,  0f,  0f, 255f, // Green channel inversion
                0f,  0f, -1f,  0f, 255f, // Blue channel inversion
                0f,  0f,  0f,  1f,   0f  // Alpha remains unchanged
            )
        )
        imageView.colorFilter = ColorMatrixColorFilter(matrix)
    }
}
