package com.bbk.studentsmvvm.bindingadapters

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import com.bbk.studentsmvvm.models.Students
import com.bbk.studentsmvvm.util.NetworkResult

class AllStudentsBinding {

    companion object {

        @BindingAdapter("readApiResponse")
        @JvmStatic
        fun handleReadDataErrors(
            view: View,
            apiResponse: NetworkResult<Students>?
        ) {
            when (view) {
                is ImageView -> {
                    view.isVisible = apiResponse is NetworkResult.Error
                }
                is TextView -> {
                    view.isVisible = apiResponse is NetworkResult.Error
                    view.text = apiResponse?.message.toString()
                }
            }
        }
    }
}