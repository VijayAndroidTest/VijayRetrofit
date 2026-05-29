package com.example.vijaynet.lib

sealed class VijayException(
    message: String,
    cause: Throwable? = null
) : VijayNetException(message, cause) {

    class NetworkException(
        message: String,
        cause: Throwable? = null
    ) : VijayException(message, cause)

    class HttpException(
        val code: Int,
        message: String
    ) : VijayException(message)

    class SerializationException(
        message: String
    ) : VijayException(message)
}