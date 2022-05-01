package com.bbk.studentsmvvm.bindingadapters

import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.BindingAdapter
import androidx.navigation.findNavController
import coil.load
import com.bbk.studentsmvvm.R
import com.bbk.studentsmvvm.models.Student
import com.bbk.studentsmvvm.ui.fragments.AllStudentsFragmentDirections

class StudentRowBinding {

    companion object {

        @BindingAdapter("onStudentClickListener")
        @JvmStatic
        fun onStudentClickListener(studentRowLayout: ConstraintLayout, student: Student) {
            studentRowLayout.setOnClickListener {
                try {
                    val action = AllStudentsFragmentDirections.actionAllStudentsFragmentToStudentDetailsFragment(student, false)
                    studentRowLayout.findNavController().navigate(action)
                } catch (e: Exception) {
                    Log.d("onStudentClickListener", e.toString())
                }
            }
        }

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