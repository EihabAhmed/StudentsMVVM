package com.bbk.studentsmvvm.models


import com.google.gson.annotations.SerializedName

data class Admin(
    @SerializedName("userName")
    val userName: String,
    @SerializedName("admin")
    val admin: Boolean
)