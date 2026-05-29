package com.example.vijaynet

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

object VijayClient {

    private val client = OkHttpClient()

    suspend fun get(url: String): String {

        return withContext(Dispatchers.IO) {

            val request = Request.Builder()
                .url(url)
                .build()

            client.newCall(request)
                .execute()
                .use { response ->

                    response.body?.string() ?: ""
                }
        }
    }
}