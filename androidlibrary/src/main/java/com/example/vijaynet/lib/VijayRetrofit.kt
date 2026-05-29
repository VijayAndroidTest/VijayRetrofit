package com.example.vijaynet.lib

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Proxy
import java.lang.reflect.Type
import java.lang.reflect.WildcardType
import kotlin.coroutines.Continuation

class VijayRetrofit private constructor(
    private val baseUrl: String,
    private val okHttpClient: OkHttpClient,
    private val converter: Converter // Decoupled interface
) {
    class Builder {
        private var baseUrl: String? = null
        private var client: OkHttpClient = OkHttpClient() // Default client
        private var converterFactory: Converter.Factory = GsonConverterFactory() // Default factory

        fun baseUrl(url: String) = apply { this.baseUrl = url }
        fun client(okHttpClient: OkHttpClient) = apply { this.client = okHttpClient }
        fun addConverterFactory(factory: Converter.Factory) = apply { this.converterFactory = factory }

        fun build() = VijayRetrofit(
            baseUrl ?: throw IllegalStateException("Base URL required"),
            client,
            converterFactory.create()
        )
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> create(service: Class<T>): T {
        return Proxy.newProxyInstance(
            service.classLoader,
            arrayOf(service)
        ) { proxy, method, args ->

            val getAnnotation = method.getAnnotation(GET::class.java)
            val postsAnnotation = method.getAnnotation(POSTS::class.java)

            val httpMethod = when {
                getAnnotation != null -> "GET"
                postsAnnotation != null -> "POST"
                else -> throw IllegalArgumentException("Unsupported HTTP method. Add @GET or @POSTS")
            }

            var relativePath = getAnnotation?.value ?: postsAnnotation!!.value

            val parameterAnnotations = method.parameterAnnotations
            val queryParams = mutableListOf<Pair<String, String>>()
            var bodyPayload: RequestBody? = null

            if (args != null) {
                for (i in args.indices) {
                    val annotations = parameterAnnotations[i]
                    val argValue = args[i]

                    if (argValue is Continuation<*>) continue

                    if (annotations.isEmpty()) {
                        if (httpMethod == "POST") {
                            // Use our pluggable converter instead of raw Gson instance
                            val jsonString = converter.toJson(argValue)
                            bodyPayload = jsonString.toRequestBody("application/json; charset=utf-8".toMediaType())
                        }
                    } else {
                        for (annotation in annotations) {
                            when (annotation) {
                                is Path -> {
                                    relativePath = relativePath.replace("{${annotation.value}}", argValue.toString())
                                }
                                is Query -> {
                                    queryParams.add(annotation.value to argValue.toString())
                                }
                            }
                        }
                    }
                }
            }

            val urlBuilder = "$baseUrl$relativePath".toHttpUrlOrNull()?.newBuilder()
                ?: throw IllegalArgumentException("Invalid Base URL configuration")

            for ((key, value) in queryParams) {
                urlBuilder.addQueryParameter(key, value)
            }
            val finalUrl = urlBuilder.build().toString()

            val lastArg = args?.lastOrNull()
            val returnTargetType: Type = if (lastArg is Continuation<*>) {
                getSuspendFunctionReturnType(method)
            } else {
                method.genericReturnType
            }

            return@newProxyInstance executeBlockingNetworkCall(finalUrl, httpMethod, bodyPayload, returnTargetType)
        } as T
    }

    private fun getSuspendFunctionReturnType(method: Method): Type {
        val lastParameterType = method.genericParameterTypes.lastOrNull()
        if (lastParameterType is ParameterizedType) {
            val insideType = lastParameterType.actualTypeArguments.firstOrNull()
            if (insideType != null) {
                if (insideType is WildcardType) {
                    val lowerBound = insideType.lowerBounds.firstOrNull()
                    if (lowerBound != null) return lowerBound
                    val upperBound = insideType.upperBounds.firstOrNull()
                    if (upperBound != null) return upperBound
                }
                return insideType
            }
        }
        return method.genericReturnType
    }

    private fun executeBlockingNetworkCall(url: String, method: String, body: RequestBody?, returnType: Type): Any {
        val requestBuilder = Request.Builder().url(url)

        if (method == "POST") {
            requestBuilder.post(body ?: "".toRequestBody(null))
        } else {
            requestBuilder.get()
        }

        val response = try {
            // Intercept raw connection issues (No internet, DNS failures, timeouts)
            okHttpClient.newCall(requestBuilder.build()).execute()
        } catch (e: IOException) {
            throw NetworkConnectivityException(e)
        }

        response.use { resp ->
            // Handle explicit HTTP status failure states cleanly
            if (!resp.isSuccessful) {
                val errorString = resp.body?.string() ?: ""
                throw HttpException(code = resp.code, errorBody = errorString)
            }

            val bodyString = resp.body?.string() ?: ""

            if (returnType == Void.TYPE || returnType == Unit::class.java) return Unit

            return try {
                converter.fromJson(bodyString, returnType)
            } catch (e: Exception) {
                throw VijayNetException("JSON Deserialization failed for type: $returnType", e)
            }
        }
    }}