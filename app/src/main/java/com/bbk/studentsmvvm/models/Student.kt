package com.bbk.studentsmvvm.models


import com.google.gson.annotations.SerializedName

data class Student(
    @SerializedName("age")
    val age: Int,
    @SerializedName("firstName")
    val firstName: String,
    @SerializedName("imageUrl")
    val imageUrl: String?,
    @SerializedName("grade")
    val grade: Int,
    @SerializedName("id")
    val id: Int
)