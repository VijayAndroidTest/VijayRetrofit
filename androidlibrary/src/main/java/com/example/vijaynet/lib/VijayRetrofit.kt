package com.example.vijaynet.lib

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Proxy
import java.lang.reflect.Type
import java.lang.reflect.WildcardType
import kotlin.coroutines.Continuation
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class VijayRetrofit private constructor(

    private val baseUrl: String,

    private val okHttpClient: OkHttpClient,

    private val converter: Converter

) {

    class Builder {

        private var baseUrl: String? = null

        private var client: OkHttpClient =
            OkHttpClient()

        private var converterFactory:
                Converter.Factory =
            GsonConverterFactory()

        fun baseUrl(url: String) =
            apply {
                this.baseUrl = url
            }

        fun client(okHttpClient: OkHttpClient) =
            apply {
                this.client = okHttpClient
            }

        fun addConverterFactory(
            factory: Converter.Factory
        ) = apply {
            this.converterFactory = factory
        }

        fun build(): VijayRetrofit {

            return VijayRetrofit(

                baseUrl
                    ?: throw IllegalStateException(
                        "Base URL required"
                    ),

                client,

                converterFactory.create()
            )
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> create(
        service: Class<T>
    ): T {

        return Proxy.newProxyInstance(

            service.classLoader,

            arrayOf(service)

        ) { _, method, args ->

            val argsList =
                args ?: emptyArray()

            val continuationIndex =
                method.parameterTypes.indexOf(
                    Continuation::class.java
                )

            val requestFactory =
                RequestFactory(baseUrl)

            val requestData =
                requestFactory.parse(
                    method,
                    argsList
                )

            val returnType =
                getSuspendFunctionReturnType(
                    method,
                    continuationIndex
                )

            val serviceMethod =
                ServiceMethod(
                    okHttpClient,
                    converter
                )

            // Suspend function support
            if (continuationIndex != -1) {

                val continuation =
                    argsList[continuationIndex]
                            as Continuation<Any?>

                CoroutineScope(Dispatchers.IO)
                    .launch {

                        try {

                            val result =
                                serviceMethod.invoke(
                                    requestData,
                                    returnType
                                )

                            continuation.resume(result)

                        } catch (e: Exception) {

                            continuation.resumeWithException(e)
                        }
                    }

                return@newProxyInstance COROUTINE_SUSPENDED
            }

            // Normal blocking call
            return@newProxyInstance runBlocking {

                serviceMethod.invoke(
                    requestData,
                    returnType
                )
            }

        } as T
    }

    private fun getSuspendFunctionReturnType(
//reflection
        method: java.lang.reflect.Method,

        continuationIndex: Int

    ): Type {

        if (continuationIndex == -1) {

            return method.genericReturnType
        }

        val continuationType =
            method.genericParameterTypes[
                continuationIndex
            ]

        if (continuationType is ParameterizedType) {

            val actualType =
                continuationType.actualTypeArguments[0]

            if (actualType is WildcardType) {

                return actualType.lowerBounds
                    .firstOrNull()

                    ?: actualType.upperBounds
                        .firstOrNull()

                    ?: Any::class.java
            }

            return actualType
        }

        return Any::class.java
    }
}