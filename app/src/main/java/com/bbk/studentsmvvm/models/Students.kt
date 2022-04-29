package com.bbk.studentsmvvm.models


import com.google.gson.annotations.SerializedName

data class Students(
    @SerializedName("students")
    var students: List<Student>
)