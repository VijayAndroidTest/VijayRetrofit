package com.example.vijaynet.lib

data class Response<T>(

    val body: T?,

    val code: Int,

    val isSuccessful: Boolean,

    val message: String
)