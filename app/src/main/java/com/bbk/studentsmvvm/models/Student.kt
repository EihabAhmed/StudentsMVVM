package com.bbk.studentsmvvm.models


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Student(
    @SerializedName("id")
    val id: Int,
    @SerializedName("firstName")
    val firstName: String,
    @SerializedName("age")
    val age: Int,
    @SerializedName("grade")
    val grade: Int,
    @SerializedName("imageUrl")
    val imageUrl: String?

) : Parcelable