package com.example.vijaynet.lib

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.lang.reflect.Method
import kotlin.coroutines.Continuation

class RequestFactory(

    private val baseUrl: String

) {

    fun parse(

        method: Method,

        args: Array<Any?>

    ): RequestData {

        val getAnnotation =
            method.getAnnotation(GET::class.java)

        val postAnnotation =
            method.getAnnotation(POSTS::class.java)

        val httpMethod =
            when {

                getAnnotation != null -> "GET"

                postAnnotation != null -> "POST"

                else -> throw IllegalStateException(
                    "No HTTP annotation found"
                )
            }

        var relativeUrl =
            getAnnotation?.value
                ?: postAnnotation?.value
                ?: ""

        val headersMap =
            mutableMapOf<String, String>()

        var bodyObject: Any? = null

        val continuationIndex =
            method.parameterTypes.indexOf(
                Continuation::class.java
            )

        // Handle annotations
        args.forEachIndexed { index, arg ->

            if (index == continuationIndex) {
                return@forEachIndexed
            }

            if (arg == null) {
                return@forEachIndexed
            }

            val annotations =
                method.parameterAnnotations[index]

            annotations.forEach { annotation ->

                when (annotation) {

                    is Path -> {

                        relativeUrl =
                            relativeUrl.replace(
                                "{${annotation.value}}",
                                arg.toString()
                            )
                    }

                    is Body -> {

                        bodyObject = arg
                    }

                    is Header -> {

                        headersMap[annotation.value] =
                            arg.toString()
                    }
                }
            }
        }

        val fullUrl =
            baseUrl.trimEnd('/') +
                    "/" +
                    relativeUrl.trimStart('/')

        val httpBuilder =
            fullUrl.toHttpUrlOrNull()
                ?.newBuilder()

                ?: throw IllegalStateException(
                    "Invalid URL: $fullUrl"
                )

        // Handle Query params
        args.forEachIndexed { index, arg ->

            if (index == continuationIndex) {
                return@forEachIndexed
            }

            if (arg == null) {
                return@forEachIndexed
            }

            val annotations =
                method.parameterAnnotations[index]

            annotations.forEach { annotation ->

                if (annotation is Query) {

                    httpBuilder.addQueryParameter(
                        annotation.value,
                        arg.toString()
                    )
                }
            }
        }

        return RequestData(

            httpMethod = httpMethod,

            url = httpBuilder.build().toString(),

            headers = headersMap,

            body = bodyObject
        )
    }
}