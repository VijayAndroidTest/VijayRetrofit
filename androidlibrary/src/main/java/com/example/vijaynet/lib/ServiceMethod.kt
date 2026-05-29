package com.example.vijaynet.lib

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.lang.reflect.Type

class ServiceMethod(

    private val okHttpClient: OkHttpClient,

    private val converter: Converter

) {

    fun invoke(

        requestData: RequestData,

        returnType: Type

    ): Any {

        val requestBuilder =
            Request.Builder()

                .url(requestData.url)

        requestData.headers.forEach { (key, value) ->

            requestBuilder.addHeader(key, value)
        }

        when (requestData.httpMethod) {

            "GET" -> {

                requestBuilder.get()
            }

            "POST" -> {

                val jsonBody =
                    converter.toJson(
                        requestData.body ?: ""
                    )

                val body =
                    jsonBody.toRequestBody(
                        "application/json"
                            .toMediaType()
                    )

                requestBuilder.post(body)
            }
        }

        val request =
            requestBuilder.build()

        val response =
            okHttpClient
                .newCall(request)
                .execute()

        response.use { resp ->

            if (!resp.isSuccessful) {

                throw HttpException(
                    resp.code,
                    resp.body?.string()
                )
            }

            val responseBody =
                resp.body?.string()
                    ?: ""

            return converter.fromJson(
                responseBody,
                returnType
            )
        }
    }
}