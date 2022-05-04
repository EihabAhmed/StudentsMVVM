package com.bbk.studentsmvvm.models

data class RegisterModel(
    val email: String,
    val password: String,
    val confirmPassword: String?
)