package com.bbk.studentsmvvm.models

data class LoginModel(
    val grant_type: String,
    val username: String,
    val password: String
)