package com.example.vijaynet.lib

import okhttp3.Interceptor

interface VijayInterceptor {

    suspend fun intercept(
        chain: Interceptor.Chain
    ): Response<Any>
}