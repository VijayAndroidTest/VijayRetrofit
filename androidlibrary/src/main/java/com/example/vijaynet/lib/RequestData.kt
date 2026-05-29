package com.example.vijaynet.lib

data class RequestData(

    val httpMethod: String,

    val url: String,

    val headers: Map<String, String> = emptyMap(),

    val body: Any? = null
)