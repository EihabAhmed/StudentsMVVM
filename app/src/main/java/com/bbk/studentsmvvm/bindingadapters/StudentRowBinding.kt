package com.bbk.studentsmvvm.bindingadapters

import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import coil.load
import com.bbk.studentsmvvm.R

class StudentRowBinding {

    companion object {

        @BindingAdapter("loadImageFromUrl")
        @JvmStatic
        fun loadImageFromUrl(imageView: ImageView, imageUrl: String?) {
            imageView.load(imageUrl) {
                crossfade(600)
                error(R.drawable.ic_placeholder)
            }
        }

        @BindingAdapter("showText")
        @JvmStatic
        fun showText(textView: TextView, value: Int) {
            when (textView.id) {
                R.id.age_textView -> {
                    textView.text = "$value years old"
                }
                R.id.grade_textView -> {
                    textView.text = "Grade $value"
                }
            }
        }
    }

}